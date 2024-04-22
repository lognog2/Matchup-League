package matchup;

import org.hibernate.*;
import org.hibernate.query.Query;
//import org.hibernate.cfg.Configuration;

import java.util.List;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DAO<T> 
{
    private SessionFactory sf;
    private Class<T> table;

    public DAO (SessionFactory sf, Class<T> table)
    {
        this.sf = sf;
        this.table = table;
    }

    //add data from csv file to database
    public void load_data(String folder, String file) throws IOException
    {
        System.out.println("Loading " + folder + " from file " + file);
        Session ses = sf.openSession();
        Transaction tr = null;
        
        final String resPath = "src\\main\\resources\\";
        String filePath = resPath + "\\" + folder + "\\" + file;

        BufferedReader lineReader = new BufferedReader(new FileReader(filePath));
        String line = lineReader.readLine(); //skip over first line
        //int fid = 0;
        while ((line = lineReader.readLine()) != null) 
        {
            String[] data = line.split(",");

            try {
                T temp = table.getDeclaredConstructor(String[].class).newInstance((Object) data);
                tr = ses.beginTransaction();
                ses.persist(temp);
                tr.commit();
                tr = null;
            } catch (RuntimeException | ReflectiveOperationException e) {
                if (tr != null) tr.rollback();
                System.out.println("Error loading file: " + e);
            } //finally {ses.close();}
        }
        lineReader.close();
    }
    
    public void run_query(String query) {
        System.out.print("Running query \"" + query + "\": ");
        Session ses = sf.openSession();
        Transaction tr = null;
        try {
            tr = ses.beginTransaction();
            ses.createNativeMutationQuery(query).executeUpdate();
            tr.commit();
            tr = null;
            System.out.println("Success");
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            System.out.println("Error: " + e);
        } //finally {ses.close();}
    } //end run_query

    //SELECT * FROM TABLE (select all items of a table)
    public List<T> select() 
    {
        List<T> list = null;
        Session ses = sf.openSession();
        Transaction tr = null;
        try
        {
            tr = ses.beginTransaction();
            Query<T> query = ses.createQuery("FROM " + table.getName(), table);
            list = query.getResultList();
            tr.commit();
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            throw e;
        } //finally {ses.close();}

        return list;
    }
    // SELECT * FROM table WHERE ... (select all items that meet condition)
    public List<T> select(String condition)
    {
        List<T> list = null;
        Session ses = sf.openSession();
        Transaction tr = null;
        try
        {
            tr = ses.beginTransaction();
            Query<T> query = ses.createQuery("FROM " + table.getName() + " WHERE " + condition, table);
            list = query.getResultList();
            tr.commit();
        } catch (RuntimeException e) {
            if (tr != null) tr.rollback();
            throw e;
        } finally {}

        return list;
    }

    public List<T> rankBy(String column, int num) {
        Session ses = sf.openSession();
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            Query<T> query = ses.createQuery(line, table);
            query.setMaxResults(num); // Limit the number of results returned
            return query.list();
        } finally {}
    }
    public List<T> rankBy(String column) {
        Session ses = sf.openSession();
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            Query<T> query = ses.createQuery(line, table);
            return query.list();
        } finally {}
    }
}
