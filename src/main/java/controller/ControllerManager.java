package controller;

import decoder.Decoder;
import decoder.WavDecoder;
import decoder.WindowFactory;
import model.AudioItem;
import timbre.AudioFeature;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ControllerManager {
    private LeftViewController leftController;
    private MainViewController mainController;
    private GraphViewController graphController;

    private ArrayList<Decoder> decoders;

    public ControllerManager(MainViewController main, LeftViewController left, GraphViewController graphController) {
        this.leftController = left;
        this.mainController = main;
        this.graphController = graphController;

        decoders = new ArrayList<Decoder>();
    }

    public boolean addFile(AudioItem item) {
        leftController.addItem(item);
        Decoder d = null;
        try {
            d = new WavDecoder(new File(item.path), WindowFactory.HammingWindow());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
            return false;
        }
        if (d != null) decoders.add(d);
        return true;
    }

    public void clearFiles() {
        leftController.clearFiles();
        decoders = new ArrayList<Decoder>();
    }

    public void removeGraphs() {
        graphController.removeGraphs();
    }

    public void analyzeDecoders(AudioFeature feature, String title, double sampleRate) {
        toggleProgress();
        graphController.analyzeDecoders(title, feature, decoders, sampleRate);
        toggleProgress();
    }

    public void toggleProgress() {
        leftController.toggleIndeterminateProgressBar();
    }
}
