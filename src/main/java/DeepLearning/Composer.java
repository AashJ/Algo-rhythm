package DeepLearning;

import jm.music.data.Part;
import jm.music.data.Score;
import jm.util.Play;
import jm.util.Read;
import jm.util.Write;

/**
 * Created by Aashish on 10/18/16.
 */
public class Composer
{
	public static void main(String[] args)
	{
		//Score s = composePiece("generated_midis/autumn_nights_2.mid", "midis/autumn_leaves.mid", new int[] {1, 2, 3});
		//Write.midi(s, "generated_midis/autumn_nights_final.mid");
		playPiece("generated_midis/autumn_nights_final.mid");
	}

	public static Score composePiece(String midiPath, String originalMidi, int[] backgroundParts)
	{
		Score s = new Score();
		Read.midi(s, midiPath);

		Score originalScore = new Score();
		Read.midi(originalScore, originalMidi);

		s.setTempo(originalScore.getTempo());

		for (int i = 0; i < backgroundParts.length; i++)
		{
			int backgroundPart = backgroundParts[i];

			Part p = originalScore.getPart(backgroundPart);

			s.add(p);
		}

		return s;
	}

	public static void playPiece(String midiPath)
	{
		Score thisScore = new Score();
		Read.midi(thisScore, midiPath);
		Play.midi(thisScore);
	}


}
