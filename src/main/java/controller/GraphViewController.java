package controller;

import decoder.Decoder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import timbre.AudioFeature;
import view.GraphView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GraphViewController extends Controller implements Initializable {
    @FXML
    private AnchorPane graphPaneView;

    private GraphView graphs;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGraphPane();
    }

    private void setupGraphPane() {
        graphs = new GraphView();
        graphs.setStyle("-fx-background-color: #" + "AABBCC");

        graphs.prefWidthProperty().bind(graphPaneView.widthProperty());
        graphs.prefHeightProperty().bind(graphPaneView.heightProperty());

        this.graphPaneView.getChildren().add(graphs);
    }

    public void removeGraphs() {
        graphs.removeTabs();
    }

    public void analyzeDecoders(String title, AudioFeature feature, List<Decoder> decoders, double sampleRate) {
        graphs.displayConstantRateFeatures(title, feature, sampleRate, decoders);
    }
}
