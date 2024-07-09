package com.Entities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;

/**
 * Player is an Entity class that represents a player, which is where
 * the decisions for what fighters to draft and which order to play them
 * in a game.
 * @since 1.0
 */
@Entity
@Table (name = "Players")
public class Player extends DataEntity
{
    /**
     * Name of the player. CPU players should end with "(CPU)".
     * Primary Key
     * @since 1.0
     */
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Player's strategy. Called 'style' in older versions.
     * Strategy determines what order to play fighters and draft range.
     * 99 denotes user.
     * @since 1.0
     */
    @Column(name = "strategy")
    private int strategy;

    /**
     * Team this player makes decisions for.
     * @since 1.0
     */
    @OneToOne (mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Team team;

    /**
     * Constructs a CPU player with specified name.
     * @param name Player name
     */
    public Player(String name) {
        setName(name);
        setStrategy((int)(Math.random() * CPU_STYLES()));
    }
    /**
     * Constructs a user or CPU player with specified name and strategy.
     * Users have a strategy of 99.
     * @param name Name of player
     * @param strategy Strategy of player
     * @since 1.0
     */
    public Player(String name, int strategy) {
        setName(name);
        setStrategy(strategy);
    }
    /**
     * Required by Hibernate
     * @param line String array
     * @since 1.0
     */
    public Player(String[] line) {
        setName(line[0]);
        if (line.length <= 1)
            setStrategy((int)(Math.random() * CPU_STYLES()));
        else
            setStrategy(Integer.parseInt(line[1]));
    }
    /**
     * Constructs autogen player or a bye player, denoted by strategy of -1.
     * @param autogen true: use random values, false: create bye player
     * @since 1.0
     */
    public Player(boolean autogen) {
        if (autogen) {
            setName(randomName());
            setStrategy((int)(Math.random() * CPU_STYLES()));
        }
        else {
            setName("Bye");
            setStrategy(-1);
        }
    }
    /**
     * Empty constructor required by Hibernate
     * @since 1.0
     */
    public Player(){}

    //get methods

    /**
     * Total number of possible CPU strategies
     * @return 4
     * @since 1.0
     */
    public int CPU_STYLES() {return 4;}
    
    /**
     * Gets name of the player.
     * @return player name
     * @since 1.0
     */
    public String getName() {return name;}

    /**
     * {@inheritDoc}
     * <p>Since player does not have a generated ID, just uses player name.
     * <p> Example: player Maxwell
     */
    public String getID() {return "player " + name;}

    /**
     * Gets strategy of the player.
     * @return player strategy
     * @since 1.0
     */
    public int getStrategy() {return strategy;}
    /**
     * Gets player's team.
     * @return player team
     * @since 1.0
     */
    public Team getTeam() {return team;}

    /**
     * @return String name of player's strategy.
     * @since 1.0
     */
    public String styleName()
    {
        switch(strategy){
            case 0: return "random";
            case 1: return "best to worst";
            case 2: return "worst to best";
            case 3: return "best to worst, best last";
            case 4: return "smart";
            case 99: return "user";
            default: return "N/A";
        }
    }

    //set methods

    /**
     * Sets player name. If it already exists, adds periods to the end
     * until it's a unique name.
     * @param name new name
     * @since 1.0
     */
    public void setName(String name) 
    {
        while (repo.playerNameExists(name))
            name += ".";
        this.name = name;
    }
    /**
     * Sets player strategy.
     * If new strategy number is not recognized, reverts to 0
     * @param strategy new strategy
     * @since 1.0
     */
    public void setStrategy(int strategy) 
    {
        if (strategy >= CPU_STYLES() && strategy < 99)
            strategy = 0;
        this.strategy = strategy;
    }
    /**
     * Sets player team.
     * @param team New team
     * @since 1.0
     */
    public void setTeam(Team team) {this.team = team;}

    //inherited methods

    /**
     * Generates player with random name and strategy.
     * @return autogen player
     * @since 1.0
     */
    public Player autogen() {
        Player auto = new Player(UUID.randomUUID().toString().substring(0, 8));
        return auto;
    }

    //utility methods

    /**
     * Arranges fighters in order according to strategy
     * @param fighters Fighters on team
     * @since 1.0
     * @version 3
     */
    public void setOrder(List<Fighter> fighters) {
        if (strategy == 0)
            Collections.shuffle(fighters);
        else if (strategy >= 1 && strategy <= 3) {
            fighters.sort(Comparator.comparing(Fighter::getBase));
            if (strategy == 2)
                Collections.reverse(fighters);
            else if (strategy == 3)
                fighters.add(fighters.size()-1, fighters.remove(0));
        }
    }
    /**
     * Gets index of next choice of fighter to play according to
     * strategy.
     * @param fighters Available fighters on team
     * @return index of fighter choice
     * @since 1.1.0
     */
    public int getChoice(List<Fighter> fighters) {
        if (fighters.size() == 1) return 0;
        else if (strategy == 0) return (int)(Math.random() * fighters.size());
        else if (strategy == 1) return fighters.indexOf(bestFighter(fighters));
        else if (strategy == 2) return fighters.indexOf(worstFighter(fighters));
        else if (strategy == 3) return fighters.indexOf(secondBestFighter(fighters));
        else return 0;
    }
    /**
     * Gets the fighter with highest base from a list of fighters.
     * @param fighters List of fighters
     * @return fighter with highest base
     * @since 1.1.0
     * @version 2
     */
    private Fighter bestFighter (List<Fighter> fighters) {
        return fighters.get(0);
    }
    /**
     * Gets fighter with the lowest base power from a list of fighters. 
     * @param fighters Fighter list
     * @param trimExtras When true, disregards the worst (FPT - FPG) fighters
     * @return Fighter with lowest base
     * @since 1.1.0
     * @version 3
     */
    private Fighter worstFighter (List<Fighter> fighters) {
        int index = fighters.size() - manager.spareFighters() - 1;
        //write(fighters.size() + " - " + manager.spareFighters() + " - 1 = " + index);
        return fighters.get(index);
    }
    /**
     * Gets the fighter with the second highest base from a list of fighters.
     * If this is the last fighter to be played in the game, returns bestFighter instead.
     * @param fighters List of fighters
     * @return Fighter with second highest base
     * @since 1.1.0
     * @version 2
     */
    private Fighter secondBestFighter(List<Fighter> fighters) {
        if (fighters.size() == (1 + (manager.spareFighters())))
            return bestFighter(fighters);
        else 
            return fighters.get(1);
    }

    /*private Fighter smartFighter(List<Fighter> fighters)
    {
        List<Fighter> candidates = new ArrayList<>();
     
        for (Fighter f : fighters)
        {

        }

        return worstFighter(candidates);
    }*/
}
