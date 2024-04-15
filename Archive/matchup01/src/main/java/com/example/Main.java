/*
 * Matchup League 0.1
 * by Logan Nyquist
 * 
 * 0.1 (3/2024): Main purpose is for doing official Matchup League games digitally
 * by reading in from a file 
 * Test subjects: Georgia, test
 */

package com.example;

import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main 
{
    public static void main (String[] args)
    {
        int answer = -1;
        Scanner in = new Scanner(System.in);
        do
        {
            answer = -1;
            System.out.print("\nMatchup League 0.1\n1: From file\n0: exit\n>");
            if (in.hasNextInt())
            {
                answer = in.nextInt();
                in.nextLine();
                if (answer == 1) //read from txt file
                {
                    answer = Register();
                }
            }
            else
            {
                System.out.println("Invalid option");
                in.nextLine();
            }
        } while (answer != 0); //end do loop
        in.close();
    } //end main method

    public static int Register()
    {
        Scanner in = new Scanner(System.in);
        String txtFile = "src\\main\\java\\com\\example\\";
        System.out.print("\nChoose a league file\n 1: Georgia\n-1: back\n 0: exit\n>");
        int answer = in.nextInt();
        if (answer == 1)
        { txtFile += "Georgia_29.txt"; }
        else if (answer == 99)
        { txtFile += "test.txt"; }
        else
        { 
            //in3.close();
            return answer; //-1 repeats main menu, 0 ends program
        }

        try
        {
            File file = new File(txtFile);
            Scanner scan = new Scanner(file);

            //line 1: number of teams
            String line = scan.nextLine();
            int numTeams = Integer.parseInt(line);

            //line 2: fighters per team
            line = scan.nextLine();
            int fpt = Integer.parseInt(line);

            System.out.println("\nEnter number for away and home team, with a space in between.");

            String[] teams = new String[numTeams];
            int i = 0;
            while (scan.hasNextLine()) 
            {
                line = scan.nextLine();
                //if (!line.isEmpty()) System.out.println(line);
                if (!line.isEmpty() && line.charAt(0) == '*')
                {
                    teams[i] = line.substring(1);
                    System.out.println(i + ": " + teams[i]);
                    i++;
                }
            } //end while loop
            scan.close();
            System.out.print(">");

            int away = in.nextInt();
            int home = in.nextInt();
            return Game(txtFile, fpt, teams[away], teams[home]);                  
        } //end try
        catch (FileNotFoundException e) 
        { 
            System.out.println("Error reading file in Register method");
            e.printStackTrace();
            return -2; //returns error
        }
    }
    
    
    public static int Game(String txtFile, int fpt, String away, String home)
    {
        try
        {
            File file = new File(txtFile);
            Scanner scan2 = new Scanner(file);
            Scanner in = new Scanner(System.in);

            String line = scan2.nextLine();
            Fighter[] awayTeam = new Fighter[fpt];
            Fighter[] homeTeam = new Fighter[fpt];

            while (scan2.hasNextLine()) 
            {
                line = scan2.nextLine(); 
                if (line.equals("*" + away))
                {
                    //register away fighters
                    for (int i = 0; i < fpt; i++)
                    {
                        String fLine = scan2.nextLine();
                        awayTeam[i] = new Fighter(fLine);
                    }
                }
                else if (line.equals("*" + home))
                {
                    //register home fighters
                    for (int i = 0; i < fpt; i++)
                    {
                        String fLine = scan2.nextLine();
                        homeTeam[i] = new Fighter(fLine);
                    }
                }
            } //end while loop

            int answer = 0;
            do 
            {
                System.out.print("\n 1: Play game\n 2: Print " + away + "\n 3: Print " + home + "\n-1: Save and return\n 0: Save and exit\n>");
                answer = in.nextInt();
                if (answer == 1) //play game
                {
                    String cont = in.nextLine(); 
                    double homeScore = 0, awayScore = 0;
                    double pointValue = 1;
                    for (int i = 0; i < fpt; i++)
                    {
                        if (i == fpt-1)
                            pointValue = 2;
                        else
                            pointValue = 1 + ((double)fpt/100 - (double)i/100);
                        
                        String winner = homeTeam[i].Match(awayTeam[i], homeTeam[i]);
                        if (winner == homeTeam[i].name())
                        {
                            homeScore += pointValue;
                            homeTeam[i].setMatch(true);
                            awayTeam[i].setMatch(false);
                        }
                        else if (winner == awayTeam[i].name())
                        {
                            awayScore += pointValue;
                            awayTeam[i].setMatch(true);
                            homeTeam[i].setMatch(false);
                        }

                        System.out.println("Winner: " + winner);
                        System.out.print(away + " " + (int)awayScore + ", " + home + " " + (int)homeScore + "\n>");
                        cont = in.nextLine(); //waits for user input before going to next match
                        if (cont.equals("?")) //hidden command: shows full decimal score
                            System.out.println(away + " " + awayScore + ", " + home + " " + homeScore);
                    }
                    if (homeScore > awayScore)
                        System.out.println(home + " Wins!");
                    else if (awayScore > homeScore)
                        System.out.println(away + " Wins!");
                    else
                        System.out.println("Tie");
                }
                else if (answer == 2) //print away
                {
                    for (Fighter f : awayTeam)
                        f.print_basic();
                }
                else if (answer == 3) //print home
                {
                    for (Fighter f : homeTeam)
                        f.print_basic();
                }
            } while (answer > 0);

            System.out.print("Name of report(no spaces): ");
            String report = in.next();
            write_report(report, awayTeam, homeTeam);
        
            scan2.close();
            if (answer > 0)
                answer = -1;
            return answer;
            //in2.close();
        } //end try
        catch (FileNotFoundException e) 
        { 
            System.out.println("Error reading file in Game method");
            e.printStackTrace();
            return -2;
        }
    } //end Game method

    //writes game report to report.txt, includes fighter names and matches won/played
    public static void write_report(String reportName, Fighter[] team1, Fighter[] team2)
    {
       String reportFile = "src\\main\\java\\com\\example\\report.txt";
       final int MAX_REPORT_LENGTH = team1[0].maxNameLength() + 4;
       try
       {
        FileWriter fw = new FileWriter(reportFile, true); //true appends, false overwrites
        BufferedWriter bw = new BufferedWriter(fw);

        for (int i = 0; i < MAX_REPORT_LENGTH; i++)
            bw.write("-"); //report divider

        bw.newLine();
        bw.write(reportName);
        for (Fighter f : team1)
        {
            bw.newLine();
            bw.write(f.name() + " " + f.matchesWon() + "/" + f.matchesPlayed());
        }
        for (Fighter f : team2)
        {
            bw.newLine();
            bw.write(f.name() + " " + f.matchesWon() + "/" + f.matchesPlayed());
        }
        bw.newLine();
        bw.write("");
        bw.close();
       }
       catch (IOException e)
       { e.printStackTrace(); }
       
    }

} //end Main class

