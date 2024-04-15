/*
 * Scheduler v0.1
 * by Logan Nyquist
 * 
 * 0.1 (4/2024): Simulates a season using round robin format
 * League used: Florida
 */

package scheduler;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main 
{
    public static void main(String[] args) 
    {
        List<Team> league = new ArrayList<>();
        //read in file
        try
        {
            File file = new File("src\\main\\resources\\Florida.txt");
            Scanner scan = new Scanner(file);

            while(scan.hasNextLine())
            {
                String line[] = scan.nextLine().trim().split(":", 2);
                String name = line[0];
                int skill = 0;
                if (line.length > 1)
                    skill = Integer.parseInt(line[1].trim());

                if (!line[0].isEmpty())
                {
                    Team t;
                    if (skill > 0)
                        t = new Team(name, skill);
                    else
                        t = new Team(name);
                    league.add(t); 
                }
            }
            scan.close();                  
        }
        catch (FileNotFoundException e) 
        { 
            System.out.println("Error reading file");
            e.printStackTrace();
        }


        Scanner in = new Scanner(System.in);
        int choice = 0;
        /*
        System.out.println("\nEnter the number of the team you want to play as:");
        for (int i = 1; i <= numTeams; i++)
        {
            Team t = league.get(i);
            System.out.println(i + ": " + t.name() + " " + t.skill());
        }
        System.out.print(">");
        int answer = in.nextInt();
        */
        
        //build schedule - round robin format
        boolean odd = league.size() % 2 != 0;
        if (odd)
        {
            Team bye = new Team("Bye", 0);
            league.add(bye);
        }
        int numRounds = league.size() - 1;
        for (Team t : league)
            t.setSchedule(numRounds);
        sched_round_robin(league);

        int round = 0;
        //menu
        do
        {
            if (round + 1 > numRounds)
                System.out.print("\n1: Finish season");
            else
                System.out.print("\n1: Next round (" + (round + 1) + "/" + numRounds + ")");

            System.out.print("\n2: Print standings\n3: Print teams by skill\n4: Print a team's schedule\n0: Quit program\n> ");
            choice = in.nextInt();

            if (choice == 1) //next round
            {
                if (round + 1 > numRounds) {
                    System.out.println("\n" + league.get(0).name() + " won the season!");
                    choice = 0;
                }
                else
                {
                    System.out.println("");
                    playRound(league, round);
                    System.out.println("");
                    rankRecord(league); 
                    round++;
                }
            } 
            else if (choice == 2) //print standings
                rankRecord(league);

            else if (choice == 3) //print by skill
                rankSkill(league);

            else if (choice == 4) //print schedule
            { 
                System.out.println("\nEnter the number of the team you want to view:");
                for (int i = 0; i < league.size(); i++)
                {
                    Team t = league.get(i);
                    if (t.skill() > 0)
                        System.out.println(i + ": " + t.name());
                }
                System.out.print("> ");
                int team = in.nextInt();
                league.get(team).print_schedule();
            }

        } while(choice != 0);

        in.close();

    } //end main method

    //rank by skill
    public static List<Team> rankSkill(List<Team> league)
    {
        Collections.sort(league, (team1, team2) -> team2.skill() - team1.skill());
        int r = 1;
        for (Team t : league)
        {
            if (t.skill() > 0)
                System.out.println(r++ + ") " + t.name() + " " + t.skill());
        }
        return league;
    }

    //rank by record (win percentage)
    public static List<Team> rankRecord(List<Team> league)
    {
        Collections.sort(league, (team1, team2) -> Double.compare(team2.winPct(), team1.winPct()));
        int r = 1;
        for (Team t : league)
        {
            if (t.skill() > 0)
                System.out.println(r++ + ") " + t.name() + " " + t.wins() + "-" + t.losses());
        }
        return league;
    }

    public static void sched_round_robin(List<Team> league) 
    {
        final int N = league.size();
    
        for (int round = 0; round < N - 1; round++) 
        {
            for (int match = 0; match < N / 2; match++) 
            {
                Team home = league.get(match);
                Team away = league.get(N - 1 - match);
                home.addGame(away);
                away.addGame(home);
            }
            league.add(1, league.remove(league.size() - 1));
        }
        //if (odd) league.remove(bye);
    }

    public static void playRound(List<Team> league, int round)
    {
        for (Team t : league)
        {
            if (!t.hasPlayed(round))
                t.playGame(round);
        }
    }

} //end Main class

