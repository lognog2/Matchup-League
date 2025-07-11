package com.entities;

import com.menu.App;

import jakarta.persistence.*;

/**
 * Fighter is an Entity class that represents a fighter. 
 * Fighters can be seen as characters that the user and CPUs 
 * can acquire. This is the lowest level relation, 
 * and are matched up against each other in a Matchup game.
 * @since 0.0
 * @version 3
 */
@Entity
@Table(name = "Fighters")
public class Fighter extends DataEntity
{
    /* VARIABLES */
    
    /**
     * Fighter ID; Uniquely identifies a fighter.
     * @since 0.3
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int FID; //fighter ID

    /**
     * Team that the fighter is part of.
     * @since 0.3
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TID")
    private Team team;

    /**
     * Name of the fighter.
     * @since 0.0
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Array containing the fighter's types.
     * @version 2
     * @since 0.0
     */
    @Column(name = "types")
    private char[] types;

    /**
     * Base power of the fighter.
     * @since 0.0
     */
    @Column(name = "base")
    private int base;

    /**
     * Fighter's strength type.
     * @since 0.0
     */
    @Column(name = "strType")
    private char strType;

    /**
     * Fighter's strength value.
     * @since 0.0
     */
    @Column(name = "strVal")
    private int strVal;

    /**
     * Fighter's weakness type.
     * @since 0.0
     */
    @Column(name = "wkType")
    private char wkType;

    /**
     * Fighter's weakness value.
     * Should always be positive.
     * @since 0.0
     */
    @Column(name = "wkVal")
    private int wkVal;

    /**
     * Fighter's rarity. 0 means fighter is unique; the higher
     * the rarity, the more likely this fighter is to be replicated.
     * @since 1.1.1
     */
    @Column(name = "rarity")
    private int rarity;

    /**
     * Fighter's nation. The country/region the fighter is from,
     * whether real or fiction.
     * @since 1.1.2
     */
    @Column(name = "nation")
    private String nation;

    /**
     * Number of matches won by this fighter.
     * @since 1.1.2
     */
    @Column(name = "won")
    private int matchesWon;

    /**
     * Number of matches played by this fighter.
     * @since 1.1.2
     */
    @Column(name = "played")
    private int matchesPlayed;

    /* CONSTRUCTORS */

    /**
     * Constructs a fighter from a csv file
     * @param line String array
     * @version 3
     * @since 0.3
    */
    public Fighter(String[] line)
    {
        setName(minimize(line[0], MAX_NAME()));
        setTypes(minimize(line[1], MAX_TYPES()).toUpperCase().toCharArray());
        setBase(Integer.parseInt(line[2]));
        setStrength(line[3].toUpperCase().charAt(0), Integer.parseInt(line[4]));
        setWeakness(line[5].toUpperCase().charAt(0), Integer.parseInt(line[6]));
        setRarity(Integer.parseInt(line[7]));
        setNation(line[8]);
    }
    
    /**
     * Constructs a fighter either using autogen or defaultgen
     * @param autogen true: use random values, false: use default values
     * @since 1.0
     */
    public Fighter(boolean autogen) {
        if (autogen) 
        {
            setName(randomName());
            setAutoTypes();
            setBase(10 * ((int)(Math.random() * 51) + 150)); //range: 1500 - 2000
            setStrength(randomType(), 10 * ((int)(Math.random() * 20) + 1)); //range: 10 - 200
            do {
                wkType = randomType();
            } while (wkType == strType);
            setWeakness(wkType, 10 * ((int)(Math.random() * 20) + 1)); //range: 10 - 200
            setRarity(0);
            setNation("Autogen");
        } else {
            setName("default");
            setTypes(".".toCharArray());
            setBase(1500);
            setStrength('.', 0);
            setWeakness('.', 0);
            setNation("Default");
        }
    }

    /**
     * Copy constructor. Can either make exact copy or 
     * Adjust base and strength/weakness values.
     * @param f Original fighter
     * @param exactCopy Determines if base and s/w values should be the same
     * @since 1.1.0
     * @version 2
     */
    public Fighter (Fighter f, boolean exactCopy)
    {
        setName(f.name);
        setTypes(f.types);
        setRarity(-1);
        setNation(f.getNation());
        setMatchesWon(f.getMatchesWon());
        setMatchesPlayed(f.getMatchesPlayed());
        if (exactCopy) {
            setBase(f.base);
            setStrength(f.strType, f.strVal);
            setWeakness(f.wkType, f.wkVal);
        } else {
            setBase(adjustBase(f.base));
            setStrength(f.strType, adjustMod(f.strVal));
            setWeakness(f.wkType, adjustMod(f.wkVal));
        }
    }
    /**
     * Blank constructor, to be used by Hibernate
     * @since 0.3
     */
    public Fighter() {}

    /* GET METHODS */

    /**
     * Gets fighter ID.
     * @return FID
     * @since 0.3
     */
    public int FID() {return FID;}

