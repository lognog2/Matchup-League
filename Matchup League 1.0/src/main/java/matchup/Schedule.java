/*
 * Scheduler v0.2
 * by Logan Nyquist
 * 
 * 0.2 (4/2024)
 * 0.1 (4/2024): Simulates a season using round robin format
 * League used: Florida
 */
package matchup;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;

import matchup.Entities.*;

public class Schedule 
{
    private String schedName; 
    private League lg;   
    
    public Schedule(League lg)
    {
        this.schedName = lg.name() + "Sched";
        this.lg = lg;
    }

    //schedule generators
    public void genSched_roundRobin(Session ses) 
    {
        List<Team> teams = lg.teamList();
        Collections.shuffle(teams);
        final boolean odd = teams.size() % 2 != 0;
        if (odd)
        {
            Team bye = new Team(lg);
            ses.persist(bye);
            ses.persist(bye.player());
        }
        final int numTeams = teams.size();
        final int numRounds = teams.size() - 1;
        for (Team t : teams)
            t.setSchedule(numRounds);

        for (int round = 0; round < numTeams - 1; round++) 
        {
            for (int match = 0; match < numTeams / 2; match++) 
            {
                Team team1 = teams.get(match);
                Team team2 = teams.get(numTeams - 1 - match);
                ses.merge(new Game(round, team1, team2));
            }
            teams.add(1, teams.remove(teams.size() - 1));
        }
    }
}