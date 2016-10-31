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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Aashish on 8/19/16.
 * Adapted from DJ4J Recurrent Model
 */
public class NetworkConfig
{

	public static MultiLayerNetwork createLSTM(int HIDDEN_LAYER_CONT, int HIDDEN_LAYER_WIDTH, int DATA_SIZE)
	{
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

		for (int i = 0; i < HIDDEN_LAYER_CONT; i++) {
			GravesLSTM.Builder hiddenLayerBuilder = new GravesLSTM.Builder();
			hiddenLayerBuilder.nIn(i == 0 ? DATA_SIZE : HIDDEN_LAYER_WIDTH);
			hiddenLayerBuilder.nOut(HIDDEN_LAYER_WIDTH);
			hiddenLayerBuilder.activation("tanh");
			listBuilder.layer(i, hiddenLayerBuilder.build());
		}

		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT);
		outputLayerBuilder.activation("softmax");
		outputLayerBuilder.nIn(HIDDEN_LAYER_WIDTH);
		outputLayerBuilder.nOut(DATA_SIZE);
		listBuilder.layer(HIDDEN_LAYER_CONT, outputLayerBuilder.build());

		listBuilder.pretrain(false);
		listBuilder.backprop(true);

		MultiLayerConfiguration conf = listBuilder.build();
		MultiLayerNetwork net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

		return net;
	}

	public static Phrase[] getPhrases(String fileName, int partNumber)
	{
		Score theScore = new Score("temp score");
		Read.midi(theScore, fileName);

		Part p = theScore.getPart(partNumber);

		return p.getPhraseArray();
	}

	public static ArrayList<Phrase> getPossiblePhrasesList(Phrase[] phrases)
	{
		List<Phrase> possible_phrases_list = new ArrayList<Phrase>();

		LinkedHashSet<Phrase> possible_phrases = new LinkedHashSet<Phrase>();
		for (Phrase p : phrases)
			possible_phrases.add(p);
		possible_phrases_list.addAll(possible_phrases);

		return (ArrayList<Phrase>) possible_phrases_list;
	}

	public static DataSet setupDataSet(Phrase[] phrases)
	{
		ArrayList<Phrase> possible_phrases_list = getPossiblePhrasesList(phrases);
		INDArray input = Nd4j.zeros(1, possible_phrases_list.size(), phrases.length);
		INDArray labels = Nd4j.zeros(1, possible_phrases_list.size(), phrases.length);
		int samplePos = 0;
		for (Phrase currentPhrase : phrases) {
			Phrase nextPhrase = phrases[(samplePos + 1) % (phrases.length)];
			input.putScalar(new int[] { 0, possible_phrases_list.indexOf(currentPhrase), samplePos }, 1);
			labels.putScalar(new int[] { 0, possible_phrases_list.indexOf(nextPhrase), samplePos }, 1);
			samplePos++;
		}
		DataSet trainingData = new DataSet(input, labels);

		return trainingData;
	}

	public static void train(DataSet trainingData, int numEpochs, MultiLayerNetwork net)
	{
		for (int epoch = 0; epoch < numEpochs; epoch++) {

			System.out.println("Epoch " + epoch);

			// train the data
			net.fit(trainingData);

		}
	}

	public static ArrayList<Phrase> generate(MultiLayerNetwork net, ArrayList<Phrase> possible_phrases_list, Phrase[] phrases)
	{
		net.rnnClearPreviousState();

		INDArray testInit = Nd4j.zeros(possible_phrases_list.size());
		testInit.putScalar(possible_phrases_list.indexOf(phrases[(phrases.length - 1) / 2]), 1);


		INDArray output = net.rnnTimeStep(testInit);

		ArrayList<Phrase> generatedPhrases = new ArrayList<>();

		for (int j = 0; j < phrases.length * 2; j++) {

			double[] outputProbDistribution = new double[possible_phrases_list.size()];
			for (int k = 0; k < outputProbDistribution.length; k++) {
				outputProbDistribution[k] = output.getDouble(k);
			}
			int sampledCharacterIdx = findIndexOfHighestValue(outputProbDistribution);

			generatedPhrases.add(possible_phrases_list.get(sampledCharacterIdx));

			INDArray nextInput = Nd4j.zeros(possible_phrases_list.size());
			nextInput.putScalar(sampledCharacterIdx, 1);
			output = net.rnnTimeStep(nextInput);

		}
		System.out.print("\n");

		return generatedPhrases;
	}

	private static int findIndexOfHighestValue(double[] distribution) {
		int maxValueIndex = 0;
		double maxValue = 0;
		for (int i = 0; i < distribution.length; i++) {
			if(distribution[i] > maxValue) {
				maxValue = distribution[i];
				maxValueIndex = i;
			}
		}
		return maxValueIndex;
	}

	public static void main(String[] args)
	{

		Score thisScore = new Score();
		/*
		String fileName = "original_metheny.mid";
		for (int i = 0; i < 5; i++)
		{

			Phrase[] phrases = getPhrases("original_metheny.mid", i);

			DataSet trainingData = setupDataSet(phrases);
			MultiLayerNetwork net = createLSTM(50, 2, phrases.length);
			ArrayList<Phrase> possible_phrases_list = getPossiblePhrasesList(phrases);
			train(trainingData, 50, net, possible_phrases_list, phrases);
			ArrayList<Phrase> generatedPhrases = generate(net, possible_phrases_list, phrases);

			Part thisPart = new Part();

			for (Phrase p : generatedPhrases)
			{
				if (p.getNoteArray().length != 0)
				{
					thisPart.add(p);
				}
			}

			thisScore.add(thisPart);
		}
		*/
		for (int i = 0; i < 4; i++)
		{

			Phrase[] phrases = getPhrases("original_metheny.mid", i);

			DataSet trainingData = setupDataSet(phrases);
			MultiLayerNetwork net = createLSTM(50, 2, phrases.length);
			ArrayList<Phrase> possible_phrases_list = getPossiblePhrasesList(phrases);
			ArrayList<Phrase> generatedPhrases = generate(net, possible_phrases_list, phrases);

			Part thisPart = new Part();

			for (Phrase p : generatedPhrases)
			{
				if (p.getNoteArray().length != 0)
				{
					thisPart.add(p);
					double totalTime = p.getStartTime() - p.getEndTime();
				}
			}

			thisScore.add(thisPart);

		}




		Write.midi(thisScore, "mentheny.mid");

	}

}