    /**
     * {@inheritDoc}
     */
    public String getID() {return "fighter" + FID;}

    /**
     * Gets fighter name.
     * @return name
     * @since 0.0
     */
    public String getName() {return name;}

    /**
     * Gets the fighter's name its Fighter ID.
     * @return Fighter name + [FID]
     * @since 1.1.1
     */
    public String getNamePlusFID() {return isGeneric() ? name + " [" + FID + "]" : name;}

    /**
     * Gets fighter's types.
     * @return types
     * @version 2 
     * @since 0.0
     */
    public char[] getTypes() {return types;}

    /**
     * Gets fighter's base.
     * @return base
     * @since 0.0
     */
    public int getBase() {return base;}

    /**
     * Gets fighter's strength type.
     * @return strType
     * @since 0.0
     */
    public char getStrType() {return strType;}

    /**
     * Gets fighter's strength value.
     * @return strVal
     * @since 0.0
     */
    public int getStrVal() {return strVal;}

    /**
     * Gets fighter's weakness type.
     * @return wkType
     * @since 0.0
     */
    public char getWkType() {return wkType;}

    /**
     * Gets fighter's weakness value.
     * @return wkVal as a positive int
     * @since 0.0
     */
    public int getWkVal() {return wkVal;}

    /**
     * Gets fighter's rarity.
     * @return rarity
     * @since 1.1.1
     */
    public int getRarity() {return rarity;}

    /**
     * Gets fighter's nation.
     * @return nation
     * @since 1.1.2
     */
    public String getNation() {return nation;}

    /**
     * Gets fighter's matches won.
     * @return matchesWon
     * @since 1.1.2
     */
    public int getMatchesWon() {return matchesWon;}

    /**
     * Gets fighter's matches played.
     * @return matchesWon
     * @since 1.1.2
     */
    public int getMatchesPlayed() {return matchesPlayed;}

    /**
     * Gets fighter's team.
     * @return team, or null if fighter is not on a team.
     * @since 0.3
     */
    public Team getTeam() {return team;}

    /* Constant get methods */

    /**
     * Maximum name length
     * @return 15
     * @since 0.1
    */
    public final int MAX_NAME() {return 15;}

    /**
     * Maximum number of types
     * @return 5
     * @since 0.3
     */
    public final int MAX_TYPES() {return 5;}

    /**
     * Maximum modifier value for strength/weakness.
     * @return 200
     * @since 1.1.1
     */
    public final int MAX_MOD() {return 200;}

    /**
     * Minimum modifier value for strength/weakness.
     * @return Interval (10)
     * @since 1.1.1
     */
    public final int MIN_MOD() {return INTERVAL();}

    /**
     * Minimum modifier value that a fighter's base can be.
     * @return Interval (10)
     * @since 1.1.1
     */
    public final int MIN_BASE() {return INTERVAL();}

    /**
     * The interval at which base and s/w values can change.
     * @return 10
     * @since 1.1.1
     */
    public final int INTERVAL() {return 10;}

    /**
     * Gets fighter's types as a string, with each type surrounded by parentheses.
     * @return Types as a string
     * @since 1.1.0
     */
    public String getTypesString() {
        StringBuilder str = new StringBuilder(MAX_TYPES() * 3);
        for (char c : types)
            str.append("(" + c + ")");
        return str.toString();
    }

    /**
     * Gets fighter's types as a string.
     * @return Base as a string
     * @since 1.1.0
     */
    public String getBaseString() {return Integer.toString(base);}

    /**
     * Gets fighter's strength type and value as a string.
     * @return (strType) + strVal
     * @since 1.1.0
     */
    public String getStrString(){return "(" + strType + ") +" + strVal;}

    /**
     * Gets fighter's weakness type and value as a string.
     * @return (wkType) - wkVal
     * @since 1.1.0
     */
    public String getWkString(){return "(" + wkType + ") -" + wkVal;}

    /* calculated get methods */

    /**
     * @return Difference between strength value and weakness value
     * @since 1.0
     */
    public int modDiff() {return strVal - wkVal;}


    /* SET METHODS */

    /**
     * Sets fighter's name.
     * @param name new name
     * @since 0.3
     */
    public void setName(String name) {this.name = name;}

    /**
     * Sets fighter's types.
     * @param types new types
     * @since 0.3
     */
    public void setTypes(char[] types) {this.types = types;}

    /**
     * Sets fighter's base.
     * @param base new base
     * @since 0.3
     */
    public void setBase(int base) {this.base = base;}

    /**
     * Sets fighter's strength type and value.
     * @param type new strType
     * @param val new strVal
     * @since 0.3
     */

    public void setStrength(char type, int val) {this.strType = type; this.strVal = val;}
    /**
     * Sets fighter's weakness type and value.
     * @param type new wkType
     * @param val new wkVal
     * @since 0.3
     */
    public void setWeakness(char type, int val) { this.wkType = type; this.wkVal = val;}

