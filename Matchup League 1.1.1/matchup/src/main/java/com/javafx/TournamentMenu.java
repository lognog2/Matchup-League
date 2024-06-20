package com.javafx;

import java.io.IOException;
import com.Bracket;
import com.Entities.Game;
import com.javafx.card.GameCard;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TournamentMenu extends MenuHandler {

    private boolean userPlays = false;
    private Bracket bracket;
    @FXML private Label titleLabel;
    @FXML private Label winnerLabel;
    @FXML private Label thanksLabel;
    @FXML private Button playButton;
    @FXML private HBox tourneyBox;
    //@FXML private VBox roundBox;

    private void finishTournament() {
        tourneyBox.getChildren().clear();
        tourneyBox.getChildren().addAll(finals.getRoundBoxes());
        thanksLabel.setVisible(true);
        winnerLabel.setText(bracket.getTeam(0).getName() + " is the winner!");
        winnerLabel.setVisible(true);
        playButton.setVisible(false);
    }

    @FXML 
    public void initialize() {
        if (round < 100) round = 100;
        bracket = finals.getBracket();
        newRound();
        if (userPlays) {
            playButton.setText("Play game");
        } else {
            playButton.setText("Sim round");
        }
    }

    //set result colors for previous round, remove losing teams, and create box for new round
    private void newRound() {
        userPlays = false;
        if (round > 100) {
            for (GameCard gc : finals.getRoundGames()) {
                gc.setWinner();
                bracket.removeTeam(gc.getLoser());
            }
        }
        if (bracket.bracketSize() < 2) {
            finishTournament();
        } else {
            addGames();
        }
    }

    //add new round of games
    private void addGames() {
        finals.setRoundGames();
        for (int i = 0; i < bracket.bracketSize(); i+=2) {
            Game g = new Game(round, bracket.getTeam(i), bracket.getTeam(i+1));
            g.getGameString();
            menuManager.persist(g);
            if (g.userPlays()) userPlays = true;
            if (g.isByeGame()) {
                finals.addGameCard(new GameCard(g, bracket.getSeed(i)+1));
            } else {
                finals.addGameCard(new GameCard(g, bracket.getSeed(i)+1, bracket.getSeed(i+1)+1));
            }
        }
        VBox newRoundBox = new VBox();
        newRoundBox.setAlignment(Pos.CENTER);
        finals.addBox(newRoundBox);
        newRoundBox.getChildren().addAll(finals.getRoundGames());
        tourneyBox.getChildren().clear();
        tourneyBox.getChildren().addAll(finals.getRoundBoxes());
    }

    @FXML private void playGame() throws IOException {
        if (userPlays) {
            App.setRoot("game_menu");
        } else {
            simRound();
            initialize();
        }
    }

    @FXML private void toMenu() throws IOException {super.toMainMenu();}
}
