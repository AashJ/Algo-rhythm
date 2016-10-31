# Algo-rhythm
Algo-rhythm is a recurrent neural network written using the DL4J framework aimed at creating new jazz improvised melodies. 

In the src/java folder, you'll find two different packages. The first package, AudioAnalysis, contains a variety of AudioAnalysis tools that can be used to extract data from mp3/wav files. The second package, Deep Learning, contains various RNNs that i've written using the DeepLearning4J framework. 


Audio Analysis:
- decoder: a collection of classes that are used to decode a wav or mp3 to their most basic parts. 
- model: a simple class to represent an audio item
- timbre: a colleciton of classes that represent different audio features
- utils: a collection of classes pulled from Princeton CS Department that are useful for audio analysis