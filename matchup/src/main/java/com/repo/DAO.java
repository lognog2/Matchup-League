package com.repo;

import org.hibernate.*;
import org.hibernate.query.Query;
import com.menu.App;
import com.menu.load.Loader;
import com.util.Debug;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Data Access Object
 * This is the only class that can directly access the database.
 * @since 0.3
 */
public class DAO<T> {
    /** 
     * Table to be accessed
     * @since 0.3
     */
    private Class<T> table;
    /**
     * This DAO's private session.
     * @since 0.3
     */
    private Session ses;
    /**
     * Name of the directory the entity classes are in.
     * @since 1.0
     */
    private final String packageName = "matchup.entities.";

    /**
     * Constructs a DAO with
     * @param table Table associated with this object.
     * Each entity class has its own DAO and table.
     * @since 0.3
     * @version 2
     */
    public DAO (Class<T> table) {
        ses = App.getSession();
        this.table = table;
    }

    /**
     * Reads data from a csv file and adds it to the database.
     * @param folder Folder name to read data from, should be the same
     * name as its table
     * @param file File name to read data from
     * @since 0.3
     * @version 3
     */
    public void load_data(String folder, String file) {
        Debug.write("Loading " + folder + " from file " + file);

        BufferedReader lineReader = App.getLineReader(folder, file);

        Transaction tr = null;
        try {
            String line = lineReader.readLine(); //skip over first line
            tr = ses.beginTransaction();
            Loader loader = App.getLoader();
            int i = 0;
            while((line = lineReader.readLine()) != null) {
                if (line.charAt(0) != '*') {
                    String[] data = line.split(",");
                    T temp = table.getDeclaredConstructor(String[].class).newInstance((Object) data);
                    ses.persist(temp);
                    if (i++ % 50 == 0) {ses.flush(); ses.clear();}
                    loader.addProgress();
                }
            }
            tr.commit();
            tr = null;
            lineReader.close();
        } catch (IOException e) {
            Debug.error(-4, e);
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            Debug.error(-2, e);
        }
    }
    
    /**
     * Run a custom mutation query.
     * Must be called from active transaction to take effect.
     * @param query Verbatim SQL query
     * @since 0.3
     * @version 2
     */
    public void run_mutationQuery(String query) {
        if (ses == null || !ses.isOpen())
            ses = App.getSF().openSession();
        try {
            System.out.println(query);
            ses.createNativeMutationQuery(query).executeUpdate();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
        }
    }

    /* SELECT methods, no transaction required */

    /**
     * Runs a custom selection query
     * No transaction is required.
     * @param line
     * @return Verbatim SQL query
     * @since 1.0
     */
    public List<T> run_selectionQuery(String line) {
        List<T> list = null;
        try {
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
        }
        return list;
    } 

    /**
     * SELECT * FROM table (select all items of a table)
     * @return List of all objects in table
     * @since 0.3
     */
    public List<T> select() {
        List<T> list = null;
        try
        {
            //tr = ses.beginTransaction();
            String line = "FROM " + table.getName();
            //System.out.println("SELECT * " + line);
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        } //finally {ses.close();}

        return list;
    }
    /**
     * SELECT * FROM table WHERE condition
     * @param condition Verbatim condition
     * @return List of objects that fit condition
     * @since 1.0
     */
    public List<T> select(String condition) {
        List<T> list = null;
        try
        {
            String line = "FROM " + table.getName() + " WHERE " + condition;
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }

        return list;
    }

