package com.menu;

import java.util.ArrayList;
import java.util.List;
import com.Tournament;
import com.entities.*;
import com.menu.card.FighterCard;
import com.menu.load.Loader;
import com.menu.load.Loader.Procedure;
import com.util.Debug;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * Abstract class that all menu controller classes extend.
 * Contains static variables and methods that carry across
 * different menus.
 * @since 1.1.0
 */
public abstract class Menu extends App
{
    protected static List<League> leagueList;
    protected static List<Team> teamList;
    protected static Team userTeam;
    protected static League userLeague;
    protected static Tournament tourney;
    protected static Mode mode;
    protected static boolean debug;
    protected static int round;

    /**
     * Starts load data procedure.
     * @since 1.1.2
     * @see Loader
     * @see Procedure
     */
    public static void startLoadData() {
        write("Menu.startLoadData");
        //TODO: persist and assign simultaneously
        loadMenu();
        /* Load units:
        * fighter file size * 2
        * total rarity * 2
        * (The * 2 is once for loading fighter and once for assigning fighter)
        */
        App.addFile("Fighters", "f_sample.csv");
        if (!isWC()) {
            String teamFile = (debug) ? "t_test.csv" : "t_sample.csv";
            App.addFile("Teams", teamFile);
        }
        double loadUnits = App.getLineCount(false);
        loader.initProcedure(Procedure.LOAD_DATA, loadUnits);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                write("start load data procedure");
                App.initSession();
                if (isWC()) loader.addLoadUnits(loadUnits);
                else loader.addLoadUnits(48.0); 
                manager.load_data(debug);
                setLeagueList();
                loader.setMessage("Ready to go!");
                loader.endLoad();
                round = 0;
                setByeTeam(manager.createByeTeam());
                return null;
            }
        };
        new Thread(task).start();
    }

    /* GET METHODS */

    public static Mode getMode() {
        return mode;
    }

    public static boolean isWC() {
        return mode == Mode.WORLDCUP;
    }

    /**
     * 
     * @return {@link #teamList}
     * @since 1.2.0
     */
    public static List<Team> getTeamList() {
        write("Menu.getTeamList");
        return teamList;
    }

    /* SET METHODS */

    /**
     * Sets user team
     * @param t team to set as user team
     * @since 1.1.0
     */
    protected static void setUserTeam(Team t) {
        write("Menu.setUserTeam", t.getName());
        userTeam = t;
        userLeague = t.getLeague();
    }

    /**
     * Sets {@link #mode}
     * @param mode
     */
    protected static void setMode(Mode mode) {
        Menu.mode = mode;
    }

    /**
     * Updates list of all leagues
     * @see Repository#allLeagues
     * @since 1.0
     */
    protected static void setLeagueList() {
        leagueList = repo.allLeagues();
    }

    /**
     * Updates list of all leagues
     * @see Repository#allTeams
     * @since 1.2.0
     */
    protected static void setTeamList() {
        teamList = repo.allTeams();
    }

    /**
     * Removes all user players from their teams and returns to the main menu.
     * @since 1.1.0
     */
    protected static void toMainMenu() {
        manager.removeAllUsers();
        App.toMainMenu();
    }

    protected static void simRound() {
        manager.simRound(round++);
    }

    //display methods

    /**
     * Display a single row of fighters from a list of FighterCards.
     * @param fcList list of fighter cards 
     * @param grid grid to display
     * @since 1.1.0
     */
    protected static void displayFighters(List<FighterCard> fcList, GridPane grid) {
        write("Menu.displayFighters", fcList, grid);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (FighterCard fc : fcList) {
            grid.addColumn(i++, fc);
            //System.out.println("add fc " + i);
        }
    }

    /**
     * Display a single row of fighters from a team.
     * @param t Team to display fighters of
     * @param grid Grid to display
     * @since 1.1.0
     */
    protected static void displayFighters(Team t, GridPane grid) {
        write("Menu.displayFighters", t.getName(), grid);
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (Fighter f : t.getFighterList()) {
            FighterCard fc = new FighterCard(f, true);
            grid.addColumn(i++, fc);
        }
    }

    /**
     * Carries out a matchup between two fighters.
     * Merges result to database.
     * @param f1 fighter from team1
     * @param f2 fighter from team2
     * @return 1 if f1 wins, -1 if f2 wins, or 0 if a tie
     * @version 3
     * @since 0.1
     */
    public static int matchup(Fighter f1, Fighter f2) {
        int base1 = f1.getBase();
        int base2 = f2.getBase();

        //f1 strength
        if (modApplies(f2.getTypes(), f1.getStrType())) {
            base1 += f1.getStrVal();
        }
        //f1 weakness
        if (modApplies(f2.getTypes(), f1.getWkType())) {
            base1 -= f1.getWkVal();
        }
        //f2 strength
        if (modApplies(f1.getTypes(), f2.getStrType())) {
            base2 += f2.getStrVal();
        }
        //f2 weakness
        if (modApplies(f1.getTypes(), f2.getWkType())) {
            base2 -= f2.getWkVal();
        }

        int result;
        if (base1 > base2) result = 1;
        else if (base1 < base2) result = -1;
        else result = 0;
        return result;
    }

    /**
     * Checks to see if a fighter's strength/weakness applies according to opponent's types.
     * @param types Types of opposing fighter
     * @param modType Type to check for a match in types
     * @return true if there is a match, false if not
     * @since 0.1
     */
    protected static boolean modApplies(char[] types, char modType) {
        for (char c : types) {
            if (c == modType)
                return true;
        }
        return false;
    }

    /**
     * @return true if all leagues have finished all their games, false if not
     * @since 1.1.0
     */
    protected static boolean allLeaguesFinished() {
        write("Menu.allLeaguesFinished");
        boolean allDone = true;
        for (League lg : leagueList) {
            if (round < lg.getGameAmt())
                allDone = false;
        }
        return allDone;
    }

    protected static void generateSchedule() {
        write("Menu.generateSchedule");
        Loader loader = loadMenu();
        double loadUnits = 0.0;
        //goes straight to tournament if teams are already drafed
        if ((mode == Mode.FINALS || isWC())) {
            loader.initProcedure(Procedure.TOURNAMENT, loadUnits);
        }
        //otherwise generates games and goes to draft menu
        else {
            loadUnits += (double)(leagueList.size() * manager.maxTPL());
            loader.initProcedure(Procedure.PREDRAFT, loadUnits);
        }
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (mode == Mode.SEASON) {
                    generateSeason();
                } else {
                    startTourney();
                }
                loader.setMessage("Ready to go!");
                loader.endLoad();
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * Generates a season for each league, where each team plays every other team once.
     * @since 1.1.2
     */
    private static void generateSeason() {
        Loader loader = App.getLoader();
        write("Menu.generateSeason");
        loader.setMessage("Generating schedule");
        int tpl = manager.maxTPL();
        for (League lg : leagueList) {
            manager.generateSchedule(lg);
            loader.addProgress(tpl);
        }
    }

    /**
     * Gets the highest ranked team from each league and initializes a tournament with them.
     * @since 1.1.1
     */
    protected static void startTourney() {
        write("Menu.startTourney");
        List<Team> tourneyTeams = getTourneyTeams();
        round = 100;
        tourney = new Tournament(tourneyTeams);
    }

    /**
     * Clears style classes from a node and sets new class style(s).
     * @param node node to set style classes
     * @param styleClasses style classes to add
     * @since 1.2.0
     */
    protected void setStyleClass(Parent node, String... styleClasses) {
        node.getStyleClass().clear();
        for (String styleClass : styleClasses) {
            node.getStyleClass().add(styleClass);
        }
    }

    /**
     * Sets a style for a node.
     * @param node node t style
     * @param styles array of styles, each a verbatim css command
     * @since 1.2.0
     */
    protected void setStyle(Parent node, String... styles) {
        StringBuilder sb = new StringBuilder();
        for (String style : styles) {
            sb.append(style);
        }
        Platform.runLater(() -> {
            node.setStyle(sb.toString());
        });
    }

    /**
     * Sets both a text fill and background color.
     * @param node node to color
     * @param colors array of colors, if one color is provided the other will be white
     * @since 1.2.0
     * @see #CSS_textFill(String)
     * @see #CSS_background(String)
     */
    protected void setLogo(Labeled node, Team t) {
        node.setText(t.getName());
        String[] colors = t.getColors();
        setStyle(node, CSS_textFill(colors[0]), CSS_background(colors[1]));
    }

    /**
     * Sets a node's background to a 2-color gradient.
     * Extra colors will be ignored, and having only 1 color will set the top color to white.
     * @param node node to color
     * @param colors  array of colors, if one color is provided the other will be white
     * @since 1.2.0
     * @see #CSS_gradient(String...)
     */
    protected void setGradient(Parent node, String... colors) {
        if (colors.length == 1) {CSS_gradient("white", colors[0]);}
        setStyle(node, CSS_gradient(colors));
    }

    /**      
     * Gets the CSS command to set the text fill color.
     * @param node node to color
     * @param color all lowercase color from colors.css sheet
     * @return Verbatim string of css command to set text fill
     * @since 1.2.0
     */
    protected String CSS_textFill(String color) {
        return "-fx-text-fill: -"+color+";";
    }

    /**
     * Gets the CSS command to set the background color.
     * @param node node to color
     * @param color all lowercase color from colors.css sheet
     * @return Verbatim string of css command to set background
     * @since 1.2.0
     */
    protected String CSS_background(String color) {
        return "-fx-background-color: -"+color+";";
    }

    /**
     * Sets a background vertical gradient with two colors.
     * @param colors array of colors to use. First color goes on top and second goes on bottom. 
     * @return Verbatim string of css command to set gradient
     * @since 1.2.0
     */
    protected String CSS_gradient(String... colors) {
        return "-fx-background-color: linear-gradient(from 50% 0% to 50% 100%, -"+colors[0]+", -"+colors[1]+");";
    }

    /**
     * Gets list of teams to include in a tournament, depending on game mode
     * @return List of teams
     * @since 1.1.2
     */
    private static List<Team> getTourneyTeams() {
        write("Menu.getTourneyTeams");
        List<Team> teams = new ArrayList<>();
        switch (mode) {
            case FINALS: case SEASON:
                for (League lg : leagueList) {
                    teams.add(lg.bestTeam());
                } break;
            case LEAGUE:
                teams = userLeague.getTeamList();
            break;
            case MADNESS:
                teams = repo.allTeams();    
            break;
            //there should only be one league in a world cup
            case WORLDCUP:
                setLeagueList();
                teams = leagueList.get(0).getTeamList();
            break;
            default:
                Debug.warn(0, "Invalid mode: " + mode);
        }
        return teams;
    }

    protected enum Mode {
        SEASON,
        FINALS,
        LEAGUE,
        MADNESS,
        WORLDCUP;
    }
}