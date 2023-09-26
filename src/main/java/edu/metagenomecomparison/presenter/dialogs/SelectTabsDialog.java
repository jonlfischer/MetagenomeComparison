package edu.metagenomecomparison.presenter.dialogs;

import edu.metagenomecomparison.presenter.GraphTab;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SelectTabsDialog extends Dialog<GraphTab[][]> {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private Button selectAllButton;

    @FXML
    private Button selectNoneButton;

    @FXML
    private ScrollPane showTabsDialogScrollpane;
    /**
     * a dialog that allows choosing tabs. opened from a list of file names, user can choose between pairwise comparison
     * and single sample display
     * @param samples the list of file names
     * @param owner owner window
     */
    public SelectTabsDialog(String[] samples, Window owner){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/edu/metagenomecomparison/ChooseTabsDialog.fxml"));
            loader.setController(this);
            DialogPane dialogPane = loader.load();

            initOwner(owner);

            setResizable(true);

        ArrayList<String> choices = new ArrayList<>();
        choices.add("Pairwise comparison");
        choices.add("Single sample");
        choiceBox.setItems(FXCollections.observableList(choices));
        choiceBox.setValue("Choose visualization");


        GridPane pairwiseGridPane = new GridPane();
        CheckBox[][] checkBoxesPairwise = new CheckBox[samples.length][samples.length];

        for (int i = 0; i < samples.length; i ++) {
            for (int j = 0; j < i; j++) {
                String tabName = samples[i] + " vs " + samples[j];
                            CheckBox checkBox = new CheckBox(tabName);
                            pairwiseGridPane.add(checkBox, i, j);
                            checkBoxesPairwise[i][j] = checkBox;
                            //TODO change indexing because empty column 0
            }
        }

        GridPane gridPaneSingle = new GridPane();
        CheckBox[] checkBoxesSingle = new CheckBox[samples.length];
        for (int i = 0; i < samples.length; i ++) {
                String tabName = samples[i];
                CheckBox checkBox = new CheckBox(tabName);
                gridPaneSingle.add(checkBox, 0, i);
                checkBoxesSingle[i] = checkBox;

        }



        selectAllButton.setOnAction(i -> {
            for (int x = 0; x < checkBoxesPairwise.length; x++) {
                for (int y = 0; y < checkBoxesPairwise[0].length; y++) {
                    if (checkBoxesPairwise[x][y] != null)
                        checkBoxesPairwise[x][y].setSelected(true);
                    if (checkBoxesSingle[x] != null)
                        checkBoxesSingle[x].setSelected(true);
                }
            }
        });


        selectNoneButton.setOnAction(i -> {
            for (int x = 0; x < checkBoxesPairwise.length; x++) {
                for (int y = 0; y < checkBoxesPairwise[0].length; y++) {
                    if (checkBoxesPairwise[x][y] != null)
                        checkBoxesPairwise[x][y].setSelected(false);
                    if (checkBoxesSingle[x] != null)
                        checkBoxesSingle[x].setSelected(false);
                }
            }
        });


        choiceBox.setOnAction(e -> {
            if (choiceBox.getValue().equals("Pairwise comparison")) {
                //anchorPane.getChildren().retainAll(choiceBox, selectAllButton, selectNoneButton);
                showTabsDialogScrollpane.setContent(pairwiseGridPane);
                //anchorPane.getChildren().add(pairwiseGridPane);
            }
            else if (choiceBox.getValue().equals("Single sample")) {
                //anchorPane.getChildren().retainAll(choiceBox, selectAllButton, selectNoneButton);
                showTabsDialogScrollpane.setContent(gridPaneSingle);
                //anchorPane.getChildren().add(gridPaneSingle);
            }

        });

        setDialogPane(dialogPane);
        this.setResultConverter(new Callback<ButtonType, GraphTab[][]>() {
            @Override
            public GraphTab[][] call(ButtonType buttonType) {
                boolean isPairwise = (choiceBox.getValue().equals("Pairwise comparison"));

                GraphTab[][] resultTabs = new GraphTab[isPairwise ? samples.length : 1]
                        [samples.length];
                for (int i = 0; i < samples.length; i++){
                    if (!isPairwise){
                        if (checkBoxesSingle[i] != null && checkBoxesSingle[i].isSelected()) {
                            resultTabs[0][i] = new GraphTab(samples[i]);
                            resultTabs[0][i].setIds(i);
                        }
                    }
                    else {
                        for (int j = 0; j < samples.length; j++) {
                            if (checkBoxesPairwise[i][j] != null && checkBoxesPairwise[i][j].isSelected()) {
                                resultTabs[i][j] = new GraphTab(samples[i] + " vs " + samples[j]);
                                resultTabs[i][j].setIds(i, j);
                            }
                        }
                    }
                }
                return resultTabs;
            }

        });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @FXML
    private void initialize(){}

    }

