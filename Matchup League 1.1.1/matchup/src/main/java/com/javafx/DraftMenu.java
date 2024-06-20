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

/**
 * Controller for the draft menu.
 * @since 1.1.0
 */
public class DraftMenu extends MenuHandler 
{
    
    private List<Fighter> fighterPool;
    private List<Team> draftOrder;
    private Fighter selectedFighter;
    private int currentPage = 1;
    private int totalPages;
    private int draftIndex;
    private int round = 1;
    private final int displaySize = 12;
    private final int displayColumns = displaySize / 2;
    private final int totalRounds = menuManager.getFPT();
    @FXML private GridPane selectedFighters;
    @FXML private GridPane availableFighters;
    @FXML private HBox topBar;
    @FXML private VBox draftList;
    @FXML private Label pageLabel;
    @FXML private Label roundLabel;
    @FXML private Button draftButton;
    
    @FXML
    public void initialize() {
        fighterPool = menuRepo.getFighterPool();
        draftOrder = menuRepo.allTeams_byFans();
        display(currentPage);
    }
 
    /**
     * Displays a page of fighters.
     * @param page Determines place in the fighter pool list to start
     * @since 1.1.0
     */
    private void display(int page) {
        totalPages = (fighterPool.size() / displaySize) + 1;
        pageLabel.setText("Page " + page + "/" + totalPages);
        availableFighters.getChildren().clear();
        int poolIndex = (page - 1) * displaySize;
        
        for (int i = 0; i < displaySize; i++) {
            int row = i / displayColumns;
            int col =  i % displayColumns;

            if (poolIndex < fighterPool.size()) {
                FighterCard fc = new FighterCard(fighterPool.get(poolIndex++));
                if (currentTeam().isUserTeam()) {
                    fc.setNameButton(event -> userSelection(fc));
                }
                availableFighters.add(fc, col, row);
            } 
        }
    }

    /**
     * Begins new round of the draft.
     * CPU teams will take turns selecting fighters until it's the
     * user's turn.
     * @since 1.1.0
     */
    private void doDraft() {
        draftIndex = 0;
        roundLabel.setText("Round " + round + "/" + totalRounds);
        while (!currentTeam().isUserTeam()) {
            selectFighter(false);
        }
        currentPage = 1;
        display(currentPage);
    }

    /**
     * Resumes draft after user has made their choice.
     * CPU team take turns selecting fighters until the round
     * is over; then the next round starts from the beginning,
     * or the draft is completed.
     * @since 1.1.0
     */
    private void continueDraft() {
        //roundLabel.setText("Loading");
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

    /**
     * A player selects a fighter to add to their team
     * @param isUser If true, user selection; if false, CPU selection
     * @since 1.1.0
     */
    private void selectFighter(boolean isUser) {
        if (!currentTeam().isBye()) {
            Fighter fighter = isUser ? selectedFighter : fighterPool.get(selectionIndex(currentTeam()));

            if (menuManager.addFighter(fighter, currentTeam())) {
                if (isUser) {
                    FighterCard fc = new FighterCard(fighter);
                    selectedFighters.add(fc, currentTeam().getFighterListSize() - 1, 0);
                }
                fighterPool.remove(fighter);
                draftList.getChildren().add(new Label(currentTeam().getName() + " selects " + fighter.getNamePlusFID()));
            } else {
                draftList.getChildren().add(new Label(currentTeam().getName() + " did not select a fighter"));
            }
        }
        draftIndex++;
    }

    /**
     * Team currently making their selection.
     * @return Current team in the draft order
     * @since 1.1.0
     */
    private Team currentTeam() {return draftOrder.get(draftIndex);}

    /**
     * Gets a CPU team's fighter selection.
     * The range they select from is 0 to 1 + strategy.
     * @param t Team making the selection
     * @return The index of the selected fighter in the fighter pool
     * @since 1.1.0
     */
    private int selectionIndex(Team t) {return (int)(Math.random() * (t.getPlayer().getStrategy() + 1));}

    @FXML private void beginDraft() {
        roundLabel = new Label();
        roundLabel.setStyle("-fx-font-size: 36px;");
        int index = topBar.getChildren().indexOf(draftButton);
        topBar.getChildren().set(index, roundLabel);
        doDraft();
    }

    @FXML private void nextPage() {
        if (++currentPage > totalPages)
            currentPage = 1;
        display(currentPage);
    }

    @FXML private void prevPage() {
        if (--currentPage < 1)
            currentPage = totalPages;
        display(currentPage);
    }

    @FXML public void userSelection(FighterCard fc) {
        selectedFighter = fc.getFighter();
        continueDraft();
    }

    @FXML private void endDraft() throws IOException {App.setRoot("season_menu");}
    @FXML private void toMenu() throws IOException {super.toMainMenu();}
}

