package com.Entities;

import jakarta.persistence.*;
import java.util.List;

import com.menu.App;

import java.util.ArrayList;

/**
 * Game is an Entity class that represents a single game.
 * @since 1.0
 */
@Entity
@Table(name = "Games")
public class Game extends DataEntity
{
    /**
     * Game ID, uniquely identifies a game.
     * @since 1.0
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int GID;
    /**
     * Round this game is in.
     * @since 1.0
     */
    @Column(name = "round")
    private int round;
    /**
     * Name of the game winner, or "Unfinished" if game hasn't been played yet.
     * @since 1.0
     */
    @Column(name = "result")
    private String result;
    /**
     * Score of team1
     * @since 1.1.1
     */
    @Column(name = "score1")
    private int score1;
    /**
     * Score of team2
     * @since 1.1.1
     */
    @Column(name = "score2")
    private int score2;
    /**
     * List of teams playing in this game. The capacity is always 2.
     * @since 1.0
     */
    @ManyToMany(mappedBy = "schedule")
    private List<Team> teams;

    /**
     * Constructs a game from a String array.
     * Not used but required by Hibernate.
     * @param line String array
     * @since 1.0
     */
    public Game(String[] line)
    {
        setRound(Integer.parseInt(line[0]));
        teams.add(repo.getTeam_byName(line[1]));
        teams.add(repo.getTeam_byName(line[2]));
        setResult("Unfinished");
        setScore(0,0);
    }
    /**
     * Standard way of constructing a game.
     * Initialized with scores at 0 and result as "Unfinished".
     * @param round round this game is part of
     * @param team1 team 1
     * @param team2 team 2
     * @since 1.0
     * @version 2
     */
    public Game(int round, Team team1, Team team2)
    {
        ///write("new Game", round, team1.getID(), team2.getID());
        setRound(round);
        setResult("Unfinished");
        setScore(0,0);
        teams = new ArrayList<>(2);
        teams.add(team1);
        teams.add(team2);
        team1.addGame(this);
        team2.addGame(this);
    }
    /**
     * Blank constructor used by Hibernate
     * @since 1.0
     */
    public Game(){}

    //get methods

    /**
     * Gets Game ID.
     * @return GID
     * @since 1.0
     */
    public int GID() {return GID;}

    /**
     * {@inheritDoc}
     */
    public String getID() {return "game" + GID;}

    /**
     * Gets game round.
     * @return round
     * @since 1.0
     */
    public int getRound() {return round;}
    /**
     * Gets game result.
     * @return result
     * @since 1.0
     */
    public String getResult() {return result;}
    /**
     * Gets score at the specified index.
     * @param index 0 or 1
     * @return score1 for index 0, score2 for index 1
     */
    public int getScore(int index) {return index == 0 ? score1 : score2;}
    /**
     * Gets the score for the specified team.
     * @param t Team to get score of
     * @return score1 or score2, depending on if t is team1 or team2
     * @since 1.1.1
     */
    public int getScore(Team t) {return teams.get(0) == t ? score1 : score2;}
    /**
     * Gets both teams in the game.
     * @return List of teams
     * @since 1.1.0
     */
    public List<Team> getTeams() {return teams;}
    /**
     * Gets team at specified index.
     * @param index 0 or 1
     * @return team at index
     * @since 1.1.0
     */
    public Team getTeam(int index) {return teams.get(index);}
    /**
     * Gets name of the team at the specified index.
     * @param index 0 or 1
     * @return Name of team at index
     * @since 1.1.0
     */
    public String getTeamName(int index) {return teams.get(index).getName();}
    /**
     * Gets the opponent of a team in this game.
     * @param t team
     * @return other team in game
     * @since 1.0
     */
    public Team getOpponent(Team t)
    {
        if (teams.get(0) == t)
            return teams.get(1);
        else if (teams.get(1) == t)
            return teams.get(0);
        else
            return null;
    }
    /**
     * Gets the Team that won the game.
     * If game is unfinished, returns null
     * @return Winning team or null
     * @since 1.1.1
     */
    public Team getWinner() {
        if (result.equals(getTeamName(0))) {
            return teams.get(0);
        } else if (result.equals(getTeamName(1))) {
            return teams.get(1);
        } else return null;
    }
    /**
     * Gets the Team that lost the game.
     * If game is unfinished, returns null
     * @return Losing team or null
     * @since 1.1.1
     */
    public Team getLoser() {
        if (result.equals(getTeamName(0))) {
            return teams.get(1);
        } else if (result.equals(getTeamName(1))) {
            return teams.get(0);
        } else return null;
    }
    /**
     * Gets the index of the winning team.
     * Will cause NullPointerException if game is unfinished.
     * @return 0 if team1 won; 1 if team2 won
     * @since 1.1.1
     */
    public int getWinnerIndex() {
        return teams.indexOf(getWinner());
    }

    public String getName() {return getGameString();}

    /**
     * Gets game summary as a string, which includes teams and score.
     * @return Game summary
     * @since 1.1.0
     * @version 2
     */
    public String getGameString()
    {
        final String onBye = " on bye";
        if (getTeam(0).isBye()) {
            return getTeamName(1) + onBye;
        } else if (getTeam(1).isBye()) {
            return getTeamName(0) + onBye;
        } else if (result.equals("Unfinished")) {
            return getTeamName(0) + " vs " + getTeamName(1);
        } else return getTeamName(0) + " " + score1 + "-" + score2 + " " + getTeamName(1);
    }
    /**
     * Gets game summary from one team's perspective,
     * which is the team's opponent and score.
     * @param t Team
     * @return One-sided game summary
     * @since 1.1.0
     * @version 2
     */
    public String getGameString(Team t) {
        write(getID()+".getGameString", t.getID());
        int index = teams.indexOf(t);
        String opponent = "vs " + getOpponent(t).getName();
        final String onBye = "On bye";
        if (getOpponent(t).getFans() < 0) {
            return onBye;
        } else if (result.equals("Unfinished")) {
            return opponent;
        } else {
            opponent += result.equals(t.getName()) ? ": Won " : ": Lost ";
            String score = getScore(index) + "-" + getScore(App.flip(index));
            return opponent + score;
        } 
    }

    //set methods

    /**
     * Sets game result.
     * @param result name of winning team
     * @since 1.0
     */
    public void setResult(String result) {this.result = result;}
    /**
     * Sets score for the game
     * @param score1 Score of team1
     * @param score2 Score of team2
     * @since 1.1.1
     */
    public void setScore(int score1, int score2) {this.score1 = score1; this.score2 = score2;}
    /**
     * Sets game round.
     * @param round
     * @since 1.0
     */
    public void setRound(int round) {this.round = round;}

    /**
     * Sees if a user team is playing in this game.
     * @return True if one of the teams is a user team, false if
     * both teams are CPU or if user's opponent is a bye team.
     * @since 1.1.1
     */
    public boolean userPlays() {
        return (teams.get(0).isUserTeam() || teams.get(1).isUserTeam()) && !isByeGame();
    }
    /**
     * @return True if one of the teams is a bye team, false if neither are
     * @since 1.1.1
     */
    public boolean isByeGame() {
        return teams.get(0).isBye() || teams.get(1).isBye();
    }
    /**
     * @return true if game has a result other than "Unfinished"
     * @since 1.1.1
     */
    public boolean isFinished() {
        return !result.equals("Unfinished");
    }

    //inherited methods

    /**
     * Game autogen (not functional)
     * @return null
     * @since 1.0
     */
    public Game autogen() {
        System.out.println("Can't autogen a game yet");
        return null;
    }
}
