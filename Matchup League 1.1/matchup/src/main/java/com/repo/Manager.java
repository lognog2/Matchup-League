package com.repo;

import org.hibernate.*;
import com.Schedule;
import com.Entities.*;
import com.javafx.App;
import com.javafx.MenuHandler;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Manager 
{
    private Session ses;
    private Repository repo;
    private int FPT = 7; //fighters per team
    private int FPG = 5; //fighters per game
    private int maxTPL = 10; //max teams per league
    private int minTPL = 6; //min teams per league

    public Manager()
    {
        repo = new Repository();
        ses = App.getSession();
    }

    //get methods
    public int getFPT() {return FPT;}
    public int getFPG() {return FPG;}
    public int maxTPL() {return maxTPL;}
    public int minTPL() {return minTPL;}
    public Session getSession() {return ses;}
    public Repository getRepo() {return repo;}

    //set methods
    public void setFPT(int FPT) {this.FPT = FPT;}
    public void setFPG(int FPG)
    {
        if (FPG > FPT)
            this.FPG = FPT;
        else
            this.FPG = FPG;
    }
    public void setMaxTPL(int maxTPL) {this.maxTPL = maxTPL;}
    public void setMinTPL(int minTPL) {this.maxTPL = minTPL;}

    /* Non-transactional methods (must be called in other transaction) */

    public boolean transferFighter(Fighter f, Team newTeam)
    {
        Team oldTeam = f.getTeam();
        return unassignFighter(f, oldTeam) && assignFighter(f, newTeam);
    }
    public boolean transferTeam(Team t, League newLeague)
    {
        League oldLeague = t.getLeague();
        return unassignTeam(t, oldLeague) && assignTeam(t, newLeague) >= 0;
    }
    public boolean transferTeam(Team t, Player newPlayer)
    {
        return unassignPlayer(t.getPlayer()) && assignPlayer(newPlayer, t);
    }
    public boolean transferPlayer(Player p, Team newTeam)
    {
        return unassignPlayer(p) && assignPlayer(p, newTeam);
    }

    public boolean replaceFighter(Fighter f, Team t)
    {
        try {
            if (t.getFighterListSize() < FPT)
                return assignFighter(f, t);
            for (Fighter current : t.getFighterList())
            {
                if (f.getBase() > current.getBase())
                    return unassignFighter(current, t) && assignFighter(f, t);
                else if (f.getBase() == current.getBase())
                {
                    if (f.modDiff() >= current.modDiff())
                        return unassignFighter(current, t) && assignFighter(f, t);
                }
            }
            //System.out.println(f.getName() + " was not added to " + t.getName());
            return false;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
    }
    public boolean assignFighter(Fighter f, Team t)
    {
        try {
            if (t.getFighterListSize() < FPT)
            {
                t.addFighter(f);
                return true;
            }
            else 
            {
                System.out.println("failed to add " + f.getName() + " to " + t.getName() + ": team is full");
                return false; 
            } 
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
        
    }
    public boolean unassignFighter(Fighter f, Team t)
    {
        try {
            if (t.getFighterListSize() > 0)
            {
                t.removeFighter(f);
                return true;
            }
            else 
            {
                System.out.println("failed to remove " + f.getName() + " from " + t.getName() + ": team is already empty");
                return false;
            }  
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
    public boolean unassignPlayer(Player p)
    {
        try {
            p.getTeam().removePlayer();
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
    }
    
    public int assignTeam(Team t, League lg)
    {
        try {
            if (lg.getTeamListSize() < maxTPL)
            {
                lg.addTeam(t);
                return 0;
            }
            else 
            {
                System.out.println("Failed to add " + t.getName() + " to " + lg.getName() + ": league is full");
                return 1;  
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        } 
    }
    public boolean unassignTeam(Team t, League lg)
    {
        try {
            if (lg.getTeamListSize() > 0)
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

    /* autogen methods */

    public boolean autogenTeams(int amt)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            League autoLg = new League(true);
            ses.persist(autoLg);
            for (int i = 0; i < amt; i++)
            {
                Team autoTm = new Team(false);
                Player autoP = new Player(true);
                autoTm = autoTm.autogen();
                assignPlayer(autoP, autoTm);
                if (assignTeam(autoTm, autoLg) != 0)
                {
                    autoLg = new League(true);
                    assignTeam(autoTm, autoLg);
                    ses.persist(autoLg);
                }
                ses.persist(autoP);
                ses.merge(autoTm);
            }
            autogenFighters((int)repo.extraFighters(FPT) * -1);
            ses.flush();
            tr.commit();
            tr = null;
            System.out.println(amt + " teams added");
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            System.out.println("\nno teams added");
            return false;
        }   
    }
    public boolean autogenFighters(int amt)
    {
        try {
            for (int i = 0; i < amt; i++)
            {
                Fighter auto = new Fighter(false);
                auto = auto.autogen();
                ses.persist(auto);
            };
            System.out.println("\n" + amt + " fighters added");
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            System.out.println("\nno fighters added");
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

    /* Transactional methods */
    
    public boolean addUser(String username, Team userTeam)
    {
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();
            if (!ses.contains(userTeam)) 
                userTeam = ses.merge(userTeam);

            Player user = new Player(username, 99);
            ses.persist(user);
            if (!transferTeam(userTeam, user))
            {
                System.out.println("Failed to add user " + username + " to " + userTeam.getName());
                tr.rollback();
                tr = null;
                return false;
            }
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

    public boolean removeAllUsers()
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            List<Player> users = repo.getAllUsers();
            for (Player p : users)
            {
                Team t = p.getTeam();
                unassignPlayer(p);
                Player newPlayer = new Player(true);
                assignPlayer(newPlayer, t);
                ses.persist(newPlayer);
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

    public boolean batchAssign()
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Map<String, League> leagueCache = new HashMap<>();
            for (Team t : repo.allTeams())
            {
                String location = t.getLocation();
                League target = leagueCache.get(location);

                if (target == null)
                {
                    target = repo.getLeague_byName(location);
                    if (target == null)
                    {
                        target = new League(location);
                        ses.persist(target);
                        //System.out.println("Created new league " + target.getName());
                    }
                    leagueCache.put(location, target);
                }
        
                String autoName = "player" + t.getTID();
                Player autoPlayer = new Player(autoName + " (CPU)");

                if (assignTeam(t, target) >= 0 && assignPlayer(autoPlayer, t))
                {
                    ses.merge(autoPlayer);
                    ses.merge(t);
                    ses.merge(target);
                }
                else
                {
                    tr.rollback();
                    tr = null;
                    System.out.println("failure in batchAssign");
                    return false;
                }
                
            }
            tr.commit();
            tr = null;
            //System.out.println("all teams added successfully");
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
            Team bye = repo.getTeam_byName("Bye");
            while (bye != null) {
                bye = repo.getTeam_byName("Bye");
                unassignPlayer(bye.getPlayer());
                unassignTeam(bye, bye.getLeague());
                repo.deleteAllByes();
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
    
    public boolean draftFighter(Fighter f, Team t)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (!replaceFighter(f, t))
            {
                tr.rollback();
                return false;
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

    public boolean generateSchedule(League lg)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Schedule sched = new Schedule(lg);
            sched.genSched_roundRobin(ses);
            //ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean simRound(int round) 
    {
        try {
            List<Game> games = repo.getGames_byRound(round);

            for (Game g : games)
            {
                Hibernate.initialize(g.getTeams());
                if (g.getResult().equals("Unfinished"))
                    playGame(g);
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    //utility methods
    private void playGame(Game g)
    {
        Team team1 = g.getTeam(0);
        Team team2 = g.getTeam(1);
        
        List<Fighter> teamList1 = new ArrayList<>(team1.getFighterListSize()); 
        List<Fighter> teamList2 = new ArrayList<>(team2.getFighterListSize());
        for (Fighter f : team1.getFighterList())
            teamList1.add(new Fighter(f));
        for (Fighter f : team2.getFighterList())
            teamList2.add(new Fighter(f));

        double score1 = 0.00, score2 = 0.00;
        String outcome = new String();
        for (double i = 0.00; i < FPG; i += 1.00)
        {
            int choice1 = team1.getPlayer().getChoice(teamList1);
            int choice2 = team2.getPlayer().getChoice(teamList2);
            Fighter f1 = teamList1.remove(choice1);
            Fighter f2 = teamList2.remove(choice2); 
            int result = MenuHandler.matchup(f1, f2);
            if (result == 1)
                score1 += 1.00 + (i / 100.00);
            else if (result == -1)
                score2 += 1.00 + (i / 100.0);
        }

        if (score1 - score2 > 0) {
            outcome = team1.getName();
        } else if (score2 - score1 > 0) {
            outcome = team2.getName();
        } else {
            outcome = "tie";
        }
        //System.out.printf(g.getGID() + ": %.2f-%.2f: " + outcome, score1, score2);
        //System.out.println();

        mergeGame(g, outcome, String.valueOf((int)score1), String.valueOf((int)score2));

    }

    public boolean mergeGame(Game g, String winner, String score1, String score2)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            
            g.setResult(winner);
            g.setScore(score1, score2);

            Team team1 = g.getTeam(0);
            Team team2 = g.getTeam(1);
            
            if (winner.equals(team1.getName())) {
                team1.addWin();
                team2.addLoss();
            }
            else if (winner.equals(team2.getName())) {
                team1.addLoss();
                team2.addWin();
            }

            ses.merge(team1);
            ses.merge(team2);
            ses.merge(g);
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

} //end class Manager