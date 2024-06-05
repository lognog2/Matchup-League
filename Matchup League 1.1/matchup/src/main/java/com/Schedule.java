package com;

import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import com.Entities.*;

public class Schedule 
{
    //private String schedName; 
    private League lg;   
    
    public Schedule(League lg)
    {
        //this.schedName = lg.getName() + " Schedule";
        this.lg = lg;
    }

    //schedule generators
    public void genSched_roundRobin(Session ses) 
    {
        
        final boolean odd = lg.getTeamListSize() % 2 != 0;
        if (odd) {
            Team bye = new Team(lg);
            ses.persist(bye);
        }
        List<Team> teams = lg.getTeamList();
        Collections.shuffle(teams);

        final int numTeams = teams.size();
        final int numRounds = lg.getGameAmt();
        for (Team t : teams)
            t.setSchedule(numRounds);

        for (int round = 0; round < numRounds; round++) 
        {
            for (int match = 0; match < numTeams / 2; match++) 
            {
                Team team1 = teams.get(match);
                Team team2 = teams.get(numTeams - 1 - match);
                Game g = new Game(round, team1, team2);
                //System.out.println(g.getTeamName(0) + " vs " + g.getTeamName(1));
                ses.persist(g);
            }
            teams.add(1, teams.remove(teams.size() - 1));
        }
    }
}