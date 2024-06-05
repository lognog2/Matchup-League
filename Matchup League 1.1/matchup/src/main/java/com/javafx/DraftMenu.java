package com.javafx;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.Entities.*;
import com.javafx.card.FighterCard;

import java.util.List;

public class DraftMenu extends MenuHandler 
{
    
    private List<Fighter> fighterPool;
    private List<Team> draftOrder;
    private Fighter selectedFighter;
    private int currentPage = 1;
    private int totalPages;
    private int draftIndex;
    private int round = 1;
    private final int displaySize = 8;
    private final int displayColumns = 4;
    private final int totalRounds = menuManager.getFPT();
    @FXML private GridPane selectedFighters;
    @FXML private GridPane availableFighters;
    @FXML private HBox topBar;
    @FXML private VBox draftList;
    @FXML private Label pageLabel;
    @FXML private Label roundLabel;
    @FXML private Button draftButton;
    
    @FXML
    public void initialize()
    {
        fighterPool = menuRepo.getFighterPool();
        draftOrder = menuRepo.allTeams_byFans();
        display(currentPage);
    }
 
    private void display(int page)
    {
        totalPages = (fighterPool.size() / displaySize) + 1;
        pageLabel.setText("Page " + page + "/" + totalPages);
        availableFighters.getChildren().clear();
        int poolIndex = (page - 1) * displaySize;
        
        for (int i = 0; i < displaySize; i++)
        {
            int row = i / displayColumns;
            int col =  i % displayColumns;

            if (poolIndex < fighterPool.size())
            {
                FighterCard fc = new FighterCard(fighterPool.get(poolIndex++));
                if (currentTeam().isUserTeam()) {
                    fc.setNameButton(event -> userSelection(fc));
                }
                availableFighters.add(fc, col, row);
            } 
        }
    }

    private void doDraft()
    {
        draftIndex = 0;
        roundLabel.setText("Round " + round + "/" + totalRounds);
        while (!currentTeam().isUserTeam()) {
            selectFighter(false);
        }
        currentPage = 1;
        display(currentPage);
    }

    private void continueDraft()
    {
        roundLabel.setText("Loading");
        selectFighter(true);

        while (draftIndex < draftOrder.size()) {
            selectFighter(false);
        }

        if (round == totalRounds) {
            draftButton.setText("End draft");
            draftButton.setOnAction(event -> {try {endDraft();} catch (IOException e) {e.printStackTrace();}});
            int index = topBar.getChildren().indexOf(roundLabel);
            topBar.getChildren().set(index, draftButton);
        }
        else {
            round++;
            doDraft();
        }
    }

    private void selectFighter(boolean isUser)
    {
        Fighter fighter = isUser ? selectedFighter : fighterPool.get(selectionIndex(currentTeam()));

        if (menuManager.draftFighter(fighter, currentTeam())) {
            if (isUser) {
                FighterCard fc = new FighterCard(fighter);
                selectedFighters.add(fc, currentTeam().getFighterListSize() - 1, 0);
            }
            fighterPool.remove(fighter);
            draftList.getChildren().add(new Label(currentTeam().getName() + " selects " + fighter.getName()));
        } else {
            draftList.getChildren().add(new Label(currentTeam().getName() + " did not select a fighter"));
        }
        draftIndex++;
    }

    private Team currentTeam() {return draftOrder.get(draftIndex);}
    private int selectionIndex(Team t) {return (int)(Math.random() * (t.getPlayer().getStrategy() + 1));}

    

    @FXML
    private void beginDraft()
    {
        roundLabel = new Label();
        roundLabel.setStyle("-fx-font-size: 36px;");
        int index = topBar.getChildren().indexOf(draftButton);
        topBar.getChildren().set(index, roundLabel);
        doDraft();
    }

    @FXML
    private void nextPage()
    {
        if (++currentPage > totalPages)
            currentPage = 1;
        display(currentPage);
    }

    @FXML
    private void prevPage()
    {
        if (--currentPage < 1)
            currentPage = totalPages;
        display(currentPage);
    }

    @FXML
    public void userSelection(FighterCard fc)
    {
        selectedFighter = fc.getFighter();
        continueDraft();
    }

    @FXML private void endDraft() throws IOException {App.setRoot("season_menu");}
    @FXML private void toMenu() throws IOException {super.toMainMenu();}
}

