package edu.metagenomecomparison;

import edu.metagenomecomparison.model.TaxonRank;
import edu.metagenomecomparison.presenter.WindowPresenter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        var view = new WindowView(); // create view

        new WindowPresenter(view.getController());

        // set the stage and show
        Scene scene = new Scene(view.getRoot());


        stage.setScene(scene);
        stage.setTitle("ComparativeTreeViewer");
        stage.show();

        System.out.println((TaxonRank.CLASS.compareTo(TaxonRank.FAMILY)));
    }

    public static void main(String[] args) {
        launch();
    }
}