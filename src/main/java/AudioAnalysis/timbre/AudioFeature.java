package AudioAnalysis.timbre;

import AudioAnalysis.decoder.Decoder;
import AudioAnalysis.decoder.WavDecoder;

import java.io.IOException;


public interface AudioFeature {
    public String title();
    public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException;
}
