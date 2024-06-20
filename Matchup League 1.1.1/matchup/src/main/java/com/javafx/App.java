package com.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import com.Entities.Team;

/**
 * The class where the JavaFX stage and scene are set, and where
 * the main method is located.
 * @since 1.1.0
 */
public class App extends Application {

    private static Scene scene;
    private final int window_width = 900;
    private final int window_height = 750;
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
    private static SessionFactory sf;
    private static Session ses;

    @Override
    public void start(Stage stage) throws IOException 
    {
        MenuHandler.onStart();
        navStack = new Stack<>();
        scene = new Scene(loadFXML("main_menu"), window_width, window_height);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Where the program starts and ends.
     * @param args
     * @since 0.0
     * @version 1.1.1
     */
    public static void main(String[] args)  {
        launch();
        if (ses.isOpen()) ses.close();
        if (sf.isOpen()) sf.close();
    }
    
    //menu navigation methods

    /**
     * Sets scene to main menu.
     * @throws IOException
     * @since 1.1.0
     */
    public static void toMainMenu() throws IOException {
        navStack.clear();
        setRoot("main_menu");
    }

    /**
     * Goes back one menu.
     * @throws IOException
     * @since 1.1.1
     */
    public static void goBack() throws IOException {
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
    public static void setRoot(String fxml) throws IOException {
        navStack.add(fxml);
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {  
        return getLoader(fxml).load();
    }

    public static Parent loadFXML(FXMLLoader loader) throws IOException {  
        return loader.load();
    }

    public static FXMLLoader getLoader(String fxml) {
        URL fxmlLocation = App.class.getResource(fxml + ".fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader;
    }

    public static Object getController(FXMLLoader loader) {
        return loader.getController();
    }

    //session methods

    /**
     * Gets the app's session factory. If null, builds a new one.
     * @return sf
     * @since 1.1.0
     */
    public static SessionFactory getSF() {
        if (sf == null)
            sf = new Configuration().configure().buildSessionFactory();
        return sf;
    }
    /**
     * Gets the app's session. If null, opens a new one.
     * @return ses
     * @since 1.1.0
     */
    public static Session getSession() {
        if (ses == null)
            ses = getSF().openSession();
        return ses;
    }

    //bye team methods

    /**
     * Sets app bye team.
     * @param t New bye team
     * @since 1.1.1
     */
    public static void setByeTeam(Team t) {
        byeTeam = t;
    }

    /**
     * Gets app bye team.
     * @return byeTeam
     * @since 1.1.1
     */
    public static Team getByeTeam() {return byeTeam;}

    //utility methods

    /**
     * returns placement suffix for numbers -110 to 110
     * @param num
     * @return 1st, 2nd, 3rd, etc.
     * @since 1.1.0
     */
    public static String addSuffix(int num) {
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
    public static int flip (int index) {return (index == 0) ? 1 : 0;}
}   