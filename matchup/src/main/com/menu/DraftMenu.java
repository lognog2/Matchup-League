package com.menu;

import java.io.IOException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.Entities.*;
import com.menu.card.FighterCard;
import java.util.List;

/**
 * Controller for the draft menu.
 * @since 1.1.0
 */
public class DraftMenu extends Menu {
    
    private List<Fighter> fighterPool;
    private List<Team> draftOrder;
    private Fighter selectedFighter;
    private int currentPage = 1;
    private int totalPages;
    private int draftIndex;
    private int round = 1;
    private final int displaySize = 12;
    private final int displayColumns = displaySize / 2;
    private final int totalRounds = manager.getFPT();
    @FXML private GridPane selectedFighters;
    @FXML private GridPane availableFighters;
    @FXML private HBox topBar;
    @FXML private VBox draftList;
    @FXML private Label pageLabel;
    @FXML private Label roundLabel;
    @FXML private Button draftButton;
    
    @FXML
    public void initialize() {
        write("DraftMenu.initialize");
        fighterPool = repo.getFighterPool();
        draftOrder = repo.allTeams_byFans();
        display(currentPage);
    }
 
    /**
     * Displays a page of fighters.
     * @param page Determines place in the fighter pool list to start
     * @since 1.1.0
     */
    private void display(int page) {
        write("runLater: DraftMenu.display", page);
        Platform.runLater(() -> {
            totalPages = (fighterPool.size() / displaySize) + 1;
            pageLabel.setText("Page " + page + "/" + totalPages);
            availableFighters.getChildren().clear();
            int poolIndex = (page - 1) * displaySize;
            for (int i = 0; i < displaySize; i++) {
                int row = i / displayColumns;
                int col =  i % displayColumns;

                if (poolIndex < fighterPool.size()) {
                    FighterCard fc = new FighterCard(fighterPool.get(poolIndex++), false);
                    //System.out.println(fc.getFighter().getName());
                    if (currentTeam().isUserTeam()) {
                        fc.setNameButton(event -> userSelection(fc));
                    }
                    availableFighters.add(fc, col, row);
                    //System.out.println(availableFighters.getChildren().size());
                } //else System.out.println("pool index > fighter pool size");
            }
        });
    }

    /**
     * Begins new round of the draft.
     * CPU teams will take turns selecting fighters until it's the
     * user's turn.
     * @since 1.1.0
     */
    private void doDraft() {
        write("DraftMenu.doDraft");
        //System.out.println("doDraft");
        draftIndex = 0;
        while (!currentTeam().isUserTeam()) {
            setTopText(currentTeam());
            selectFighter(false);
        }
        //continueDraft();
        currentPage = 1;
        display(currentPage);
        setTopText(currentTeam());
    }

    /**
     * Resumes draft after user has made their choice.
     * CPU team take turns selecting fighters until the round
     * is over; then the next round starts from the beginning,
     * or the draft is completed.
     * @since 1.1.0
     */
    private void continueDraft() {
        write("DraftMenu.continueDraft");
        //System.out.println("continueDraft");
        selectFighter(true);
        //not a loading thread, so explicitly call startThread and endThread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                startThread("SimDraft");
                while (draftIndex < draftOrder.size()) {
                    setTopText(currentTeam());
                    selectFighter(false);
                }
                if (round == totalRounds) {
                    endDraft();
                } else {
                    round++;
                    doDraft();
                }
                endThread("SimDraft");
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * A player selects a fighter to add to their team
     * @param isUser If true, user selection; if false, CPU selection
     * @since 1.1.0
     */
    private void selectFighter(boolean isUser) {
        write("DraftMenu.selectFighter", isUser);
        Team team = currentTeam();
            if (!currentTeam().isBye()) {
                //Fighter fighter = fighterPool.get(selectionIndex(team));
                Fighter fighter = isUser ? selectedFighter : fighterPool.get(selectionIndex(team));
    
                if (manager.addFighter(fighter, team)) {
                    if (isUser) {
                        FighterCard fc = new FighterCard(fighter, false);
                        selectedFighters.add(fc, team.getFighterListSize() - 1, 0);
                    }
                    fighterPool.remove(fighter);
                    addSelection(team.getName() + " selects " + fighter.getNamePlusFID());
                } else {
                    addSelection(team.getName() + " did not select a fighter");
                }
            }
            draftIndex++;
    }

    /**
     * Team currently making their selection.
     * @return Current team in the draft order
     * @since 1.1.0
     */
    private Team currentTeam() {
        //write("DraftMenu.currentTeam");
        return draftOrder.get(draftIndex);
    }

    /**
     * Gets a CPU team's fighter selection.
     * The range they select from is 0 to 1 + strategy.
     * If their selection is outside the range of available fighers, the index of the last fighter is chosen.
     * @param t Team making the selection
     * @return The index of the selected fighter in the fighter pool
     * @since 1.1.0
     */
    private int selectionIndex(Team t) {
        write("DraftMenu.selectionIndex", t.getName());
        int index = (int)(Math.random() * (t.getPlayer().getStrategyID() + 1));
        return (index < fighterPool.size()) ? index : fighterPool.size() - 1;
    }

    /**
     * Sets text at top of draft menu to indicate which team is making their selection.
     * Says the team name if a CPU, says "your" if a user team. This method is placed in the runLater queue.
     * @since 1.1.2
     */
    private void setTopText(Team t) {
        write("runLater: DraftMenu.setTopText", t.getName());
        Platform.runLater(() -> {
            //write("execute: DraftMenu.setTopText", t.getName());
            String teamName = (t.isUserTeam()) ? "Your" : t.getName() + "'s";
            roundLabel.setText(teamName + " turn to choose");
        });
    }

    private void addSelection (String text) {
        write("runLater: DraftMenu.addSelection", text);
        Platform.runLater(() -> {
            //write("execute: DraftMenu.addSelection", text);
            draftList.getChildren().add(new Label(text));
        });
    }

    private void endDraft() {
        write("runLater: DraftMenu.endDraft");
        Platform.runLater(() -> {
            //write("execute: DraftMenu.endDraft");
            draftButton.setText("End draft");
            draftButton.setOnAction
                (event -> {try {nextMenu();} catch (IOException e) {e.printStackTrace();}});
            int index = topBar.getChildren().indexOf(roundLabel);
            topBar.getChildren().set(index, draftButton);
        });
    }

    @FXML private void beginDraft() {
        write("FXML: DraftMenu.beginDraft");
        roundLabel = new Label();
        roundLabel.setStyle("-fx-font-size: 36px;");
        int index = topBar.getChildren().indexOf(draftButton);
        topBar.getChildren().set(index, roundLabel);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                doDraft();
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML private void nextPage() {
        write("FXML: DraftMenu.nextPage");
        if (++currentPage > totalPages)
            currentPage = 1;
        display(currentPage);
    }

    @FXML private void prevPage() {
        write("FXML: DraftMenu.prevPage");
        if (--currentPage < 1)
            currentPage = totalPages;
        display(currentPage);
    }

    @FXML public void userSelection(FighterCard fc) {
        write("FXML: DraftMenu.userSelection", fc.getFighter().getName());
        selectedFighter = fc.getFighter();
        //System.out.println("user selected " + selectedFighter.getName());
        continueDraft();
    }

    @FXML private void nextMenu() throws IOException {
        write("FXML: DraftMenu.nextMenu");
        String next = (getMode() == Mode.SEASON) ? "season_menu" : "tournament_menu";
        App.setRoot(next);
    }
    @FXML private void toMenu() throws IOException {write("FXML: DraftMenu.toMainMenu"); super.toMainMenu();}
}

