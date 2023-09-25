package edu.metagenomenewtest;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Objects;

/**
 * this creates scene graph from a fxml file
 * Daniel Huson, 5.2022
 */
public class WindowView {
	private final WindowController controller;
	private final Parent root;

	public WindowView() throws IOException {
		try (var ins = Objects.requireNonNull(getClass().getResource("MainWindow.fxml")).openStream()) {
			var fxmlLoader = new FXMLLoader();
			root = fxmlLoader.load(ins);
			controller = fxmlLoader.getController();
			layoutView();
		}
	}

	public WindowController getController() {
		return controller;
	}

	public Parent getRoot() {
		return root;
	}

	public void layoutView(){
		GridPane bottomGrid = controller.getBottomGrid();
		VBox vBox = controller.getvBox();
		ProgressBar progressBar = controller.getProgressBar();
		System.out.println(vBox.getWidth());
		vBox.widthProperty().addListener((observableValue, number, t1) -> {
			bottomGrid.setPrefWidth(vBox.getWidth()-20);
			progressBar.setPrefWidth(bottomGrid.getPrefWidth() / 2 -10);
		});
	}
}
