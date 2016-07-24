package timbre;

import decoder.Decoder;
import decoder.WavDecoder;

import java.io.IOException;

public interface AudioFeature {
    public String title();
    public double[] calculateFeature(Decoder decoder, double samplingRate) throws IOException;
}
