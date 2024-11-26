package com;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import com.entities.*;
import com.menu.App;
//import com.javafx.load.Loader;
import com.util.Debug;

/**
 * Class that generates a round-robins schedule for a league.
 * @since 1.0
 * @version 1.01
 */
public class Schedule 
{
    /**
     * Associated league
     * @since 1.0
     */
    private League lg;
    /**
     * List of teams in schedule
     * @since 1.0
     */
    private List<Team> teams; 
    
    /**
     * Constructs a schedule with a league 
     * and a new team list that references league's team list
     * @param lg
     * @since 1.0
     */
    public Schedule(League lg)
    {
        this.lg = lg;
        this.teams = new ArrayList<>(lg.getTeamList());
    }
    //1.1.1
    public Schedule(List<Team> teams) {
        this.lg = null;
        this.teams = teams;
    }

    public List<Team> getTeams() {return teams;}

    /**
     * Generates a schedule where each team in the team list
     * plays each other once.
     * For each pairing between teams, a new game object is created.
     * <p>If there's an odd number of teams, a bye team is added.
     * Bye teams only exist as schedule placeholders.
     * @param ses Session to persist new games
     * @since 1.0
     * @version 1.01
     */
    public void genRoundRobin(Session ses) {
        final boolean odd = teams.size() % 2 != 0;
        if (odd) {
            Team byeTeam = new Team(App.getByeTeam(), lg.getName() + "Bye");
            //byeTeam.addPlayer(App.getByeTeam().getPlayer());
            ses.persist(byeTeam);
            lg.addTeam(byeTeam);
            ses.merge(lg);
            teams.add(byeTeam);
        }
        Collections.shuffle(teams);

        final int numTeams = teams.size();
        final int numRounds = lg.getGameAmt();
        for (Team t : teams)
            t.setNewSchedule(numRounds);

        for (int round = 0; round < numRounds; round++) {
            for (int match = 0; match < numTeams / 2; match++) {
                Team team1 = teams.get(match);
                Team team2 = teams.get(numTeams - 1 - match);
                Game g = new Game(round, team1, team2);
                Debug.write(g.getName());
                ses.persist(g);
                ses.merge(team1);
                ses.merge(team2);
            }
            teams.add(1, teams.remove(teams.size() - 1));
        }
    }
}