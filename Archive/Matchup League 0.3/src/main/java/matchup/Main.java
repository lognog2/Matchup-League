/*
 * Matchup League v0.3
 * by Logan Nyquist
 */

package matchup;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

import matchup.Entities.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException
    {
        Scanner in = new Scanner(System.in);
           
        SessionFactory sf = new Configuration().configure().buildSessionFactory();

        DAO<Fighter> fDAO = new DAO<>(sf, Fighter.class);
        DAO<Team> tDAO = new DAO<>(sf, Team.class);
        DAO<League> lgDAO = new DAO<>(sf, League.class);

        System.out.println("1. reset database (will end the program)\n2: reset tables");
        int answer = in.nextInt();
        if (answer == 1)
        {
            fDAO.run_query("DROP TABLE Fighters");
            tDAO.run_query("DROP TABLE Teams");
            lgDAO.run_query("DROP TABLE Leagues");
            in.close();
            sf.close();
            return;
        }
        else if (answer == 2)
        {
            fDAO.run_query("DELETE FROM Fighters");
            tDAO.run_query("DELETE FROM Teams");
            lgDAO.run_query("DELETE FROM Leagues");
        }

        fDAO.load_data("Fighters","sample.csv");
        tDAO.load_data("Teams","Florida.csv");
        lgDAO.load_data("Leagues","test.csv");


        List<Fighter> topTen = fDAO.rankBy("base", 10);
        int rank = 1;
        for (Fighter f : topTen)
        {
            System.out.println(rank + ": " + f.name() + " " + f.base());
        }

        Manager leagueManager = new Manager(sf, lgDAO.select());
        leagueManager.batchAssign_byLocation(tDAO.select());

        sf.close();
        in.close();
    } //end main method

    

} //end class
