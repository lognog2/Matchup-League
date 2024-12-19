package com.repo;

import org.hibernate.*;
import com.Schedule;
import com.entities.*;
import com.menu.App;
import com.menu.Menu;
import com.menu.load.Loader;
import com.util.Debug;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
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
    private int FPT = 0;

    /**
     * Fighters per game.
     * Default is 5.
     * @since 1.1.0
     */
    private int FPG = 0;

    /**
     * Maximum teams per league.
     * Default is 16.
     * @since 0.3
     */
    private int maxTPL = 16;

    /**
     * Minumum teams per league.
     * Default is 4.
     * @since 1.0
     */
    private int minTPL = 4;

    /**
     * @deprecated
     * Constructs a Manager object with a new repository and App's session.
     * @since 1.1.0
     */
    public Manager() {
        Debug.write("new Manager");
        repo = new Repository();
        ses = App.getSession();
    }
    /**
     * @deprecated
     * Constructs a Manager object with an existing repository and App's session.
     * @param repo repository
     * @since 1.1.1
     */
    public Manager(Repository repo) {
        Debug.write("new Manager", repo);
        this.repo = repo;
        ses = App.getSession();
    }
    /**
     * Constructs a Manager object with a new repository and App's session.
     * @param ses session
     * @since 1.0
     */
    public Manager(Session ses) {
        Debug.write("new Manager", ses);
        repo = new Repository();
        this.ses = ses;
    }

    /* GET METHODS */

    /**
     * Gets fighters per team.
     * @return FPT
     * @since 1.0
     */
    public int getFPT() {Debug.write("Manager.getFPT"); return FPT;}

    /**
     * Gets fighters per game.
     * @return FPG
     * @since 1.1.0
     */
    public int getFPG() {Debug.write("Manager.getFPG"); return FPG;}

    /**
     * Gets max teams per league.
     * @return maxTPL
     * @since 1.0
     */
    public int maxTPL() {Debug.write("Manager.maxTPL"); return maxTPL;}

    /**
     * Gets min teams per league.
     * @return minTPL
     * @since 1.0
     */
    public int minTPL() {Debug.write("Manager.minTPL"); return minTPL;}

    /**
     * Gets session.
     * @return ses
     * @since 1.0
     */
    public Session getSession() {Debug.write("Manager.getSession"); return ses;}

    /**
     * Gets repository.
     * @return repo
     * @since 1.1.0
     */
    public Repository getRepo() {Debug.write("Manager.getRepo"); return repo;}

    /**
     * Gets the number of fighters unused by a team
     * after a game.
     * @return FPT - FPG
     */
    public int spareFighters() {Debug.write("Manager.spareFighters"); return FPT - FPG;}

    /**
     * Returns total number of each fighter in database as ints
     * @return total fighter count
     * @since 1.2.1
     * @see Repository#totalFighterCount()
     */
    public int getTotalFighters() {
        return (int)repo.totalFighterCount();
    }

    /**
     * Returns total number of each entity in database as ints
     * @return total fighter count
     * @since 1.2.1
     * @see Repository#totalFighterCount()
     */
    public int getTotalTeams() {
        return (int)repo.totalFighterCount();
    }


    /* SET METHODS */

    /**
     * Sets fighters per team.
     * @param FPT new FPT
     * @since 1.0
     */
    public void setFPT(int FPT) {Debug.write("Manager.setFPT", FPT); this.FPT = FPT;}

    /**
     * Sets fighters per game. If result FPG is higher than
     * FPT, it reverts to the value of FPT.
     * @param FPG
     * @since 1.1.0
     */
    public void setFPG(int FPG) {
        Debug.write("Manager.setFPG", FPG);
        this.FPG = (FPG > FPT) ? FPT : FPG;
    }

    /**
     * Sets FPG and FPT simultaneously.
     * @param fpg fighters per game
     * @param fpt fighters per team
     * @since 1.1.2
     */
    public void setTeamSize(int fpg, int fpt) {
        Debug.write("Manager.setTeamSize", fpg, fpt);
        setFPT(fpt);
        setFPG(fpg);
    }

    /**
     * Sets max teams per league.
     * The absolute most teams a league can have is 101, as any more would
     * mess with the tournament game generation.
     * @param maxTPL new maxTPL
     * @since 1.0
     */
    public void setMaxTPL(int maxTPL) {
        Debug.write("Manager.setMaxTPL", maxTPL);
        final int ABSOLUTE_MAX = 101;
        if (maxTPL <=  ABSOLUTE_MAX)
            this.maxTPL = (maxTPL < minTPL) ? minTPL : maxTPL;
        else
            this.maxTPL =  ABSOLUTE_MAX;
    }

    /** 
     * Sets min teams per league.
     * The absolute minimum a league can have 2, 
     * @param minTPL new minTPL
     * @since 1.0
     */
    public void setMinTPL(int minTPL) {
        Debug.write("Manager.setMinTPL", minTPL);
        final int ABSOLUTE_MIN = 2;
        if (minTPL >= ABSOLUTE_MIN)
            this.minTPL = (minTPL > maxTPL) ? maxTPL : minTPL;
        else 
            this.minTPL = ABSOLUTE_MIN;
    }

    public void setLeagueSize(int min, int max) {
        Debug.write("Manager.setLeagueSize", min, max);
        setMaxTPL(max);
        setMinTPL(min);
    }

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
        //Debug.write("Manager.transferFighter", f.getName(), newTeam.getName());
        return unassignFighter(f) && assignFighter(f, newTeam);
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
        //Debug.write("Manager.transferTeam", t.getName(), newLeague.getName());
        return unassignTeam(t) && assignTeam(t, newLeague) > 0;
    }

    /**
     * Removes a player from its team and adds a new player to the team.
     * @param t team
     * @param newPlayer new player
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean transferTeam(Team t, Player newPlayer) {
        //Debug.write("Manager.transferTeam", t.getName(), newPlayer.getName());
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
        //Debug.write("Manager.transferPlayer", p.getName(), newTeam.getName());
        return unassignPlayer(p) && assignPlayer(p, newTeam);
    }

    /**
     * Adds a fighter to a team, and removes the fighter with the worst
     * base (or in a tie, highter mod diff) from the team if full.
     * @param f new fighter
     * @param t team
     * @return true if new fighter was added, false if not
     * @since 1.0
     * @version 2
     */
    public boolean replaceFighter(Fighter f, Team t) {
        //Debug.write("Manager.replaceFighter", f.getID(), t.getID());
        try {
            //check if team is full
            if (t.getFighterListSize() < FPT)
                return assignFighter(f, t);

            //sort fighters on team by base, lowest to highest
            t.getFighterList().sort(Comparator.comparing(Fighter::getBase));
            Fighter worst = t.getFighter(0);

            // check if f is better than the worst fighter on team
            if (f.getBase() > worst.getBase() || (f.getBase() == worst.getBase() && f.modDiff() > worst.modDiff())) {
                return unassignFighter(worst) && assignFighter(f, t);
            }
            else {
                //Debug.warn(0, f.getID() + " was not added to " + t.getID()); 
                return false;
            }
        } catch (RuntimeException e) {
            Debug.error(-1, e);
            return false;
        } 
    }

    /**
     * Assigns a fighter to a team. This should only be used when you assume the team is not already full.
     * @param f fighter
     * @param t team
     * @return true if successful, false if not
     * @since 0.3
     */
    public boolean assignFighter(Fighter f, Team t) {
        //Debug.write("Manager.assignFighter", f.getName(), t.getName());
        try {
            if (t.getFighterListSize() < FPT) {
                t.addFighter(f);
                return true;
            } else {
                String message = "Failed to assign " + f.getID() + "to" + t.getID() + ": team is full";
                Debug.error(-5, new Exception(message));
                return false; 
            } 
        } catch (RuntimeException e) {
            Debug.error(-1, e);
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
    public boolean unassignFighter(Fighter f) {
        //Debug.write("Manager.unassignFighter", f.getName());
        try {
            Team t = f.getTeam();
            if (t.getFighterListSize() > 0) {
                t.removeFighter(f);
                return true;
            }
            else {
                String message = "failed to remove " + f.getName() + " from " + t.getName() + ": team is already empty";
                Debug.error(-5, new Exception(message));
                return false;
            }  
        } catch (RuntimeException e) {
            Debug.error(-1, e);
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
    public boolean assignPlayer(Player p, Team t) {
        //Debug.write("Manager.assignPlayer", p.getName(), t.getName());
        try {
            t.addPlayer(p);
            return true;
        } catch (RuntimeException e) {
            Debug.error(-1, e);
            return false;
        } 
    }

    /**
     * Removes a player from a team.
     * @param p player
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean unassignPlayer(Player p) {
        //Debug.write("Manager.unassignPlayer", p.getName());
        try {
            p.getTeam().removePlayer();
            return true;
        } catch (RuntimeException e) {
            Debug.error(-1, e);
            return false;
        } 
    }
    
    /**
     * Assigns a team to a league.
     * @param t team
     * @param lg league
     * @return {@link ExitCode} 1: Team was added, -1: Runtime error, -5: League was full
     * @since 0.3
     */
    public int assignTeam(Team t, League lg) {
        //Debug.write("Manager.assignTeam", t.getID(), lg.getID());
        try {
            if (lg.getTeamListSize() < maxTPL) {
                lg.addTeam(t);
                return 1; //success
            } else {
                String message = "Failed to add " + t.getID() + " to " + lg.getID() + ": league is full";
                Debug.error(-5, new Exception(message));
                return -5;  
            }
        } catch (RuntimeException e) {
            Debug.error(-1, e);
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
    public boolean unassignTeam(Team t) {
        //Debug.write("Manager.unassignTeam", t.getName());
        try {
            League lg = t.getLeague();
            if (lg.getTeamListSize() > 0) {
                lg.removeTeam(t);
                return true;
            }
            else return false;  
        } catch (RuntimeException e) {;
            Debug.error(-1, e);
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
    public boolean autogenTeams(int amt) {
        Debug.write("Manager.autogenTeams", amt);
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            League autoLg = new League(true);
            persist(autoLg);
            for (int i = 0; i < amt; i++) {
                Team autoTm = new Team(false);
                Player autoP = new Player(true);
                autoTm = autoTm.autogen();
                assignPlayer(autoP, autoTm);
                if (assignTeam(autoTm, autoLg) <= 0) {
                    autoLg = new League(true);
                    assignTeam(autoTm, autoLg);
                    persist(autoLg);
                }
                persist(autoP);
                ses.merge(autoTm);
            }
            autogenFighters((int)repo.extraFighters(FPT) * -1);
            ses.flush();
            tr.commit();
            tr = null;
            Debug.write(amt + " teams added");
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }   
    }

    /**
     * Auto generates specified amount of fighters.
     * @param amt Amount of fighters to autogen
     * @return true if at least one fighters was generated, false if none were
     * @since 1.0
     */
    public boolean autogenFighters(int amt) {
        Debug.write("Manager.autogenFighters", amt);
        try {
            for (int i = 0; i < amt; i++) {
                Fighter auto = new Fighter(false);
                auto = auto.autogen();
                persist(auto);
            };
            Debug.write("\n" + amt + " fighters added");
            return true;
        } catch (RuntimeException e) {
            Debug.error(-1, e);
            return false;
        }
    }

    /**
     * Auto generates specified amount of players.
     * @param amt Amount of players to autogen
     * @return true if at least one player was generated, false if none were
     * @since 1.0
     */
    public boolean autogenPlayers(int amt) {
        Debug.write("Manager.autogenPlayers", amt);
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
            Debug.error(-2, e);
            return false;
        }
    }

    /* Transactional methods */

    /**
     * Creates a user player based on name and team specified by the user.
     * @param username Name entered by the user
     * @param userTeam Team the user has selected
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean addUser(String username, Team userTeam) {
        Debug.write("Manager.addUser", username, userTeam.getID());
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();
            if (!ses.contains(userTeam)) 
                userTeam = ses.merge(userTeam);

            Player user = new Player(username, 99);
            persist(user);
            if (!transferTeam(userTeam, user)) {
                String message = "Failed to add user " + username + " to " + userTeam.getID();
                Debug.warn(0, message);
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
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Unassigns all user players from their teams and assigns a new CPU
     * player.
     * @return true if all removals successful, false if not
     * @since 1.1.0
     */
    public boolean removeAllUsers() {
        Debug.write("Manager.removeAllUsers");
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            List<Player> users = repo.getAllUsers();
            for (Player p : users) {
                Team t = p.getTeam();
                unassignPlayer(p);
                Player newPlayer = new Player(true);
                assignPlayer(newPlayer, t);
                persist(newPlayer);
            }
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Adds all teams to leagues that match their location.
     * If no league exists with a team's location, a new league is created
     * @return true if successful, false if not
     * @since 0.3
     * @version 2
     */
    public boolean load_data(boolean debug) {
        Debug.write("Manager.load_data", debug);
        Transaction tr = null;
        try {
            if (debug) {
                setTeamSize(3, 4);
            } else {
                setTeamSize(5, 7);
            } //Debug.write("FPG: " + FPG + " FPT: " + FPT);

            /* Load Data */
            boolean isWC = Menu.isWC();
            repo.load_data(debug, !isWC);

            tr = ses.beginTransaction();

            //replicate generic fighters and do batch assign
            //procedure depends on if mode is world cup or not
            if (isWC) {
                setLeagueSize(2, 101);
                if (!replicateGenericOnce() || !assignFighters_byNation() || !setWorldCupTeams()) {
                    tr.rollback();
                    tr = null;
                    return false;
                }
            } else {
                App.getLoader().addLoadUnits((double)requiredFighters());
                if (replicateGeneric() < 0 || !assignTeams_byLocation()) {
                    tr.rollback();
                    tr = null;
                    return false;
                }
            }

            tr.commit();
            tr = null;
            Debug.write(1, "All data loaded successfully");
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Assigns all teams to leagues based on their location
     * @return true if successful, false if not
     * @since 0.3
     * @version 2
     */
    private boolean assignTeams_byLocation() {
        Debug.write("Manager.assignTeams_byLocation");
        Loader loader = App.getLoader();
        loader.setMessage("Assigning teams to leagues");
        
        Map<String, League> leagueCache = new HashMap<>();
        for (Team t : repo.allTeams()) {
            String location = t.getLocation();
            League target = leagueCache.get(location);

            if (target == null) {
                target = repo.getLeague_byName(location);
                if (target == null) {
                    target = new League(location);
                    persist(target);
                    //Debug.write("Created new league " + target.getName());
                }
                leagueCache.put(location, target);
            }

            if (!autogen_mergeTeam(t, target)) return false;
            loader.addProgress();
        }
        return true;
    }

    /**
     * Assigns all fighters to teams based on their nation.
     * @return true if successful, false if not
     * @since 1.1.2
     */
    private boolean assignFighters_byNation() {
        Debug.write("Manager.assignFighters_byNation");
        Loader loader = App.getLoader();
        loader.setMessage("Assigning fighters to teams");

        Map<String, Team> teamCache = new HashMap<>();
        for (Fighter f : repo.allFighters()) {
            String nation = f.getNation();
            Team target = teamCache.get(nation);

            //create new team if nation's team doesn't exist
            if (target == null) {
                target = new Team(nation);
                persist(target);
                mergeTeam(target);
                teamCache.put(nation, target);
            } 
            if (replaceFighter(f, target)) {
                ses.merge(f);
                ses.merge(target);
            }
            loader.addProgress();
        }
        return true;
    }

    /**
     * Generates specific amount of generic fighters.
     * A fighter is generic if its rarity is above zero, meaning
     * it can be replicated.
     * <p>Each repetition, a fighter is chosen from the list of generics
     * using a 'raffle' system. A fighter's rarity is how many entries
     * they have in the raffle.
     * @param amt
     * @return 1: Successfully replicated fighters 0: No fighters needed to be replicated;
     * -1: Runtime error; -2: Failed to persist fighter; -3: No generic fighters to replicate
     * @since 1.1.1
     * @version 2
     */
    public int replicateGeneric() {
        Debug.write("Manager.replicateGeneric");

        long amt = extraFighters();
        if (amt >= FPT) {
            Debug.write("0", "No replication needed, with " + amt + " to spare");
            return 0;
        }

        // If extra fighters are needed, generates just enough to fill each team plus one extra team.
        Loader loader = App.getLoader();
        try {
            //tr = ses.beginTransaction();
            int reqFighters = ((int)amt * -1) + FPT;
            loader.setMessage("Generating fighters");
            Debug.write("Generating " + reqFighters + " fighters");
            List<Fighter> generics = repo.getGenericFighters();
            int totalRarity = repo.getTotalRarity();
            if (generics.isEmpty() || totalRarity < 1) {
                loader.setMessage("(-3)An error occured generating fighters");
                Debug.error(-3, "No generic fighters to replicate");
                return -3;
            }
            for (int i = 0; i < reqFighters; i++) {
                int index = (int)(Math.random() * totalRarity) + 1;
                int rarityPassed = 0;
                for (Fighter f : generics) {
                    rarityPassed += f.getRarity();
                    if (rarityPassed >= index) {
                        if (persist(new Fighter(f, false)))
                            break;
                        else {
                            loader.setMessage("(-2)An error occured generating fighters");
                            Debug.error(-2, "An error occured persisting " + f.getID());
                            return -2;
                        }
                    }
                }
                loader.addProgress();
                if (i % 50 == 0) ses.flush();
            }
            //tr.commit();
            //tr = null;
            return 1;
        } catch (RuntimeException e) {
            if (loader != null) loader.setMessage("(-1)An error occured generating fighters");
            //if (tr != null) tr.rollback();
            Debug.error(-1, e);
            return -1;
        }
    }

    /**
     * Replicates generic fighters the exact amount of times as their rarity.
     * @return true if successful, false, if not
     * @since 1.1.2
     */
    public boolean replicateGenericOnce() {
        Debug.write("Manager.replicateGenericOnce");
        Loader loader = App.getLoader();
        try {
            for (Fighter f : repo.getGenericFighters()) {
                for (int i = 0; i < f.getRarity(); i++) {
                    persist(new Fighter(f, false));
                    loader.addProgress();
                }
            }
            return true;
        }
        catch (RuntimeException e) {
            if (loader != null) loader.setMessage("(-1)An error occured generating fighters");
            Debug.error(-1, e);
            return false;
        }
    }

    /**
     * @return number of fighters left over after every team has been filled
     * @since 1.0
     * @see Repository#extraFighters(int)
     */
    public long extraFighters() {
        Debug.write("Manager.extraFighters");
        return repo.extraFighters(FPT);
    }
    /**
     * @return how many fighters are still needed to fill every team,
     * or 0 if there are already enough
     * @since 1.0
     * @see #extraFighters()
     */
    public long requiredFighters() {
        Debug.write("Manager.requiredFighters");
        long extra = -1 * extraFighters();
        //System.out.println(extra);
        return (extra > 0) ? extra : 0;
    }

    /**
     * Unassigns all bye teams and players from their league.
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean removeByes() {
        Debug.write("Manager.removeByes");
        Transaction tr = null;
        try {   
            tr = ses.beginTransaction();
            Team bye = repo.getTeam_byName("Bye");
            while (bye != null) {
                bye = repo.getTeam_byName("Bye");
                unassignPlayer(bye.getPlayer());
                unassignTeam(bye);
                repo.deleteAllByes();
            }
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }
    
    /**
     * Adds a fighter to a team. If team is full, the fighter
     * with the lowet base is unassigned from the team.
     * @return true if fighter added, false if not
     * @since 1.1.0
     */
    public boolean addFighter(Fighter f, Team t) {
        //Debug.write("Manager.addFighter", f.getID(), t.getID());
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            if (!replaceFighter(f, t)) {
                Debug.write("Did not add fighter");
                tr.rollback();
                return false;
            }
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Generates a round-robin schedule for specified league.
     * @param lg league
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean generateSchedule(League lg) {
        Debug.write("Manager.generateSchedule", lg.getID());
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
            Debug.error(-2, e);
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
    private void playGame(Game g) {
        Debug.write("Manager.playGame", g.getID());
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
                teamList1.add(f);
            for (Fighter f : team2.getFighterList())
                teamList2.add(f);

            teamList1.sort(Comparator.comparingInt(Fighter::getBase).reversed());
            teamList2.sort(Comparator.comparingInt(Fighter::getBase).reversed());

            double score1 = 0.0, score2 = 0.0;
            for (int i = 0; i < FPG; i += 1) {
                int choice1 = team1.getPlayer().getChoice(teamList1.subList(0, FPG - i));
                int choice2 = team2.getPlayer().getChoice(teamList2.subList(0, FPG - i));
                Fighter f1 = teamList1.remove(choice1);
                Fighter f2 = teamList2.remove(choice2); 
                int result = Menu.matchup(f1, f2);
                mergeMatch(f1, f2, result);
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
            Debug.error(-1, e);
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
        //Debug.write("Manager.matchPoints", matchRound);
        return 1.0 + (1.0 / (10.0 * (FPT - matchRound)));
    }

    /**
     * Simulates all the unplayed games in a round.
     * 
     * Should be called after user games have completed.
     * @param round Round to simulate
     * @return true if successful, false if not
     * @since 1.0
     * @version 3
     */
    public boolean simRound(int round) {
        Debug.write("Manager.simRound", round);
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            List<Game> games = repo.getGames_byRound(round);
            for (Game g : games) {
                //System.out.println(g.getGameString());
                Hibernate.initialize(g.getTeams());
                if (g.getResult().equals("Unfinished")) {
                    playGame(g);
                    ses.flush();
                }
            }
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }


    /**
     * Creates one single league to store all eligible world cup teams. 
     * A team is eligible if it has at least {@code FPG} fighters.
     * @return true if successful, false if not
     * @since 1.1.2
     */
    private boolean setWorldCupTeams() {
        Debug.write("Manager.getWorldCupTeams");
        //Transaction tr = null;
        try {
            //tr = ses.beginTransaction();
            League wcLeague = new League("World Cup");
            persist(wcLeague);
            for (Team t : repo.allTeams()) {
                t.getFighterList().sort(Comparator.comparing(Fighter::getBase, Comparator.reverseOrder()));
                if (t.getFighterListSize() >= FPG) {
                    t.setFans((int)repo.avgBase(t));
                    assignTeam(t, wcLeague);
                }
            }
            ses.flush();
            //tr.commit();
            //tr = null;
            return true;
        } catch (RuntimeException e) {
            //if (tr != null) tr.rollback();
            Debug.error(-1, e);
            return false;
        }
        
    }


    /**
     * Persists a single object to the database.
     * @param de
     * @return true if successful, false if not
     * @since 1.1.1
     */
    public boolean persistSolo(DataEntity de) {
        Debug.write("Manager.persistSolo", de.getID());
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            ses.persist(de);
            ses.flush();
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Persists an object to a transaction. Must be called from inside a transaction.
     * @param o
     * @return true if successful, false if not
     * @since 1.1.2
     */
    public boolean persist(DataEntity de) {
        //Debug.write("Manager.persist", de.getID());
        try {
            ses.persist(de);
            return true;
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return false;
        }
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
        Debug.write("Manager.mergeGame", g.getID(), winner, score1, score2);
        try {
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
            return true;
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return false;
        }
    }

    public boolean mergeSoloGame(Game g, String winner, int score1, int score2) {
        Debug.write("Manager.mergeSoloGame", g.getID(), winner, score1, score2);
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            mergeGame(g, winner, score1, score2);
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * After a match is completed, updates database with a win for winning fighter
     * and a loss for the losing fighter.
     * Ties are considered no contest.
     * @param f1
     * @param f2
     * @param result
     * @return true if successful, false if not
     * @since 1.1.2
     */
    public boolean mergeMatch(Fighter f1, Fighter f2, int result) {
        Debug.write("Manager.mergeMatch", f1.getID(), f2.getID(), result);
        try {
            if (result == 1) {
                f1.addWin();
                f2.addLoss();
            } else if (result == -1) {
                f1.addLoss();
                f2.addWin();
            } 
            ses.merge(f1);
            ses.merge(f2);
            return true;
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return false;
        }
    }

    public boolean mergeSoloMatch(Fighter f1, Fighter f2, int result) {
        Debug.write("Manager.mergeSoloMatch", f1.getID(), f2.getID(), result);
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            mergeMatch(f1, f2, result);
            tr.commit();
            tr = null;
            return true;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Auto-generates a player and assigns it to a team. Assigns team to specified league.
     * Merges team, league, and new player to database.
     * @param t team
     * @param lg desired league for team
     * @return true if both assignments successful, false if not
     * @since 1.1.2
     */
    public boolean autogen_mergeTeam(Team t, League lg) {
        //Debug.write("mergeTeam", t.getID(), lg.getID());
        try {
            String autoName = "player" + t.getID();
            Player autoPlayer = new Player(autoName + " (CPU)");

            if (assignTeam(t, lg) > 0 && assignPlayer(autoPlayer, t)) {
                ses.merge(autoPlayer);
                ses.merge(t);
                ses.merge(lg);
                return true;
            } else {
                Debug.write("Could not ");
                return false;
            }
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return false;
        }
    }

    /**
     * Auto-generates a player and assigns it to a team.
     * Merges team and new player to database.
     * @param t team
     * @return true if successful, false if not
     * @since 1.1.2
     */
    public boolean mergeTeam(Team t) {
        //Debug.write("Manager.mergeTeam", t.getID());
        String autoName = "player" + t.getID();
        Player autoPlayer = new Player(autoName + " (CPU)");

        if (assignPlayer(autoPlayer, t)) {
            ses.merge(autoPlayer);
            ses.merge(t);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates and persists a new bye team and player.
     * @return the bye team created.
     * @since 1.1.1
     */
    public Team createByeTeam() {
        Debug.write("Manager.createByeTeam");
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
            Debug.error(-2, e);
            return null;
        }
    }
} //end class Manager

// Congrats!! You made it to the end of the Manager class!