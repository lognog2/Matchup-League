package com.entities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
    private Strategy strategy;

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
     * @version 2
     */
    public Strategy getStrategy() {return strategy;}

    /**
     * Gets strategy ID of the player.
     * @return player strategy
     * @since 1.2.0
     */
    public int getStrategyID() {return strategy.getID();}

    /**
     * Gets player's team.
     * @return player team
     * @since 1.0
     */
    public Team getTeam() {return team;}

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
     * If new strategy number is not recognized, reverts to random
     * @param strategy new strategy
     * @since 1.0
     */
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
    /**
     * Sets player strategy.
     * If new strategy number is not recognized, reverts to 0
     * @param strategy new strategy
     * @since 1.0
     */
    public void setStrategy(int id) {
        this.strategy = strategy.getStrategy(id);
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
     * Gets index of next choice of fighter to play according to
     * strategy.
     * @param fighters Available fighters on team. They should be in descending order by base power.
     * @return index of fighter choice
     * @since 1.1.0
     * @version 2
     */
    public int getChoice(List<Fighter> fighters) {
        return strategy.getChoice(fighters.size());
    }

    /**
     * Enum containing all possible strategies a player can have.
     * Strategies contain an int ID and an implementation of Selection.
     * @since 1.2.0
     */
    public enum Strategy {
        RANDOM(0, (size) -> {return (int)(Math.random() * size);}),
        BEST(1, (size) -> {return 0;}),
        WORST(2, (size) -> {return size - 1;}),
        SECONDBEST(3, (size) -> {return (size <= 1 ? 0 : 1);}),
        USER(99, null);

        private int ID;
        private Selection choice;
        
        Strategy(int ID, Selection choice) {
            this.ID = ID;
            this.choice = choice;
        }

        public int getID() {
            return ID;
        }
        public int getChoice(int size) {
            return choice.select(size);
        }

        public Strategy getStrategy(int ID) {
            return (ID < values().length - 1) ? values()[ID] : USER;
        }
    } //end Strategy enum
    @FunctionalInterface
    interface Selection {
        int select(int size);
    } //end Selection interface
} //end Player class
