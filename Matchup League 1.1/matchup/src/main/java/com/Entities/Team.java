package com.Entities;

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
    private List<Fighter> fighterList;

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
        fighterList = new ArrayList<>();
    }
    public Team (String name)
    {
        this.name = name;
        setFans(0);
        setLocation("");
        wins = 0;
        gamesPlayed = 0;
        fighterList = new ArrayList<>();
    }
    public Team(boolean autogen)
    {
        fighterList = new ArrayList<>();
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
    public Team() {}

    /* get methods */

    //get private variable
    public int getTID() {return TID;}
    public String getName() {return name;}
    public String getLocation() {return location;}
    public int getFans() {return fans;}
    public int getWins() {return wins;}
    public int getGamesPlayed() {return gamesPlayed;}

    public int getRanking() {return entityRepo.getStandings(league).indexOf(this) + 1;}
    public boolean onBye(int round) {return getOpponent(round).getFans() < 0;}

    //get relational variable/object
    public List<Fighter> getFighterList() {return fighterList;}
    public Fighter getFighter(int index) {return fighterList.get(index);}
    public int getFighterListSize() {return fighterList.size();}
    public List<Game> getSchedule() {return schedule;}
    public League getLeague() {return league;}
    public Player getPlayer() {return player;}
    public Game getGame(int round) {return schedule.get(round);}
    public Team getOpponent(int round) {return getGame(round).getOpponent(this);} 
    public Team getOpponent(Game g) {return g.getOpponent(this);}
    
    //derived get methods
    public int getLosses() {return gamesPlayed - wins;}
    public double winPct() {return ((double)wins / (double)gamesPlayed);}
    public int winDiff() {return wins - getLosses();}

    /* set methods */

    //set/change private variable
    public void setName(String name) 
    {
        if (name.toLowerCase().equals("bye"))
        {
            System.out.println("Nice try. You can't name a team 'Bye'");
            this.name = "Team Named Bye";
        }
        else
            this.name = name;
    }
    public void setLocation(String loc) {this.location = loc;}
    public void setFans(int fans) {this.fans = fans;}
    public void changeFans(int change) {this.fans += change;}
    public void addWin() {this.gamesPlayed += 1; this.wins += 1; }
    public void addLoss() {this.gamesPlayed += 1;}

    //change relation
    public void addFighter(Fighter f) {fighterList.add(f); f.setTeam(this);}
    public void removeFighter(Fighter f) {fighterList.remove(f); f.setTeam(null);}
    public void addPlayer(Player player) {this.player = player; player.setTeam(this);}
    public void removePlayer() {this.player = null;}
    public void setSchedule(int games) {schedule = new ArrayList<>(games);}
    public void addGame(Game g) {schedule.add(g);}
    public void setOrder() {player.setOrder(getFighterList());}

    //dependent set methods
    public void setLeague(League lg) {this.league = lg;}

    //utility methods
    public boolean isUserTeam()
    {
        return player.getStrategy() == 99;
    }

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
        for (Fighter f : getFighterList())
        {
            f.print_oneLine();
        }
    }

}
