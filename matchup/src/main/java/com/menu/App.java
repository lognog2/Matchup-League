package com.menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.entities.Player.Strategy;
import com.entities.Team;
import com.menu.load.Loader;
import com.repo.Manager;
import com.repo.Repository;
import com.util.Debug;

/**
 * The class where the JavaFX stage and scene are set, and where
 * the main method is located.
 * @since 1.1.0
 */
public class App extends Application {

    public final static String VERSION = "1.2.0 Development";

    private final int window_width = 900;
    private final int window_height = 750;
    private static Scene scene;

    /**
     * The default bye team, all bye teams should be copies of this.
     * @since 1.1.1
     * @see Team
     */
    protected static Team byeTeam;
    /**
     * Navitagion stack; Holds the list of menus the user has naviagated.
     * @since 1.1.1
     */
    private static Stack<String> navStack;
    /**
     * Map of files to download. 
     * Key is folder name, value is set of file names
     * @since 1.2.0
     * @see #load_data(boolean, boolean)
     */
    private static Map<String, Set<String>> filesToLoad;
    /**
     * Map of all strategies, indexed by their ID.
     * @since 1.2.0
     * @see Strategy
     */
    protected static Map<Integer, Strategy> strategyMap;
    /**
     * App manager; static reference to {@link Manager}.
     * @since 1.1.2
     * @see App
     */
    protected static Manager manager;
    /**
     * App repo; static reference to the {@link Repository} located in {@link App#manager}.
     * @since 1.2.0
     * @see App
     */
    protected static Repository repo;
    /**
     * App loader; static reference to {@link Loader}.
     * @since 1.1.2
     * @see Loader
     */
    protected static Loader loader;

    private static SessionFactory sf;
    private static Session ses;

    /* INITIALIZING METHODS */

