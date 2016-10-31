package DeepLearning;

import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.Read;
import jm.util.Write;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Aashish on 8/21/16.
 */

/**
 * The GRAVESLSTMNoteCreation class is another model of an RNN which essentially breaks apart the training set and uses BPTT
 * on small data sets, and then compounds them. The problem with this model is that my data set is not large enough for this
 * to be useful...
 * Adapted from DL4J's GravesLSTMModel
 */
public class GravesLSTMNoteCreation
{
	public static void main(String[] args) throws IOException
	{
		int lstmLayerSize = 100;
		int miniBatchSize = 32;
		int exampleLength = 1000;
		int tbpttLength = 50;
		int numEpochs = 1;
		int generateSamplesEveryNMinibatches = 10;
		int nSamplesToGenerate = 50;
		int nCharactersToSample = 300;
		Note[] generationInitialization = null;


		Random rng = new Random(12345);


		NoteIterator iter = getTristezaIterator(miniBatchSize, exampleLength);
		int nOut = iter.totalOutcomes();

		MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
				  .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT).iterations(1)
				  .learningRate(0.1)
				  .rmsDecay(0.95)
				  .seed(12345)
				  .regularization(true)
				  .l2(0.001)
				  .weightInit(WeightInit.XAVIER)
				  .updater(Updater.RMSPROP)
				  .list()
				  .layer(0, new GravesLSTM.Builder().nIn(iter.inputColumns()).nOut(lstmLayerSize)
							 .activation("tanh").build())
				  .layer(1, new GravesLSTM.Builder().nIn(lstmLayerSize).nOut(lstmLayerSize)
							 .activation("tanh").build())
				  .layer(2, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT).activation("softmax")        //MCXENT + softmax for classification
							 .nIn(lstmLayerSize).nOut(nOut).build())
				  .backpropType(BackpropType.TruncatedBPTT).tBPTTForwardLength(tbpttLength).tBPTTBackwardLength(tbpttLength)
				  .pretrain(false).backprop(true)
				  .build();

		MultiLayerNetwork net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

		ArrayList<Note> generatedNotes = new ArrayList<>();

		int miniBatchNumber = 0;
		for( int i=0; i<numEpochs; i++ )
		{
			while(iter.hasNext())
			{
				DataSet ds = iter.next();
				net.fit(ds);

				System.out.println("MiniBatchNumber: " + miniBatchNumber);
				if(++miniBatchNumber % generateSamplesEveryNMinibatches == 0)
				{
					System.out.println("--------------------");
					System.out.println("Completed " + miniBatchNumber + " minibatches of size " + miniBatchSize + "x" + exampleLength + " notes" );
					System.out.println("Sampling notes from network given initialization \"" + (generationInitialization == null ? "" : generationInitialization) + "\"");
					Note[] samples = sampleNotesFromNetwork(generationInitialization,net,iter,rng,nCharactersToSample,nSamplesToGenerate);
					for( int j=0; j<samples.length; j++ )
					{
						System.out.println("----- Sample " + j + " -----");
						System.out.println(samples[j]);
						System.out.println();
					}

					for (Note n : samples)
					{
						//Play.midi(n);
						generatedNotes.add(n);
					}

					generationInitialization = new Note[] {generatedNotes.get(generatedNotes.size() - 1)};
				}


			}

			iter.reset();	//Reset iterator for another epoch
		}




		Phrase p = new Phrase();
		//p.setTempo(204);
		for (Note n : generatedNotes)
		{
			p.addNote(n);
			//Play.midi(n);
		}

		Score newScore = new Score();
		Part newPart = new Part();
		newPart.addPhrase(p);
		newScore.addPart(newPart);
		Write.midi(newScore, "testFile.mid");


		//Play.midi



		System.out.println("\n\nExample complete");
	}

	public static NoteIterator getTristezaIterator(int miniBatchSize, int sequenceLength) throws IOException
	{
		//int[] validPitches = PitchIterator.getPitchSet();

		Score testScore = new Score();

		Read.midi(testScore, "autumn_leaves.mid");

		Note[] n = testScore.getPart(0).getPhrase(0).getNoteArray();

		//return new NoteIterator(n, miniBatchSize, sequenceLength, n, new Random(12345));

		return new NoteIterator(n, miniBatchSize, sequenceLength, new Random(1234));
	}

	private static Note[] sampleNotesFromNetwork(Note[] initialization, MultiLayerNetwork net, NoteIterator iter, Random rng, int notesToSample, int numSamples)
	{

		if( initialization == null )
		{
			initialization = new Note[] {iter.getRandomNote()};
		}

		INDArray initializationInput = Nd4j.zeros(numSamples, iter.inputColumns(), initialization.length);
		Note[] init = new Note[initialization.length];

		for (int i = 0; i < initialization.length; i++)
		{
			init[i] = initialization[i];
		}

		for( int i = 0; i < init.length; i++ ){
			int idx = iter.convertNoteToIndex(init[i]);
			for( int j = 0; j < numSamples; j++ ){
				initializationInput.putScalar(new int[]{j,idx,i}, 1.0f);
			}
		}


		ArrayList<Note> notes = new ArrayList<Note>();


		net.rnnClearPreviousState();
		INDArray output = net.rnnTimeStep(initializationInput);
		output = output.tensorAlongDimension(output.size(2)-1,1,0);

		for( int i=0; i<notesToSample; i++ ){
			//Set up next input (single time step) by sampling from previous output
			INDArray nextInput = Nd4j.zeros(numSamples,iter.inputColumns());
			//Output is a probability distribution. Sample from this for each example we want to generate, and add it to the new input
			for( int s=0; s<numSamples; s++ ){
				double[] outputProbDistribution = new double[iter.totalOutcomes()];
				for( int j=0; j<outputProbDistribution.length; j++ ) outputProbDistribution[j] = output.getDouble(s,j);
				int sampledCharacterIdx = sampleFromDistribution(outputProbDistribution,rng);

				nextInput.putScalar(new int[]{s, sampledCharacterIdx}, 1.0f);		//Prepare next time step input

				//if (new Note(iter.convertIndexToPitch(sampledCharacterIdx), 0.5).isScale(Scales.MINOR_SCALE))
				//{

				Note newNote = iter.convertIndexToNote(sampledCharacterIdx);
				System.out.println(newNote);

				notes.add(newNote);
				//
						  //iter.fileNotes[(int) (rng.nextDouble() * iter.fileNotes.length)].getRhythmValue()));
				//sb[s].append(iter.convertIndexToPitch(sampledCharacterIdx));	//Add sampled character to StringBuilder (human readable output)
			}

			output = net.rnnTimeStep(nextInput);	//Do one time step of forward pass
		}

		Note[] out = new Note[numSamples];
		for (int i = 0; i < numSamples; i++) out[i] = notes.get(i);
		return out;
	}

	public static int sampleFromDistribution( double[] distribution, Random rng ){
		double d = rng.nextDouble();
		double sum = 0.0;
		for( int i=0; i<distribution.length; i++ ){
			sum += distribution[i];
			if( d <= sum ) return i;
		}
		//Should never happen if distribution is a valid probability distribution
		throw new IllegalArgumentException("Distribution is invalid? d="+d+", sum="+sum);
	}
}
