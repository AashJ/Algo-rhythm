package view;

import decoder.Decoder;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import timbre.AudioFeature;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GraphView extends TabPane {
    public boolean displayConstantRateFeatures(String title, AudioFeature feature, double sampleRate, List<Decoder> decoders) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(title);

        //creating the chart
        final LineChart<Number,Number> lineChart = new LineChart<>(xAxis,yAxis);
        lineChart.setTitle(title);
        lineChart.setCreateSymbols(false);
        for (Decoder d : decoders) {
            synchronized(d) {
                Task<Void> task = new Task<Void>() {
                    @Override protected Void call() throws Exception {
                        double[] values = feature.calculateFeature(d, sampleRate); // TODO: Do in background
                        XYChart.Series series = new XYChart.Series();
                        series.setName(d.toString());
                        for (int i = 0; i < values.length; i++) {
                            series.getData().add(new XYChart.Data(i, values[i]));
                        }
                        Platform.runLater(() -> {
                            lineChart.getData().add(series);
                        });
                        return null;
                    }
                };
                new Thread(task).start();
            }
        }
        Tab tab = new Tab();
        tab.setText(title);
        tab.setContent(lineChart);
        this.getTabs().add(tab);
        //this.getChildren().add(lineChart);

        return true;
    }

    public void removeTabs() {
        this.getTabs().remove(0, this.getTabs().size());
    }
}
