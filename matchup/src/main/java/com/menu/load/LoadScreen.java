package com.menu.load;

import com.menu.App;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class LoadScreen extends Loader {

    private int benchmark = 0; //current loading percentage, rounded down to last benchmark interval
    private final int interval = 10; //interval at which benchmark is set
    @FXML private Label loadingLabel;
    @FXML private Label hintLabel;
    @FXML private ProgressBar pb;

    public LoadScreen(LoadScreen ls) {
        super(ls);
    }
    public LoadScreen() {
        //System.out.println("someone used blank loadscreen constructor");
    }

    @FXML
    public void initialize() {
        Loader loader = new Loader(this);
        App.setLoader(loader);
        hintLabel.setText(Hint.randomHint());
    }

    public double getProgress() {return pb.getProgress();}

    public void setLoadLabel(String message) {
        //System.out.println(message);
        loadingLabel.setText(message);
    }    

    public void addProgress(double update) {
        //System.out.print(update + " ");
        pb.setProgress(pb.getProgress() + update);
        int percent = ((int)(pb.getProgress()*100.0));
        if (percent % interval == 0 && percent > benchmark) {
            benchmark = percent;
            write("Load progress: " + percent + "%");
        }
    }
}

/**
 * Enum of all hints.
 * @since 1.1.2
 */
enum Hint {
    HINT0 ("If you finish first in your league,\nyou get to play in a tournament with all other league winners"),
    HINT1 ("Every CPU player has a strategy they stick to.\nFiguring out their strategy is the key to winning"),
    HINT2 ("The skill types (M, R, E) are the most common types.\nYou'll want fighters who are strong against these"),
    HINT3 ("Go to github.com/lognog2 to see more projects by me"),
    HINT4 ("The card game Matchup was created in 2015\nand used cards made of paper"),
    HINT5 ("Matchup Madness is a gigantic tournament\nincluding every single team in the database"),
    HINT6 ("World Cup puts fighters on teams\naccording to their nation, whether real or fiction"),
    HINT7 ("If there are not enough fighters to fill every team,\nnew fighters are created based on existing generic fighters"),
    HINT8 ("The very first league ever made was called Palette League.\nIt featured teams named after colors and ran for 28 seasons."),
    HINT99 ("The hint provider works for tips!");

    private final String hint;
    Hint (String hint) {
        this.hint = hint;
    }
    private static String getHint(int key) {
        return Hint.values()[key].hint;
    }
    public static String randomHint() {
        return getHint((int)(Math.random() * (Hint.values().length)));
    }
}
