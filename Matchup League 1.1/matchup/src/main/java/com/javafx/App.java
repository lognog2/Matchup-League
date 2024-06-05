package com.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class App extends Application {

    private static Scene scene;
    private final int window_width = 900;
    private final int window_height = 750;

    private static SessionFactory sf;
    private static Session ses;

    @Override
    public void start(Stage stage) throws IOException 
    {
        MenuHandler.onStart();
        scene = new Scene(loadFXML("main_menu"), window_width, window_height);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
    
    static void setRoot(String fxml) throws IOException { scene.setRoot(loadFXML(fxml));}

    public static Parent loadFXML(String fxml) throws IOException 
    {  
        return getLoader(fxml).load();
    }
    public static Parent loadFXML(FXMLLoader loader) throws IOException 
    {  
        return loader.load();
    }

    public static FXMLLoader getLoader(String fxml)
    {
        URL fxmlLocation = App.class.getResource(fxml + ".fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader;
    }

    public static Object getController(FXMLLoader loader)
    {
        return loader.getController();
    }

    public static void main(String[] args) 
    {
        launch();
        if (ses.isOpen()) ses.close();
        if (sf.isOpen()) sf.close();
    }

    public static SessionFactory getSF() 
    {
        if (sf == null)
            sf = new Configuration().configure().buildSessionFactory();
        return sf;
    }
    public static Session getSession() 
    {
        if (ses == null)
            ses = getSF().openSession();
        return ses;
    }

    //returns placement suffix for numbers -110 to 110
    public static String addSuffix(int num)
    {
        if (num < 0) num *= -1;
        if (num > 20) num %= 10;
        if (num == 1) return num + "st";
        else if (num == 2) return num + "nd";
        else if (num == 3) return num + "rd";
        else return num + "th";
    }

}   