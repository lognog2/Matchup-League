package com.repo;

import java.io.IOException;
import java.util.List;
import com.Entities.*;

/* 
 * Repository serves as the 'middleman' between DAO and surface classes
 * this is the only class allowed to have DAO objects
 */
public class Repository 
{
    public Repository()
    {}

    //initialize DAO object for each entity
    private final DAO<Fighter> fDAO = new DAO<>(Fighter.class);
    private final DAO<Team> tDAO = new DAO<>(Team.class);
    private final DAO<League> lgDAO = new DAO<>(League.class);
    private final DAO<Player> pDAO = new DAO<>(Player.class);
    private final DAO<Game> gDAO = new DAO<>(Game.class);

    //load data
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

    /* SELECT queries */

    //get count
    public boolean playerNameExists(String name) {return pDAO.count("name = '" + name + "'") > 0;}

    public long extraFighters(int fpt) {return fDAO.count() - (tDAO.count() * fpt);}
    public boolean hasExtraFighters(int fpt) {return extraFighters(fpt) >= 0;}

    //get tuple
    public League getLeague_byName(String name) {return lgDAO.selectOne("name", name);}
    public Team getTeam_byName(String name) {return tDAO.selectOne("name", name);}
    public Fighter getFighter_byName(String name) {return fDAO.selectOne("name", name);}
    public Fighter getFighter_byID(int id) {return fDAO.selectOne("FID", id);}
    public Player getPlayer_byName(String name) {return pDAO.selectOne("name", name);}
    public Game getGame_byID(int id) {return gDAO.selectOne("GID", id);}
    public Game getGame_byTeams(Team team1, Team team2) 
    {return gDAO.selectOne("team1.name = '" + team1.getName() + "' AND team2.name = '" + team2.getName() + "'");}

    //get list
    public List<Game> getGames_inLeague_byRound(int round, League lg) 
    {return gDAO.run_selectionQuery("SELECT DISTINCT g FROM com.Entities.Game g " +
    "JOIN g.teams t " + "JOIN t.league l " +
    "WHERE g.round = " + round + " AND l.name = '" + lg.getName() + "'");}
    
    public List<Game> getGames_byRound(int round) {return gDAO.select("round = " + round);}

    public List<Team> getStandings(League lg)
    {return tDAO.orderByDesc("league.name = '" + lg.getName() + "'", "wins / gamesPlayed");}

    public List<Fighter> getFighters_byTeam (Team t)
    {return fDAO.orderByDesc("team.name = '" + t.getName() + "'", "base");}

    public List<String> getAllLeagueNames()
    {return lgDAO.selectNames();}

    public void deleteAllByes()
    {tDAO.run_mutationQuery("DELETE FROM Teams WHERE name = 'Bye'");}

    public List<Team> getTiedTeams(League lg) 
    {return tDAO.select("league.name = '" + lg.getName() + 
        "' AND wins/gamesPlayed = " + tDAO.max("wins/gamesPlayed"));}

    public List<Player> getAllUsers()
    {
        return pDAO.select("strategy = 99");
    }

    //get full list
    public List<Fighter> allFighters() {return fDAO.select();}
    public List<Team> allTeams() {return tDAO.select();}
    public List<League> allLeagues() {return lgDAO.select();}

    //draft queries
    public List<Fighter> getFighterPool() {return fDAO.orderByDesc("team IS NULL", "base");}
    public List<Team> allTeams_byFans() {return tDAO.orderByDesc("fans");}

    //fighter survey
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

    //return full name of given type
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
