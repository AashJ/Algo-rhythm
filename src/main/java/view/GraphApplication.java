package view;

import controller.ControllerManager;
import controller.MainViewController;
import decoder.MP3Decoder;
import decoder.WavDecoder;
import decoder.WindowFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import timbre.AudioFeature;
import timbre.Frequency;
import timbre.SpectralCentroid;
import timbre.SpectralRolloff;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class GraphApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/MainView.fxml"));
        Parent root = loader.load();
        MainViewController controller = loader.getController();
        controller.passStage(stage);
        stage.setMinHeight(500);
        stage.setMinWidth(500);
        stage.setTitle("Audio Analysis");

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
