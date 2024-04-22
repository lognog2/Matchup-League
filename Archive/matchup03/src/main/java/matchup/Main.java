/*
* Matchup League 0.3
* by Logan Nyquist
*
* 0.3 (4/2024) Added draft, scheduling, and season
*
* 0.2 (3/2024): In a game, displays power after modifiers applied instead of just base,
* added option to exit without saving, changed from 1-1-1-2 format to 1-1-1-1 random tiebreaker draw
*
* 0.1 (3/2024): Main purpose is for doing official Matchup League games digitally
* by reading in from a file 
*/

package matchup;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main 
{
    public static void main (String[] args)
    {
        int answer = -1;
        Scanner in = new Scanner(System.in);
        do
        {
            answer = -1;
            System.out.print("\nMatchup League 0.3\n1: Begin draft\n2: Auto draft\n0: Exit program\n>");
            if (in.hasNextInt())
            {
                answer = in.nextInt();
                in.nextLine();
                if (answer == 1) //manual draft
                {
                    answer = Draft('m');
                }
                else if (answer == 2) //auto draft
                {
                    answer = Draft('a');
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

    public static int Draft(char mode)
    {
        Scanner in = new Scanner(System.in);
        String txtFile = "src\\main\\resources\\fighters.txt";
        final int fpt = 5;

        try
        {
            File file = new File(txtFile);
            Scanner scan = new Scanner(file);

            List<Fighter> fighterList = new ArrayList<>();
            while(scan.hasNextLine())
            {
                String line = scan.nextLine();
                Fighter f = new Fighter(line);
                fighterList.add(f);
            }


            scan.close();                  
        } //end try
        catch (FileNotFoundException e) 
        { 
            System.out.println("Error reading file in Register method");
            e.printStackTrace();
            return -2; //returns error
        }
    }
    
    
    public static int Game(String txtFile, final int fpt, String away, String home)
    {
        try
        {
            

            List<Fighter> awayList = Arrays.asList(awayTeam);
            List<Fighter> homeList = Arrays.asList(homeTeam);
            int answer = 0;
            //game options loop
            do 
            {
                System.out.print("\n 1: Play game\n 2: Print " + away + "\n 3: Print " + home + 
                "\n 4: Save and return\n-1: Return without saving\n 0: Exit program\n>");
                answer = in.nextInt();
                if (answer == 1) //play game
                {
                    String cont = in.nextLine(); 
                    int homeScore = 0, awayScore = 0;
                    final int pointValue = 1;

                    //randomize order
                    Collections.shuffle(awayList);
                    Collections.shuffle(homeList);

                    int i = 0;
                    do
                    {
                        int turn = i % fpt;
                        String winner = homeTeam[turn].playMatch(awayTeam[turn], homeTeam[turn]);
                        if (winner == homeTeam[turn].name())
                        {
                            homeScore += pointValue;
                            homeTeam[turn].setMatch(true);
                            awayTeam[turn].setMatch(false);
                        }
                        else if (winner == awayTeam[turn].name())
                        {
                            awayScore += pointValue;
                            awayTeam[turn].setMatch(true);
                            homeTeam[turn].setMatch(false);
                        }

                        System.out.println("Winner: " + winner);
                        System.out.print(away + " " + awayScore + ", " + home + " " + homeScore + "\n>");
                        if (cont != "skip")
                            cont = in.nextLine(); //waits for user input before going to next match
                        //if (cont.equals("?")) //hidden command: shows full decimal score
                            //System.out.println(away + " " + awayScore + ", " + home + " " + homeScore);
                        i++;
                        if (i % fpt == 0 && homeScore == awayScore)
                        {
                            System.out.println("Overtime!");
                            Collections.shuffle(awayList);
                            Collections.shuffle(homeList);
                        }
                    } while (i < fpt || homeScore == awayScore); //end match cycle
                    if (homeScore > awayScore)
                        System.out.println(home + " Wins!");
                    else //if (awayScore > homeScore)
                        System.out.println(away + " Wins!");
                    
                } //end play game decision
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
                if (answer == 4) //save fighter wins in report
                {
                    write_report(awayList, homeTeam);
                    answer = -1;
                }
            } while (answer > 0); //end game options loop
        
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

    // returns the next space in given string
    // if no space is found, returns last char in string
    public static int nextChar(String line)
    {
        int i;
        for (i = 0; i < line.length(); i++)
        {           
            if(line.charAt(i) == ' ') 
            { break; }
        }
        return i;
    } //end nextChar method

    //overloaded to find next of any char
    public static int nextChar(String line, char delim)
    {
        int i;
        for (i = 0; i < line.length(); i++)
        {           
            if(line.charAt(i) == delim) 
            { break; }
        }
        return i;
    } //end nextChar method

} //end Main class

class Fighter
{
    //fighter variables
    private String name;
    private String FID; //fighter ID
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
        int next = Main.nextChar(line);
        name = line.substring(start, next);
        if (name.length() > MAX_NAME_LENGTH)
        { name = name.substring(0, MAX_NAME_LENGTH); }
        name = replace(name, '-', ' ');

        //fighter id
        start = next + 1;
        next = line.substring(0, start).length() + Main.nextChar(line.substring(start));
        FID = line.substring(start, next);

        //types
        start = next + 1;
        next = line.substring(0, start).length() + Main.nextChar(line.substring(start));
        types = line.substring(start, next).toCharArray();

        //base power
        start = next + 1;
        next = line.substring(0, start).length() + Main.nextChar(line.substring(start));
        base = Integer.parseInt(line.substring(start, next));

        //strength type
        start = next + 1;
        next = line.substring(0, start).length() + Main.nextChar(line.substring(start));
        strType= line.charAt(start);

        //strength value
        start = next + 1;
        next =  line.substring(0, start).length() + Main.nextChar(line.substring(start));
        strVal = Integer.parseInt(line.substring(start, next));

        //weakness type
        start = next + 1;
        next =  line.substring(0, start).length() + Main.nextChar(line.substring(start));
        wkType = line.charAt(start);

        //weakness value
        start = next + 1;
        next = line.substring(0, start).length() + Main.nextChar(line.substring(start));
        wkVal = Integer.parseInt(line.substring(start));

        matchesWon = 0;
        matchesPlayed = 0;

    } //end constructor

    //accessors
    public String name() {return name;}
    public String FID() {return FID;}
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
    public String playMatch(Fighter away, Fighter home)
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