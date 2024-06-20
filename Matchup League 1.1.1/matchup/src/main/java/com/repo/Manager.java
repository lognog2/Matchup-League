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

/**
 * Handles the transfer, assigning, and unassigning of entites to each other,
 * as well as persist and merge entities after data has been loaded from {@code DAO}.
 * @since 0.3.1
 */
public class Manager 
{
    private Session ses;
    private Repository repo;
    /**
     * Fighters per team.
     * Default is 7.
     * @since 0.3
     */
    private int FPT = 7;
    /**
     * Fighters per game.
     * Default is 5.
     * @since 1.1.0
     */
    private int FPG = 5; //fighters per game
    /**
     * Maximum teams per league.
     * Default is 12.
     * @since 0.3
     */
    private int maxTPL = 12;
    /**
     * Minumum teams per league.
     * Default is 4.
     * @since 1.0
     */
    private int minTPL = 4; //min teams per league

    /**
     * Constructs a Manager object with a new repository and App's session.
     * @since 1.1.0
     */
    public Manager() {
        repo = new Repository();
        ses = App.getSession();
    }
    /**
     * Constructs a Manager object with an existing repository and
     * App's session.
     * @param repo repository
     * @since 1.1.1
     */
    public Manager(Repository repo) {
        this.repo = repo;
        ses = App.getSession();
    }
    /**
     * @deprecated
     * @param ses
     * @since 1.0
     */
    public Manager(Session ses) {
        repo = new Repository();
        this.ses = ses;
    }

    //get methods

    /**
     * Gets fighters per team.
     * @return FPT
     * @since 1.0
     */
    public int getFPT() {return FPT;}
    /**
     * Gets fighters per game.
     * @return FPG
     * @since 1.1.0
     */
    public int getFPG() {return FPG;}
    /**
     * Gets max teams per league.
     * @return maxTPL
     * @since 1.0
     */
    public int maxTPL() {return maxTPL;}
    /**
     * Gets min teams per league.
     * @return minTPL
     * @since 1.0
     */
    public int minTPL() {return minTPL;}
    /**
     * Gets session.
     * @return ses
     * @since 1.0
     */
    public Session getSession() {return ses;}
    /**
     * Gets repository.
     * @return repo
     * @since 1.1.0
     */
    public Repository getRepo() {return repo;}

    /**
     * Gets the number of fighters unused by a team
     * after a game.
     * @return FPT - FPG
     */
    public int extraFighters() {return FPT - FPG;}

    //set methods

    /**
     * Sets fighters per team.
     * @param FPT new FPT
     * @since 1.0
     */
    public void setFPT(int FPT) {this.FPT = FPT;}
    /**
     * Sets fighters per game. If result FPG is higher than
     * FPT, it reverts to the value of FPT.
     * @param FPG
     * @since 1.1.0
     */
    public void setFPG(int FPG) {
        if (FPG > FPT)
            this.FPG = FPT;
        else
            this.FPG = FPG;
    }
    /**
     * Sets max teams per league.
     * @param maxTPL new maxTPL
     * @since 1.0
     */
    public void setMaxTPL(int maxTPL) {this.maxTPL = maxTPL;}
    /** 
     * Sets min teams per league
     * @param minTPL new minTPL
     * @since 1.0
     */
    public void setMinTPL(int minTPL) {this.maxTPL = minTPL;}

    /* Non-transactional methods (must be called in other transaction) */

    /**
     * Removes a fighter from its team and adds it to a new team.
     * @param f fighter
     * @param newTeam new team
     * @return true if successful, false if not
     * @since 0.3
     * @version 2
     */
    public boolean transferFighter(Fighter f, Team newTeam) {
        Team oldTeam = f.getTeam();
        return unassignFighter(f, oldTeam) && assignFighter(f, newTeam);
    }
    /**
     * Removes a team from its league and adds it to a new league.
     * @param t team
     * @param newLeague new league
     * @return true if successful, false if not
     * @since 0.3
     * @version 2
     */
    public boolean transferTeam(Team t, League newLeague) {
        League oldLeague = t.getLeague();
        return unassignTeam(t, oldLeague) && assignTeam(t, newLeague) >= 0;
    }
    /**
     * Removes a player from its team and adds a new player to the team.
     * @param t team
     * @param newPlayer new player
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean transferTeam(Team t, Player newPlayer) {
        return unassignPlayer(t.getPlayer()) && assignPlayer(newPlayer, t);
    }
    /**
     * Removes a player from its team and adds it to a new team.
     * @param p player
     * @param newTeam new tram
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean transferPlayer(Player p, Team newTeam) {
        return unassignPlayer(p) && assignPlayer(p, newTeam);
    }

    /**
     * Adds a fighter to a team, and removes the fighter with the worst
     * base from the team if full.
     * @param f new fighter
     * @param t team
     * @return true if new fighter was added, false if not
     * @since 1.0
     */
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
    /**
     * Assigns a fighter to a team.
     * @param f fighter
     * @param t team
     * @return true if successful, false if not
     * @since 0.3
     */
    public boolean assignFighter(Fighter f, Team t) {
        try {
            if (t.getFighterListSize() < FPT) {
                t.addFighter(f);
                return true;
            }
            else {
                System.out.println("failed to add " + f.getName() + " to " + t.getName() + ": team is full");
                return false; 
            } 
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } 
        
    }
    /**
     * Removes a fighter from a team.
     * Fighter still exists after removal.
     * @param f fighter
     * @param t team
     * @return true if successful, false if not
     * @since 0.3
     */
    public boolean unassignFighter(Fighter f, Team t) {
        try {
            if (t.getFighterListSize() > 0) {
                t.removeFighter(f);
                return true;
            }
            else {
                System.out.println("failed to remove " + f.getName() + " from " + t.getName() + ": team is already empty");
                return false;
            }  
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }  
    }
    