    /**
     * Set fighter's rarity.
     * @param rarity new rarity
     * @since 1.1.1
     */
    public void setRarity(int rarity) {this.rarity = rarity;}

    /**
     * Set fighter's nation.
     * @param nation
     * @since 1.1.2
     */
    public void setNation(String nation) {this.nation = nation;}

    /**
     * Sets fighter's matches won.
     * @since 1.1.2
     */
    public void setMatchesWon(int matchesWon) {this.matchesWon = matchesWon;}

    /**
     * Sets fighter's matches played
     * @since 1.1.2
     */
    public void setMatchesPlayed(int matchesPlayed) {this.matchesPlayed = matchesPlayed;}

    /**
     * Adds a win and match played.
     * @since 1.1.2
     */
    public void addWin() {matchesWon++; matchesPlayed++;}

    /**
     * Adds a match played but not a win, equating to a loss.
     * @since 1.1.2
     */
    public void addLoss() {matchesPlayed++;}

    //auto set methods

    /**
     * Sets a randomized array of types for autogen
     * @since 1.0
     */
    public void setAutoTypes() {
        int numTypes = (int)(Math.random() * MAX_TYPES()) + 1;
        char[] types = new char[numTypes];
        for (int i = 0; i < numTypes; i++)
        {
            char newType = randomType();
            if (!hasType(types, newType)) 
                types[i] = newType;
            else i--;    
        }
        setTypes(types);
    }

    //dependent methods
    /**
     * Sets fighter's team. This is a dependent method,
     * and should only be called from Team.addFighter.
     * @param team new team
     * @since 0.3
     */
    public void setTeam(Team team) {this.team = team;}

    //inherited methods

    /**
     * Creates an auto-generated fighter.
     * @return auto-generated fighter
     * @since 1.0
     */
    public Fighter autogen() {
        Fighter auto = new Fighter(true);
        return auto;
    }

    /* UTILITY METHODS */

    /**
     * Checks if an array of types contains a type
     * @param types
     * @param type
     * @return true if contains, false if doesn't
     * @since 1.0
     */
    private boolean hasType(char[] types, char type) {
        for (char c : types) {
            if (c == type)
                return true;
        } return false;
    }

    /**
     * Fighters that are a copy will have a rarity below zero.
     * @return True if fighter is a copy, false if not
     * @since 1.1.1
     */
    public boolean isCopy() {return rarity < 0;}

    /**
     * Generic fighters have a rarity either above zero
     * or below.
     * @return True if rarity is not zero, false if it is
     * @since 1.1.1
     */
    public boolean isGeneric() {return rarity != 0;}

    /**
     * @return Random type from the array of all types.
     * @since 1.0
     */
    private char randomType() {
        return App.allTypes()[(int)(Math.random() * App.allTypes().length)].getChar();
    }

    /**
     * Minimizes string to specified maximum length.
     * If the string is shorter than max length, returns itself unchanged.
     * @param str String to check
     * @param max maximum length of string
     * @return minimized or original string
     * @since 0.3
     */
    private String minimize(String str, int max) {
        return (str.length() > max) ? 
            str.substring(0, max) + "." : str;
    }
    
    /**
     * Changes base power by up to +/- 2%.
     * Will revert min base if result is 0 or lower.
     * @param base
     * @since 1.1.1
     */
    private int adjustBase(int base) {
        int adjustment = (int)((Math.random() - .5) * base * .02);
        base = roundNumber(base + adjustment, INTERVAL());
        return (base < MIN_BASE()) ? MIN_BASE() : base;
    }

    /**
     * Changes strength/weakness value by up to +/- 30.
     * Will revert to min/max mod value if outside range.
     * @param val
     * @since 1.1.1
     */
    private int adjustMod(int val) {
        int adjustment = (int)((Math.random() - .5) * 30);
        val = roundNumber(val + adjustment, INTERVAL());
        if (val < MIN_MOD()) return MIN_MOD();
        else if (val > MAX_MOD()) return MAX_MOD();
        else return val;
    }

    /**
     * Rounds a number to the specified interval.
     * @param num Number to round
     * @param interval Interval to round to
     * @return rounded number
     * @since 1.1.1
     */
    private int roundNumber(int num, int interval) {
        int roundedNum = num / interval;
        if (num % interval > interval / 2) roundedNum++;
        return roundedNum * interval;
    }

    //debug methods
    
    /**
     * Prints fighter info to console in "card" format.
     * @since 0.1
     */
    public void print_basic() {
        System.out.println("\n[" + FID + "] ");
        System.out.println(getTypesString());
        System.out.println("\n" + name);
        System.out.println(base);
        System.out.println(getStrString());
        System.out.println(getWkString());
    }

    /**
     * Prints fighter info on one line.
     * @since 1.0
     */
    public void print_oneLine() {
        System.out.print(getTypesString());
        System.out.print(" " + name);
        System.out.print(" " + base);
        System.out.print(" " + getStrString());
        System.out.println(" " + getWkString());
    }

} //end Fighters class
