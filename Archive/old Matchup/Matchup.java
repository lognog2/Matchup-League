import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Matchup 
{
    public static void Main (String[] args)
    {
        int answer = -1;
        while (answer != 0)
        {
            Scanner in = new Scanner(System.in);

            System.out.print("Matchup League 0.1\n0: Exit\n1:From file\n");
            answer = in.nextInt();

            if (answer == 1)
            {
                String txtFile;
                System.out.print("0: exit 1: Georgia");
                answer = in.nextInt();
                if (answer == 1)
                { txtFile = "Georgia_29.txt"; }
                else { break; }

                try
                {
                    File file = new File(txtFile);
                    Scanner scan = new Scanner(file);

                    while (scan.hasNextLine()) 
                    {
                        String line = scan.nextLine();
                        System.out.println(line);
                    } //end while loop

                    scan.close();
                } //end try
                catch (FileNotFoundException e) 
                { e.printStackTrace(); }
            } //end from file

            in.close();
        } //end while loop
    } //end main
} //end Matchup class

class Fighter
{
    private String name;
    private String position;
    private String types;
    private int base;
    private String strType;
    private int strVal;
    private String wkType;
    private int wkVal;

    public Fighter(String name, String position, String types, int base, String strType, int strVal, String wkType, int wkVal)

    {
        this.name = name;
        this.position = position;
        this.types = types;
        this.base = base;
        this.strType = strType;
        this.strVal = strVal;
        this.wkType = wkType;
        this.wkVal = wkVal;
    }

    public String name() {return name;}
    public String position() {return position;}
    public String types() {return types;}
    public int base() {return base;}
    public String strType() {return strType;}
    public int strVal() {return strVal;}
    public String wkType() {return wkType;}
    public int wkVal() {return wkVal;}

} //end Fighters class
