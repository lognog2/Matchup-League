package com.menu;

import java.io.IOException;
import com.Entities.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TeamSelect extends Menu
{
    private League selectedLeague;
    private Team selectedTeam;
    @FXML private ChoiceBox<String> leagues;
    @FXML private ChoiceBox<String> teams;
    @FXML private Label teamLabel, fansLabel, errorLabel, debug;
    @FXML private TextField name;
     
    @FXML
    public void initialize() {
        write("TeamSelect.initialize");
        for (League lg : leagueList) {
            leagues.getItems().add(lg.getName()); 
        }
        leagueListener();
        teamListener();
    }

    private void leagueListener() {
        write("TeamSelect.leagueListener");
        leagues.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                if (newVal.intValue() >= 0) {
                    selectedLeague = leagueList.get(newVal.intValue());
                    teamLabel.setText("");
                    fansLabel.setText("");
                    teams.getItems().clear();
                    for (Team t : selectedLeague.getTeamList())
                        teams.getItems().add(t.getName()); 
                }
                else write("league choice bar index is less than 0");
            }
        });
    }

    private void teamListener() {
        write("TeamSelect.teamListener");
        teams.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                if (newVal.intValue() >= 0) {
                    selectedTeam = selectedLeague.getTeamList().get(newVal.intValue());
                    setTextColor(teamLabel, selectedTeam.getColor(0));
                    teamLabel.setText(selectedTeam.getName());
                    fansLabel.setText("Fans: " + selectedTeam.getFans());
                }  
                else write("team choice bar index is less than 0");
            }
        });
    }

    @FXML
    private void startSeason() throws IOException {
        write("FXML: TeamSelect.startSeason");
        if (name.getText().isEmpty() || selectedTeam == null) {
            errorLabel.setText("Enter a name and select a team before continuing");
        } else if (manager.addUser(name.getText(), selectedTeam)) {
            setUserTeam(selectedTeam);
            generateSchedule();
        }
        else {
            errorLabel.setText("An error occured, try again");
            //initialize();
        }
    }

    @FXML private void toMenu() throws IOException {write("FXML: TeamSelect.toMenu"); super.toMainMenu();}
}
