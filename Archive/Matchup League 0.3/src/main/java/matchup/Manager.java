package matchup;

import org.hibernate.*;
//import org.hibernate.cfg.Configuration;

import matchup.Entities.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class Manager 
{
    private Map<String, League> leagues;
    private SessionFactory sf;
    private final int FPT = 7; //fighters per team
    private final int TPL = 16; //teams per league

    public Manager(SessionFactory sf, List<League> lgs)
    {
        this.sf = sf;
        leagues = new HashMap<>();
        for (League lg : lgs) addLeague(lg);
    }

    //get methods
    public League getLeague(String name) {return leagues.get(name);}

    //set methods
    public void addLeague(League lg) {leagues.put(lg.name(), lg);}

    //utility methods
    public boolean exists(String name) {return leagues.containsKey(name);}

    public boolean assignFighter(Fighter f, Team t)
    {
        Session ses = sf.openSession();
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (t.size() < FPT)
            {
                t.addFighter(f);
                ses.merge(t);
                ses.merge(f);
                tr.commit();
                tr = null;
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            System.out.println("Error: " + e);
            return false;
        }
    }
    public boolean unassignFighter(Fighter f, Team t)
    {
        Session ses = sf.openSession();
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (t.size() > 0)
            {
                t.removeFighter(f);
                ses.merge(t);
                ses.merge(f);
                tr.commit();
                tr = null;
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            System.out.println("Error: " + e);
            return false;
        }
    }

    public boolean assignTeam(Team t, League lg)
    {
        Session ses = sf.openSession();
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (lg.size() < TPL)
            {
                lg.addTeam(t);
                ses.merge(t);
                ses.merge(lg);
                tr.commit();
                tr = null;
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            System.out.println("Error: " + e);
            return false;
        } 
    }
    public boolean unassignTeam(Team t, League lg)
    {
        Session ses = sf.openSession();
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (lg.size() > 0)
            {
                lg.removeTeam(t);
                ses.merge(t);
                ses.merge(lg);
                tr.commit();
                tr = null;
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            System.out.println("Error: " + e);
            return false;
        } 
    }
    
    public void transferFighter(Fighter f, Team newTeam)
    {
        Team oldTeam = f.team();
        unassignFighter(f, oldTeam);
        assignFighter(f, newTeam);
    }
    public void transferTeam(Team t, League newLeague)
    {
        League oldLeague = t.league();
        unassignTeam(t, oldLeague);
        assignTeam(t, newLeague);
    }

    public void batchAssign_byLocation (List<Team> teamList)
    {
        for (Team t : teamList)
        {
            if (exists(t.location()))
            {
                assignTeam(t, getLeague(t.location()));
            }
        }
    }

} //end class Manager
