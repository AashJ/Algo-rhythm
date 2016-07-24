package decoder;

import utils.Complex;
import utils.FFT;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

public abstract class Decoder {
    protected AudioInputStream ais;
    protected Window window;
    protected double[] allData;
    protected Complex[][] cData;
    protected Complex[][] fft;
    protected double[][] magnitudes;

    public int getWindowLength() {
        return window.windowLength;
    }

    public double getSamplingRate() {
        return ais.getFormat().getSampleRate();
    }

    /**
     * Decodes stream with given window size to prepare to retrieve data.
     *
     * @throws IOException
     */
    abstract void decode() throws IOException;

    public Complex[][] getData() throws IOException {
        if (cData == null) decode();
        return cData;
    }

    public void calculateMagnitudes() throws IOException {
        getData();
        calculateFFT();
        magnitudes = new double[fft.length][];
        for (int i = 0; i < cData.length; i++) {
            Complex[] ffti = fft[i];
            magnitudes[i] = new double[ffti.length];

            // Only want first half -  http://dsp.stackexchange.com/questions/4825/why-is-the-fft-mirrored/4827#4827
            for (int j = 0; j < ffti.length/2 - 1; j++) {
                double re = ffti[j].re();
                double im = ffti[j].im();
                magnitudes[i][j] = Math.sqrt(re*re + im*im);
            }
        }
    }

    public double[][] getMagnitudes() throws IOException {
        if (magnitudes == null) calculateMagnitudes();
        return magnitudes;
    }

    public Magnitude getMaximumMagnitude(int index) throws IOException {
        if (magnitudes == null) calculateMagnitudes();

        double max = -Double.MAX_VALUE;
        int maxIndex = -1;

        double[] mi = magnitudes[index];
        for (int i = 0; i < mi.length; i++) {
            double mag = mi[i];
            if (mag > max) {
                max = mag;
                maxIndex = i;
            }
        }

        return new Magnitude(maxIndex, max);
    }

    public void calculateFFT() throws IOException {
        getData();

        fft = new Complex[cData.length][window.windowLength];

        for (int i = 0; i < cData.length; i++) {
            fft[i] = FFT.fft(cData[i]);
        }
    }

    public Complex[][] getFFT() throws IOException {
        if (fft == null) calculateFFT();
        return fft;
    }
}
