package com.Entities;

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
    private List<Team> teamList;

    @Column(name = "tier", nullable = false)
    private char tier;

    public League(String[] line)
    {
        setName(line[0]);
        setTier(line[1].charAt(0));
        teamList = new ArrayList<>();
    }
    public League(String name)
    {
        setName(name);
        setTier('C');
        teamList = new ArrayList<>();
    }
    public League(boolean autogen)
    {
        teamList = new ArrayList<>();
        if (autogen)
        {
            setName(randomName());
            setTier('Z');
        }
    }
    public League(){}

    //get methods
    public int getLID() {return LID;}
    public String getName() {return name;}
    public List<Team> getTeamList() {return teamList;}
    public Team getTeam(int index) {return teamList.get(index);}
    public int getTeamListSize() {return teamList.size();}
    public char getTier() {return tier;}
    public int getGameAmt() {return teamList.size() - 1;}

    //set methods
    public void setName(String name) {this.name = name;}
    public void setTier(char tier) {this.tier = tier;}
    public void addTeam(Team t) {teamList.add(t); t.setLeague(this);}
    public void removeTeam(Team t) {teamList.remove(t); t.setLeague(null);}

    //utility methods
    public Team tiebreaker()
    {
        List<Team> contenders = entityRepo.getTiedTeams(this);
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
        teamList = entityRepo.getStandings(this);
        System.out.println("\n" + name);
        int rank = 1;
        for (Team t : teamList)
        {
            if (t.getFans() != -1)
                System.out.println(rank++ + ". " + t.getName() + " " + t.getWins() + "-" + t.getLosses());
        }
    }
    public void print_teamList()
    {
        System.out.println(name);
        for (Team t : teamList)
        {
            System.out.println(t.getName());
        }
    }
}
