package matchup;

import org.hibernate.*;
import org.hibernate.query.Query;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DAO<T> 
{
    private Class<T> table;
    private Session ses;
    private final String packageName = "matchup.Entities.";

    public DAO (Session ses, Class<T> table)
    {
        this.table = table;
        this.ses = ses;
    }

    //add data from csv file to database
    public void load_data(String folder, String file) throws IOException
    {
        //System.out.println("Loading " + folder + " from file " + file);
        Transaction tr = null;
        
        final String dataPath = "src\\main\\data";
        String filePath = dataPath + "\\" + folder + "\\" + file;

        BufferedReader lineReader = new BufferedReader(new FileReader(filePath));
        String line = lineReader.readLine(); //skip over first line
        
        try {
            tr = ses.beginTransaction();
            int i = 0;
            while((line = lineReader.readLine()) != null)
            {
                String[] data = line.split(",");
                T temp = table.getDeclaredConstructor(String[].class).newInstance((Object) data);
                ses.persist(temp);
                if (i++ % 50 == 0) {ses.flush(); ses.clear();}    
            }
            tr.commit();
            tr = null;
        } catch (RuntimeException | ReflectiveOperationException e) {
            if (tr != null) tr.rollback();
            e.printStackTrace();
        }
        lineReader.close();
    }
    
    //mutation query, transaction required to take effect
    public void run_mutationQuery(String query) {
        //System.out.println(query);
        try {
            ses.createNativeMutationQuery(query).executeUpdate();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /* select methods, no transaction required */

    //custom query
    public List<T> run_selectionQuery(String line) {
        //System.out.println(query);
        List<T> list = null;
        try {
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return list;
    } 

    //SELECT * FROM TABLE (select all items of a table)
    public List<T> select() 
    {
        List<T> list = null;
        try
        {
            //tr = ses.beginTransaction();
            String line = "FROM " + table.getName();
            //System.out.println("SELECT * FROM " + line);
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        } //finally {ses.close();}

        return list;
    }
    // SELECT * FROM table WHERE condition
    public List<T> select(String condition)
    {
        List<T> list = null;
        //Transaction tr = null;
        try
        {
            //tr = ses.beginTransaction();
            String line = "FROM " + table.getName() + " WHERE " + condition;
            //System.out.println("SELECT * FROM " + line);
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
            //tr.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }

    //SELECT table FROM table tableAlias, otherTable otherAlias WHERE condition
    public List<T> select(Class<? extends Object> otherTable, String condition) {
        List<T> list = null;
        try {
            String tableAlias = table.getName().substring(packageName.length());
            String otherAlias = otherTable.getName().substring(packageName.length());
            
            String line = "SELECT " + tableAlias +  
            " FROM " + table.getName() + " " + tableAlias + "," + otherTable.getName() + " " + otherAlias + 
            " WHERE " + condition;
            Query<T> query = ses.createQuery(line, table);
            list = query.getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
    
    //SELECT 1 FROM table WHERE condition (using parameter)
    public T selectOne(String column, Object value)
    {
        try
        {
            String line = "SELECT e FROM " + table.getName() + " e WHERE e." + column + " = :value";
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            query.setParameter("value", value);
            T obj = query.uniqueResult();
            return obj;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }
    //SELECT 1 FROM table WHERE condition
    public T selectOne(String condition)
    {
        try
        {
            String line = "SELECT e FROM " + table.getName() + " e WHERE " + condition;
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            T obj = query.uniqueResult();
            return obj;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    //SELECT * FROM table ORDER BY column DESC
    public List<T> orderByDesc(String column) 
    {
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * FROM " + line);
            Query<T> query = ses.createQuery(line, table);
            return query.list();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }
    //SELECT * FROM table WHERE condition ORDER BY column DESC
    public List<T> orderByDesc(String condition, String column) 
    {
        try {
            String line = "FROM " + table.getName() + " WHERE " + condition + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * FROM " + line);
            Query<T> query = ses.createQuery(line, table);
            return query.list();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }
    //SELECT amt FROM table ORDER BY column
    public List<T> orderByDesc(String column, int amt) {
        try {
            String line = "FROM " + table.getName() + " ORDER BY " + column + " DESC";
            //System.out.println("SELECT * FROM " + line);
            Query<T> query = ses.createQuery(line, table);
            query.setMaxResults(amt); // Limit the number of results returned
            return query.list();
        } catch (RuntimeException e) {
            System.out.println("Error in rankBy: " + e);
            return null;
        }
    }
    
    //SELECT COUNT(*) FROM table
    public long count()
    {
        try {
            String line = "SELECT COUNT(*) FROM " + table.getName();
            //System.out.println(line);
            Query<Long> query = ses.createQuery(line, Long.class);
            return query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in count: " + e);
            return 0;
        }
    }
    //SELECT COUNT(*) FROM table WHERE condition
    public long count(String condition)
    {
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

    //SELECT AVG(column) FROM table
    public double avg(String column)
    {
        try {
            String line = "SELECT AVG(" + column + ") FROM " + table.getName();
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
    //SELECT AVG(column) FROM table WHERE condition
    public double avg(String column, String condition)
    {
        try {
            String line = "SELECT AVG(" + column + ") FROM " + table.getName() + " WHERE " + condition;
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }

    //SELECT MAX(column) FROM table
    public double max(String column)
    {
        try {
            String line = "SELECT MAX(" + column + ") FROM " + table.getName();
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
    //SELECT MAX(column) FROM table WHERE condition
    public double max(String column, String condition)
    {
        try {
            String line = "SELECT MAX(" + column + ") FROM " + table.getName() + " WHERE " + condition;
            //System.out.println(line);
            Query<T> query = ses.createQuery(line, table);
            if (query.uniqueResult() == null) 
                return 0.0;
            else 
                return (double)query.uniqueResult();
        } catch (RuntimeException e) {
            System.out.println("Error in avg: " + e);
            return 0.0;
        }
    }
}
