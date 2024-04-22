package matchup;

import org.hibernate.*;

import matchup.Entities.*;

import java.util.Scanner;
import java.util.Set;
import java.util.List;

public class Manager 
{
    private Session ses;
    private int FPT = 7; //fighters per team
    private int maxTPL = 16; //max teams per league
    private int minTPL = 4; //min teams per league

    public Manager(Session ses)
    {
        this.ses = ses;
    }

    //get methods
    public int FPT() {return FPT;}
    public int maxTPL() {return maxTPL;}
    public int minTPL() {return minTPL;}
    public Session getSession() {return ses;}

    //set methods
    public void setFPT(int FPT) {this.FPT = FPT;}
    public void setMaxTPL(int maxTPL) {this.maxTPL = maxTPL;}
    public void setMinTPL(int minTPL) {this.maxTPL = minTPL;}

    /* Non-transaction methods (called in other transaction) */

    public boolean transferFighter(Fighter f, Team newTeam)
    {
        Team oldTeam = f.team();
        return unassignFighter(f, oldTeam) && assignFighter(f, newTeam);
    }
    public boolean transferTeam(Team t, League newLeague)
    {
        League oldLeague = t.league();
        return unassignTeam(t, oldLeague) && assignTeam(t, newLeague);
    }
    public boolean transferTeam(Team t, Player newPlayer)
    {
        Player oldPlayer  = t.player();
        return unassignPlayer(oldPlayer, t) && assignPlayer(newPlayer, t);
    }
    public boolean transferPlayer(Player p, Team newTeam)
    {
        Team oldTeam = p.team();
        return unassignPlayer(p, oldTeam) && assignPlayer(p, newTeam);
    }

    public boolean replaceFighter(Fighter f, Team t)
    {
        try {
            if (t.size() <= FPT)
                return assignFighter(f, t);
            for (Fighter current : t.fighterList())
            {
                if (f.base() > current.base())
                    return unassignFighter(current, t) && assignFighter(f, t);
                else if (f.base() == current.base())
                {
                    if (f.modDiff() >= current.modDiff())
                        return unassignFighter(current, t) && assignFighter(f, t);
                }
            }
            return false;
        } catch (RuntimeException e) {
            //if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        } 
    }
    public boolean assignFighter(Fighter f, Team t)
    {
        try {
            if (t.size() < FPT)
            {
                t.addFighter(f);
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
        
    }
    public boolean unassignFighter(Fighter f, Team t)
    {
        try {
            if (t.size() > 0)
            {
                t.removeFighter(f);;
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
        
    }
    
    public boolean assignPlayer(Player p, Team t)
    {
        try {
            t.addPlayer(p);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
    }
    public boolean unassignPlayer(Player p, Team t)
    {
        try {
            t.removePlayer();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
    }
    
    public boolean assignTeam(Team t, League lg)
    {
        try {
            if (lg.size() <= maxTPL)
            {
                lg.addTeam(t);
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
    }
    public boolean unassignTeam(Team t, League lg)
    {
        try {
            if (lg.size() > 0)
            {
                lg.removeTeam(t);
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {;
            e.printStackTrace();
            return false;
        } 
    }

    public boolean autogenFighters(int amt)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            for (int i = 0; i < amt; i++)
            {
                Fighter auto = new Fighter(false);
                auto = auto.autogen();
                ses.merge(auto);
            }
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean autogenPlayers(int amt)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            for (int i = 0; i < amt; i++)
            {
                Player auto = new Player(false);
                auto = auto.autogen();
                ses.merge(auto);
            }
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    /* Game functionalities */
    //teams should be ordered by fans, and fighterPool ordered by base

    public boolean addUser(String username, String userTeamName)
    {
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();

            Player user = new Player(username, 99);
            ses.persist(user);
            Team userTeam = Entities.getTeam_byName(userTeamName);
            if (!transferTeam(userTeam, user))
                return false;
            ses.merge(userTeam);
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeByes()
    {
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();
            Team bye = Entities.getTeam_byName("Bye");
            while (bye != null) {
                bye = Entities.getTeam_byName("Bye");
                unassignPlayer(bye.player(), bye);
                unassignTeam(bye, bye.league());
                Entities.deleteAllByes();
            }
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean draft(List<Team> teams, List<Fighter> fighterPool, Scanner in)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            boolean allTeamsFull = true;
            //outer loop continues until all teams are full
            do {
                allTeamsFull = true;
                //each team drafts 1 fighter each inner loop
                for (Team t : teams) 
                {
                    if (fighterPool.isEmpty()) 
                    {
                        System.out.println("Ran out of draftable fighters");
                        break;
                    } 
                    Fighter candidate = null;
                    if(t.player().style() == 99)
                    {
                        for (int i = 0; i < 3; i++)
                            fighterPool.get(i).print_basic();
                        
                        System.out.print("\n" + t.player().name() + ", your turn to choose!\nEnter FID: ");
                        int answer = in.nextInt();
                        candidate = Entities.getFighter_byID(answer);
                    }
                    else
                    {
                        candidate = fighterPool.get(0);
                    }
                    if (replaceFighter(candidate, t))
                    {
                        System.out.println(t.name() + " selects " + candidate.name());
                        fighterPool.remove(candidate);
                        ses.merge(t);
                        ses.merge(candidate);
                        allTeamsFull = false;
                    } 
                    else System.out.println(t.name() + " passes on their pick");    
                }
            } while (!allTeamsFull);
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean generateSchedule(League lg)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Schedule sched = new Schedule(lg);
            sched.genSched_roundRobin(ses);
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean playRound(int round, League lg) 
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            
            List<Game> games = Entities.getGames_inLeague_byRound(round, lg);

            for (Game g : games)
            {
                g.setResult(g.play());
                ses.merge(g);
            }
            ses.merge(lg);
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

} //end class Manager