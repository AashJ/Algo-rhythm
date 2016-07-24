package view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;

public class FileTable<T> extends VBox {
    ListView<T> listView;
    ObservableList<T> items;

    public FileTable(TitledPane titlePane) {
        super();
        this.prefWidthProperty().bind(titlePane.prefWidthProperty());
        this.prefHeightProperty().bind(titlePane.prefHeightProperty());
        listView = new ListView<T>();
        items = FXCollections.observableArrayList();
        listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>(){
            @Override
            public ListCell<T> call(ListView<T> p) {
                ListCell<T> cell = new ListCell<T>(){

                    @Override
                    protected void updateItem(T t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) setText(t.toString());
                    }
                };

                return cell;
            }
        });
        this.listView.setItems(items);
        this.getChildren().add(listView);
    }

    public void addItem(T item) {
        items.add(item);
    }

    public void clearFiles() {
        items = FXCollections.observableArrayList();
        this.listView.setItems(items);
    }

    public ArrayList<T> getItems() {
        return new ArrayList<T>(items);
    }
}
