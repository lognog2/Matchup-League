package com.menu;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import com.entities.*;
import com.menu.card.FighterCard;

/**
 * Controller for the game menu.
 * @since 1.1.0
 */
public class GameMenu extends Menu {

    private double userScore = 0.0, opponentScore = 0.0;
    private Team opponentTeam;
    private List<FighterCard> userFighters, opponentFighters;
    private Game game;
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
        write("GameMenu.initialize");
        opponentTeam = userTeam.getOpponent(round);
        game = userTeam.getGame(round);
        FPT = manager.getFPT();
        FPG = manager.getFPG();

        userFighters = new ArrayList<>(FPT);
        opponentFighters = new ArrayList<>(FPT);
    
        userLabel.setText(userTeam.getName());
        opponentLabel.setText(opponentTeam.getName());
        setTextColor(userLabel, userTeam.getColor(0));
        setTextColor(opponentLabel, opponentTeam.getColor(0));
        setGrids();
    }

    private void setGrids() {
        write("GameMenu.setGrids");
        setFighterList(userTeam, userFighters, userGrid, true);
        setFighterList(opponentTeam, opponentFighters, opponentGrid, false);
        setPlayField();
    }

    private void setFighterList(Team t, List<FighterCard> fcList, GridPane grid, boolean setButton) {
        write("GameMenu.setFighterList", t.getName(), fcList, grid, setButton);
        for (Fighter f : t.getFighterList()) {
            FighterCard fc = new FighterCard(f, true);
            if (setButton) fc.setNameButton(event -> selectFighter(fc));
            fcList.add(fc);
        }
        displayFighters(fcList, grid);
    }

    private void setPlayField() {
        write("GameMenu.setPlayField");
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
        write("GameMenu.selectFighter", userFC.getFighter().getName());
        resetOpponents();        

        if (testMode()) {
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
                
                manager.mergeSoloGame(game, winner, score1, score2);
            }   
        }
    }
    
    private int getCPUChoice() {
        write("GameMenu.getCPUChoice");
        List<Fighter> availableFighters = new ArrayList<>(FPG - matchRound);
        for (int i = 0; i < FPG - matchRound; i++) {
            availableFighters.add(opponentFighters.get(i).getFighter());
        }
            
        return opponentTeam.getPlayer().getChoice(availableFighters);
    }

    private double getMatchPoints() {
        return manager.matchPoints(matchRound);
    }

    /**
     * Carries out a matchup between two fighters.
     * Displays winning and losing fighter and modifiers applied on fighter card.
     * Merges result to database.
     * @param fc1 fighter card from team1
     * @param fc2 fighter card from team2
     * @return 1 if f1 wins, -1 if f2 wins, or 0 if a tie
     * @since 1.1.0
     * @version 2
     */
    private int matchup(FighterCard fc1, FighterCard fc2) {
        write("GameMenu.matchup", fc1.getFighterName(), fc2.getFighterName());
        Fighter f1 = fc1.getFighter();
        Fighter f2 = fc2.getFighter();

        int base1 = f1.getBase();
        int base2 = f2.getBase();
        boolean str1 = false, wk1 = false, str2 = false, wk2 = false;

        //f1 strength
        if (modApplies(f2.getTypes(), f1.getStrType())) {
            str1 = true;
            base1 += f1.getStrVal();
        }
        //f1 weakness
        if (modApplies(f2.getTypes(), f1.getWkType())) {
            wk1 = true;
            base1 -= f1.getWkVal();
        }
        //f2 strength
        if (modApplies(f1.getTypes(), f2.getStrType())) {
            str2 = true;
            base2 += f2.getStrVal();
        }
        //f2 weakness
        if (modApplies(f1.getTypes(), f2.getWkType())) {
            wk2 = true;
            base2 -= f2.getWkVal();
        }

        int result;
        if (base1 > base2) result = 1;
        else if (base1 < base2) result = -1;
        else result = 0;

        if (!testMode()) manager.mergeSoloMatch(f1, f2, result);

        fc1.setResult(result, base1, str1, wk1);
        fc2.setResult(result * -1, base2, str2, wk2);
        return result;
    }

    private void resetOpponents() {
        write("GameMenu.resetOpponents");
        for (FighterCard oppFC : opponentFighters) {
            oppFC.resetCard();
        }
    }

    private void disableButtons() {
        write("GameMenu.disableButtons");
        for (FighterCard fc : userFighters) {
            fc.resetNameLabel();
        }
    }

    /**
     * Checks if the test mode checkbox has been selected.
     * @return true when in testing mode, false if not
     * @since 1.1.2
     */
    private boolean testMode() {return testCheck.isSelected();} 

    @FXML private void setTest() {
        write("FXML: GameMenu.setTest");
        resetOpponents();
    }

    @FXML private void exitGame() throws IOException {
        write("FXML: GameMenu.exitGame");
        simRound();
        App.goBack();
    } 
}
