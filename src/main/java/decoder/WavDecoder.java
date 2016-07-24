package decoder;

import utils.Complex;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class WavDecoder extends Decoder {
    public WavDecoder(File file, Window w) throws IOException, UnsupportedAudioFileException {
        this.window = w;
        ais = AudioSystem.getAudioInputStream(file);
    }

    @Override
    public void decode() throws IOException {
        int size = ais.available();
        byte[] data = new byte[size];
        ais.read(data);
        allData = new double[data.length/2];

        ByteBuffer bb = ByteBuffer.wrap(data);
        if (ais.getFormat().isBigEndian()) {
            bb.order(ByteOrder.BIG_ENDIAN);
        } else {
            bb.order(ByteOrder.LITTLE_ENDIAN);
        }

        for (int i = 0; i < allData.length; i++) {
            double ret = (double)(bb.getShort());
            ret /= Short.MAX_VALUE;
            allData[i] = ret;
        }

        cData = new Complex[allData.length/window.windowLength][window.windowLength];
        for (int i = 0; i < cData.length; i++) {
            // overlap is important -- http://edoc.mpg.de/get.epl?fid=55356&did=395068&ver=0
            cData[i] = window(i*window.windowLength);
        }
    }

    private Complex[] window(int pos) {
        // w(n) = alpha - Beta * cos(2πn/(N-1))
        Complex[] output = new Complex[window.windowLength];

        for (int i = pos; i < pos + window.windowLength; i++) {
            int j = i - pos; // go to 0
            double windowModifier = window.getWeight(j);
            output[j] = new Complex(allData[i]*windowModifier/2, 0);
        }

        return output;
    }

}
