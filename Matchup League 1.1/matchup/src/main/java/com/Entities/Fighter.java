package com.Entities;

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
    public Fighter (Fighter f)
    {
        setName(f.name);
        setTypes(f.types);
        setBase(f.base);
        setStrength(f.strType, f.strVal);
        setWeakness(wkType, wkVal);
    }
    public Fighter() {}

    //get methods
    public int getFID() {return FID;}
    public String getName() {return name;}
    public char[] getTypes() {return types;}
    public int getBase() {return base;}
    public char getStrType() {return strType;}
    public int getStrVal() {return strVal;}
    public char getWkType() {return wkType;}
    public int getWkVal() {return wkVal;}
    public Team getTeam() {return team;}
    public int MAX_NAME() {return 24;}
    public int MAX_TYPES() {return 5;}
    public char[] allTypes() 
    {
        char[] typeList = {'M','R','E','F','W','L','G','C','A','I','V','S'};
        return typeList;
    }

    //get varaibles as strings
    public String getTypesString()
    {
        StringBuilder str = new StringBuilder(MAX_TYPES() * 3);
        for (char c : types)
            str.append("(" + c + ")");

        return str.toString();
    }
    public String getBaseString() {return Integer.toString(base);}
    public String getStrString(){return "(" + strType + ") +" + strVal;}
    public String getWkString(){return "(" + wkType + ") -" + wkVal;}

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

    //display methods
    public void print_basic()
    {
        System.out.println("\n[" + FID + "] ");
        System.out.println(getTypesString());
        System.out.println("\n" + name);
        System.out.println(base);
        System.out.println(getStrString());
        System.out.println(getWkString());
    }
    public void print_oneLine()
    {
        System.out.print(getTypesString());
        System.out.print(" " + name);
        System.out.print(" " + base);
        System.out.print(" " + getStrString());
        System.out.println(" " + getWkString());
    }

} //end Fighters class
