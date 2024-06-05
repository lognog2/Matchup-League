package com.Entities;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

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

    @Column(name = "score")
    private String score;

    @ManyToMany(mappedBy = "schedule")
    private List<Team> teams;

    public Game(String[] line)
    {
        setRound(Integer.parseInt(line[0]));
        teams.add(entityRepo.getTeam_byName(line[1]));
        teams.add(entityRepo.getTeam_byName(line[2]));
        setResult("Unfinished");
        setScore("","");
    }
    public Game(int round, Team team1, Team team2)
    {
        setRound(round);
        setResult("Unfinished");
        setScore("","");
        teams = new ArrayList<>(2);
        teams.add(team1);
        teams.add(team2);
        team1.addGame(this);
        team2.addGame(this);
    }
    public Game(){}

    //get methods
    public int getGID() {return GID;}
    public int getRound() {return round;}
    public String getResult() {return result;}
    public String getScore() {return score;}
    public List<Team> getTeams() {return teams;}
    public Team getTeam(int pos) {return teams.get(pos);}

    public String getTeamName(int pos) {return teams.get(pos).getName();}
    public Team getOpponent(Team t)
    {
        if (teams.get(0) == t)
            return teams.get(1);
        else if (teams.get(1) == t)
            return teams.get(0);
        else
            return null;
    }

    public String getGameString()
    {
        final String onBye = " on bye";
        if (teams.get(0).getFans() < 0) {
            return teams.get(1) + onBye;
        } else if (teams.get(1).getFans() < 0) {
            return teams.get(0) + onBye;
        } else if (result.equals("Unfinished")) {
            return getTeamName(0) + " vs " + getTeamName(1);
        } else return getTeamName(0) + " " + score + " " + getTeamName(1);
    }
    public String getGameString(Team t)
    {
        String opponent = "vs " + getOpponent(t).getName();
        final String onBye = "On bye";
        if (getOpponent(t).getFans() < 0) {
            return onBye;
        } else if (result.equals("Unfinished")) {
            return opponent;
        } else {
            opponent += result.equals(t.getName()) ? ": Won " + score : ": Lost " + score;
            return opponent;
        } 
    }

    //set methods
    public void setResult(String result) {this.result = result;}
    public void setScore(String score1, String score2) {this.score = score1 + "-" + score2;}
    public void setRound(int round) {this.round = round;}

    //inherited methods
    public Game autogen() 
    {
        System.out.println("Can't autogen a game yet");
        return null;
    }
}
