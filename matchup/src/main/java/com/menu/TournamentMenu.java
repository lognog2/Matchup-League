package com.menu;

import java.io.IOException;
import com.Bracket;
import com.entities.Game;
import com.menu.card.GameCard;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TournamentMenu extends Menu {

    private Bracket bracket;
    @FXML private Label titleLabel;
    @FXML private Label winnerLabel;
    @FXML private Label thanksLabel;
    @FXML private Button playButton;
    @FXML private HBox tourneyBox;
    //@FXML private VBox roundBox;

    private void finishTournament() {
        write("TournamentMenu.");
        tourneyBox.getChildren().clear();
        tourneyBox.getChildren().addAll(tourney.getRoundBoxes());
        thanksLabel.setVisible(true);
        winnerLabel.setText(bracket.getTeam(0).getName() + " is the winner!");
        winnerLabel.setVisible(true);
        playButton.setVisible(false);
    }

    @FXML 
    public void initialize() {
        write("TournamentMenu.initialize");
        //if (round < 100) startTourney();
        bracket = tourney.getBracket();
        setResults();
        displayGames();

        if (userPlays())
            playButton.setText("Play game");
        else
            playButton.setText("Sim round");
    }

    /**
     * Sets result colors for previous round, remove losing teams.
     * If tournament is not over, it calls {@link TournamentMenu#addGames()}
     * Otherwise it calls {@link TournamentMenu#finishTournament()}
     * @since 1.1.1
     * @version 2
     */
    private void setResults() {
        write("TournamentMenu.setResults");
        if (round > 100) {
            for (GameCard gc : tourney.getRoundGames()) {
                if (gc.gameFinished()) {
                    gc.setWinner();
                    bracket.removeTeam(gc.getLoser());
                } else break;
            }
        }
        if (bracket.bracketSize() < 2) {
            finishTournament();
        } else {
            if (tourney.newRound()) addGames();
        }
    }

    /**
     * Adds new round of games.
     * @since 1.1.0
     */
    private void addGames() {
        write("TournamentMenu.addGames");
        tourney.setRoundGames();
        for (int i = 0; i < bracket.bracketSize(); i+=2) {
            Game g = new Game(round, bracket.getTeam(i), bracket.getTeam(i+1));
            g.getGameString();
            manager.persistSolo(g);
            if (g.isByeGame()) {
                tourney.addGameCard(new GameCard(g, bracket.getSeed(i)+1));
            } else {
                tourney.addGameCard(new GameCard(g, bracket.getSeed(i)+1, bracket.getSeed(i+1)+1));
            }
        }
        VBox newRoundBox = new VBox();
        newRoundBox.setAlignment(Pos.TOP_CENTER);
        tourney.addBox(newRoundBox);
        newRoundBox.getChildren().addAll(tourney.getRoundGames());
    }

    private void displayGames() {
        write("TournamentMenu.displayGames");
        tourneyBox.getChildren().clear();
        tourneyBox.getChildren().addAll(tourney.getRoundBoxes());
    }

    private boolean userPlays() {
        write("TournamentMenu.userPlays");
        for (GameCard gc : tourney.getRoundGames()) {
            if (gc.getGame().userPlays()) return true;
        }
        return false;
    }

    @FXML private void playGame() throws IOException {
        write("FXML: TournamentMenu.playGame");
        if (userPlays()) {
            App.setRoot("game_menu");
        } else {
            simRound();
            initialize();
        }
    }

    @FXML private void toTeamView() throws IOException {write("FXML: TournamentMenu.toTeamView"); App.setRoot("view\\team_view");}
    @FXML private void toMenu() throws IOException {write("TournamentMenu.toMenu"); super.toMainMenu();}
}
