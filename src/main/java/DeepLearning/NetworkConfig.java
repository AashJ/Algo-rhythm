package DeepLearning;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Created by Aashish on 7/24/16.
 */
public class NetworkConfig
{
	public NeuralNetConfiguration.ListBuilder createNetConfiguration(int numIterations, double learningRate, OptimizationAlgorithm opt, int seed, int biasInit,
																			boolean miniBatch, Updater updater, WeightInit weightInit)
	{
		NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
		builder.iterations(numIterations);
		builder.learningRate(learningRate);
		builder.optimizationAlgo(opt);
		builder.seed(seed);
		builder.biasInit(biasInit);
		builder.miniBatch(miniBatch);
		builder.updater(updater);
		builder.weightInit(weightInit);

		return builder.list();
	}

	public MultiLayerNetwork createRNN(NeuralNetConfiguration.ListBuilder listBuilder, int numHiddenLayers, int hiddenLayerSize,
												  String hiddenActivation, String outputActivation, int outputSize, boolean pretrain,
												  boolean backprop, LossFunctions.LossFunction lossFunction)
	{
		for (int i = 0; i < numHiddenLayers; i++)
		{
			GravesLSTM.Builder hiddenLayerBuilder = new GravesLSTM.Builder();
			hiddenLayerBuilder.nIn(i == 0 ? outputSize : hiddenLayerSize);
			hiddenLayerBuilder.nOut(hiddenLayerSize);
			hiddenLayerBuilder.activation(hiddenActivation);
			listBuilder.layer(i, hiddenLayerBuilder.build());
		}

		RnnOutputLayer.Builder outputLayerBuilder = new RnnOutputLayer.Builder(lossFunction);

		outputLayerBuilder.activation(outputActivation);
		outputLayerBuilder.nIn(hiddenLayerSize);
		outputLayerBuilder.nOut(outputSize);
		listBuilder.layer(numHiddenLayers, outputLayerBuilder.build());
		listBuilder.pretrain(pretrain);
		listBuilder.backprop(backprop);
		MultiLayerConfiguration conf = listBuilder.build();
		MultiLayerNetwork net = new MultiLayerNetwork(conf);
		net.init();
		net.setListeners(new ScoreIterationListener(1));

		return net;
	}
}
