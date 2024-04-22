package matchup;

import org.hibernate.*;

import matchup.Entities.*;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException
    {
        Scanner in = new Scanner(System.in);
        
        Session ses = Entities.getSession();

        DAO<Fighter> fDAO = new DAO<>(ses, Fighter.class);
        DAO<Team> tDAO = new DAO<>(ses, Team.class);
        DAO<League> lgDAO = new DAO<>(ses, League.class);

        //System.out.println("1. reset database (will end the program)\n2: reset tables");
        //int answer = in.nextInt();
        int answer = 0;
        if (answer == 1)
        {
            Transaction tr = null;
            try {
                tr = ses.beginTransaction();
                Entities.dropAllEntities();
                tr.commit();
                tr = null;
            } catch (RuntimeException e) {
                if (tr != null) tr.rollback();
                e.printStackTrace();
            }
            ses.close();
            in.close();
            return;
        }
        else if (answer == 2)
        {
            Transaction tr = null;
            try {
                tr = ses.beginTransaction();
                Entities.clearAllEntities();
                tr.commit();
                tr = null;
            } catch (RuntimeException e) {
                if (tr != null) tr.rollback();
                e.printStackTrace();
            }
        }

        fDAO.load_data("Fighters","f_sample.csv");
        tDAO.load_data("Teams","t_sample.csv");
        lgDAO.load_data("Leagues","lg_sample.csv");

        Manager manager = new Manager(ses);

        //assign leagues and players to teams
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            for (Team t : Entities.allTeams())
            {
                League target = Entities.getLeague_byName(t.location());
                if (target == null)
                    target = new League(t.location());

                manager.assignTeam(t, target);
                        
                String autoPlayer = "player" + t.TID();
                Player player = new Player(autoPlayer);
                manager.assignPlayer(player, t);
                ses.merge(player);
                ses.merge(t);
            }
            tr.commit();
            tr = null;
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
        }

        //menu
        boolean didDraft = false;
        boolean madeSchedule = false;
        do
        {
            System.out.print("\nWhat would you like to do?\n0: quit\n" +
            "1: Begin season\n" +
            "2: Check draft eligibility\n" +
            "3: Add user\n" +
            "4: Fighter survey\n" + 
            "5: Auto generate fighters\n" +
            ">");
            answer = in.nextInt();
            
            if (answer == 1) //sim draft, schedule, and season
            {
                List<League> leagues = Entities.allLeagues();
                if (Entities.hasExtraFighters(manager.FPT()))
                    didDraft = manager.draft(tDAO.orderByDesc("fans"), fDAO.orderByDesc("team IS NULL", "base"), in);
                if (!didDraft) break;
                for (League lg : Entities.allLeagues())
                {
                    madeSchedule = manager.generateSchedule(lg);
                }
                if (madeSchedule)
                {
                    //season loop
                    
                    for (int round = 0; round < manager.maxTPL(); round++)
                    {
                        boolean seasonDone = true;
                        boolean allSeasonsDone = true;
                        //round loop
                        for (League lg : leagues)
                        {
                            seasonDone = round >= lg.teamList().size() - 1;
                            if (!seasonDone)
                            {
                                manager.playRound(round, lg);
                                allSeasonsDone = false;
                            } 
                        }
                        if (!allSeasonsDone) 
                        { 
                            System.out.println("\nRound " + (round + 1) + " results");
                            for (League lg2 : leagues)
                            {
                                seasonDone = round >= lg2.teamList().size() - 1;
                                lg2.print_standings();
                                if (seasonDone)
                                {
                                    System.out.println("\n" + lg2.name() + " season has ended");                      
                                }
                            }
                            System.out.print("\nPress any key and hit enter to continue:");
                            String pause = in.next();
                        }
                    } 
                    //manager.removeByes();
                    for (League lg : leagues)
                    {

                        List<Team> finalRank = Entities.standings(lg);
                        Team champion = finalRank.get(0);
                        /*if (champion.winPct() == finalRank.get(1).winPct())
                        {
                            champion = lg.tiebreaker();
                        } */
                        System.out.println("\n***" + champion.name() + " has won the " + lg.name() + " League!***");
                    }

                }

            }
            else if (answer == 2) //check if there are enough fighters to draft
            {
                if (Entities.hasExtraFighters(manager.FPT()))
                    System.out.println("Eligible to draft, with " + Entities.extraFighters(manager.FPT()) + " extra fighters");
                else
                    System.out.println("Not enough fighters, need " + Entities.extraFighters(manager.FPT()) * -1);                  
            }
            else if (answer == 3) //add user
            {
                System.out.print("Enter a your name (must be 1 word): ");
                String username = in.next();
                System.out.print("Enter team name: ");
                String userTeamName = in.next();
                manager.addUser(username, userTeamName);
            }
            else if (answer == 4) //fighter survey
                Entities.fighterSurvey();
            else if (answer == 5) //add autogen fighters
            {
                System.out.print("How many: ");
                int amt = in.nextInt();
                manager.autogenFighters(amt);
            }
        } while (answer != 0);
        ses.close();
        in.close();
    } //end main method

}
