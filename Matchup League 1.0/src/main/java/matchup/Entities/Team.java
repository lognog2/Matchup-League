package matchup.Entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "Teams")
public class Team extends Entities
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int TID;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LID")
    private League league;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Fighter> fighters = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    private Player player;

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinTable (
        name = "schedule",
        joinColumns = {@JoinColumn(name = "TID")},
        inverseJoinColumns = {@JoinColumn(name = "GID")} )
    private List<Game> schedule = new ArrayList<>();

    //constructors
    public Team(String[] line)
    {
        setName(line[0]);
        setFans(Integer.parseInt(line[1]));
        setLocation(line[2]);
        wins = 0;
        gamesPlayed = 0;
    }
    public Team (String name)
    {
        this.name = name;
        setFans(0);
        setLocation("");
        wins = 0;
        gamesPlayed = 0;
        
    }
    public Team(boolean autogen)
    {
        if (autogen)
        {
            setName(randomName());
            setLocation("Autogen");
            setFans((int)Math.random() * 101); //range: 0 - 100
            wins = 0;
            gamesPlayed = 0;
        }
        else {
            this.name = "default";
            setLocation("");
            setFans(0);
            wins = 0;
            gamesPlayed = 0;
            Player auto = new Player(true);
            addPlayer(auto);
        }
    }
    public Team(League lg)
    {
        this.name = "Bye";
        setLocation("");
        setFans(-1);
        wins = 0;
        gamesPlayed = -1;
        Player bye = new Player(false);
        addPlayer(bye);
        lg.addTeam(this);
    }
    protected Team() {};

    //get methods
    public int TID() {return TID;}
    public String name() {return name;}
    public String location() {return location;}
    public int fans() {return fans;}
    public int wins() {return wins;}
    public int gamesPlayed() {return gamesPlayed;}
    public int size() {return fighters.size();}
    public List<Fighter> fighterList() {return fighters;}
    public List<Game> schedule() {return schedule;}
    public League league() {return league;}
    public Player player() {return player;}
    public Game gameAt(int round) {return schedule.get(round);}
    public Team opponent(int round) {return gameAt(round).opponent(this);} 
    public Team opponent(Game g) {return g.opponent(this);}
    

    //derived get methods
    public int losses() {return gamesPlayed - wins;}
    public double winPct() {return ((double)wins / (double)gamesPlayed);}
    public int winDiff() {return wins - losses();}

    //set methods
    public void setName(String name) 
    {
        if (name.toUpperCase().equals("BYE"))
        {
            System.out.println("Nice try. You can't name a team 'Bye'");
            this.name = "Real Team Named Bye";
        }
        else
            this.name = name;
    }
    public void setLocation(String loc) {this.location = loc;}
    public void setFans(int fans) {this.fans = fans;}
    public void changeFans(int change) {this.fans += change;}
    public void addWin() {this.gamesPlayed += 1; this.wins += 1; }
    public void addLoss() {this.gamesPlayed += 1;}
    public void addFighter(Fighter f) {fighters.add(f); f.setTeam(this);}
    public void removeFighter(Fighter f) {fighters.remove(f); f.setTeam(null);}
    public void addPlayer(Player player) {this.player = player; player.setTeam(this);}
    public void removePlayer() {this.player = null;}
    public void setSchedule(int games) {schedule = new ArrayList<>(games);}
    public void addGame(Game g) {schedule.add(g);}
    public void setOrder() {player.setOrder(fighterList());}

    //dependent methods
    public void setLeague(League lg) {this.league = lg;}

    //inherited methods
    public Team autogen()
    {
        Team auto = new Team(true);
        return auto;
    }

    //display methods
    public void print_fighters() 
    {
        System.out.println("\n" + name);
        for (Fighter f : fighterList())
        {
            f.print_oneLine();
        }
    }

}
