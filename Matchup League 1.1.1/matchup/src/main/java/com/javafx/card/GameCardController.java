package com.javafx.card;

import com.javafx.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Controller for Game Card. Methods should only be accessed through GameCard.
 * @since 1.1.1
 */
public class GameCardController {

    private GameCard gc;
    @FXML private VBox gameBox;
    @FXML private HBox teamBox1;
    @FXML private Label nameLabel1;
    @FXML private Label scoreLabel1;
    @FXML private HBox teamBox2;
    @FXML private Label nameLabel2;
    @FXML private Label scoreLabel2;
    private Label[] nameLabel;
    private Label[] scoreLabel;

    @FXML
    private void initialize() {
        nameLabel = new Label[]{nameLabel1, nameLabel2};
        scoreLabel = new Label[]{scoreLabel1, scoreLabel2};
    }

    //gets and sets
    protected GameCard getgameCard() {return gc;}
    protected void setCard(GameCard gc) {this.gc = gc;}

    protected void setNameLabels(String name1, String name2) {
        nameLabel[0].setText(name1);
        nameLabel[1].setText(name2);
    }

    protected void setScoreLabels(int score1, int score2) {
        scoreLabel[0].setText(String.valueOf(score1));
        scoreLabel[1].setText(String.valueOf(score2));
    }

    /**
     * Sets score labels to be blank
     */
    protected void setScoreLabels() {
        scoreLabel[0].setText("");
        scoreLabel[1].setText("");
    }

    public void setWinner(int index) {
        nameLabel[index].setTextFill(Color.GREEN);
        scoreLabel[index].setTextFill(Color.GREEN);
        index = App.flip(index);
        nameLabel[index].setTextFill(Color.RED);
        scoreLabel[index].setTextFill(Color.RED);
    }
}
