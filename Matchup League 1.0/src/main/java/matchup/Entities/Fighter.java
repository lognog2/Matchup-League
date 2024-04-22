package matchup.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Fighters")
public class Fighter extends Entities
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int FID; //fighter ID

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TID")
    private Team team;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "types")
    private char[] types;

    @Column(name = "base")
    private int base;

    @Column(name = "strType")
    private char strType;

    @Column(name = "strVal")
    private int strVal;

    @Column(name = "wkType")
    private char wkType;

    @Column(name = "wkVal")
    private int wkVal;


    //constructors
    public Fighter(String[] line)
    {
        setName(minimize(line[0], MAX_NAME()));
        setTypes(minimize(line[1], MAX_TYPES()).toUpperCase().toCharArray());
        setBase(Integer.parseInt(line[2]));
        setStrength(line[3].toUpperCase().charAt(0), Integer.parseInt(line[4]));
        setWeakness(line[5].toUpperCase().charAt(0), Integer.parseInt(line[6]));
    }
    //true: use random values, false: use default values
    public Fighter(boolean autogen)
    {
        if (autogen) 
        {
            setName(randomName());
            setAutoTypes();
            setBase(10 * ((int)(Math.random() * 51) + 150)); //range: 1500 - 2000
            setStrength(autoType(), 10 * ((int)(Math.random() * 20) + 1)); //range: 10 - 200
            do {
                wkType = autoType();
            } while (wkType == strType);
            setWeakness(wkType, 10 * ((int)(Math.random() * 20) + 1)); //range: 10 - 200
        } else {
            setName("default");
            setTypes(".".toCharArray());
            setBase(1500);
            setStrength('.', 0);
            setWeakness('.', 0);
        }
    }
    protected Fighter() {};

    //get methods
    public int FID() {return FID;}
    public String name() {return name;}
    public char[] types() {return types;}
    public int base() {return base;}
    public char strType() {return strType;}
    public int strVal() {return strVal;}
    public char wkType() {return wkType;}
    public int wkVal() {return wkVal;}
    public Team team() {return team;}
    public int MAX_NAME() {return 24;}
    public int MAX_TYPES() {return 5;}
    public char[] allTypes() 
    {
        char[] allTypes = {'M','R','E','F','W','L','G','C','A','I','V','S'};
        return allTypes;
    }

    //calculated get methods
    public int modDiff()
    {return strVal - wkVal;}

    //set methods
    public void setName(String name) {this.name = name;}
    public void setTypes(char[] types) {this.types = types;}
    public void setBase(int base) {this.base = base;}
    public void setStrength(char type, int val) {this.strType = type; this.strVal = val;}
    public void setWeakness(char type, int val) { this.wkType = type; this.wkVal = val;}

    //auto set methods
    public void setAutoTypes()
    {
        int numTypes = (int)(Math.random() * MAX_TYPES()) + 1;
        char[] types = new char[numTypes];
        for (int i = 0; i < numTypes; i++)
        {
            char newType = autoType();
            if (!hasType(types, newType)) 
                types[i] = newType;
            else i--;    
        }
        setTypes(types);
    }

    //dependent methods
    public void setTeam(Team team) {this.team = team;}

    //inherited methods
    public Fighter autogen()
    {
        Fighter auto = new Fighter(true);
        return auto;
    }

    //utility methods
    private boolean hasType(char[] types, char type) {
        for (char c : types){
            if (c == type)
                return true;
        } return false;
    }
    private char autoType()
    {
        return allTypes()[(int)(Math.random() * allTypes().length)];
    }
    //shortens a string to specified length
    private String minimize(String input, int max) 
    {
        return input.length() > max ? input.substring(0, max) : input;
    }

    //executes match between two fighters, returns name of winner or "no contest"
    public static String match(Fighter away, Fighter home, boolean print)
    {
        int awayPower = away.base();
        int homePower = home.base();

        //if a strength/weakness is applied, it will be stored in this bool array
        boolean[] mods = new boolean[]{false, false, false, false};
        if (Applies(home.types, away.strType)) //away strength
        { 
            awayPower += away.strVal;
            mods[0] = true;
        }
        if (Applies(home.types, away.wkType)) //away weakness
        { 
            awayPower -= away.wkVal; 
            mods[1] = true;
        }
        if (Applies(away.types, home.strType)) //home strength
        { 
            homePower += home.strVal;
            mods[2] = true;
        }
        if (Applies(away.types, home.wkType)) //home weakness
        { 
            homePower -= home.wkVal;
            mods[3] = true;
        }
        
        if (print)
        {
            /*print away fighter*/
        System.out.println("");
        //top row: position/types
        //System.out.print("\n[" + away.position + "] ");
        for (char c : away.types)
        { System.out.print("(" + c + ")"); }

        //name, base power
        System.out.println("\n" + away.name);
        System.out.println(awayPower);

        //strength
        System.out.print("(" + away.strType + ")+" + away.strVal);
        if (mods[0])
            System.out.print("*");
        System.out.println("");

        //weakness
        System.out.print("(" + away.wkType + ")-" + away.wkVal);
        if (mods[1])
            System.out.print("*");

        /*print home fighter*/
        System.out.println("\n");
        //top row: position/types
        //System.out.print("\n[" + home.position + "] ");
        for (char c : home.types)
        { System.out.print("(" + c + ")"); }

        //name, base power
        System.out.println("\n" + home.name);
        System.out.println(homePower);

        //strength
        System.out.print("(" + home.strType + ")+" + home.strVal);
        if (mods[2])
            System.out.print("*");
        System.out.println("");

        //weakness
        System.out.print("(" + home.wkType + ")-" + home.wkVal);
        if (mods[3])
            System.out.print("*");
        System.out.println("");
        }
        
        
        if (homePower > awayPower)
        { return home.name; }
        else if (awayPower > homePower)
        { return away.name; }
        else //in a tie, no contest
        { return "No contest"; }
    }

    //checks if given strength/weakness type is in opponent's types list
    private static boolean Applies(char[] types, char check)
    {
        for (char c : types)
        {
            if (c == check)
            { return true; }
        }
        return false;
    }

    //display methods
    public void print_basic()
    {
        //top row: position/types
        System.out.println("\n[" + FID + "] ");
        for (char c : types)
        { System.out.print("(" + c + ")"); }
        //name, base power
        System.out.println("\n" + name);
        System.out.println(base);
        //strength/weakness
        System.out.println("(" + strType + ")+" + strVal);
        System.out.println("(" + wkType + ")-" + wkVal);
    }
    public void print_oneLine()
    {
        //System.out.print("[" + FID + "] ");
        for (char c : types)
        { System.out.print("(" + c + ")"); }
        //name, base power
        System.out.print(" " + name);
        System.out.print(" " + base);
        //strength/weakness
        System.out.print(" (" + strType + ")+" + strVal);
        System.out.println(" (" + wkType + ")-" + wkVal);
    }

} //end Fighters class
