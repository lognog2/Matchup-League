package matchup.Entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Teams")
public class Team 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int TID;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "leagueName")
    private League league;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Fighter> fighters = new ArrayList<>();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location")
    private String location;

    @Column(name = "fans")
    private int fans;

    @Column(name = "wins")
    private int wins;

    @Column(name = "games")
    private int gamesPlayed;

    //constructors
    public Team(String[] line)
    {
        setName(line[0]);
        setFans(Integer.parseInt(line[1]));
        setLocation(line[2]);
        wins = 0;
        gamesPlayed = 0;
    }
    protected Team() {};

    //get methods
    public int TID() {return TID;}
    public League league() {return league;}
    public String name() {return name;}
    public String location() {return location;}
    public int fans() {return fans;}
    public int wins() {return wins;}
    public int gamesPlayed() {return gamesPlayed;}
    public int losses() {return gamesPlayed - wins;}
    public double winPct() {return ((double)gamesPlayed / (double)wins);}
    public int size() {return fighters.size();}

    //set methods
    public void setName(String name) {this.name = name;}
    public void setLocation(String loc) {this.location = loc;}
    public void setFans(int fans) {this.fans = fans;}
    public void changeFans(int change) {this.fans += change;}
    public void addWin() {this.gamesPlayed++; this.wins++; }
    public void addLoss() {this.gamesPlayed++;}
    public void addFighter(Fighter f) {fighters.add(f); f.setTeam(this);}
    public void removeFighter(Fighter f) {fighters.remove(f); f.setTeam(null);}
    public void setLeague(League lg) {this.league = lg;}

    //utility methods
}