class Fighter
{
    //fighter variables
    private String name;
    private char position;
    private char[] types;
    private int base;
    private char strType;
    private int strVal;
    private char wkType;
    private int wkVal;
    private int matchesPlayed;
    private int matchesWon;

    //constants
    private final int MAX_NAME_LENGTH = 16;

    //constructor
    public Fighter(String line)
    {
        //name
        int start = 0;
        int next = nextSpace(line);
        name = line.substring(start, next);
        if (name.length() > MAX_NAME_LENGTH)
        { name = name.substring(0, MAX_NAME_LENGTH); }
        name = replace(name, '-', ' ');

        //position
        start = next + 1;
        next = line.substring(0, start).length() + nextSpace(line.substring(start));
        position = line.charAt(start);

        //types
        start = next + 1;
        next = line.substring(0, start).length() + nextSpace(line.substring(start));
        types = line.substring(start, next).toCharArray();

        //base power
        start = next + 1;
        next = line.substring(0, start).length() + nextSpace(line.substring(start));
        base = Integer.parseInt(line.substring(start, next));

        //strength type
        start = next + 1;
        next = line.substring(0, start).length() + nextSpace(line.substring(start));
        strType= line.charAt(start);

        //strength value
        start = next + 1;
        next =  line.substring(0, start).length() + nextSpace(line.substring(start));
        strVal = Integer.parseInt(line.substring(start, next));

        //weakness type
        start = next + 1;
        next =  line.substring(0, start).length() + nextSpace(line.substring(start));
        wkType = line.charAt(start);

        //weakness value
        start = next + 1;
        next = line.substring(0, start).length() + nextSpace(line.substring(start));
        wkVal = Integer.parseInt(line.substring(start));

    } //end constructor

