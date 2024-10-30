package com.menu.view;

import java.io.IOException;
import com.entities.*;
import com.menu.App;
import com.menu.Menu;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class TeamView extends Menu {

    private int lgIndex, tmIndex;
    @FXML private Label teamLabel;
    @FXML private Label leagueLabel;
    @FXML private Label fansLabel;
    @FXML private Label recordLabel;
    @FXML private GridPane fightersGrid;
    @FXML private VBox scheduleBox;
    
    @FXML
    public void initialize() {
        lgIndex = leagueList.indexOf(userLeague);
        tmIndex = userLeague.getTeamList().indexOf(userTeam);
        displayTeam(userTeam);
    }

    private void displayTeam(Team t) {
        leagueLabel.setText(t.getLeague().getName());
        teamLabel.setText(t.getName());
        setTextColor(teamLabel, t.getColor(0));
        fansLabel.setText("Fans: " + t.getFans());
        recordLabel.setText("Record: " + t.getWins() + "-" + t.getLosses());
        displayFighters(t);
        displaySchedule(t);
    }

    private void displayFighters(Team t) {
        fightersGrid.getChildren().clear();
        while (fightersGrid.getColumnCount() > 0)
            fightersGrid.getColumnConstraints().remove(0);

        displayFighters(t, fightersGrid);
    }

    private void displaySchedule(Team t) {
        scheduleBox.getChildren().clear();
        for (Game g : t.getSchedule()) {
            scheduleBox.getChildren().add(new Label(g.getGameString(t)));
        }
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

    //TODO: change name
    @FXML private void toSeasonMenu() throws IOException {App.goBack();}
}
