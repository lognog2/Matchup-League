package com.menu;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.entities.Player.Strategy;
import com.entities.Team;
import com.menu.load.Loader;
import com.repo.Manager;
import com.util.Debug;

/**
 * The class where the JavaFX stage and scene are set, and where
 * the main method is located.
 * @since 1.1.0
 */
public class App extends Application {

    private final int window_width = 900;
    private final int window_height = 750;
    private static Scene scene;

    /**
     * The default bye team, all bye teams should be copies of this.
     * @since 1.1.1
     */
    private static Team byeTeam;

    /**
     * Navitagion stack; Holds the list of menus the user has naviagated.
     * @since 1.1.1
     */
    private static Stack<String> navStack;

    /**
     * Map of all strategies, indexed by their ID.
     * @since 1.2.0
     */
    public static Map<Integer, Strategy> strategyMap;

    private static Manager appManager;
    private static Loader loader;

    private static SessionFactory sf;
    private static Session ses;

    @Override
    public void start(Stage stage) throws IOException {
        Debug.write("App.start", stage);
        navStack = new Stack<>();
        strategyMap = new HashMap<>();
        scene = new Scene(loadFXML("main_menu"), window_width, window_height);
        scene.getStylesheets().add(getClass().getResource("/css/colors.css").toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
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
        launch(args);
        if (ses != null && ses.isOpen()) ses.close();
        if (sf != null && sf.isOpen()) sf.close();
        Debug.close();
    }
    
    //menu navigation methods

    /**
     * Sets scene to main menu.
     * @throws IOException
     * @since 1.1.0
     */
    protected static void toMainMenu() {
        Debug.write("App.toMainMenu");
        navStack.clear();
        setRoot("main_menu");
    }

    /**
     * Goes back one menu.
     * @throws IOException
     * @since 1.1.1
     */
    public static void goBack() throws IOException {
        Debug.write("App.goBack");
        navStack.pop();
        setRoot(navStack.pop());
    }

    //fxml load methods

    /**
     * Adds new menu to the nav stack and sets scene 
     * @param fxml Name of the .fxml file to set
     * @throws IOException
     * @since 1.1.0
     */
    public static void setRoot(String fxml) {
        Debug.write("App.setRoot", fxml);
        try {
            navStack.add(fxml);
            scene.setRoot(loadFXML(fxml));
        } catch (IOException e) {
            Debug.error(-1, e);
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {  
        //Debug.write("App.loadFXML", fxml);
        return getLoader(fxml).load();
    }
    public static Parent loadFXML(FXMLLoader loader) throws IOException {  
        //Debug.write("App.loadFXML", loader.toString());
        return loader.load();
    }

    public static FXMLLoader getLoader(String fxml) {
        //Debug.write("App.getLoader", fxml);
        URL fxmlLocation = App.class.getResource(fxml + ".fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader;
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
        //Debug.write("App.getController", fxml, parent.getAccessibleText());
        FXMLLoader loader = getLoader(fxml);
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
    public static Scene setController(String fxml, Object o) throws IOException {
        Debug.write("App.getController", fxml, o);
        FXMLLoader loader = getLoader(fxml);
        Parent root = loadFXML(loader);
        return new Scene(root, 640, 480);
    }

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

    //session methods

    /**
     * Gets the app's session factory. If null, builds a new one.
     * @return sf
     * @since 1.1.0
     */
    public static SessionFactory getSF() {
        Debug.write("App.getSF");
        if (sf == null) {
            //loader.setMessage("Building session factory");
            sf = new Configuration().configure().buildSessionFactory();
            //loader.addProgress(.2);
        }
        return sf;
    }
    /**
     * Gets the app's session. If null, opens a new one.
     * @return ses
     * @since 1.1.0
     */
    public static Session getSession() {
        Debug.write("App.getSession");
        if (ses == null) {
            ses = getSF().openSession();
            System.out.println("\n========== CONNECTION SUCCESSFUL ===========\n");
        }
        return ses;
    }
    /**
     * Gets the app's manager. If null, opens a new one.
     * @return appManager
     * @since 1.1.2
     */
    public static Manager getManager() {
        Debug.write("App.getManager");
        if (appManager == null)
            appManager =  new Manager(getSession());
        return appManager;
    }

    public static String getTopNav() {Debug.write("App.getTopNav"); return navStack.peek();}

    //bye team methods

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

    //utility methods

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
}   