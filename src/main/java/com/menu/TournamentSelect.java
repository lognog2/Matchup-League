package com.menu;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

public class TournamentSelect extends Menu {

    private final ToggleGroup modes = new ToggleGroup();
    @FXML RadioButton leagueRB;
    @FXML RadioButton madnessRB;
    @FXML RadioButton worldcupRB;
    @FXML Label errorLabel;

    @FXML
    public void initialize() {
        write("FXML: TournamentSelect.initialize");
        leagueRB.setUserData(Mode.LEAGUE);
        leagueRB.setToggleGroup(modes);
        madnessRB.setUserData(Mode.MADNESS);
        madnessRB.setToggleGroup(modes);
        worldcupRB.setUserData(Mode.WORLDCUP);
        worldcupRB.setToggleGroup(modes);
        errorLabel.setVisible(false);
    }

    @FXML
    public void select() throws IOException {
        write("FXML: TournamentSelect.select");
        if (modes.getSelectedToggle() == null) {
            errorLabel.setVisible(true);
        } else {
            setMode((Mode)modes.getSelectedToggle().getUserData());
            startLoad();
        }
    }
}
