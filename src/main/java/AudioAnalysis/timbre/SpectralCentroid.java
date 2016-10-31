package AudioAnalysis.timbre;

import AudioAnalysis.decoder.Decoder;

import java.io.IOException;
import java.util.Arrays;

/**
 * Based on: https://en.wikipedia.org/wiki/Spectral_centroid
 * http://jcis.sbrt.org.br/index.php/JCIS/article/viewFile/292/205
 */
public class SpectralCentroid implements AudioFeature {


    @Override
    public String title() {
        return "Spectral Centroid";
    }

    /**
     * Method: calculateFeature
     *
     * Calculates the spectral centroid for each sample by multiplying the frequency with its bin and dividing
     * by total frequency.
     *
     * @param decoder   The decoder to provide the fft and magnitudes values
     * @param samplingRate  The sampling rate of the audio file, used to calculate central frequency
     * @return  a double array consisting of all the spectral centroids per magnitude
     */
    @Override
    public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException {
        double[][] magnitudes = decoder.getMagnitudes();
        double[] ret = new double[magnitudes.length];

        for (int i = 0; i < magnitudes.length; i++) {
            double[] mags = magnitudes[i];
            double nSum = 0;
            double dSum = 0;

            for (int j = 1;  j < mags.length; j++) {
                double n = samplingRate/decoder.getWindowLength();
                nSum += mags[j]*mags[j]*j;
                dSum += mags[j]*mags[j];
            }

            ret[i] = nSum/dSum;
        }
        System.out.println(Arrays.toString(ret));
        return ret;
    }
}
