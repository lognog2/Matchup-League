package com.entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * A league is an Entity class that represents a league.
 * A league consists of a set of teams, which play each other.
 * @since 0.3.1
 */
@Entity
@Table(name = "Leagues")
public class League extends DataEntity
{
    /**
     * League ID, uniquely identifies a league.
     * @since 1.0
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int LID;
    
    /**
     * League's name
     * @since 0.3
     */
    @Column(name = "name")
    private String name;

    /**
     * List of teams in this league.
     * @since 0.3
     */
    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Team> teamList;

    /**
     * League's tier
     * @since 0.3
     */
    @Column(name = "tier", nullable = false)
    private char tier;

    /* CONSTRUCTORS */

    /**
     * Constructs a league from a csv file
     * @param line String array
     * @since 0.3
     */
    public League(String[] line) {
        setName(line[0]);
        setTier(line[1].charAt(0));
        teamList = new ArrayList<>();
    }
    /**
     * Constructs a league from just a name, and is 
     * placed in lowest real tier, which is currently C.
     * @param name name of league
     * @since 0.3
     */
    public League(String name) {
        setName(name);
        setTier('C');
        teamList = new ArrayList<>();
    }
    /**
     * Generate a league in the lowest possible tier (Z) with an auto-generated name.
     * true: use random values, false: use default values
     * @param autogen true: use random values, false: use default values
     * @since 1.0
     */
    public League(boolean autogen) {
        teamList = new ArrayList<>();
        if (autogen) {
            setName(randomName());
            setTier('Z');
        } else {
            setName("League");
            setTier('Z');
        }
    }
    /**
     * Empty constructor, used by Hibernate
     * @since 0.3
     */
     public League(){}

    /* GET METHODS */

    /**
     * Gets League ID.
     * @return LID
     * @since 1.0
     */
    public int LID() {return LID;}

    /**
     * {@inheritDoc}
     */
    public String getID() {return "league" + LID;}

    /**
     * Gets league name.
     * @return name
     * @since 0.3
     */
    public String getName() {return name;}
    /**
     * Gets list of teams in this league.
     * @return teamList
     * @since 0.3
     */
    public List<Team> getTeamList() {return teamList;}
    /**
     * Gets team at specified index.
     * @param index
     * @return Team
     */
    public Team getTeam(int index) {return teamList.get(index);}
    public int getTeamListSize() {return teamList.size();}
    /**
     * Gets league's tier.
     * @return tier
     * @since 0.3
     */
    public char getTier() {return tier;}
    /**
     * Gets the number of games each team in the league plays.
     * Since each team plays every other team once, this is equvalent
     * to one less than the number of teams. 
     * @return Team list size - 1
     * @since 1.1.0
     */
    public int getGameAmt() {return teamList.size() - 1;}

    /* SET METHODS */

    /**
     * Sets league name
     * @param name new name
     * @since 0.3
     */
    public void setName(String name) {this.name = name;}
    /** 
     * Sets league tier
     * @param tier new tier
     * @since 0.3
     */
    public void setTier(char tier) {this.tier = tier;}
    /**
     * Adds a team to the league, and sets team's league to this
     * @param t new team
     * @since 0.3
     */
    public void addTeam(Team t) {teamList.add(t); t.setLeague(this);}
    /**
     * Removes a team from the league, and sets team's league to null.
     * @param t removed team
     * @since 0.3
     */
    public void removeTeam(Team t) {teamList.remove(t); t.setLeague(null);}

    /* UTILITY METHODS */

    /**
     * Orders teams in this league first by wins, then by tiebreaker points, then by fans
     * @return the ordered list of teams
     * @since 1.1.1
     * @see Team#getTieBreaker
     */
    public List<Team> rankTeams() {
        teamList.sort(Comparator.comparing(Team::winPct).reversed());
        Team best = teamList.get(0);
        int bestWins = best.getWins();
        if (bestWins == 0) return teamList;

        List<Team> contenders = new ArrayList<>(teamList.size());
        for (Team t : teamList) {
            if (t.getWins() == bestWins) {
                contenders.add(t);
            } else break;
        }
        //sort contenders by tiebreak points, then by fans
        contenders.sort(Comparator.comparing((Team team) -> team.getTieBreaker(contenders))
            .thenComparing(Team::getFans).reversed());

        /*for (Team t : contenders) {
            System.out.println(t.getName() + " " + t.getTieBreaker(contenders));
        }*/
        
        //add rest of teams to contenders then replace team list with it
        contenders.addAll(teamList.subList(contenders.size(), teamList.size()));
        teamList = contenders;
        return teamList;
    }

    /**
     * Gets highest ranked team from {@link League#rankTeams()}
     * @since 1.1.1
     */
    public Team bestTeam() {
        return rankTeams().get(0);
    }

    //inherited methods

    /**
     * Auto-generates a league
     * @return An auto-generated league
     * @since 1.0
     */
    public League autogen() {
        return new League(true);
    }

    //display methods

    /**
     * Prints the teams in this league in rank order
     * @since 1.0
     * @see League#rankTeams()
     */
    public void print_standings() {
        teamList = rankTeams();
        System.out.println("\n" + name);
        int rank = 1;
        for (Team t : teamList) {
            if (t.getFans() != -1)
                System.out.println(rank++ + ". " + t.getName() + " " + t.getWins() + "-" + t.getLosses());
        }
    }
}
