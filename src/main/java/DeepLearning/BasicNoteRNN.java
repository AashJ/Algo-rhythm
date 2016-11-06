package DeepLearning;

import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Read;
import jm.util.Write;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Aashish
 * Adapted from DL4J Basic Recurrent Network
 */
public class BasicNoteRNN
{
	//The array of notes in the training set
	public static Note[] notes;

	// A data structure to store the notes
	public static final List<Note> notes_list = new ArrayList<Note>();

	// Network configurations
	public static final int HIDDEN_LAYER_WIDTH = 50;
	public static final int HIDDEN_LAYER_CONT = 2;

	public static final Random r = new Random(1234);

	/**
	 * Main method. Trains on a given set of notes, and then creates a model based on
	 * these sets of notes. The model is then used to generate a completely new set
	 * of notes, based on the old set.
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException
	{
		notes = getAutumnLeavesSoloNotes();

		LinkedHashSet<Note> notes_linked_list = new LinkedHashSet<Note>();

		for (Note n : notes)
		{
			notes_linked_list.add(n);
		}

		notes_list.addAll(notes_linked_list);



		//save training data here

		File f = new File("AutumnLeavesNet");

		MultiLayerNetwork net = null;

		if (f.exists())
		{
			ModelSerializer ms = new ModelSerializer();
			net = ms.restoreMultiLayerNetwork(f);
		}
		else
		{
			/**
			 * Neural Network configurations
			 */
			NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
			builder.iterations(10);
			builder.learningRate(0.001);
			builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT);
			builder.seed(123);
			builder.biasInit(0);
			builder.miniBatch(false);
			builder.updater(Updater.RMSPROP);
			builder.weightInit(WeightInit.XAVIER);

			NeuralNetConfiguration.ListBuilder listBuilder = builder.list();

			for (int i = 0; i < HIDDEN_LAYER_CONT; i++)
			{
				GravesLSTM.Builder hiddenLayerBuilder = new GravesLSTM.Builder();
				hiddenLayerBuilder.nIn(i == 0 ? notes_list.size() : HIDDEN_LAYER_WIDTH);
				hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);

				hiddenLayerBuilder.activation("tanh");
				listBuilder.layer(i, hiddenLayerBuilder.build());
			}

			RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT);

			// The way I understand it, softmax is a basic probability function for everything in the network.
			outputLayerBuilder.activation("softmax");
			outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
			outputLayerBuilder.nOut(notes_list.size());
			listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build());

			listBuilder.pretrain(false);
			listBuilder.backprop(true);

			MultiLayerConfiguration conf = listBuilder.build();
			net = new MultiLayerNetwork(conf);
			net.init();
			net.setListeners(new ScoreIterationListener(1));

			INDArray input = Nd4j.zeros(1, notes_list.size(), notes.length);
			INDArray labels = Nd4j.zeros(1, notes_list.size(), notes.length);

			int samplePos = 0;
			for (Note currentNote : notes) {
				Note nextNote = notes[(samplePos + 1) % (notes.length)];
				input.putScalar(new int[] { 0, notes_list.indexOf(currentNote), samplePos }, 1);
				labels.putScalar(new int[] { 0, notes_list.indexOf(nextNote), samplePos }, 1);
				samplePos++;
			}
			DataSet trainingData = new DataSet(input, labels);

			int numEpochs = 50;

			/**
			 * Trains the model
			 */
			for (int epoch = 0; epoch < numEpochs; epoch++)
			{

				System.out.println("Epoch " + epoch);

				net.fit(trainingData);

				net.rnnClearPreviousState();
			}

			ModelSerializer ms = new ModelSerializer();
			ms.writeModel(net, f, true);
		}





	/**
		 * Generates new notes through the output distribution.
		 */
		INDArray testInit = Nd4j.zeros(notes_list.size());
		testInit.putScalar(notes_list.indexOf(notes[0]), 1);

		// run one step -> IMPORTANT: rnnTimeStep() must be called, not
		// output()
		// the output shows what the net thinks what should come next
		INDArray output = net.rnnTimeStep(testInit);

		ArrayList<Note> generatedNotes = new ArrayList<>();

		for (int j = 0; j < notes.length; j++) {

			double[] outputProbDistribution = new double[notes_list.size()];
			for (int k = 0; k < outputProbDistribution.length; k++) {
				outputProbDistribution[k] = output.getDouble(k);
			}
			int sampledCharacterIdx = findIndexOfHighestValue(outputProbDistribution);


			generatedNotes.add(notes_list.get(sampledCharacterIdx));

			// use the last output as input
			INDArray nextInput = Nd4j.zeros(notes_list.size());
			nextInput.putScalar(sampledCharacterIdx, 1);
			output = net.rnnTimeStep(nextInput);
		}


		System.out.print("\n");

		/**
		 * Adds these notes to a phrase (useful to put into a MIDI file)
		 */

		Score newScore = new Score();

		Phrase newPhrase = new Phrase();
		for (Note n : generatedNotes)
		{
			/**
			 * Can add basic logic here to filter out notes...
			 */

			if (n.getDuration() > 0.3)
			{
				newPhrase.add(n);
			}

		}

		/**
		 * Saves notes into a score
		 */
		Part newPart = new Part();

		newPart.addPhrase(newPhrase);

		/**
		 * Adds a new part
		 */
		newScore.add(newPart);

		Write.midi(newScore, "autumnLeaves_test.mid");

		Score finalScore = Composer.composePiece("autumnLeaves_test.mid", "midis/autumn_leaves.mid", new int[] {1, 2, 3});

		Write.midi(finalScore, "generated_midis/autumn_nights.mid");
		Composer.playPiece("generated_midis/autumn_nights.mid");

	}

	/**
	 * Finds the index of the highest value from a probability distribution. To be used
	 * with the RNN when using the softmax activation function.
	 *
	 * @param distribution The probability distrubtion that is passed through
	 * @return
	 */
	private static int findIndexOfHighestValue(double[] distribution)
	{
		int maxValueIndex = 0;
		double maxValue = 0;
		for (int i = 0; i < distribution.length; i++)
		{
			if(distribution[i] > maxValue) {
				maxValue = distribution[i];
				maxValueIndex = i;
			}
		}
		return maxValueIndex;
	}


	/**
	 * A basic function that returns the melody notes of AutumnLeaves
	 * @return
	 */
	private static Note[] getAutumnLeavesSoloNotes()
	{
		Score newScore = new Score();
		Read.midi(newScore, "midis/autumn_leaves.mid");

		Part soloPart = newScore.getPart(0);

		Phrase soloPhrase = soloPart.getPhrase(0);

		return soloPhrase.getNoteArray();
	}

	/**
	 * A method that returns the notes for a classical piece...the results vary because
	 * this song has a LOT more background noise. 
	 * @return
	 */
	private static Note[] getSolomonNotes()
	{
		Score newScore = new Score();
		Read.midi(newScore, "Solomon67.mid");

		return newScore.getPart(2).getPhrase(0).getNoteArray();

	}

}
