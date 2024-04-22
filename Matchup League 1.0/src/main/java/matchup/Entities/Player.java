package matchup.Entities;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table (name = "Players")
public class Player extends Entities
{
    
    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "style")
    private int style;

    @OneToOne (mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Team team;

    public Player(String name)
    {
        setName(name);
        setStyle((int)(Math.random() * CPU_STYLES()));
    }
    public Player(String name, int style)
    {
        setName(name);
        setStyle(style);
    }
    public Player(String[] line)
    {
        setName(line[0]);
        if (line.length <= 1)
            setStyle((int)(Math.random() * CPU_STYLES()));
        else
            setStyle(Integer.parseInt(line[1]));
    }
    public Player(boolean autogen)
    {
        if (!autogen)
        {
            setName("Bye");
            setStyle(-1);
        }
    }
    protected Player(){}

    //get methods
    public int CPU_STYLES() {return 4;} //number of cpu styles, final int workaround
    public String name() {return name;}
    public int style() {return style;}
    public Team team() {return team;}

    //derived get methods
    public String styleName()
    {
        switch(style){
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
    public void setName(String name) 
    {
        while (playerNameExists(name))
            name += ".";
        this.name = name;
    }
    public void setStyle(int style) 
    {
        if (style >= CPU_STYLES() && style < 99)
            style = 0;
        this.style = style;
    }
    public void setTeam(Team team) {this.team = team;}

    //inherited methods
    public Player autogen()
    {
        Player auto = new Player(UUID.randomUUID().toString().substring(0, 8));
        return auto;
    }

    //utility methods
    public void setOrder(List<Fighter> fighters)
    {
        if (style == 0)
            Collections.shuffle(fighters);
        else if (style >= 1 && style <= 3)
        {
            fighters = fDAO.orderByDesc("team.name = '" + team.name() + "'", "base");
            if (style == 2)
                Collections.reverse(fighters);
            else if (style == 3)
                fighters.add(fighters.size()-1, fighters.remove(0));
        }
    }
}
