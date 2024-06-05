package com.javafx.card;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class FighterCardController 
{
    private FighterCard fc;
    @FXML private Label nameLabel;
    @FXML private Label typesLabel;
    @FXML private Label baseLabel;
    @FXML private Label strengthLabel;
    @FXML private Label weaknessLabel;
    @FXML private VBox cardBox;
    private Button nameButton;
    

    @FXML
    private void initialize()
    {
        //System.out.println("fighter card controller initialized");
    }

    public FighterCard getFighterCard() {return fc;}
    public Button getNameButton() {return nameButton;}

    //set label text
    public void setNameLabel(String name) {nameLabel.setText(name);}
    public void setTypesLabel(String types) {typesLabel.setText(types);}
    public void setBaseLabel(String base) {baseLabel.setText(base);}
    public void setStrengthLabel(String strength) {strengthLabel.setText(strength);}
    public void setWeaknessLabel(String weakness) {weaknessLabel.setText(weakness);}

    //set label color
    public void setNameColor(int result) 
    {
        //0 = tie, -1 = loss, 1 = win, 2 = reset
        if (result == 2) {nameLabel.setTextFill(Color.BLACK); baseLabel.setTextFill(Color.BLACK);}
        else if (result == -1) {nameLabel.setTextFill(Color.RED); baseLabel.setTextFill(Color.RED);}
        else if (result == 1) {nameLabel.setTextFill(Color.GREEN); baseLabel.setTextFill(Color.GREEN);}
        else if (result == 0) {nameLabel.setTextFill(Color.BLUE); baseLabel.setTextFill(Color.BLUE);}
        else System.out.println("Invalid result value for FCC.setNameColor");
    }
    public void setStrengthColor(boolean applied) {strengthLabel.setTextFill(applied ? Color.GREEN : Color.BLACK);}
    public void setWeaknessColor(boolean applied) {weaknessLabel.setTextFill(applied ? Color.RED : Color.BLACK);}
    public void resetCard()
    {
        setNameColor(2);
        setStrengthColor(false);
        setWeaknessColor(false);
    }

    public void setFighterCard(FighterCard fc) {this.fc = fc;}

    public void setNameButton(String name, EventHandler<ActionEvent> event)
    {
        nameButton = new Button(name);
        int nameIndex = cardBox.getChildren().indexOf(nameLabel);
        cardBox.getChildren().set(nameIndex, nameButton);
        nameButton.setOnAction(event);
    }
    public void resetNameLabel(String name)
    {
        int nameIndex = cardBox.getChildren().indexOf(nameButton);
        cardBox.getChildren().set(nameIndex, nameLabel);
    }

}
