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

public class GameMenu extends MenuHandler {

    private double userScore = 0.00, opponentScore = 0.00;
    private Team opponentTeam;
    private List<FighterCard> userFighters, opponentFighters;
    private Game game;
    private boolean test;
    private int FPT, FPG;
    private int matchRound = 1;
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
    
    @FXML private void initialize()
    {
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

    private void setGrids()
    {
        setFighterList(userTeam, userFighters, userGrid, true);
        setFighterList(opponentTeam, opponentFighters, opponentGrid, false);
        setPlayField();
    }

    private void setFighterList(Team t, List<FighterCard> fcList, GridPane grid, boolean setButton)
    {
        for (Fighter f : t.getFighterList()) {
            FighterCard fc = new FighterCard(f);
            if (setButton) fc.setNameButton(event -> selectFighter(fc));
            fcList.add(fc);
        }
        displayFighters(fcList, grid);
    }

    private void setPlayField()
    {
        for (int i = 1; i <= FPG; i++)
        {
            playField.addColumn(i);
        }
    }

    @FXML private void selectFighter(FighterCard userFC)
    {
        for (FighterCard oppFC : opponentFighters) {oppFC.resetCard();}

        if (test)
        {
            for (FighterCard oppFC : opponentFighters)
            {
                matchup(userFC, oppFC);
            }
            userFC.resetCard();
        }
        else
        {
            userFC.resetNameLabel();
            playField.add(userFC, matchRound, 1);
            userFighters.remove(userFC);

            FighterCard oppFC = opponentFighters.get(getCPUChoice());
            playField.add(oppFC, matchRound, 0);
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

            /*if ((int)userScore == (int)opponentScore)
            {
                userScoreLabel.setText(Double.toString(userScore));
                opponentScoreLabel.setText(Double.toString(opponentScore));
            }*/

            //System.out.println(userScore + "-" + opponentScore);

            if (++matchRound > FPG) {
                String winner = "nobody";
                if (userScore > opponentScore)
                    winner = userTeam.getName();
                else if (opponentScore > userScore)
                    winner = opponentTeam.getName();
                resultLabel.setText(winner + " wins!");
                resultBox.setVisible(true);
                String score1 = userScoreLabel.getText();
                String score2 = opponentScoreLabel.getText();
                menuManager.mergeGame(game, winner, score1, score2);
            }
                
        }
    }
    private int getCPUChoice()
    {
        List<Fighter> availableFighters = new ArrayList<>(FPG - matchRound + 1);
        for (FighterCard fc : opponentFighters)
            availableFighters.add(fc.getFighter());
        return opponentTeam.getPlayer().getChoice(availableFighters);
    }

    private double getMatchPoints()
    {
        double tiebreak = ((double)matchRound / 100.00);
        return 1.00 + tiebreak;
    }

    @FXML private void setTest() {test = testCheck.isSelected();}

    @FXML private void toSeasonMenu() throws IOException 
    {
        //userLabel.setText("Loading");
        menuManager.simRound(round++);
        App.setRoot("season_menu");
    }
}
