package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.Entities.Team;
import com.javafx.App;

/**
 * A modification of Bracket Builder 1.0, see documentation for full breakdown.
 * Generates a bracket with traditional one-time seeding.
 * @since 1.1.1
 * @version 1.0
 */
public class Bracket {

    private List<Team> teams;
    private List<Team> bracket;
    private int teamsSize;
    private int fullBracketSize;

    public Bracket(List<Team> teams) {
        this.teams = teams;
        teamsSize = teams.size();
        fullBracketSize = nextFactorOfTwo(teamsSize);
        bracket = new ArrayList<>(fullBracketSize);
        fillBracket();
    }

    public int bracketSize() {return bracket.size();}
    public List<Team> getBracket() {return bracket;}
    public int getSeed(Team t) {return teams.indexOf(t);}
    public int getSeed(int index) {return teams.indexOf(bracket.get(index));}
    public Team getTeamBySeed(int seed) {return teams.get(seed);}
    public Team getTeam(int pos) {return bracket.get(pos);}
    public Team removeTeam(int pos) {return bracket.remove(pos);}
    public boolean removeTeam(Team t) {return bracket.remove(t);}

    public void fillBracket() {
        orderTeams();
        if (!bracket.isEmpty()) bracket.clear();
        bracket.add(teams.get(0));
        for (int n = 2; n <= fullBracketSize; n *= 2) {
            for (int i = 0; i < n/2; i++) {
                int seed = (n/2)+i;
                Team thisTeam = seed >= teamsSize ? App.getByeTeam() : teams.get(seed);
                Team opponentTeam = teams.get(n-1 - seed);
                bracket.add(bracket.indexOf(opponentTeam) + 1, thisTeam);
            }  
        }
    }

    /**
     * sorts by winning percentage, then wins, then fans
     */
    private void orderTeams() {
        Collections.sort(teams, 
            Comparator.comparing(Team::winPct, Comparator.reverseOrder())
            .thenComparing(Team::getWins, Comparator.reverseOrder())
            .thenComparing(Team::getFans, Comparator.reverseOrder()));
    }

    public int roundAmt() {
        return (int)(Math.log(fullBracketSize) / Math.log(2));
    }

    public void print() {
        for (int i = 0; i < bracket.size(); i++) {
            System.out.print(getTeam(i).getName());
            if (i % 2 == 0) System.out.print("/"); 
            else System.out.println(" ");
        }
    }

    private int nextFactorOfTwo(int num) {
        if (num < 1) return 0;
        int factor = 2;
        while (num > factor) {
            factor *= 2;
        }
        return factor;
    }
}
