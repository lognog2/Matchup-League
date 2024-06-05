package com.Entities;

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

    @Column(name = "strategy")
    private int strategy;

    @OneToOne (mappedBy = "player", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Team team;

    public Player(String name)
    {
        setName(name);
        setStrategy((int)(Math.random() * CPU_STYLES()));
    }
    public Player(String name, int style)
    {
        setName(name);
        setStrategy(style);
    }
    public Player(String[] line)
    {
        setName(line[0]);
        if (line.length <= 1)
            setStrategy((int)(Math.random() * CPU_STYLES()));
        else
            setStrategy(Integer.parseInt(line[1]));
    }
    public Player(boolean autogen)
    {
        if (autogen)
        {
            setName(randomName());
            setStrategy((int)(Math.random() * CPU_STYLES()));
        }
        else
        {
            setName("Bye");
            setStrategy(-1);
        }

    }
    public Player(){}

    //get methods
    public int CPU_STYLES() {return 4;} //number of cpu styles, final int workaround
    public String getName() {return name;}
    public int getStrategy() {return strategy;}
    public Team getTeam() {return team;}

    //derived get methods
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
    public void setName(String name) 
    {
        while (entityRepo.playerNameExists(name))
            name += ".";
        this.name = name;
    }
    public void setStrategy(int strategy) 
    {
        if (strategy >= CPU_STYLES() && strategy < 99)
            strategy = 0;
        this.strategy = strategy;
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
        if (strategy == 0)
            Collections.shuffle(fighters);
        else if (strategy >= 1 && strategy <= 3)
        {
            fighters = entityRepo.getFighters_byTeam(team);
            if (strategy == 2)
                Collections.reverse(fighters);
            else if (strategy == 3)
                fighters.add(fighters.size()-1, fighters.remove(0));
        }
    }
    public int getChoice(List<Fighter> fighters)
    {
        if (fighters.size() == 1) return 0;
        else if (strategy == 0) return (int)(Math.random() * fighters.size());
        else if (strategy == 1) return fighters.indexOf(bestFighter(fighters));
        else if (strategy == 2) return fighters.indexOf(worstFighter(fighters));
        else if (strategy == 3) return fighters.indexOf(secondBestFighter(fighters));
        else return 0;
    }
    private Fighter bestFighter (List<Fighter> fighters)
    {
        Fighter best = fighters.get(0);
        for (Fighter f : fighters)
        {
            if (f.getBase() > best.getBase())
                best = f;
            else if (f.getBase() == best.getBase())
            {
                if (f.modDiff() >= best.modDiff())
                    best = f;
            }
        }
        return best;
    }
    private Fighter worstFighter (List<Fighter> fighters)
    {
        Fighter worst = fighters.get(0);
        for (Fighter f : fighters)
        {
            if (f.getBase() < worst.getBase())
                worst = f;
            else if (f.getBase() == worst.getBase())
            {
                if (f.modDiff() <= worst.modDiff())
                    worst = f;
            }
        }
        return worst;
    }
    private Fighter secondBestFighter(List<Fighter> fighters)
    {
        Fighter best = bestFighter(fighters);
        fighters.remove(best);
        Fighter second = bestFighter(fighters);
        fighters.add(best);
        return second;
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
