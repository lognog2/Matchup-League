package com.Entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A team is an Entity class that represents a team.
 * Each team can have fighters added to it and play games
 * with those fighters.
 * Team is considered the 'central' entity, since every other
 * entity has a relation to it.
 * @since 0.3
 */
@Entity
@Table(name = "Teams")
public class Team extends DataEntity
{
    /**
     * Team ID; uniquely identifies a team.
     * @since 0.3
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int TID;

    /**
     * Team name
     * @since 0.3
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * If this is the first team read in with this location, 
     * the program constructs a new league with this location 
     * as the name. If such a league already exists, 
     * this team is added to it.
     * @since 0.3
     */
    @Column(name = "location")
    private String location;

    /**
     * Number of 'fans' this team has. Determines draft order.
     * @since 0.3
     */
    @Column(name = "fans")
    private int fans;

    /**
     * Number of games this team has won.
     * @since 0.3
     */
    @Column(name = "wins")
    private int wins;

    /**
     * Number of games this team has played.
     * @since 0.3
     */
    @Column(name = "games")
    private int gamesPlayed;

    /**
     * Team's colors. An array of strings that match color values in styles\color.css.
     * @since 1.2.0
     */
    @Column(name = "colors")
    private String[] colors;

    /* RELATIONAL VARIABLES */

    /**
     * League this team is part of.
     * Null if team is not in a league.
     * @since 0.3
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "LID")
    private League league;

    /**
     * List of fighters on this team.
     * @since 0.3
     */
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Fighter> fighterList;

    /**
     * Team's player.
     * @since 1.0
     */
    @OneToOne(fetch = FetchType.EAGER)
    private Player player;

    /**
     * List of games this team plays.
     * Creates a new table called schedule.
     * @since 1.0
     */
    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinTable (
        name = "schedule",
        joinColumns = {@JoinColumn(name = "TID")},
        inverseJoinColumns = {@JoinColumn(name = "GID")} )
    private List<Game> schedule = new ArrayList<>();

    /* CONSTRUCTORS */

