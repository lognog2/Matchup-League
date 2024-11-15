package com.menu;

import com.entities.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TeamSelect extends Menu {
    private int lgIndex, tmIndex;
    @FXML private Label leagueLabel, teamLabel, teamLogo, fansLabel, errorLabel, debug;
    @FXML private TextField name;
    @FXML private VBox root;
     
    @FXML
    public void initialize() {
        write("TeamSelect.initialize");
        tmIndex = 0;
        lgIndex = 0;
        errorLabel.setVisible(true);
        debug.setVisible(true);
        displayTeam(currentTeam());
    }

    private void displayTeam(Team t) {
        leagueLabel.setText(t.getLeague().getName());
        teamLabel.setText(t.getName());
        setLogo(teamLogo, t);
        fansLabel.setText("Fans: " + t.getFans());
    }

    private League currentLeague() {return leagueList.get(lgIndex);}
    private Team currentTeam() {return currentLeague().getTeam(tmIndex);}

    @FXML private void nextLeague() {
        tmIndex = 0;
        if (++lgIndex >= leagueList.size())
            lgIndex = 0;
        displayTeam(currentTeam());
    }

    @FXML private void prevLeague() {
        tmIndex = 0;
        if (--lgIndex < 0)
            lgIndex = leagueList.size() - 1;
        displayTeam(currentTeam());
    }

    @FXML private void nextTeam() {
        if (++tmIndex >= currentLeague().getTeamListSize())
            tmIndex = 0;
        if (currentTeam().isBye()) {
            nextTeam();
        } else {
            displayTeam(currentTeam());
        }
    }

    @FXML private void prevTeam() {
        if (--tmIndex < 0)
            tmIndex = currentLeague().getTeamListSize() - 1;
        
        if (currentTeam().isBye()) {
            prevTeam();
        } else {
            displayTeam(currentTeam());
        }
    }

    @FXML
    private void startSeason() {
        write("FXML: TeamSelect.startSeason");
        if (name.getText().isEmpty() || currentTeam() == null) {
            setStyleClass(errorLabel, "label.warning");
            errorLabel.setVisible(false);
            errorLabel.setText("Enter a name and select a team before continuing");
        } else if (manager.addUser(name.getText(), currentTeam())) {
            setUserTeam(currentTeam());
            generateSchedule();
        } else {
            setStyleClass(errorLabel, "label.error");
            errorLabel.setVisible(false);
            errorLabel.setText("(-2) An error occured, try again");
        }
    }

    @FXML private void toMenu() {
        write("FXML: TeamSelect.toMenu"); super.toMainMenu();
    }
}
