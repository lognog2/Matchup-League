package com.menu;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

public class MainMenu extends Menu
{
    @FXML private CheckBox debugCheckBox;
    @FXML private Label versionLabel;
    

    @FXML
    private void initialize() {
        versionLabel.setText(App.VERSION);
    }

    @FXML
    private void newSeason() throws IOException {
        write("FXML: MainMenu.newSeason");
        setMode(Mode.SEASON);
        debug = debugCheckBox.isSelected();
        startLoadData();
    }

    @FXML
    private void newTournament() throws IOException {
        write("FXML: MainMenu.newTournament");
        debug = debugCheckBox.isSelected();
        App.setRoot("tournament_select");
    }
}