class Team
{
    private String name;
    private List<Team> schedule;
    private char[] result;
    //private int wins, losses;
    private int skill;
    
    //name only constructor
    public Team(String str)
    {
        name = replace(str, '-', ' ');
        schedule = new ArrayList<>();
        skill = (int)(Math.random() * 100);
    } //end constructor

    //name & skill constructor
    public Team(String str, int skill)
    {
        name = replace(str, '-', ' ');
        if (skill > 0)
        {
            double offset = (Math.random() * 10) - 5;
            this.skill = skill + (int)offset;
        }   
    } //end constructor

    //accessors
    public String name() {return name;}
    public int skill() {return skill;}
    public Team opponent(int i) {return schedule.get(i);}
    public String oppName(int i) {return schedule.get(i).name();}
    public char result(int i) {return result[i];}

    public int wins() 
    {
        int w = 0;
        for (char c : result)
        {
            if (c == 'W')
                w++;
        }
        return w;
    }
    public int losses() 
    {
        int l = 0;
        for (char c : result)
        {
            if (c == 'L')
                l++;
        }
        return l;
    }
    public int gamesPlayed()
    {
        int g = 0;
        for (char c : result)
        {
            if (c == 'W' || c == 'L')
                g++;
        }
        return g;
    }
    public double winPct() 
    {
        if (this.gamesPlayed() == 0)
            return 0;
        return (double)wins() / (double)gamesPlayed();
    }

    //mutators
    public void addWin(int i) {result[i] = 'W';}
    public void addLoss(int i) {result[i] = 'L';}
    public void setResult(int i, char c) {result[i] = c;}
    public void addGame(Team t) {schedule.add(t);}
    public void addGame(Team t, int i) {schedule.add(i, t);}

    //null checks
    //public boolean hasGame(Team t) {return schedule.contains(t);}
    //public boolean hasGame(int i) {return schedule.get(i) != null;}
    public boolean hasPlayed(int i) {return result[i] != '\0';}

    public void setSchedule(int games)
    {
        schedule = new ArrayList<>(games);
        result = new char[games];
    }

    public void print_schedule()
    {
        System.out.println("\n" + name + " vs:");

        for (int i = 0; i < schedule.size(); i++)
        {
            System.out.println(oppName(i) + " " + result[i]);
        }
    }

    public void playGame(int round)
    {
        Team team1 = this;
        Team team2 = opponent(round);
        //skip game if either team is a bye
        if (team1.skill() <= 0)
        {
            System.out.println(team2.name() + " on bye week");
            team1.setResult(round, ' ');
            team2.setResult(round, ' ');
        }
        else if (team2.skill() <= 0)
        {
            System.out.println(team1.name() + " on bye week");
            team1.setResult(round, ' ');
            team2.setResult(round, ' ');
        }
        else 
        {
            System.out.print(team1.name() + " vs " + team2.name() + ": ");
            int range = team1.skill() + team2.skill();
            int score = (int)(Math.random() * range) + 1;
            //System.out.print(score + ", ");
            if (score <= team1.skill())
            {
                team1.addWin(round);
                team2.addLoss(round);
                System.out.println(team1.name() + " wins");
            }
            else
            {
                team2.addWin(round);
                team1.addLoss(round);
                System.out.println(team2.name() + " wins");
            }
        }
        
    } //end playGame

    private String replace(String str, char remove, char add)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == remove)
                str = str.substring(0, i) + add + str.substring(i+1);
        }
        return str;
    }

} //end Team class