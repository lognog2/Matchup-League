package matchup.Entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "Leagues")
public class League extends Entities
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int LID;
    
    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Team> teams = new ArrayList<>();

    @Column(name = "tier", nullable = false)
    private char tier;

    public League(String[] line)
    {
        setName(line[0]);
        setTier(line[1].charAt(0));
    }
    public League(String name)
    {
        setName(name);
        setTier('C');
    }
    protected League(){}

    //get methods
    public int LID() {return LID;}
    public String name() {return name;}
    public List<Team> teamList() {return teams;}
    public int size() {return teams.size();}
    public char tier() {return tier;}

    //set methods
    public void setName(String name) {this.name = name;}
    public void setTier(char tier) {this.tier = tier;}
    public void addTeam(Team t) {teams.add(t); t.setLeague(this);}
    public void removeTeam(Team t) {teams.remove(t); t.setLeague(null);}

    //utility methods
    public Team tiebreaker()
    {
        List<Team> contenders = tDAO.select("league.name = '" + name + 
        "' AND wins/gamesPlayed = " + tDAO.max("wins/gamesPlayed"));
        /*for (Team t : contenders)
        {
            double hhWins = 0.0, hhGames = 0.0;
            for (Game g : t.schedule())
            {
                if (contenders.contains(t.opponent(g)))
                {
                    hhGames += 1.0;
                    if (g.result().equals(t.name()))
                        hhWins += 1.0;
                }
            }
        }*/
        return contenders.get(0);
    }

    //inherited methods
    public Team autogen()
    {
        System.out.println("Can't autogen a league yet");
        return null;
    }

    //display methods
    public void print_standings()
    {
        teams = standings(this);
        System.out.println("\n" + name);
        int rank = 1;
        for (Team t : teams)
        {
            if (t.fans() != -1)
                System.out.println(rank++ + ". " + t.name() + " " + t.wins() + "-" + t.losses());
        }
    }
}
