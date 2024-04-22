package matchup.Entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Leagues")
public class League 
{
    @Id
    @Column(name = "name")
    private String leagueName;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Team> teams = new ArrayList<>();

    @Column(name = "tier", nullable = false)
    private char tier;

    public League(String[] line)
    {
        setName(line[0]);
        setTier(line[1].charAt(0));
    }
    public League(String name)
    {
        setName(name);
        setTier('D');
    }
    protected League(){}

    //get methods
    public String name() {return leagueName;}
    public List<Team> teams() {return teams;}
    public char tier() {return tier;}
    public int size() {return teams.size();}

    //set methods
    public void setName(String name) {this.leagueName = name;}
    public void setTier(char tier) {this.tier = tier;}
    public void addTeam(Team t) {teams.add(t); t.setLeague(this);}
    public void removeTeam(Team t) {teams.remove(t); t.setLeague(null);}

    //utility methods
}
