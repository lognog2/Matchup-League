package com.javafx.card;

import java.io.IOException;
import com.Entities.Fighter;
import com.javafx.App;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

//card dimensions: 150/190

public class FighterCard extends VBox
{
    private final String fxmlPath = "card\\fighter_card"; 

    private Fighter fighter;
    private FighterCardController fcc;

    public FighterCard()
    {
        connect();
    }
    public FighterCard(Fighter fighter)
    {
        if (connect()) {
            setFighter(fighter);
        } else fcc.setNameLabel("Error");
    }

    private boolean connect()
    {
        try {

            FXMLLoader loader = App.getLoader(fxmlPath);
            Parent root = App.loadFXML(loader);
            getChildren().add(root);
            fcc = loader.getController();
            fcc.setFighterCard(this);
            return true;
        } catch (IOException e) {
            System.out.println("Error displaying fighter card");
            e.printStackTrace();
            return false;
        }
    }

    //get methods
    public Fighter getFighter() {return fighter;}
    public FighterCardController getController() {return fcc;}

    //set methods
    public void setFighter(Fighter fighter) {this.fighter = fighter; setLabels();}
    private void setLabels()
    {
        fcc.setTypesLabel(fighter.getTypesString());
        fcc.setNameLabel(fighter.getName());
        fcc.setBaseLabel(fighter.getBaseString());
        fcc.setStrengthLabel(fighter.getStrString());
        fcc.setWeaknessLabel(fighter.getWkString());
    }

    public void setNameButton(EventHandler<ActionEvent> event) {fcc.setNameButton(fighter.getName(), event);}
    public void resetNameLabel() {fcc.resetNameLabel(fighter.getName());}

    public void setResult(int result, int newBase, boolean appliedStrength, boolean appliedWeak)
    {
        fcc.setBaseLabel(Integer.toString(newBase));
        fcc.setNameColor(result);
        fcc.setStrengthColor(appliedStrength);
        fcc.setWeaknessColor(appliedWeak);
    }
    public void resetCard() 
    {
        fcc.setBaseLabel(fighter.getBaseString());
        fcc.resetCard();
    }


}
