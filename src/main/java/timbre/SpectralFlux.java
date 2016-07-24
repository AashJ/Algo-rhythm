package timbre;

import decoder.Decoder;

import java.io.IOException;

/**
 * @author Aashish Jain
 * @version December 21, 2015
 *
 * Based on: http://jcis.sbrt.org.br/index.php/JCIS/article/viewFile/292/205
 */
public class SpectralFlux implements AudioFeature
{
	@Override
	public String title() {
		return "Spectral Flux";
	}

	/**
	 * Calculates the spectral flux for each sample by the calculation given in this
	 * paper: http://jcis.sbrt.org.br/index.php/JCIS/article/viewFile/292/205
	 *
	 *
	 * @param decoder The decoder to provide the fft and magnitude values
	 * @param samplingRate The sampling rate of the audio file
	 * @return A double array consisting of all the spectral flux per magnitude.
	 * @throws IOException
	 */
	@Override
	public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException
	{
		double[][] magnitudes = decoder.getMagnitudes();
		double[] ans = new double[magnitudes.length];
		for (int i = 1; i < magnitudes.length; i++)
		{
			double[] mags = magnitudes[i];
			double[] prevMags = magnitudes[i - 1];
			double sum = 0;

			for (int j = 1; j < mags.length; j++)
			{
				double logCalc = Math.log10(mags[j]) - Math.log10(prevMags[j]);
				sum += (logCalc * logCalc);
			}

			ans[i] = sum;
		}
		return ans;
	}
}
