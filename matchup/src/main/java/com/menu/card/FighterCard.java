package com.menu.card;

import com.entities.Fighter;
import com.menu.App;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.VBox;

//card dimensions: 150/190

/**
 * UI for a single fighter, showing all its stats.
 * @since 1.1.0
 */
public class FighterCard extends VBox
{
    private final String fxmlPath = "card\\fighter_card"; 

    private Fighter fighter;
    private FighterCardController fcc;

    /**
     * Contrsucts an empty fighter card.
     * @since 1.1.0
     */
    public FighterCard() {
        connect();
    }
    /**
     * Constructs a fighter card associated with a fighter.
     * @param fighter fighter
     * @param showMatches Whether to show number of matches played/won or not
     * @since 1.1.0
     * @version 2
     */
    public FighterCard(Fighter fighter, boolean showMatches) {
        if (connect()) {
            setFighter(fighter);
            fcc.showMatches(showMatches);
        }
    }

    /**
     * Connects this fighter card to an fc controller
     * @return True if successful, false if not
     * @since 1.1.0
     * @version 2
     */
    private boolean connect() {
        fcc = (FighterCardController)App.getController(fxmlPath, this);
        fcc.setFighterCard(this);
        return true;
    }

    //get methods

    /**
     * Gets associated fighter.
     * @return fighter
     * @since 1.1.0
     */
    public Fighter getFighter() {return fighter;}

    public String getFighterName() {return fighter.getName();}
    
    /**
     * Gets associated controller.
     * @return fcc
     * @since 1.1.0
     */
    public FighterCardController getController() {return fcc;}

    //set methods

    /**
     * Sets fighter and labels.
     * @param fighter new fighter
     * @since 1.1.0
     */
    public void setFighter(Fighter fighter) {this.fighter = fighter; setLabels();}

    private void setLabels() {
        fcc.setTypesLabel(fighter.getTypesString());
        fcc.setNameLabel(fighter.getName());
        fcc.setBaseLabel(fighter.getBaseString());
        fcc.setStrengthLabel(fighter.getStrString());
        fcc.setWeaknessLabel(fighter.getWkString());
        fcc.setMatchesLabel(fighter.getMatchesWon(), fighter.getMatchesPlayed());
    }

    /**
     * Changes the name label to a button.
     * @param event Method called when button is clicked
     * @since 1.1.0
     */
    public void setNameButton(EventHandler<ActionEvent> event) {fcc.setNameButton(fighter.getName(), event);}

    /**
     * Chaanges the name button back to a label.
     * @since 1.1.0
     */
    public void resetNameLabel() {fcc.resetNameLabel(fighter.getName());}

    /**
     * Changes labels colors to match result.
     * Sets name and base color green if win, red if loss, blue if tie
     * @param result 1 for a win, -1 for a loss, 0 for a tie
     * @param newBase
     * @param appliedStrength green if true
     * @param appliedWeak red if true
     * @since 1.1.0
     */
    public void setResult(int result, int newBase, boolean appliedStrength, boolean appliedWeak) {
        fcc.setBaseLabel(Integer.toString(newBase));
        fcc.setNameColor(result);
        fcc.setStrengthColor(appliedStrength);
        fcc.setWeaknessColor(appliedWeak);
        fcc.setMatchesLabel(fighter.getMatchesWon(), fighter.getMatchesPlayed());
    }

    /**
     * Sets labels and colors back to their original values.
     * @since 1.1.0
     */
    public void resetCard() {
        fcc.setBaseLabel(fighter.getBaseString());
        fcc.resetCard();
    }


}
