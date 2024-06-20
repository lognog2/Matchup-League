package com.javafx;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import com.Entities.*;
import com.javafx.card.FighterCard;

/**
 * Controller for the game menu.
 * @since 1.1.0
 */
public class GameMenu extends MenuHandler {

    private double userScore = 0.0, opponentScore = 0.0;
    private Team opponentTeam;
    private List<FighterCard> userFighters, opponentFighters;
    private Game game;
    private boolean test;
    private int FPT, FPG;
    private int matchRound = 0;
    @FXML private Label userLabel;
    @FXML private Label opponentLabel;
    @FXML private Label userScoreLabel;
    @FXML private Label opponentScoreLabel;
    @FXML private Label resultLabel;
    @FXML private CheckBox testCheck;
    @FXML private GridPane playField;
    @FXML private GridPane userGrid;
    @FXML private GridPane opponentGrid;
    @FXML private HBox resultBox;
    
    @FXML private void initialize() {
        opponentTeam = userTeam.getOpponent(round);
        game = userTeam.getGame(round);
        FPT = menuManager.getFPT();
        FPG = menuManager.getFPG();

        userFighters = new ArrayList<>(FPT);
        opponentFighters = new ArrayList<>(FPT);
    
        userLabel.setText(userTeam.getName());
        opponentLabel.setText(opponentTeam.getName());
        setGrids();
    }

    private void setGrids() {
        setFighterList(userTeam, userFighters, userGrid, true);
        setFighterList(opponentTeam, opponentFighters, opponentGrid, false);
        setPlayField();
    }

    private void setFighterList(Team t, List<FighterCard> fcList, GridPane grid, boolean setButton) {
        for (Fighter f : t.getFighterList()) {
            FighterCard fc = new FighterCard(f);
            if (setButton) fc.setNameButton(event -> selectFighter(fc));
            fcList.add(fc);
        }
        displayFighters(fcList, grid);
    }

    private void setPlayField() {
        for (int i = 1; i <= FPG; i++) {
            playField.addColumn(i);
        }
    }

    /**
     * After user selects the fighter they want to play, gets the CPU selection.
     * Carries out the match between the two fighters.
     * If it's the last match of the game, the result and scores are
     * set and the game is updated in the database.
     * @param userFC FighterCard that the user selected
     * @since 1.1.0
     */
    @FXML private void selectFighter(FighterCard userFC) {
        resetOpponents();        

        if (test) {
            for (FighterCard oppFC : opponentFighters) {
                matchup(userFC, oppFC);
            }
            userFC.resetCard();
        }
        else {
            //matchRound + 1 because score takes up index 0
            userFC.resetNameLabel();
            playField.add(userFC, matchRound + 1, 1);
            userFighters.remove(userFC);

            FighterCard oppFC = opponentFighters.get(getCPUChoice());
            playField.add(oppFC, matchRound + 1, 0);
            opponentFighters.remove(oppFC);

            int result = matchup(userFC, oppFC);
            if (result == 1) {
                userScore += getMatchPoints();
                userScoreLabel.setText(Integer.toString((int)userScore));
            }
            else if (result == -1) {
                opponentScore += getMatchPoints();
                opponentScoreLabel.setText(Integer.toString((int)opponentScore));
            }

            if (++matchRound >= FPG) {
                disableButtons();
                //user wins ties
                String winner = (opponentScore > userScore) ?
                    opponentTeam.getName() : userTeam.getName();
                resultLabel.setText(winner + " wins!");
                resultBox.setVisible(true);
                int score1 = 0, score2 = 0;
                if (game.getTeam(0).equals(userTeam)) {
                    score1 = Integer.parseInt(userScoreLabel.getText());
                    score2 = Integer.parseInt(opponentScoreLabel.getText());
                } else {
                    score2 = Integer.parseInt(userScoreLabel.getText());
                    score1 = Integer.parseInt(opponentScoreLabel.getText());
                }
                
                menuManager.mergeGame(game, winner, score1, score2);
            }   
        }
    }
    
    private int getCPUChoice() {
        List<Fighter> availableFighters = new ArrayList<>(FPT - matchRound);
        for (FighterCard fc : opponentFighters)
            availableFighters.add(fc.getFighter());
        return opponentTeam.getPlayer().getChoice(availableFighters);
    }

    private double getMatchPoints() {
        return menuManager.matchPoints(matchRound);
    }

    private void resetOpponents() {
        for (FighterCard oppFC : opponentFighters) {
            oppFC.resetCard();
        }
    }

    private void disableButtons() {
        for (FighterCard fc : userFighters) {
            fc.resetNameLabel();
        }
    }

    @FXML private void setTest() {
        test = testCheck.isSelected();
        resetOpponents();
    }

    //TODO: change name
    @FXML private void toSeasonMenu() throws IOException {
        simRound();
        App.goBack();
    } 
}
