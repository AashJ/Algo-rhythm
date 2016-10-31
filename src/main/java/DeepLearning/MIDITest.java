package DeepLearning;

import jm.constants.Pitches;
import jm.constants.Scales;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.music.tools.ChordAnalysis;
import jm.util.Play;
import jm.util.Read;
import jm.util.Write;
import org.deeplearning4j.nn.layers.factory.PretrainLayerFactory;

/**
 * Created by Aashish on 8/21/16.
 */
public class MIDITest
{
	public static void main(String[] args)
	{
		/*
		Score s = getScore("testFile.mid");
		s.setTempo(140);
		Play.midi(s);
		*/

		playFile("autumnLeaves_test.mid");

	}

	public static Score getScore(String fileName)
	{
		Score thisScore = new Score();
		Read.midi(thisScore, fileName);
		return thisScore;
	}
	public static void playFile(String fileName)
	{
		Score thisScore = new Score();
		Read.midi(thisScore, fileName);
		Play.midi(thisScore);
	}

	public static void playPhrase(String fileName, int phrase)
	{
		Score thisScore = new Score();
		Read.midi(thisScore, fileName);
		Part p = thisScore.getPart(0);
		Phrase phr = p.getPhrase(phrase);
		phr.setTempo(thisScore.getTempo());
		Play.midi(phr);
	}
}
