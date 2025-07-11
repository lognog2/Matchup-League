package com.menu;

import com.entities.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SeasonMenu extends Menu
{
    private League selectedLeague;
    private int selectedRound;
    private int userRank;
    @FXML private Label teamLabel, nextLabel, opponentLabel;
    @FXML private Label recordLabel, roundLabel;
    @FXML private Button playButton, analyticsButton;
    @FXML private ChoiceBox<String> leagueChoice, gameChoice;
    @FXML private VBox standingsBox, gameBox;
    
    @FXML 
    public void initialize() {
        write("SeasonMenu.initialize");
        selectedLeague = userLeague;
        selectedRound = round;
        setLeague(selectedLeague);
        
        setLogo(teamLabel, userTeam);
        recordLabel.setText("Record: " + userTeam.getWins() + "-" + userTeam.getLosses());

        if (!seasonOver()) {
            roundLabel.setText("Game " + (round + 1) + "/" + userLeague.getGameAmt());
            if (userOnBye()) {
                nextLabel.setText("On bye");
                setStyle(nextLabel, CSS_textFill("black)"));
                opponentLabel.setDisable(true);
                playButton.setText("Sim round");
            } else {
                nextLabel.setText("Next game:");
                Team opponent = userTeam.getOpponent(round);
                setLogo(opponentLabel, opponent);
                opponentLabel.setDisable(false);
                playButton.setText("Play game");
            }
        } else {
            roundLabel.setText("Season over");
            setStyle(nextLabel, CSS_textFill("black)"));
            nextLabel.setText("Congrats! You finished " + App.addSuffix(userRank));
            opponentLabel.setDisable(true);
            playButton.setText("To tournament");
        }
        leagueListener();
        gameListener();
    }

    private void leagueListener() {
        write("SeasonMenu.leagueListener");
        for (League lg : leagueList) {
            leagueChoice.getItems().add(lg.getName()); 
        }
        leagueChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                selectedLeague = leagueList.get(newVal.intValue());
                if (selectedLeague != null) {
                    setLeague(selectedLeague);
                }
                else System.out.println("league choice bar index is less than 0");
            }
        });
    }

    private void gameListener() {
        write("SeasonMenu.gameListener");
        gameChoice.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                selectedRound = newVal.intValue();
                if (selectedRound >= 0) {
                    setGames(selectedRound, selectedLeague);
                }
                else System.out.println("game choice bar index is less than 0");
            }
        });
    }

    private void setLeague(League lg) {
        write("SeasonMenu.setLeague", lg.getName());
        setStandings(lg);
        gameChoice.getItems().clear();
        for (int i = 1; i <= lg.getGameAmt(); i++)
            gameChoice.getItems().add("Game " + i);
        setGames(selectedRound, lg);
    }

    private void setStandings(League lg) {
        write("SeasonMenu.setStandings", lg.getName());
        standingsBox.getChildren().clear(); 
        int rank = 1;
        for (Team t : lg.rankTeams()) {
            if (t == userTeam)
                userRank = rank;
            if (t.getFans() != -1)
                standingsBox.getChildren().add(new Label(rank++ + ". " + t.getName() + " " + t.getWins() + "-" + t.getLosses()));
        }
    }

    private void setGames(int round, League lg) {
        write("SeasonMenu.setGames", round, lg.getName());
        gameBox.getChildren().clear();
        for (Game g : repo.getGames_inLeague_byRound(round, lg)) {
            gameBox.getChildren().add(new Label(g.getGameString()));
        }
    }

    private boolean seasonOver() {
        write("SeasonMenu.seasonOver");
        return round >= userLeague.getGameAmt();
    }
    private boolean userOnBye() {
        write("SeasonMenu.userOnBye");
        return userTeam.onBye(round);
    }

    @FXML 
    private void startGame() {
        write("SeasonMenu.startGame");
        if (!seasonOver()) {
            if (userOnBye()) {
                simRound();
                initialize();
                //App.setRoot("season_menu");
            } else App.setRoot("game_menu");
        } else {
            while (!allLeaguesFinished()) {
                simRound();
            }
            setMode(Mode.FINALS);
            generateSchedule();
            //App.setRoot("tournament_menu");
        }
    }

    @FXML private void toTeamView() {write("SeasonMenu.toTeamView"); App.setRoot("view\\team_view");}
    @FXML private void toAnalytics() {write("SeasonMenu.toAnalytics"); App.setRoot("analytics\\analytics");}
    @FXML private void toMenu() {write("SeasonMenu.toMainMenu"); super.toMainMenu();}
}