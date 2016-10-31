package AudioAnalysis.decoder;

import AudioAnalysis.utils.Complex;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by Aashish on 12/20/15.
 */
public class MP3Decoder extends Decoder
{
	public MP3Decoder(File file, Window w) throws IOException, UnsupportedAudioFileException
	{
		this.window = w;
		ais = AudioSystem.getAudioInputStream(file);
	}

	@Override
	void decode() throws IOException
	{
		AudioInputStream din = null;
		AudioFormat baseFormat = ais.getFormat();

		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding
				  .PCM_SIGNED,
				  baseFormat.getSampleRate(),
				  16,
				  baseFormat.getChannels(),
				  baseFormat.getChannels() * 2,
				  baseFormat.getSampleRate(),
				  false);

		din = AudioSystem.getAudioInputStream(decodedFormat, ais);

		byte[] b = new byte[din.available()];
		din.read(b);
		ByteBuffer bb = ByteBuffer.wrap(b);
		allData = new double[b.length/2];
		for (int i = 0; i < allData.length; i++) {
			short val = bb.getShort();
			allData[i] = (double)val;
			allData[i] /= Short.MAX_VALUE;
		}

		cData = new Complex[allData.length/window.windowLength][window.windowLength];
		for (int i = 0; i < cData.length; i++) {
			cData[i] = window(i*window.windowLength);
		}
	}

	private Complex[] window(int pos) {
		// w(n) = alpha - Beta * cos(2πn/(N-1))
		Complex[] output = new Complex[window.windowLength];

		for (int i = pos; i < pos + window.windowLength; i++) {
			int j = i - pos; // go to 0
			double windowModifier = window.getWeight(j);
			output[j] = new Complex(allData[i]*windowModifier, 0);
		}

		return output;
	}
}

