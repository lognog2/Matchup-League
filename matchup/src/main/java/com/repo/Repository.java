package com.repo;

import java.util.List;
import java.util.ArrayList;
import com.entities.*;
import com.menu.App;
import com.menu.load.Loader;
import com.util.Debug;

/** 
 * Repository serves as the 'middleman' between DAO and surface classes.
 * This is the only class that should have DAO objects.
 * @since 1.1.0
 * @see DAO
 */
public class Repository 
{
    /**
     * Repository constructor
     * @since 1.1.0
     */
    public Repository() {
        Debug.write("new Repository");
    }

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
    public boolean load_data(boolean debug, boolean useTeams) {
        Debug.write("Repository.load_data", debug, useTeams);
        try {
            Loader loader = App.getLoader();
            loader.setMessage("Loading fighters");
            fDAO.load_data("Fighters", "f_sample.csv");
            if (useTeams) {
                loader.setMessage("Loading teams");
                String teamFile = (debug) ? "t_test" : "t_sample.csv";
                tDAO.load_data("Teams", teamFile);
            } else {
                //add double total rarity, once to generate and once to assign
                loader.addLoadUnits((double)(getTotalRarity()*2));
            }
            //lgDAO.load_data("Leagues","lg_sample.csv");
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
    /**
     * Deletes all bye teams from the database.
     * @since 1.0
     */
    public void deleteAllByes() {
        Debug.write("Repository.deleteAllByes");
        tDAO.run_mutationQuery("DELETE FROM Teams WHERE name = 'Bye'");
    }


    /* SELECT queries - will not change database */

    /**
     * Searches the Player table to see if a name already exists.
     * @param name to search
     * @return true if the number of existing players with name is above zero, false if not
     * @since 1.0
     */
    public boolean playerNameExists(String name) {return pDAO.count("name = '" + name + "'") > 0;}

    /* GET TUPLE */

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


    /* GET LIST */

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
    {return gDAO.run_selectionQuery("SELECT DISTINCT g FROM com.entities.Game g " +
    "JOIN g.teams t " + "JOIN t.league l " +
    "WHERE g.round = " + round + " AND l.name = '" + lg.getName() + "'");}
    
    public List<Game> getGames_byRound(int round) {return gDAO.select("round = " + round);}

    /**
     * @param lg league
     * @return List of teams in the league from highest winning percentage to lowest.
     * @since 1.0
     */
    public List<Team> getStandings(League lg)
    {return tDAO.orderByDesc("wins / gamesPlayed", "league.name = '" + lg.getName() + "'");}

    /**
     * Gets all generic fighters in the database.
     * @return List of fighters whose rarity is above zero
     * @since 1.1.1
     */
    public List<Fighter> getGenericFighters() {
        return fDAO.orderByDesc("rarity", "rarity > 0");
    }
    /**
     * Gets the sum of the rarity of all fighters in the database.
     * @return sum of rarities
     * @since 1.1.1
     */
    public int getTotalRarity() {return fDAO.sum("rarity");}

    /**
     * Gets all user players in the database.
     * <p>mySQL stores enums as index, change if number of strategies changes
     * @return List of players with user strategy
     * @since 1.1.1
     */
    public List<Player> getAllUsers() {
        return pDAO.select("strategy = 5");
    }

   /* GET FULL LIST */

    /**
     * @return List of all fighters in the database
     * @since 1.0
     */
    public List<Fighter> allFighters() {return fDAO.select();}
    /**
     * @return List of all teams in the database, excluding byes
     * @since 1.0
     */
    public List<Team> allTeams() {return tDAO.select("fans >= 0");}
    /**
     * @return List of all leagues in the database
     * @since 1.0
     */
    public List<League> allLeagues() {return lgDAO.select();}

    /* COUNTS */

    /**
     * Gets number of total fighters in database as a long.
     * @return total num of fighters
     * @since 1.2.1
     */
    public long totalFighterCount() {return fDAO.count();}

    /**
     * Gets number of total teams in database as a long.
     * @return total num of teams
     * @since 1.2.1
     */
    public long totalTeamCount() {return tDAO.count();}

    /**
     * Gets number of total leaguesin database as a long.
     * @return total num of leagues
     * @since 1.2.1
     */
    public long totalLeagueCount() {return lgDAO.count();}

    /* STATS */

    public double avgBase(Team t) {return fDAO.avg("base", "team.TID = " + t.TID());}

    /**
     * Gets the frequency of a type among all fighters
     * @param type type, in its char from
     * @param column types, strType, or wkType
     * @return percentage of fighters with that type in that column
     */
    public double typeFreq(String column, char type) {return (100.0 * (fDAO.count(column + " LIKE '%" + type + "%'") / (double)totalFighterCount()));}

    /* DRAFT QUERIES */

    /**
     * @return List of all fighters without a team
     * @since 1.0
     */
    public List<Fighter> getFighterPool() {return fDAO.orderByDesc("base", "team IS NULL");}
    /**
     * @return List of all teams ordered by number of fans, exlcuding byes
     * @since 1.0
     */
    public List<Team> allTeams_byFans() {return tDAO.orderByDesc("fans", "fans >= 0");}

    //check draft eligibility
    
    /**
     * Gets the number of fighters that would be left over after all teams have drafted.
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

    /* MISC */

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
