package matchup.Entities;

import java.util.List;
import java.util.UUID;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import matchup.*;

public abstract class Entities 
{
    /* open session */
    private static SessionFactory sf = new Configuration().configure().buildSessionFactory();
    protected static Session ses = sf.openSession();
    //protected Manager enManager = new Manager(ses);

    /* used by entities */
    protected static DAO<Fighter> fDAO = new DAO<>(ses, Fighter.class);
    protected static DAO<Team> tDAO = new DAO<>(ses, Team.class);
    protected static DAO<League> lgDAO = new DAO<>(ses, League.class);
    protected static DAO<Player> pDAO = new DAO<>(ses, Player.class);
    protected static DAO<Game> gDAO = new DAO<>(ses, Game.class);

    protected String randomName() {return UUID.randomUUID().toString().substring(0, 8);}
    protected boolean playerNameExists(String name) {return pDAO.count("name = '" + name + "'") > 0;}

    /* used by non-entities */

    public static Session getSession() {return ses;}
    public static long extraFighters(int fpt) {return fDAO.count() - (tDAO.count() * fpt);}
    public static boolean hasExtraFighters(int fpt) {return extraFighters(fpt) >= 0;}

    //get single by value
    public static League getLeague_byName(String name) {return lgDAO.selectOne("name", name);}
    public static Team getTeam_byName(String name) {return tDAO.selectOne("name", name);}
    public static Fighter getFighter_byName(String name) {return fDAO.selectOne("name", name);}
    public static Fighter getFighter_byID(int id) {return fDAO.selectOne("FID", id);}
    public static Player getPlayer_byName(String name) {return pDAO.selectOne("name", name);}
    public static Game getGame_byID(int id) {return gDAO.selectOne("GID", id);}
    public static Game getGame_byTeams(Team team1, Team team2) 
    {return gDAO.selectOne("team1.name = '" + team1.name() + "' AND team2.name = '" + team2.name() + "'");}

    //get list by value
    public static List<Game> getGames_inLeague_byRound(int round, League lg) 
    {return gDAO.run_selectionQuery("SELECT DISTINCT g FROM matchup.Entities.Game g " +
    "JOIN g.teams t " + "JOIN t.league l " +
    "WHERE g.round = " + round + " AND l.name = '" + lg.name() + "'");}

    public static List<Team> standings(League lg)
    {return tDAO.orderByDesc("league.name = '" + lg.name() + "'", "wins / gamesPlayed");}

    public static void deleteAllByes()
    {
        tDAO.run_mutationQuery("DELETE FROM Teams WHERE name = 'Bye'");
    }

    //all access
    public static List<Fighter> allFighters() {return fDAO.select();}
    public static List<Team> allTeams() {return tDAO.select();}
    public static List<League> allLeagues() {return lgDAO.select();}

    public static void fighterSurvey()
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
    
    //returns full name of given type
    public static String fullType(char type)
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

    public static void dropAllEntities ()
    {
        fDAO.run_mutationQuery("DROP TABLE Fighters");
        tDAO.run_mutationQuery("DROP TABLE Teams");
        lgDAO.run_mutationQuery("DROP TABLE Leagues");
        pDAO.run_mutationQuery("DROP TABLE Players");
        gDAO.run_mutationQuery("DROP TABLE Games");
    }
    public static void clearAllEntities()
    {
        fDAO.run_mutationQuery("DELETE FROM Fighters");
        tDAO.run_mutationQuery("DELETE FROM Teams");
        lgDAO.run_mutationQuery("DELETE FROM Leagues");
        pDAO.run_mutationQuery("DELETE FROM Players");
        gDAO.run_mutationQuery("DELETE FROM Games");
    }

    //abstract classes
    public abstract Object autogen();
    //public abstract Object defaultgen();
}
