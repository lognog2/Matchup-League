package com.menu.card;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controller for an instance of FighterCard.
 */
public class FighterCardController
{
    private FighterCard fc;
    @FXML private Label nameLabel;
    @FXML private Label typesLabel;
    @FXML private Label baseLabel;
    @FXML private Label strengthLabel;
    @FXML private Label weaknessLabel;
    @FXML private Label matchesLabel;
    @FXML private VBox cardBox;
    private Button nameButton;
    
    @FXML
    private void initialize()
    {
        //System.out.println("fighter card controller initialized");
    }

    protected FighterCard getFighterCard() {return fc;}
    protected Button getNameButton() {return nameButton;}

    //set label text
    protected void setNameLabel(String name) {nameLabel.setText(name);}
    protected void setTypesLabel(String types) {typesLabel.setText(types);}
    protected void setBaseLabel(String base) {baseLabel.setText(base);}
    protected void setStrengthLabel(String strength) {strengthLabel.setText(strength);}
    protected void setWeaknessLabel(String weakness) {weaknessLabel.setText(weakness);}
    protected void setMatchesLabel(int won, int played) {matchesLabel.setText(won + "/" + played);}

    protected void showMatches(boolean showMatches) {matchesLabel.setVisible(showMatches);}

    //set label color

    /**
     * Sets name label color.
     * @param result 0 = tie, -1 = loss, 1 = win, 2 = reset
     * @since 1.1.0
     */
    protected void setNameColor(int result) {
        if (result == 2) {nameLabel.setTextFill(Color.BLACK); baseLabel.setTextFill(Color.BLACK);}
        else if (result == -1) {nameLabel.setTextFill(Color.RED); baseLabel.setTextFill(Color.RED);}
        else if (result == 1) {nameLabel.setTextFill(Color.GREEN); baseLabel.setTextFill(Color.GREEN);}
        else if (result == 0) {nameLabel.setTextFill(Color.BLUE); baseLabel.setTextFill(Color.BLUE);}
        else System.out.println("Invalid result value for FCC.setNameColor");
    }
    /**
     * Sets strength label color.
     * @param applied green if true, black if false
     * @since 1.1.0
     */
    protected void setStrengthColor(boolean applied) {strengthLabel.setTextFill(applied ? Color.GREEN : Color.BLACK);}
    /**
     * Sets weakness label color.
     * @param applied red if true, black if false
     * @since 1.1.0
     */
    protected void setWeaknessColor(boolean applied) {weaknessLabel.setTextFill(applied ? Color.RED : Color.BLACK);}
    /**
     * Resets label colors to default color (black).
     * @since 1.1.0
     */
    public void resetCard()
    {
        setNameColor(2);
        setStrengthColor(false);
        setWeaknessColor(false);
    }

    protected void setFighterCard(FighterCard fc) {this.fc = fc;}

    protected void setNameButton(String name, EventHandler<ActionEvent> event)
    {
        nameButton = new Button(name);
        int nameIndex = cardBox.getChildren().indexOf(nameLabel);
        cardBox.getChildren().set(nameIndex, nameButton);
        nameButton.setOnAction(event);
    }
    protected void resetNameLabel(String name)
    {
        int nameIndex = cardBox.getChildren().indexOf(nameButton);
        cardBox.getChildren().set(nameIndex, nameLabel);
    }

}
