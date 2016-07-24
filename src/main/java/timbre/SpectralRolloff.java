package timbre;

import decoder.Decoder;

import java.io.IOException;


// http://dspace.library.uvic.ca:8080/bitstream/handle/1828/1344/tsap02gtzan.pdf?sequence=1
public class SpectralRolloff implements AudioFeature {

    @Override
    public String title() {
        return "Spectral Rolloff";
    }

    @Override
    public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException {
        double[][] magnitudes = decoder.getMagnitudes();
        double[] ret = new double[magnitudes.length];

        for (int i = 0; i < magnitudes.length; i++) {
            double sum = 0;

            double[] magi = magnitudes[i];
            for (double d : magi) {
                sum += d;
            }

            sum *= 0.85;
            ret[i] = sum;
        }

        return ret;
    }
}
