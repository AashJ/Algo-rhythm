package DeepLearning;

import decoder.HammingWindow;
import decoder.MP3Decoder;
import decoder.Window;
import org.bytedeco.javacpp.opencv_core;
import timbre.Frequency;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * Created by Aashish on 7/24/16.
 */
public class DataConfig
{
	public void parseMP3File(File f)
	{
		try
		{
			MP3Decoder decoder = new MP3Decoder(f, new HammingWindow());
			Frequency frequency = new Frequency();
			frequency.calculateFeature(decoder, 44100);


		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e)
		{
			e.printStackTrace();
		}
	}
}
