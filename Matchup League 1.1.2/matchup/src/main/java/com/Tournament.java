package com;

import java.util.ArrayList;
import java.util.List;
import com.Entities.Team;
import com.menu.card.GameCard;

import javafx.scene.layout.VBox;

/**
 * Holds data for a tournament.
 * @since 1.1.1
 * @version 1.0
 */
public class Tournament {

    private Bracket bracket;
    private List<GameCard> roundGames;
    private List<VBox> roundBoxes;

    public Tournament(List<Team> teams) {
        bracket = new Bracket(teams);
        roundBoxes = new ArrayList<>(bracket.roundAmt());
        setRoundGames();
    }

    public Bracket getBracket() {return bracket;}
    public List<GameCard> getRoundGames() {return roundGames;}
    public List<VBox> getRoundBoxes() {return roundBoxes;}

    /**
     * Initializes a new ArrayList to hold the next round of game cards.
     * @return true if new round of games initialized
     * @since 1.1.1
     */
    public void setRoundGames() {
        roundGames = new ArrayList<>(bracket.bracketSize() / 2);
    }
    public void addGameCard(GameCard gc) {roundGames.add(gc);}
    public void addBox(VBox vbox) {roundBoxes.add(vbox);}

    /**
     * @return true if bracket display has not been updated for the current round.
     * @since 1.1.2
     */
    public boolean newRound() {return roundGames.size() > bracket.bracketSize() / 2 || roundGames.isEmpty();}
}
