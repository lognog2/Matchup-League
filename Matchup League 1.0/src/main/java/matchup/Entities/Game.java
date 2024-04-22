package matchup.Entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

@Entity
@Table(name = "Games")
public class Game extends Entities
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int GID;
    
    @Column(name = "round")
    private int round;

    @Column(name = "result")
    private String result;

    @ManyToMany(mappedBy = "schedule")
    private List<Team> teams = new ArrayList<>(2);

    public Game(String[] line)
    {
        setRound(Integer.parseInt(line[0]));
        teams.add(getTeam_byName(line[1]));
        teams.add(getTeam_byName(line[2]));
        setResult("Unfinished");
    }
    public Game(int round, Team team1, Team team2)
    {
        setRound(round);
        setResult("Unfinished");
        teams.add(team1);
        teams.add(team2);
        team1.addGame(this);
        team2.addGame(this);
    }
    protected Game(){}

    //get methods
    public int round() {return round;}
    public String result() {return result;}
    public Team opponent(Team t)
    {
        if (teams.get(0) == t)
            return teams.get(1);
        else if (teams.get(1) == t)
            return teams.get(0);
        else
            return null;
    }

    //set methods
    public void setResult(String result) {this.result = result;}
    public void setRound(int round) {this.round = round;}

    //utility methods
    public String play()
    {
        Team team1 = teams.get(0);
        Team team2 = teams.get(1);
        Scanner in = new Scanner(System.in);

        if (team1.name().equals("Bye"))
        {
            System.out.println("\n" + team2.name() + " on bye week");
            setResult("Bye game");
            return "Bye game";
        }
        else if (team2.name().equals("Bye"))
        {
            System.out.println("\n" + team1.name() + " on bye week");
            setResult("Bye game");
            return "Bye game";
        }
        else 
        {
            System.out.println("\n" + team1.name() + " vs " + team2.name());
            boolean watchGame = false;
            if (team1.player().style() == 99 || team2.player().style() == 99)
                watchGame = true;

            //watchGame = true;

            team1.setOrder();
            team2.setOrder();
            List<Fighter> list1 = new ArrayList<>(team1.fighterList());
            List<Fighter> list2 = new ArrayList<>(team2.fighterList());
            double score1 = 0;
            double score2 = 0;

            int matchLength = team1.fighterList().size();
            //match loop
            for (int i = 0; i < matchLength; i++)
            {
                double tiebreaker = (double)i / 100.0;
                Fighter fighter1 = null;
                Fighter fighter2 = null;
                if (watchGame)
                {
                    System.out.println("\nMatch " + (i + 1));
                    if (team1.player().style() == 99)
                    {
                        print_remaining(team2.name(), list2);
                        fighter2 = list2.get(0);

                        print_remaining_WithIndex(team1.name(), list1);
                        System.out.print("Choose a fighter: ");
                        fighter1 = list1.get(in.nextInt());
                    }
                    else if (team2.player().style() == 99)
                    {
                        print_remaining(team1.name(), list1);
                        fighter1 = list1.get(0);

                        print_remaining_WithIndex(team2.name(), list2);
                        System.out.print("Choose a fighter: ");
                        fighter2 = list2.get(in.nextInt());
                    } else {
                        fighter1 = list1.get(0);
                        fighter2 = list2.get(0);
                        print_remaining(team1.name(), list1);
                        print_remaining(team2.name(), list2);
                    }
                } else {
                    fighter1 = list1.get(0);
                    fighter2 = list2.get(0);
                }

                String winner = Fighter.match(fighter1, fighter2, watchGame);
                if (winner.equals(fighter1.name()))
                    score1 += 1.0 + tiebreaker;
                else if (winner.equals(fighter2.name()))
                    score2 += 1.0 + tiebreaker;

                if (watchGame)
                {
                    System.out.println("\nWinner: " + winner);
                    System.out.println(team1.name() + " " + (int)score1 + "-" + (int)score2 + " " + team2.name());
                }

                list1.remove(fighter1);
                list2.remove(fighter2);
            }
            if ((int)score1 == (int)score2)
            {
                System.out.println("Tiebreaker score: " + team1.name() + " " + score1 + "-" + score2 + " " + team2.name());
            }

            if (score1 > score2)
            {
                team1.addWin();
                team2.addLoss();
                System.out.println(team1.name() + " wins " + (int)score1 + "-" + (int)score2);
                return team1.name();
            }
            else if (score2 > score1)
            {
                team2.addWin();
                team1.addLoss();
                System.out.println(team2.name() + " wins " + (int)score1 + "-" + (int)score2);
                return team2.name();
            } else return "N/A";
        }
    } //end play method
    private void print_remaining_WithIndex(String team, List<Fighter> fighters)
    {
        System.out.println("\n" + team);
        for (int i = 0; i < fighters.size(); i++)
        {
            System.out.print(i + ": ");
            fighters.get(i).print_oneLine();
        }
    }
    private void print_remaining(String team, List<Fighter> fighters)
    {
        System.out.println("\n" + team);
        Collections.shuffle(fighters);
        for (Fighter f : fighters)
            f.print_oneLine();
    }

    //inherited methods
    public Game autogen() 
    {
        System.out.println("Can't autogen a game yet");
        return null;
    }
}
