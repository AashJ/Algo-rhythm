package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import model.AudioItem;
import view.FileTable;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class LeftViewController extends Controller implements Initializable {
    @FXML
    private AnchorPane leftPaneView;

    private FileTable<AudioItem> fileTable;
    private TitledPane fileInformationPane;
    private ProgressBar progressBar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initialized Left View Controller");
        addVerticalLayout();
    }

    private void addVerticalLayout() {
        leftPaneView.setMinHeight(100);
        leftPaneView.setMinWidth(150);

        fileInformationPane = new TitledPane();
        fileInformationPane.setText("Analyzed Files");
        fileInformationPane.prefWidthProperty().bind(leftPaneView.widthProperty());

        fileTable = new FileTable(fileInformationPane);
        fileInformationPane.setContent(fileTable);

        leftPaneView.heightProperty().addListener(
                (observer, oldValue, newValue) -> {
                    fileInformationPane.setPrefHeight(newValue.doubleValue());
                });

        VBox totalBox = new VBox();
        totalBox.prefWidthProperty().bind(leftPaneView.widthProperty());
        totalBox.prefHeightProperty().bind(leftPaneView.heightProperty());

        progressBar = new ProgressBar();
        progressBar.setProgress(0);
        progressBar.prefWidthProperty().bind(leftPaneView.widthProperty());

        totalBox.getChildren().add(progressBar);
        totalBox.getChildren().add(fileInformationPane);

        this.leftPaneView.getChildren().add(totalBox);
    }

    public void addItem(AudioItem item) {
        fileTable.addItem(item);
    }

    public void clearFiles() {
        fileTable.clearFiles();
    }

    public ArrayList<AudioItem> getItems() {
        return fileTable.getItems();
    }

    public void toggleIndeterminateProgressBar() {
        Platform.runLater(() -> {
            if (this.progressBar.getProgress() == 0) this.progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            else this.progressBar.setProgress(0);
        });

    }
}