    /**
     * Assigns a player to a team.
     * @param p player
     * @param t team
     * @return true if successful, false if not
     * @since 1.0
     */
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
    /**
     * Removes a player from a team.
     * @param p player
     * @return true if successful, false if not
     * @since 1.0
     */
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
    
    /**
     * Assigns a team to a league.
     * @param t team
     * @param lg league
     * @return true if successful, false if not
     * @since 0.3
     */
    public int assignTeam(Team t, League lg) {
        try {
            if (lg.getTeamListSize() < maxTPL) {
                lg.addTeam(t);
                return 0;
            }
            else {
                System.out.println("Failed to add " + t.getName() + " to " + lg.getName() + ": league is full");
                return 1;  
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return -1;
        } 
    }
    /**
     * Removes a team from a league.
     * Team still exists after removal
     * @param t team
     * @param lg league
     * @return true if successful, false if not
     * @since 0.3
     */
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

    /**
     * Auto generates specified amount of teams, 
     * a player for each team, 
     * enough leagues to hold the teams,
     * and enough fighters to fill those teams.
     * @param amt Amount of teams to autogen
     * @return true if at least one team was generated, false if none were
     * @since 1.0
     */
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
    /**
     * Auto generates specified amount of fighters.
     * @param amt Amount of fighters to autogen
     * @return true if at least one fighters was generated, false if none were
     * @since 1.0
     */
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
    /**
     * Auto generates specified amount of players.
     * @param amt Amount of players to autogen
     * @return true if at least one player was generated, false if none were
     * @since 1.0
     */
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

    /**
     * Persists an object to the database.
     * @param o
     * @return true if successful, false if not
     * @since 1.1.1
     */
    public boolean persist(Object o) {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            ses.persist(o);
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

    /**
     * Creates a user player based on name and team specified by the user.
     * @param username Name entered by the user
     * @param userTeam Team the user has selected
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean addUser(String username, Team userTeam) {
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();
            if (!ses.contains(userTeam)) 
                userTeam = ses.merge(userTeam);

            Player user = new Player(username, 99);
            ses.persist(user);
            if (!transferTeam(userTeam, user)) {
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

    /**
     * Unassigns all user players from their teams and assigns a new CPU
     * player.
     * @return true if all removals successful, false if not
     * @since 1.1.0
     */
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

    /**
     * Adds all teams to leagues that match their location.
     * If no league exists with a team's location, a new league is created
     * @return true if successful, false if not
     * @since 0.3.1
     * @version 2
     */
    public boolean batchAssign() {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Map<String, League> leagueCache = new HashMap<>();
            for (Team t : repo.allTeams()) {
                String location = t.getLocation();
                League target = leagueCache.get(location);

                if (target == null) {
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

                if (assignTeam(t, target) >= 0 && assignPlayer(autoPlayer, t)) {
                    ses.merge(autoPlayer);
                    ses.merge(t);
                    ses.merge(target);
                } else {
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

    /**
     * Unassigns all bye teams and players from their league.
     * @return true if successful, false if not
     * @since 1.0
     */
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
    
    /**
     * Calls adds a fighter to a team. If team is full, the fighter
     * with the lowet base is unassigned from the team.
     * @return true if fighter added, false if not
     * @since 1.1.0
     */
    public boolean addFighter(Fighter f, Team t) {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (!replaceFighter(f, t)) {
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

    /**
     * Generates a round-robin schedule for specified league.
     * @param lg league
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean generateSchedule(League lg)
    {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Schedule sched = new Schedule(lg);
            sched.genRoundRobin(ses);
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean simRound(int round) {
        try {
            List<Game> games = repo.getGames_byRound(round);

            for (Game g : games) {
                //System.out.println(g.getGameString());
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

    /**
     * Plays out a game between 2 teams and merges the
     * result.
     * @param g The game to be played
     * @since 0.1
     * @version 5
     */
    private void playGame(Game g)
    {
        Team team1 = g.getTeam(0);
        Team team2 = g.getTeam(1);
        if (g.isByeGame()) {
            String winner = team1.isBye() ? team2.getName() : team1.getName();
            mergeGame(g, winner, 0, 0);
            return;
        }
        try {
            List<Fighter> teamList1 = new ArrayList<>(team1.getFighterListSize()); 
            List<Fighter> teamList2 = new ArrayList<>(team2.getFighterListSize());
            for (Fighter f : team1.getFighterList())
                teamList1.add(new Fighter(f, true));
            for (Fighter f : team2.getFighterList())
                teamList2.add(new Fighter(f, true));

            double score1 = 0.0, score2 = 0.0;
            for (int i = 0; i < FPG; i += 1)
            {
                int choice1 = team1.getPlayer().getChoice(teamList1);
                int choice2 = team2.getPlayer().getChoice(teamList2);
                Fighter f1 = teamList1.remove(choice1);
                Fighter f2 = teamList2.remove(choice2); 
                int result = MenuHandler.matchup(f1, f2);
                if (result == 1)
                    score1 += matchPoints(i);
                else if (result == -1)
                    score2 += matchPoints(i);
            }

            //team1 wins tie
            String outcome = (score1 - score2 < 0) ? 
                team2.getName() : team1.getName();
      
            //System.out.printf(g.getGID() + ": %.2f-%.2f: " + outcome, score1, score2);
            //System.out.println();

            mergeGame(g, outcome, (int)score1, (int)score2);
        } catch (RuntimeException e) {
            System.out.println(g.getGameString());
            e.printStackTrace();
        }
    }

    /**
     * Calculates the number of points awarded for
     * winning a match.
     * Each round is worth 1 + x, where x is 0.1 in the final round
     * and is 10 times less each round earlier.
     * <p> Example with 3 fpt: 1.001, 1.01, 1.1
     * @param matchRound match round
     * @return points won this match round
     * @since 1.1.1
     */
    public double matchPoints(int matchRound) {
        return 1.0 + (1.0 / (10.0 * (FPT - matchRound)));
    }

    /**
     * After a game is completed, updates its score and result
     * in the database.
     * @param g game
     * @param winner name of the winning team
     * @param score1 score of team1
     * @param score2 score of team2
     * @return true if successful, false if not
     * @since 1.1.0
     * @version 2
     */
    public boolean mergeGame(Game g, String winner, int score1, int score2) {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            
            g.setResult(winner);
            g.setScore(score1, score2);

            Team team1 = g.getTeam(0);
            Team team2 = g.getTeam(1);
            
            if (winner.equals(team1.getName()) && !team2.isBye()) {
                team1.addWin();
                team2.addLoss();
            }
            else if (winner.equals(team2.getName()) && !team1.isBye()) {
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
    
    /**
     * Generates specifiec amount of generic fighters.
     * A fighter is generic if its rarity is above zero, meaning
     * it can be replicated.
     * <p>Each repetition, a fighter is chosen from the list of generics
     * using a 'raffle' system. A fighter's rarity is how many entries
     * they have in the raffle.
     * @param amt
     * @return true if all replications successful, false if not
     * @since 1.1.1
     */
    public boolean generateGeneric(long amt) {
        List<Fighter> generics = repo.getGenericFighters();
        int totalRarity = repo.getTotalRarity();
        if (generics.isEmpty() || totalRarity < 1)
            return false;
        for (int i = 0; i < amt; i++) {
            int index = (int)(Math.random() * totalRarity) + 1;
            int rarityPassed = 0;
            for (Fighter f : generics) {
                rarityPassed += f.getRarity();
                if (rarityPassed >= index) {
                    if (persist(new Fighter(f, false)))
                        break;
                    else
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates and persists a new bye team and player.
     * @return the bye team created.
     * @since 1.1.1
     */
    public Team createByeTeam() {
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            Team byeTeam = new Team(-1);
            Player byePlayer = new Player(false);
            byeTeam.addPlayer(byePlayer);
            ses.persist(byeTeam);
            ses.persist(byePlayer);
            ses.flush();
            tr.commit();
            tr = null;
            return byeTeam;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
            return null;
        }
    }


} //end class Manager