    /**
     * Constructs a team from a csv file.
     * @param line String array
     * @since 0.3
     */
    public Team(String[] line) {
        setName(line[0]);
        setFans(Integer.parseInt(line[1]));
        setLocation(line[2]);
        if (line.length <= 3) {
            setColors("black", "black");
        } else if (line.length == 4) {
            setColors(line[3], line[3]);
        } else {
            setColors(line[3], line[4]);
        }
        wins = 0;
        gamesPlayed = 0;
        fighterList = new ArrayList<>();
    }
    /**
     * Constructs an empty team with the specified name.
     * @param name Team name
     * @since 1.0
     */
    public Team(String name) {
        this.name = name;
        setFans(0);
        setLocation("");
        wins = 0;
        gamesPlayed = 0;
        fighterList = new ArrayList<>();
    }
    /**
     * Constructs a team either using autogen or default
     * @param autogen true: use autogen values, false: use default values
     * @since 1.0
     */
    public Team(boolean autogen) {
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
            setName("default");
            setLocation("");
            setFans(0);
            wins = 0;
            gamesPlayed = 0;
            Player auto = new Player(true);
            addPlayer(auto);
        }
    }
    /**
     * Constructs a bye team with specified number of fans.
     * Bye teams should have a negative number of fans.
     * @param fans Should be negative
     * @since 1.1.1
     */
    public Team(int fans) {
        setName("Bye");
        setLocation("Bye");
        setFans(fans);
        wins = 0;
        gamesPlayed = 100;
        fighterList = new ArrayList<>(1);
        //addFighter(new Fighter(false));
    }
    /**
     * Copy constructor
     * @param t Original team
     * @since 1.1.1
     */
    public Team (Team t) {
        setName(t.getName());
        setFans(t.getFans());
        setLocation(t.location);
        wins = t.getWins();
        gamesPlayed = t.getGamesPlayed();
        fighterList = new ArrayList<>(t.getFighterList());
        schedule = new ArrayList<>(t.getSchedule());
    }
    /**
     * Copies a team, but gives it a new name.
     * @param t Original team
     * @param newName New name
     * @since 1.1.1
     */
    public Team (Team t, String newName) {
        setName(newName);
        setFans(t.getFans());
        setLocation(t.location);
        wins = t.getWins();
        gamesPlayed = t.getGamesPlayed();
        fighterList = new ArrayList<>(t.getFighterList());
        schedule = new ArrayList<>(t.getSchedule());
    }
    /**
     * Empty constructor used by Hibernate.
     * @since 0.3
     */
    public Team() {}

    /* GET METHODS */

    /**
     * Gets team ID
     * @return TID
     * @since 0.3
     */
    public int TID() {return TID;}

    /**
     * {@inheritDoc}
     */
    public String getID() {return "Team" + TID;}
    /**
     * Gets team name.
     * @return name
     * @since 0.3
     */
    public String getName() {return name;}
    /**
     * Gets team location.
     * @return location
     * @since 0.3
     */
    public String getLocation() {return location;}
    /**
     * Gets number of fans for this team.
     * @return fans
     * @since 0.3
     */
    public int getFans() {return fans;}
    /**
     * Gets team wins.
     * @return wins
     * @since 0.3
     */
    public int getWins() {return wins;}
    /**
     * Gets games played by this team.
     * @return gamesPlayed
     * @since 0.3
     */
    public int getGamesPlayed() {return gamesPlayed;}

    /**
     * Gets the team's colors.
     * @return
     */
    public String[] getColors() {return colors;}

    /**
     * Gets one color of the team.
     * @param i index of colors array - 0 for primary, 1 for secondary
     * @return
     */
    public String getColor(int i) {return colors[i];}

    /**
     * Gets this team's rank in the league standings.
     * @return Index in standings + 1
     * @since 1.1.0
     */
    public int getRanking() {return repo.getStandings(league).indexOf(this) + 1;}
    /**
     * @param round Game to check during this round
     * @return True if opponent is a bye team, false if not
     * @since 1.1.0
     */
    public boolean onBye(int round) {
        write(getID()+".onBye", round);
        write("",getGame(round).getID());
        return getOpponent(round).isBye();
    }

    /**
     * Gets list of fighters currently on the team.
     * @return fighterList
     * @since 1.0
     */
    public List<Fighter> getFighterList() {return fighterList;}
    /**
     * Gets a fighter from this team's fighter list at the specified index.
     * @param index
     * @return Fighter at index
     * @since 1.1.0
     */
    public Fighter getFighter(int index) {return fighterList.get(index);}

    /**
     * Gets number of fighters currently on the team.
     * @return size of fighterList
     * @since 1.0
     */
    public int getFighterListSize() {return fighterList.size();}
    /**
     * Gets team's schedule.
     * @return schedule
     * @since 1.1.0
     */
    public List<Game> getSchedule() {return schedule;}
    /**
     * Gets league this team is part of.
     * @return league, or null if team is not in a league
     * @since 0.3.1
     */
    public League getLeague() {return league;}
    /**
     * Gets this team's player.
     * @return player
     * @since 1.0
     */
    public Player getPlayer() {return player;}
    /**
     * Gets the game this team plays in during the specified round.
     * If in a tournament, returns the last game scheduled.
     * @param round
     * @return Game in schedule from this round
     * @since 1.0
     * @version 2
     */
    public Game getGame(int round) {
        write(getID()+".getGame", round);
        if (round < 100) {
            return schedule.get(round);
        } else {
            return schedule.get(schedule.size() - 1);
        }
    }
    /**
     * Gets opposing team from the specified round's game.
     * @param round
     * @return Opponent for Game in schedule from this round.
     * @since 1.0
     */
    public Team getOpponent(int round) {
        write(getID()+".getOpponent", round);
        return getGame(round).getOpponent(this);
    } 
    /**
     * Gets opposing team from the specified game.
     * @param game
     * @return Opponent for this game
     * @since 1.0
     */
    public Team getOpponent(Game g) {
        write(getID()+".getOpponent", g.getID());
        return g.getOpponent(this);
    }
    
    /**
     * Gets team losses.
     * @return gamesPlayed - wins
     * @since 0.3
     */
    public int getLosses() {return gamesPlayed - wins;}
    /**
     * Gets winning percentage, which is wins / games.
     * @return win percentage as a decimal between [0, 1].
     * @since 0.3
     */
    public double winPct() {return ((double)wins / (double)gamesPlayed);}
    /**
     * @return The difference between wins and losses.
     * @since 1.0
     */
    public int winDiff() {return wins - getLosses();}
    /**
     * +100 for win, +1 for each score
     * @param contenders teams in league with same number of wins
     * @return long int of tiebreaker points
     * @since 1.1.1
     */
    public int getTieBreaker(List<Team> contenders) {
        int points = 0;
        for (Game g : schedule) {
            if (g.isFinished() && contenders.contains(g.getOpponent(this))) {
                //System.out.println(g.getGameString());
                if (g.getWinner() == this) points += 100;
                points += g.getScore(this);
            }
        }
        return points;
    }

    /* SET METHODS */

    /**
     * Sets team name.
     * @param name new name
     * @since 0.3
     */
    public void setName(String name) {this.name = name;}
    /**
     * Sets team location.
     * @param loc new location
     * @since 0.3
     */
    public void setLocation(String loc) {this.location = loc;}
    /**
     * Sets number of fans to a new amount.
     * @param fans new amount of fans
     * @since 0.3
     */
    public void setFans(int fans) {this.fans = fans;}
    /**
     * Adds/subtracts a number of fans from the current amount.
     * If fans would fall below zero, fans is set to 0.
     * @param change how many fans will be added
     * (or subtracted if negative)
     * @since 0.3
     */
    public void changeFans(int change) {
        this.fans += change;
        if (fans < 0) fans = 0;
    }
    /**
     * Adds a win and game played.
     * @since 0.3
     */
    public void addWin() {this.gamesPlayed += 1; this.wins += 1; }
    /**
     * Adds a game played but not a win, which is equivalent to a loss.
     * @since 0.3
     */
    public void addLoss() {this.gamesPlayed += 1;}

    /**
     * Sets the team's colors. If colors have not been set, intializes an array of size 2.
     * @param primary
     * @param secondary
     * @since 1.2.0
     */
    public void setColors(String primary, String secondary) {
        if (colors == null) colors = new String[2];
        colors[0] = primary;
        colors[1] = secondary;
    }

    /**
     * Sets one color
     * @param i index - 0 for primary, 1 for secondary
     * @param color new color
     * @since 1.2.0
     */
    public void setColor(int i, String color) {
        colors[i] = color;
    }

    /**
     * Adds a fighter to this team.
     * @param f new fighter
     * @since 0.3.1
     */
    public void addFighter(Fighter f) {fighterList.add(f); f.setTeam(this);}
    /**
     * Removes a fighter from this team.
     * @param f fighter to remove
     * @since 0.3.1
     */
    public void removeFighter(Fighter f) {fighterList.remove(f); f.setTeam(null);}
    /**
     * Adds a player for the team.
     * @param player new player
     * @since 1.0
     */
    public void addPlayer(Player player) {this.player = player; player.setTeam(this);}
    /**
     * Removes the player from the team.
     * @since 1.0
     */
    public void removePlayer() {this.player = null;}
    /**
     * Initializes an empty schedule with capacity of the number of games
     * @param games Number of games to be played in schedule
     * @since 1.0
     */
    public void setNewSchedule(int games) {
        write(getID()+".setNewSchedule", games);
        schedule = new ArrayList<>(games);
    }
    /**
     * Adds a game to the next available round in the schedule.
     * Schedule list will resize if needed.
     * @param g Game to be added.
     * @since 1.0
     */
    public void addGame(Game g) {schedule.add(g);}
    /**
     * Sets the order that fighters will be played in.
     * @since 1.0
     */
    public void setOrder() {player.setOrder(getFighterList());}
    /**
     * Sets this team's league.
     * This is a dependent method, so it should only be called by a League class.
     * @param lg new league
     * @since 0.3
     */
    public void setLeague(League lg) {this.league = lg;}

    //boolean methods

    /**
     * @return true if this is a user team, false if this is a CPU team
     * @since 1.1.0
     */
    public boolean isUserTeam() {
        return player.getStrategy() == 99;
    }
    /**
     * @return True if this is a bye team, false if not
     * @since 1.1.1
     */
    public boolean isBye() {
        //write(getTID()+".isBye");
        return fans < 0;
    }

    //inherited methods

    /**
     * @return A new team with autogen values.
     * @since 1.0
     */
    public Team autogen() {
        Team auto = new Team(true);
        return auto;
    }

    //display methods
    
    /**
     * Prints to console one fighter per line.
     * @since 1.0
     */
    public void print_fighters() {
        System.out.println("\n" + name);
        for (Fighter f : getFighterList()) {
            f.print_oneLine();
        }
    }

}
