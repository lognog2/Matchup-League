package com.repo;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import com.Entities.*;

/** 
 * Repository serves as the 'middleman' between DAO and surface classes.
 * This is the only class that should have DAO objects.
 * @since 1.1.0
 */
public class Repository 
{
    /**
     * Empty constructor
     * @since 1.1.0
     */
    public Repository()
    {}

    /**
     * DAO for accessing the Fighter table.
     * @since 0.3.1
     */
    private final DAO<Fighter> fDAO = new DAO<>(Fighter.class);
    /**
     * DAO for accessing the Team table.
     * @since 0.3.1
     */
    private final DAO<Team> tDAO = new DAO<>(Team.class);
    /**
     * DAO for accessing the League table.
     * @since 0.3.1
     */
    private final DAO<League> lgDAO = new DAO<>(League.class);
    /**
     * DAO for accessing the Player table.
     * @since 1.0
     */
    private final DAO<Player> pDAO = new DAO<>(Player.class);
    /**
     * DAO for accessing the Game table.
     * @since 1.0
     */
    private final DAO<Game> gDAO = new DAO<>(Game.class);

    /* Mutation queries - will change database if successful */

    /**
     * Loads data from the 'sample' csv files, which contain all
     * fighter and team data.
     * @return true if successful, false if not
     * @since 1.0
     */
    public boolean load_data() {
        try {
            fDAO.load_data("Fighters","f_sample.csv");
            tDAO.load_data("Teams","t_sample.csv");
            //lgDAO.load_data("Leagues","lg_sample.csv");
            return true;
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Deletes all bye teams from the database.
     * @since 1.0
     */
    public void deleteAllByes()
    {tDAO.run_mutationQuery("DELETE FROM Teams WHERE name = 'Bye'");}

    /* SELECT queries - will not change database */

    /**
     * Searches the Player table to see if a name already exists.
     * @param name to search
     * @return true if the number of existing players with name is above zero, false if not
     * @since 1.0
     */
    public boolean playerNameExists(String name) {return pDAO.count("name = '" + name + "'") > 0;}

    //get tuple
    /**
     * @param name league name
     * @return first league found with specified name, or null if none found
     * @since 1.0
     */
    public League getLeague_byName(String name) {return lgDAO.selectOne("name", name);}
    /**
     * @param name team name
     * @return first team found with specified name, or null if none found
     * @since 1.0
     */
    public Team getTeam_byName(String name) {return tDAO.selectOne("name", name);}
    /**
     * @param name fighter name
     * @return first fighter found with specified name, or null if none found
     * @since 1.0
     */
    public Fighter getFighter_byName(String name) {return fDAO.selectOne("name", name);}
    /**
     * @param id fighter ID (FID)
     * @return fighter with specified FID, or null if none found
     * @since 1.0
     */
    public Fighter getFighter_byID(int id) {return fDAO.selectOne("FID", id);}
    /**
     * @param name player name
     * @return first player found with specified name, or null if none found
     * @since 1.0
     */
    public Player getPlayer_byName(String name) {return pDAO.selectOne("name", name);}
    /**
     * @param id game ID (FID)
     * @return game with specified FID, or null if none found
     * @since 1.0
     */
    public Game getGame_byID(int id) {return gDAO.selectOne("GID", id);}
    /**
     * @param team1 team 1
     * @param team2 team 2
     * @return First game found with these two teams, or null if none found
     * @since 1.0
     */
    public Game getGame_byTeams(Team team1, Team team2) 
    {return gDAO.selectOne("team1.name = '" + team1.getName() + "' AND team2.name = '" + team2.getName() + "'");}

    //get list

    /**
     * Gets the team at the top of the rankings in each league.
     * @return List of top-ranked teams
     * @since 1.1.1
     */
    public List<Team> getChampions() {
        List<Team> teams = new ArrayList<>();
        for (League lg : allLeagues()) {
            Team t = getStandings(lg).get(0);
            teams.add(t);
        }
        return teams;
    }
    /**
     * @param round round
     * @param lg league
     * @return list of games in specified league during specified round
     * @since 1.0
     */
    public List<Game> getGames_inLeague_byRound(int round, League lg) 
    {return gDAO.run_selectionQuery("SELECT DISTINCT g FROM com.Entities.Game g " +
    "JOIN g.teams t " + "JOIN t.league l " +
    "WHERE g.round = " + round + " AND l.name = '" + lg.getName() + "'");}
    
    public List<Game> getGames_byRound(int round) {return gDAO.select("round = " + round);}

    /**
     * @param lg league
     * @return List of teams in the league from highest winning percentage to lowest.
     * @since 1.0
     */
    public List<Team> getStandings(League lg)
    {return tDAO.orderByDesc("league.name = '" + lg.getName() + "'", "wins / gamesPlayed");}

    /**
     * Gets all generic fighters in the database.
     * @return List of fighters whose rarity is above zero
     * @since 1.1.1
     */
    public List<Fighter> getGenericFighters() {
        return fDAO.orderByDesc("rarity > 0", "rarity");
    }
    /**
     * Gets the sum of the rarity of all fighters in the database.
     * @return sum of rarities
     * @since 1.1.1
     */
    public int getTotalRarity() {return fDAO.sum("rarity");}

    /**
     * Gets all user players in the database.
     * @return List of players with user strategy (99)
     * @since 1.1.1
     */
    public List<Player> getAllUsers() {return pDAO.select("strategy = 99");}

    //get full list

    /**
     * @return List of all fighters in the database
     * @since 1.0
     */
    public List<Fighter> allFighters() {return fDAO.select();}
    /**
     * @return List of all teams in the database
     * @since 1.0
     */
    public List<Team> allTeams() {return tDAO.select();}
    /**
     * @return List of all leagues in the database
     * @since 1.0
     */
    public List<League> allLeagues() {return lgDAO.select();}

    //draft queries

    /**
     * @return List of all fighters without a team
     * @since 1.0
     */
    public List<Fighter> getFighterPool() {return fDAO.orderByDesc("team IS NULL", "base");}
    /**
     * @return List of all teams ordered by number of fans
     * @since 1.0
     */
    public List<Team> allTeams_byFans() {return tDAO.orderByDesc("fans");}

    //check draft eligibility
    
    /**
     * Gets the number of fighters that would beleft over after all teams have drafted.
     * @param fpt fighters per team
     * @return number of extra fighters as a primitive long;
     * negtative indicates fighter defecit
     * @since 1.0
     */
    public long extraFighters(int fpt) {return fDAO.count() - (tDAO.count() * fpt);}
    /**
     * @param fpt fighters per team
     * @return true if the number of extra fighters is zero or above,
     * or false if there is a fighter defecit
     * @since 1.0
     */
    public boolean hasExtraFighters(int fpt) {return extraFighters(fpt) >= 0;}

    //TODO: move to menu where it will display
    /**
     * Executes fighter survey, prints to console the frequency of each
     * type, each strength and weakness type, and average strength and weakness value
     * @since 1.0
     */
    public void fighterSurvey()
    {
        //setup
        final long totalFighters = fDAO.count();
        if (totalFighters == 0) return;
        final char[] allTypes = {'M', 'R', 'E', 'F', 'W', 'L', 'G', 'C', 'A', 'I', 'S', 'V'};
        System.out.println(totalFighters + " total fighters");

        System.out.println("\nType frequency");
        for (char type : allTypes)
        {
            double pct = (100.0 * (fDAO.count("types LIKE '%" + type + "%'") / (double)totalFighters));
            System.out.printf(fullType(type) + ": %.0f%%\n", pct);
        }

        //avg base
        System.out.printf("\nAverage base power: %.0f\n", fDAO.avg("base"));

        System.out.println("\nStrength frequency/average");
        for (char type : allTypes)
        {
            double pct = (100.0 * (fDAO.count("strType LIKE '%" + type + "%'") / (double)totalFighters));
            double avg = fDAO.avg("strVal", "strType LIKE '%" + type + "%'");
            System.out.printf(fullType(type) + ": %.0f%%, %.0f\n", pct, avg);
        }

        System.out.println("\nWeakness frequency/average");
        for (char type : allTypes)
        {
            double pct = (100.0 * (fDAO.count("wkType LIKE '%" + type + "%'") / (double)totalFighters));
            double avg = -1.0 * (fDAO.avg("wkVal", "wkType LIKE '%" + type + "%'"));
            System.out.printf(fullType(type) + ": %.0f%%, %.0f\n", pct, avg);
        }
    }

    /**
     * Returns full name of given type.
     * @param type type character to be expanded
     * @return String of full name
     * @since 0.1
     */
    public String fullType(char type)
    {
        switch(type)
        {
            case 'M': return "Melee";
            case 'R': return "Ranged";
            case 'E': return "Explosive";
            case 'F': return "Fire";
            case 'W': return "Water";
            case 'L': return "Electric";
            case 'G': return "Magic";
            case 'C': return "Mechanical";
            case 'A': return "Aerial";
            case 'I': return "Ice";
            case 'S': return "Star";
            case 'V': return "Evil";
            default: return "N/A";
        }
    }
}
