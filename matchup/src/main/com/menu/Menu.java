package com.menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.Tournament;
import com.Entities.*;
import com.menu.card.FighterCard;
import com.menu.load.Loader;
import com.menu.load.Loader.Procedure;
import com.repo.*;
import javafx.concurrent.Task;
import javafx.scene.Parent;
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
    protected static Repository repo;
    protected static Manager manager;
    protected static List<League> leagueList;
    protected static Team userTeam;
    protected static League userLeague;
    protected static Tournament tourney;
    protected static Mode mode;
    protected static boolean debug;
    protected static int round;

    /**
     * @throws IOException 
     */
    public static void startLoad() throws IOException {
        write("Menu.startLoad");

        Loader loader = loadMenu();
        /* Load units:
        * fighter file size: 178
        * team file size: 48
        * batch assign: same as dataset size */
        double loadUnits = 178.0;
        loader.initProcedure(Procedure.LOAD_DATA, loadUnits);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                manager = getManager();
                repo = manager.getRepo();
                if (isWC()) loader.addLoadUnits(178.0);
                else loader.addLoadUnits(48.0 * 2.0); 
                DataEntity.onStart(debug);
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

    public static List<League> getLeagueList() {
        write("Menu.getLeagueList");
        return leagueList;
    }

    /* SET METHODS */

    /**
     * Sets user team
     * @param t
     * @since 1.1.0
     */
    protected static void setUserTeam(Team t) {
        write("Menu.setUserTeam", t.getName());
        userTeam = t;
        userLeague = t.getLeague();
    }

    protected static void setMode(Mode mode) {
        Menu.mode = mode;
    }

    protected static void setLeagueList() {
        leagueList = repo.allLeagues();
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
            loadUnits += (double)(getLeagueList().size() * manager.maxTPL());
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
     * Sets the color of a node's text.
     * @param node
     * @param color all lowercase color from colors.css sheet
     * @since 1.2.0
     */
    protected void setTextColor(Parent node, String color) {
        node.setStyle("-fx-text-fill: -"+color+";");
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
                write("Invalid mode");
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