package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * @since 1.2.0
 */
public class ConnectionTest {

    private String url = "jdbc:mysql://localhost:3306/Matchup?createDatabaseIfNotExist=true&serverTimezone=UTC";
    private String user = "root";
    private String pass = "root";

    public boolean testConnection_mySQL() {
        try {
        Connection conn = DriverManager.getConnection(url, user,pass); 
        return conn != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean testConnection_hibernate() {
        try {
            Configuration configuration = new Configuration()
                .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", user)
                .setProperty("hibernate.connection.password", pass)
                .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
                .setProperty("hibernate.hbm2ddl.auto", "create-drop");

            SessionFactory sessionFactory = configuration.buildSessionFactory();
            System.out.println("Hibernate initialized successfully!");
            sessionFactory.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
