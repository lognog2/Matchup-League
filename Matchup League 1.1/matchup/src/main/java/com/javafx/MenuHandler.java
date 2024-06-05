package com.javafx;

import java.io.IOException;
import java.util.List;

import com.Entities.Fighter;
import com.Entities.League;
import com.Entities.Team;
import com.javafx.card.FighterCard;
import com.repo.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

public abstract class MenuHandler 
{
    protected static Repository menuRepo;
    protected static Manager menuManager;
    protected static List<League> leagueList;
    protected static Team userTeam;
    protected static League userLeague;
    protected static int round;

    public static void onStart()
    {
        menuRepo = new Repository();
        menuManager = new Manager();

        menuRepo.load_data();
        menuManager.batchAssign();

        leagueList = menuRepo.allLeagues();
        round = 0;
    }

    protected static void displayFighters(List<FighterCard> fcList, GridPane grid)
    {
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (FighterCard fc : fcList)
        {
            grid.addColumn(i++, fc);
        }
    }

    protected static void displayFighters(Team t, GridPane grid)
    {
        grid.getColumnConstraints().add(new ColumnConstraints(150));

        int i = 0;
        for (Fighter f : t.getFighterList())
        {
            FighterCard fc = new FighterCard(f);
            grid.addColumn(i++, fc);
        }
    }

    protected static void setUserTeam(Team t)
    {
        userTeam = t;
        userLeague = t.getLeague();
    }

    //1: fighter1 wins, -1: fighter2 wins, 0: tie
    public static int matchup(FighterCard fc1, FighterCard fc2)
    {
        Fighter f1 = fc1.getFighter();
        Fighter f2 = fc2.getFighter();

        int base1 = f1.getBase();
        int base2 = f2.getBase();
        boolean str1 = false, wk1 = false, str2 = false, wk2 = false;

        //f1 strength
        if (modApplies(f2.getTypes(), f1.getStrType(), f1.getStrVal())) {
            str1 = true;
            base1 += f1.getStrVal();
        }
        //f1 weakness
        if (modApplies(f2.getTypes(), f1.getWkType(), f1.getWkVal())) {
            wk1 = true;
            base1 -= f1.getWkVal();
        }
        //f2 strength
        if (modApplies(f1.getTypes(), f2.getStrType(), f2.getStrVal())) {
            str2 = true;
            base2 += f2.getStrVal();
        }
        //f2 weakness
        if (modApplies(f1.getTypes(), f2.getWkType(), f2.getWkVal())) {
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

    public static int matchup(Fighter f1, Fighter f2)
    {
        int base1 = f1.getBase();
        int base2 = f2.getBase();

        //f1 strength
        if (modApplies(f2.getTypes(), f1.getStrType(), f1.getStrVal())) {
            base1 += f1.getStrVal();
        }
        //f1 weakness
        if (modApplies(f2.getTypes(), f1.getWkType(), f1.getWkVal())) {
            base1 -= f1.getWkVal();
        }
        //f2 strength
        if (modApplies(f1.getTypes(), f2.getStrType(), f2.getStrVal())) {
            base2 += f2.getStrVal();
        }
        //f2 weakness
        if (modApplies(f1.getTypes(), f2.getWkType(), f2.getWkVal())) {
            base2 -= f2.getWkVal();
        }

        int result;
        if (base1 > base2) result = 1;
        else if (base1 < base2) result = -1;
        else result = 0;

        return result;
    }

    private static boolean modApplies(char[] types, char modType, int modVal)
    {
        for (char c : types) {
            if (c == modType)
                return true;
        }
        return false;
    }

    protected static boolean allLeaguesFinished()
    {
        boolean allDone = true;
        for (League lg : leagueList)
        {
            if (round < lg.getGameAmt())
                allDone = false;
        }
        return allDone;
    }

    protected void toMainMenu() throws IOException
    {
        menuManager.removeAllUsers();
        App.setRoot("main_menu");
    }
}