    //accessors
    public String name() {return name;}
    public char position() {return position;}
    public char[] types() {return types;}
    public int base() {return base;}
    public char strType() {return strType;}
    public int strVal() {return strVal;}
    public char wkType() {return wkType;}
    public int wkVal() {return wkVal;}
    public int matchesPlayed() {return matchesPlayed;}
    public int matchesWon() {return matchesWon;}
    public int maxNameLength() {return MAX_NAME_LENGTH;}

    //mutators
    public void setMatch(boolean won)
    {
        matchesPlayed++;
        if (won)
            matchesWon++;
    }

    //prints fighter in basic format
    public void print_basic()
    {
        //top row: position/types
        System.out.print("\n[" + position + "] ");
        for (char c : types)
        { System.out.print("(" + c + ")"); }
        //name, base power
        System.out.println("\n" + name);
        System.out.println(base);
        //strength/weakness
        System.out.println("(" + strType + ")+" + strVal);
        System.out.println("(" + wkType + ")-" + wkVal);
    }

    //returns full name of given type
    public String fullType(char type)
    {
        switch(type)
        {
            case 'M': return "Melee";
            case 'R': return "Ranged";
            case 'E': return "Explosive";
            case 'F': return "Fire";
            case 'W': return "Water";
            case 'L': return "Electric";
            case 'G': return "Magic";
            case 'C': return "Mechanical";
            case 'A': return "Aerial";
            case 'I': return "Ice";
            case 'S': return "Star";
            case 'V': return "Evil";
            default: return "N/A";
        }
    }

    //executes match between two fighters, returns name of winner or "no contest"
    public String Match(Fighter away, Fighter home)
    {
        int awayPower = away.base();
        int homePower = home.base();

        /*print away fighter*/
        //top row: position/types
        System.out.print("\n[" + away.position + "] ");
        for (char c : away.types)
        { System.out.print("(" + c + ")"); }
        //name, base power
        System.out.println("\n" + away.name);
        System.out.println(away.base);
        //strength
        System.out.print("(" + away.strType + ")+" + away.strVal);
        if (Applies(home.types, away.strType))
        { 
            awayPower += away.strVal; 
            System.out.print("*");
        }
        System.out.println("");
        //weakness
        System.out.print("(" + away.wkType + ")-" + away.wkVal);
        if (Applies(home.types, away.wkType))
        { 
            awayPower -= away.wkVal; 
            System.out.print("*");
        }
        System.out.println("");

        /*print home fighter*/
        //top row: position/types
        System.out.print("\n[" + home.position + "] ");
        for (char c : home.types)
        { System.out.print("(" + c + ")"); }
        //name, base power
        System.out.println("\n" + home.name);
        System.out.println(home.base);
        //strength
        System.out.print("(" + home.strType + ")+" + home.strVal);
        if (Applies(away.types, home.strType))
        { 
            homePower += home.strVal;
            System.out.print("*");
        }
        System.out.println("");
        //weakness
        System.out.print("(" + home.wkType + ")-" + home.wkVal);
        if (Applies(away.types, home.wkType))
        { 
            homePower -= home.wkVal;
            System.out.print("*");
        }
        System.out.println("");
        
        if (homePower > awayPower)
        { return home.name; }
        else if (awayPower > homePower)
        { return away.name; }
        else //in a tie, no contest
        { return "No contest"; }
    }

    //checks if given strength/weakness type is in opponent's types list
    private boolean Applies(char[] types, char check)
    {
        for (char c : types)
        {
            if (c == check)
            { return true; }
        }
        return false;
    }

    // returns the next space in given string
    // if no space is found, returns one after last char in string
    private int nextSpace(String line)
    {
        int i;
        for (i = 0; i < line.length(); i++)
        {
            if(line.charAt(i) == ' ') 
            { return i; }
        }
        return i+1;
    } //end nextSpace

    //replaces each dash in a string with a space
    private String replace(String str, char remove, char place)
    {
        for (int i = 0; i < str.length(); i++)
        {
            if (str.charAt(i) == remove)
            {
                str = str.substring(0, i) + place + str.substring(i+1);
            }
        }
        return str;
    }

} //end Fighters class