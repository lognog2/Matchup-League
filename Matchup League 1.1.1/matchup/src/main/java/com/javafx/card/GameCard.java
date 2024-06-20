package com.javafx.card;

import com.Entities.Game;
import com.Entities.Team;
import com.javafx.App;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class GameCard extends VBox {

    private final String fxmlPath = "card\\game_card";

    private Game game;
    private GameCardController gcc;

    //constructors
    public GameCard() {
        connect();
    }
    public GameCard(Game game) {
        if (connect()) {
            setGame(game);
            setLabels();
        }
    }
    public GameCard(Game game, int seed) {
        if (connect()) {
            setGame(game);
            setLabels(seed);
        } //else gcc.setNameLabel("Error");
    }
    public GameCard(Game game, int seed1, int seed2) {
        if (connect()) {
            setGame(game);
            setLabels(seed1, seed2);
        }
    }

    private boolean connect() {
        try {
            FXMLLoader loader = App.getLoader(fxmlPath);
            Parent root = App.loadFXML(loader);
            getChildren().add(root);
            gcc = loader.getController();
            gcc.setCard(this);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //gets and sets
    public Game getGame() {return game;}
    public GameCardController getController() {return gcc;}
    public Team getLoser() {return game.getLoser();}
    public void setGame(Game game) {this.game = game;}

    public void setWinner() {
        if (!game.isByeGame()) {
            gcc.setWinner(game.getWinnerIndex());
            gcc.setScoreLabels(game.getScore(0), game.getScore(1));
        }
    }
    
    private void setLabels() {
        gcc.setNameLabels(game.getTeamName(0), game.getTeamName(1));
        //gcc.setScoreLabels(0, 0);
    }
    private void setLabels(int seed) {
        gcc.setNameLabels(seed + " " + game.getTeamName(0), game.getTeamName(1));
        gcc.setScoreLabels();
    }
    private void setLabels(int seed1, int seed2) {
        gcc.setNameLabels(seed1 + " " + game.getTeamName(0), seed2 + " " + game.getTeamName(1));
        //gcc.setScoreLabels(0, 0);
    }
}

