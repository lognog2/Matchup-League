package com.menu.load;

import com.menu.App;
import com.util.Debug;
import javafx.application.Platform;

/**
 * Communication point between LoadScreen and background logic.
 * @since 1.1.2
 */
public class Loader {

    private double loadUnits = 0.0;
    protected String nextMenu;
    private LoadScreen loadScreen;
    private Procedure proc;

    public Loader(LoadScreen ls) {
        Debug.write("new Loader", ls);
        setLoadScreen(ls);
    }
    public Loader() {
        write("new Loader");
        //System.out.println("someone used blank loader constructor");
    }

    public String getNextMenu() {Debug.write("Loader.getNextMenu"); return nextMenu;}

    public void setLoadScreen(LoadScreen ls) {Debug.write("Loader.setLoadScreen", ls); this.loadScreen = ls;}
    
    /**
     * Initializes a loading procedure.
     * @since 1.1.2
     */
    public void initProcedure(Procedure proc, double loadUnits) {
        Debug.startThread(proc.toString());
        Debug.write("Loader.initProcedure", proc.toString(), loadUnits);
        this.proc = proc;
        this.loadUnits = loadUnits;
        setNextMenu(proc.getNextMenu());
        setMessage(proc.getInitMessage());
    }

    /**
     * Sets next menu to move to after loading ends.
     * @param menu
     * @since 1.1.2
     */
    public void setNextMenu(String menu) {Debug.write("Loader.setNextMenu", menu); this.nextMenu = menu;}

    /**
     * Sets loading message
     * @param message
     */
    public void setMessage(String message) {
        Debug.write("runLater: Loader.setMessage", message);
        Platform.runLater(() -> {
            //Debug.write("execute: Loader.setMessage", message);
            loadScreen.setLoadLabel(message);
        });
    }

    /**
     * Increases load progress by n load units.
     * @param n
     * @since 1.1.2
     */
    public void addProgress(int n) {
        //Debug.write("Loader.addProgress", n);
        double percent = ((double)n) / loadUnits;
        //System.out.println(percent);
        addProgress(percent);
    }

    /**
     * Increases load progress by 1 load unit.
     * @since 1.1.2
     */
    public void addProgress() {
        addProgress(1.0 / loadUnits);
    }
    /**
     * Increases load progress by a percentage of the total load units.
     * Does not appear on debug log, on account that it would take up hundreds of lines.
     * @param percent
     * @since 1.1.2
     */
    public void addProgress(double percent) {
        //Debug.write("runLater: Loader.addProgress", percent);
        Platform.runLater(() -> {
            loadScreen.addProgress(percent);
        });
    }

    /**
     * Adds new processes to the current loading screen.
     * Recalculates loading units so loading bar still ends at 100%.
     * <p>Resizes total load units by the formula N = L + (a/p)
     * <p>N = new total load units
     * <p>L = old total load units 
     * <p>a = number of new load units
     * <p>p = percent of progress bar remaining
     * @param amt number of load units to add
     * @return number of load units after added
     * @since 1.1.2
     * @version 2
     */
    public double addLoadUnits(double amt) {
        Debug.write("Loader.addLoadUnits", amt);
        double remProg = 1.0 - loadScreen.getProgress(); //remaining progress
        if (remProg > 0.0) {
            loadUnits += (amt / remProg);
            write("new LU: " + loadUnits);
        } else Debug.warn("no new load units added");
        return loadUnits;
    }

    /**
     * Sets app's loader to null and sets app root to the next menu.
     * @since 1.1.2
     */
    public void endLoad() {
        write("runLater: Loader.endLoad");
        Platform.runLater(() -> {
                double finalProgress = loadScreen.getProgress();
                write("Final load progress: " + finalProgress);
                App.verifyProgress(finalProgress);
                Debug.endThread(proc.toString());
                proc = null;
                App.setLoader(null);
                App.setRoot(nextMenu);
        });
    }

    /**
     * Debug write shortcut
     * @param line written to debug log
     * @since 1.1.2
     * @see Debug#write(Object)
     */
    public void write(String line) {
        Debug.write(line);
    }

    /**
     * Enum of procedures that can be done by the program.
     * Procedures have 2 terms: 
     * <p><code>nextMenu</code>: FXML name of the menu to display after loading
     * <p><code>initMessage</code>" first message to display on loading screen"
     * @since 1.1.2
     */
    public enum Procedure {
        /**
         * Sets up load data procedure.
         * @since 1.1.2
         */
        LOAD_DATA 
            ("team_select",
            "Connecting to database\n(This may take a moment...)"),
        PREDRAFT
            ("draft_menu",
            "Loading draft menu"),
        TOURNAMENT
            ("tournament_menu",
            "Loading tournament");

        private String nextMenu;
        private String initMessage;

        Procedure(String menu, String message) {
            this.nextMenu = menu;
            this.initMessage = message;
        }

        public String getNextMenu() {return nextMenu;}
        public String getInitMessage() {return initMessage;}
    } //end Procedure
} //end Loader


