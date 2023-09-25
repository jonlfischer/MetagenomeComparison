package edu.metagenomenewtest.presenter.Coloring;

import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public abstract class ColorScale {
     double MIN;
     double MAX;


    public abstract Color getValueColor(Double value);


    //TODO: make this prettier and scalable or smth

    public StackPane createColorScaleImage(int width, int height, Orientation orientation,
                                                   String title, String topLabel, String bottomLabel) {
        WritableImage image = new WritableImage(width / 2, height);
        PixelWriter pixelWriter = image.getPixelWriter();
           /* if (orientation == Orientation.HORIZONTAL) {
                for (int x = 0; x < width; x++) {
                    double value = MIN + (MAX - MIN) * x / width;
                    Color color = getValueColor(value, 1);
                    for (int y = 0; y < height; y++) {
                        pixelWriter.setColor(x, y, color);
                    }
                }
            } else { */
        StackPane pane = new StackPane();
        pane.setPrefWidth(width);
        pane.setPrefHeight(height);
        for (int y = 0; y < height; y++) {
            double value = MAX - (MAX - MIN) * y / height;
            Color color = getValueColor(value);
            for (int x = 0; x < width / 2; x++) {
                if (y % 10 == 0 && x > width * 0.4)
                    pixelWriter.setColor(x, y, Color.BLACK);
                pixelWriter.setColor(x, y, color);
                //TODO this is pretty naiive
            }
        }
        ImageView view = new ImageView(image);
        view.setX(0);
        view.setY(0);
        pane.getChildren().add(view);
        Group tickLabels = new Group();
        for (int z = 0; z <= height; z += height / 10) {
            double value = MAX - (MAX - MIN) * z / height;
            Text text = new Text(String.format("%.2f", value));
            text.setX(width);
            text.setY(z);
            tickLabels.getChildren().add(text);
        }
        //TODO position these better
        Text titleText = new Text(title);
        titleText.setY(-0.15 * height);
        titleText.setX(width);

        Text topText = new Text(topLabel);
        topText.setY((int) (-0.1 * height));
        topText.setX(width);

        Text bottomText = new Text(bottomLabel);
        bottomText.setY((int) (height + 0.05 * height));
        bottomText.setX(width);

        Group legendLabels = new Group();
        legendLabels.getChildren().addAll(titleText, topText, bottomText);

        pane.getChildren().addAll(tickLabels, legendLabels);
        return pane;
    }




}