    @Override
    public void start(Stage stage) {
        Debug.write("App.start", stage);
        navStack = new Stack<>();
        strategyMap = new HashMap<>();
        loadFonts(); 
        scene = new Scene(loadFXML("main_menu"), window_width, window_height);
        scene.getStylesheets().add(getClass().getResource("/css/colors.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public boolean loadFonts() {
        Font agency = Font.loadFont(App.class.getResourceAsStream("/css/fonts/agency_fb.ttf"), 40);
        Font brit = Font.loadFont(App.class.getResourceAsStream("/css/fonts/britannic_bold.ttf"), 40);
        return (agency != null && brit != null) ? true : false;
    }

    /**
     * Where the program starts and ends.
     * @param args
     * @since 0.0
     * @version 1.1.2
     */
    public static void main(String[] args)  {
        Debug.write("App.main");
        Debug.init();
        filesToLoad = new HashMap<>();
        launch(args);
        if (ses != null && ses.isOpen()) ses.close();
        if (sf != null && sf.isOpen()) sf.close();
        Debug.close();
    }

    /**
     * Initializes Hibernate session, {@link App#manager}, and {@link App#repo}.
     * @since 1.2.0
     */
    public static void initSession() {
        try {
            write("App.initSession");
            Configuration config = new Configuration().configure();
            write("configured hibernate file");
            sf = config.buildSessionFactory();
            write("built session factory");
            ses = sf.openSession();
            final String success = "========== CONNECTION SUCCESSFUL ===========";
            write(success);
            System.out.println("\n" + success + "\n");
        } catch (Exception e) {
            Debug.error(-2, e);
        }
        
        
        manager =  new Manager(ses);
        repo = manager.getRepo();
    }

    /* GET METHODS */

    /**
     * Gets the app's session factory.
     * @return sf
     * @since 1.1.0
     */
    public static SessionFactory getSF() {
        Debug.write("App.getSF");
        return sf;
    }
    /**
     * Gets the app's session.
     * @return ses
     * @since 1.1.0
     */
    public static Session getSession() {
        write("App.getSession");
        return ses;
    }

    /**
     * Gets the app's manager
     * @return appManager
     * @since 1.1.2
     */
    public static Manager getManager() {
        Debug.write("App.getManager");
        return manager;
    }

    /**
     * Gets the menu at the top of the nav stack.
     * @return Menu last added to the nav stack
     * @since 1.1.1
     * @see App#navStack
     */
    public static String getTopNav() {
        Debug.write("App.getTopNav"); return navStack.peek();
    }
    
    /* MENU NAVIGATION */

    /**
     * Sets scene to main menu.
     * @since 1.1.0
     */
    protected static void toMainMenu() {
        Debug.write("App.toMainMenu");
        navStack.clear();
        setRoot("main_menu");
    }

    /**
     * Goes back one menu.
     * @since 1.1.1
     */
    public static void goBack() {
        Debug.write("App.goBack");
        navStack.pop();
        setRoot(navStack.pop());
    }

    /* FXML LOADING */

    /**
     * Adds new menu to the nav stack and sets scene 
     * @param fxml Name of the .fxml file to set  
     * @since 1.1.0
     */
    public static void setRoot(String fxml) {
        Debug.write("App.setRoot", fxml);
        try {
            navStack.add(fxml);
            scene.setRoot(loadFXML(fxml));
        } catch (Exception e) {
            Debug.error(-1, e);
        }
    }

    private static Parent loadFXML(String fxml) {  
        Debug.write("App.loadFXML", fxml);
        try {
            return getFXMLLoader(fxml).load();
        } catch (IOException e) {
            Debug.error(-6, e);
            return null;
        }
    }
    public static Parent loadFXML(FXMLLoader loader) {  
        Debug.write("App.loadFXML", loader.toString());
        try {
            return loader.load();
        } catch (IOException e) {
            Debug.error(-6, e);
            return null;
        }
    }

    public static FXMLLoader getFXMLLoader(String fxml) {
        Debug.write("App.getFXMLLoader", fxml);
        try {
            URL fxmlLocation = App.class.getResource(fxml + ".fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
            return fxmlLoader;
        } catch (Exception e) {
            Debug.error(-6, e);
            return null;
        }
        
    }

    /**
     * Loads fxml file, adds parent node to root, and returns the controller object.
     * @param fxml Name of fxml file
     * @param parent Parent pane of controller
     * @return Controller object
     * @throws IOException
     * @since 1.1.0
     * @version 2
     */
    public static Object getController(String fxml, Pane parent) throws IOException {
        Debug.write("App.getController", fxml, parent.getAccessibleText());
        FXMLLoader loader = getFXMLLoader(fxml);
        Parent root = loadFXML(loader);
        parent.getChildren().add(root);
        return loader.getController();
    }

    /**
     * Sets a new scene. 
     * @param fxml fmxl file path
     * @param o Controller object
     * @return New scene
     * @throws IOException
     * @since 1.1.2
     */
    public static Scene setController(String fxml, Object o) {
        Debug.write("App.getController", fxml, o);
        FXMLLoader loader = getFXMLLoader(fxml);
        Parent root = loadFXML(loader);
        return new Scene(root, 640, 480);
    }

    /**
     * Sets root to load menu
     * @return {@link App#loader}
     */
    public static Loader loadMenu() {
        Debug.write("App.loadMenu");
        setRoot("load\\load_screen");
        return loader;
    }
    
    public static Loader getLoader() {
        Debug.write("App.getLoader");
        if (loader == null) System.out.println("null loader accessed");
        return loader;
    }

    public static void setLoader(Loader ld) {
        Debug.write("App.setLoader", ld);
        loader = ld;
    }

    /* BYE TEAM METHODS */

    /**
     * Sets app bye team.
     * @param t New bye team
     * @since 1.1.1
     */
    public static void setByeTeam(Team t) {
        Debug.write("App.setByeTeam", t.getName());
        byeTeam = t;
    }

    /**
     * Gets app bye team.
     * @return byeTeam
     * @since 1.1.1
     */
    public static Team getByeTeam() {Debug.write("App.getByeTeam"); return byeTeam;}


    /* FILE READING */

    /**
     * Adds a file to the {@link #filesToLoad} map.
     * @param folder folder name
     * @param file file name, including extension
     * @return true if file was added, false if not
     */
    public static boolean addFile(String folder, String file) {
        Set<String> folderSet = filesToLoad.get(folder);
        if (folderSet == null) {
            folderSet = new HashSet<>();
            folderSet.add(file);
            return filesToLoad.put(folder, folderSet) != null;
        } else {
            return folderSet.add(file);
        }
    }

    /**
     * Gets line reader for a file in <code>src\\main\\data</code>
     * @param folder
     * @param file
     * @return BufferedReader
     * @since 1.2.0
     */
    public static BufferedReader getLineReader(String folder, String file) {
        try {
        final String dataPath = "src\\main\\data";
        final String filePath = dataPath + "\\" + folder + "\\" + file;
        return new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            Debug.error(-4, e);
            return null;
        }
    }

    /**
     * Counts the number of lines in a file.
     * @param folder folder name
     * @param file file name within folder
     * @param includeAll when true, counts all lines; 
     * when false, ignores first line and lines starting with '*'
     * @return negative if error occured, else line count
     * @since 1.2.0
     */
    public static int getLineCount(boolean includeAll) {
        Debug.write("App.getLineCount", includeAll);
        if (filesToLoad.isEmpty()) { 
            Debug.warn(0, "filesToUpload map is empty. Why didn't you check for this?");
            return 0;
        }
        BufferedReader lineReader = null;
        int count = 0;
        try {
            for (String folder : filesToLoad.keySet()) {
                for (String file : filesToLoad.get(folder)) {
                    lineReader = getLineReader(folder, file);
                    String line = (includeAll) ? "" : lineReader.readLine();
                    while((line = lineReader.readLine()) != null) {
                        if (includeAll || (!line.isEmpty() && line.charAt(0) != '*'))
                            count++;
                    }
                    write(file + ": " + count);
                }
            }
            lineReader.close();
            //write("line count: " + count);
            return count;
        } catch (IOException e) {
            Debug.error(-4, e);
            return -4;
        } catch (RuntimeException e) {
            Debug.error(-1, e);
            return -1;
        }
    }

    /* UTILITY METHODS */

    /**
     * Checks if progress bar is between 0.95 and 1.05 (95% and 105%).
     * Throws Exit Code 5 if not within parameters.
     * @param progress amount of progress on bar, in decimal form
     * @return true if within parameters, false if not
     * @since 1.2.0
     */
    public static boolean verifyProgress(double progress) {
        if (progress < 0.95 || progress > 1.05) {
            Debug.warn(5, "Progress bar finished at" + progress);
            return false;
        }
        else return true;
    }

    //debug writer shortcuts
    public static void write(String line) {
        Debug.write(line);
    }
    public static void write(String line, Object... obj) {
        Debug.write(line, obj);
    }
    public static void error(Exception e) {
        Debug.error(e);
    }
    public static void startThread(String threadName) {
        Debug.startThread(threadName);
    }
    public static void endThread(String threadName) {
        Debug.endThread(threadName);
    }


    /**
     * returns placement suffix for numbers -110 to 110.
     * If outside of [-110, 110] returns number + "th".
     * @param num
     * @return 1st, 2nd, 3rd, etc.
     * @since 1.1.0
     */
    public static String addSuffix(int num) {
        Debug.write("App.addSuffix", num);
        if (num < 0) num *= -1;
        if (num > 20) num %= 10;
        if (num == 1) return num + "st";
        else if (num == 2) return num + "nd";
        else if (num == 3) return num + "rd";
        else return num + "th";
    }
    
    /**
     * if index is 0, returns 1. Otherwise returns 0
     * @param index
     * @return index flipped
     * @since 1.1.1
     */
    public static int flip (int index) {Debug.write("App.flip", index);return (index == 0) ? 1 : 0;}
} //end App class 