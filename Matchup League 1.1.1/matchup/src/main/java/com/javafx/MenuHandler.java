package com.javafx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.Tournament;
import com.Entities.Fighter;
import com.Entities.League;
import com.Entities.Team;
import com.javafx.card.FighterCard;
import com.repo.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * Abstract class that all menu controller classes extend.
 * Contains static variables and methods that carry across
 * different menus.
 * @since 1.1.0
 */
public abstract class MenuHandler 
{
    protected static Repository menuRepo;
    protected static Manager menuManager;
    protected static List<League> leagueList;
    protected static Team userTeam;
    protected static League userLeague;
    protected static Tournament finals;
    protected static int round;

    public static void onStart() {
        menuRepo = new Repository();
        menuManager = new Manager(menuRepo);

        menuRepo.load_data();
        menuManager.batchAssign();

        App.setByeTeam(menuManager.createByeTeam());

        leagueList = menuRepo.allLeagues();
        round = 0;
    }

    protected static void toMainMenu() throws IOException {
        
        menuManager.removeAllUsers();
        App.toMainMenu();
    }

    protected static void simRound() {
        menuManager.simRound(round++);
    }

    //display methods
    protected static void displayFighters(List<FighterCard> fcList, GridPane grid) {
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (FighterCard fc : fcList) {
            grid.addColumn(i++, fc);
        }
    }

    protected static void displayFighters(Team t, GridPane grid) {
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (Fighter f : t.getFighterList()) {
            FighterCard fc = new FighterCard(f);
            grid.addColumn(i++, fc);
        }
    }

    protected static void setUserTeam(Team t) {
        userTeam = t;
        userLeague = t.getLeague();
    }

    //1: fighter1 wins, -1: fighter2 wins, 0: tie
    public static int matchup(FighterCard fc1, FighterCard fc2) {
        Fighter f1 = fc1.getFighter();
        Fighter f2 = fc2.getFighter();

        int base1 = f1.getBase();
        int base2 = f2.getBase();
        boolean str1 = false, wk1 = false, str2 = false, wk2 = false;

        //f1 strength
        if (modApplies(f2.getTypes(), f1.getStrType())) {
            str1 = true;
            base1 += f1.getStrVal();
        }
        //f1 weakness
        if (modApplies(f2.getTypes(), f1.getWkType())) {
            wk1 = true;
            base1 -= f1.getWkVal();
        }
        //f2 strength
        if (modApplies(f1.getTypes(), f2.getStrType())) {
            str2 = true;
            base2 += f2.getStrVal();
        }
        //f2 weakness
        if (modApplies(f1.getTypes(), f2.getWkType())) {
            wk2 = true;
            base2 -= f2.getWkVal();
        }

        int result;
        if (base1 > base2) result = 1;
        else if (base1 < base2) result = -1;
        else result = 0;

        fc1.setResult(result, base1, str1, wk1);
        fc2.setResult(result * -1, base2, str2, wk2);

        return result;
    }

    /**
     * Carries out a matchup between two fighters.
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
     * Checks to see if a fighter's strength/weakness applies
     * according to opponent's types.
     * @param types Types of opposing fighter
     * @param modType Type to check for a match in types
     * @return true if there is a match, false if not
     * @since 0.1
     */
    private static boolean modApplies(char[] types, char modType) {
        for (char c : types) {
            if (c == modType)
                return true;
        }
        return false;
    }

    /**
     * @return true if all leagues have finished all their games,
     * false if not
     * @since 1.1.0
     */
    protected static boolean allLeaguesFinished() {
        boolean allDone = true;
        for (League lg : leagueList) {
            if (round < lg.getGameAmt())
                allDone = false;
        }
        return allDone;
    }

    /**
     * Gets the highest ranked team from each league and initializes a
     * tournament with them.
     * @since 1.1.1
     */
    protected static void startFinals() {
        List<Team> champions = new ArrayList<>(leagueList.size());
        for (League lg : leagueList) {
            champions.add(lg.bestTeam());
        }
        finals = new Tournament(champions);
    }

}
