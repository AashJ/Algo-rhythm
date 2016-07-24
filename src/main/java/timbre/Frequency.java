package timbre;

import decoder.Decoder;
import decoder.WavDecoder;
import decoder.WindowFactory;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Frequency implements AudioFeature {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        WavDecoder decoder = new WavDecoder(new File("/Users/Rishabh/Desktop/audio/test2.wav"), WindowFactory.HammingWindow());
        Frequency f = new Frequency();
        try {
            f.calculateFeature(decoder, 44100);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String title() {
        return "Frequency";
    }

    @Override
    public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException {

        /**
         * max_magnitude = -INF
         max_index = -1
         for i = 0 to N / 2 - 1
         if magnitude[i] > max_magnitude
         max_magnitude = magnitude[i]
         max_index = i

         // convert index of largest peak to frequency
         freq = max_index * Fs / N
         */


        double[][] magnitudes = decoder.getMagnitudes();
        double[] ret = new double[magnitudes.length];
        for (int i = 0; i < magnitudes.length; i++) {
            double[] mg = magnitudes[i];
            double max = -Double.MAX_VALUE;
            int maxIndex = -1;

            for (int j = 0; j < mg.length; j++) {
                if (mg[j] > max) {
                    max = mg[j];
                    maxIndex = j;
                }
            }

            ret[i] = maxIndex*samplingRate/decoder.getWindowLength();
        }

        return ret;
    }
}
