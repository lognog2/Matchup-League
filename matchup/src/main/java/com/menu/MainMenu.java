package com.menu;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class MainMenu extends Menu
{
    @FXML private CheckBox debugCheckBox;
    
    @FXML
    private void newSeason() throws IOException {
        write("FXML: MainMenu.newSeason");
        setMode(Mode.SEASON);
        debug = debugCheckBox.isSelected();
        startLoad();
    }

    @FXML
    private void newTournament() throws IOException {
        write("FXML: MainMenu.newTournament");
        debug = debugCheckBox.isSelected();
        App.setRoot("tournament_select");
    }
}