    /**
     * SELECT name FROM table (gets the names of all objects in table)
     * @return List of names
     */
    public List<String> selectNames() {
        List<String> list = null;
        try
        {
            String line = "SELECT name FROM " + table.getName();
            Query<String> query = ses.createQuery(line, String.class);
            list = query.getResultList();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
        return list;
    }

    
    /**
     * SELECT table FROM table tableAlias, otherTable otherAlias WHERE condition
     * Run a select query using a condition from another table.
     * @param otherTable
     * @param condition Verbatim condition
     * @return List of objects that fit condition
     * @since 1.0
     */
    public List<T> select(Class<? extends Object> otherTable, String condition) {
        List<T> list = null;
        try {
            String tableAlias = table.getName().substring(packageName.length());
            String otherAlias = otherTable.getName().substring(packageName.length());   
            String line = "SELECT " + tableAlias +  
            " FROM " + table.getName() + " " + tableAlias + "," + otherTable.getName() + " " + otherAlias + 
            " WHERE " + condition;
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
        return list;
    }
    
    /**
     * SELECT 1 FROM table WHERE column = value
     * @param column Name of variable for condition
     * @param value Value of variable for condition
     * @return First object that meets condition
     * @since 1.0
     */
    public T selectOne(String column, Object value) {
        try
        {
            String line = "SELECT e FROM " + table.getName() + " e WHERE e." + column + " = :value";
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            query.setParameter("value", value);
            T obj = query.uniqueResult();
            return obj;
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
    }
    /**
     * SELECT 1 FROM table WHERE condition
     * @param condition Verbatim condition
     * @return First object that meets condition
     * @since 1.0
     */
    public T selectOne(String condition) {
        try
        {
            String line = "SELECT e FROM " + table.getName() + " e WHERE " + condition;
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            T obj = query.uniqueResult();
            return obj;
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
    }
    /**
     * SELECT * FROM table ORDER BY column DESC
     * (Orders objects in table according to specified column, highest to lowest)
     * @param column name of variable to order by
     * @return List of objects in desired order
     * @since 0.3
     */
    public List<T> orderByDesc(String column) {
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * " + line);
            Query<T> query = ses.createQuery(line, table);
            return query.list();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
    }
    /**
     * SELECT * FROM table WHERE condition ORDER BY column DESC
     * @param condition Verbatim condition
     * @param column Name of variable to order by
     * @return All objects that meet condition, ordered by column
     * @since 1.0
     */
    public List<T> orderByDesc(String column, String condition) {
        try {
            String line = "FROM " + table.getName() + " WHERE " + condition + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * " + line);
            Query<T> query = ses.createQuery(line, table);
            return query.list();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
    }
    /**
     * SELECT amt FROM table ORDER BY column
     * (Orders objects in table according to specified column, highest to lowest
     * and gets the top amount of objects)
     * @param column name of variable to order by
     * @param amt number of objects to return
     * @return List of objects in desired order and amount
     * @since 0.3
     */
    public List<T> orderByDesc(String column, int amt) {
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * " + line);
            Query<T> query = ses.createQuery(line, table);
            query.setMaxResults(amt); // Limit the number of results returned
            return query.list();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return null;
        }
    }
    /**
     * SELECT COUNT(*) FROM table
     * @return The amount of all objects in table
     * @since 1.0
     */
    public long count() {
        try {
            String line = "SELECT COUNT(*) FROM " + table.getName();
            //System.out.println(line);
            Query<Long> query = ses.createQuery(line, Long.class);
            return query.uniqueResult();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return 0;
        }
    }
    /**
     * SELECT COUNT(*) FROM table WHERE condition
     * @param condition Verbatim condition
     * @return Amount of objects that meet condition
     * @since 1.0
     */
    public long count(String condition) {
        try {
            String line = "SELECT COUNT(*) FROM " + table.getName() + " WHERE " + condition;
            //System.out.println(line);
            Query<Long> query = ses.createQuery(line, Long.class);
            return query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in count: " + e);
            return 0;
        }
    }
    /**
     * SELECT AVG(column) FROM table
     * @param column Name of variable
     * @return Average value of variable across all objects in table
     * @since 1.0
     */
    public double avg(String column) {
        try {
            String line = "SELECT AVG(" + column + ") FROM " + table.getName();
            //System.out.println(line);
            Query<Double> query = ses.createQuery(line, Double.class);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
    /**
     * SELECT AVG(column) FROM table WHERE condition
     * @param column Name of variable
     * @param condition Verbatim condition
     * @return Average value of variable across objects that meet condition
     * @since 1.0
     */
    public double avg(String column, String condition) {
        try {
            String line = "SELECT AVG(" + column + ") FROM " + table.getName() + " WHERE " + condition;
            //System.out.println(line);
            Query<Double> query = ses.createQuery(line, Double.class);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
    /**
     * SELECT MAX(column) FROM table
     * @param column Name of variable
     * @return Maximum value of variable across all objects in table
     */
    public double max(String column) {
        try {
            String line = "SELECT MAX(" + column + ") FROM " + table.getName();
            //System.out.println(line);
            Query<Double> query = ses.createQuery(line, Double.class);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
    /**
     * SELECT MAX(column) FROM table WHERE condition
     * @param column Name of variable
     * @param condition Verbatim condition
     * @return Maximum value of variable from objects that meet condition
     */
    public double max(String column, String condition) {
        try {
            String line = "SELECT MAX(" + column + ") FROM " + table.getName() + " WHERE " + condition;
            //System.out.println(line);
            Query<Double> query = ses.createQuery(line, Double.class);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }

    /**
     * SELECT SUM(column) FROM table
     * @param column Name of variable
     * @return Sum of variable for each object in table
     * @since 1.1.1
     */
    public int sum(String column) {
        try {
            String line = "SELECT SUM(" + column + ") FROM " + table.getName();
            Query<Long> query = ses.createQuery(line, Long.class);
            Long sum = query.uniqueResult();
            if (sum == null)
                return 0;
            else
                return sum.intValue();
        } catch (RuntimeException e) {
            Debug.error(-2, e);
            return 0;
        }
    }
}