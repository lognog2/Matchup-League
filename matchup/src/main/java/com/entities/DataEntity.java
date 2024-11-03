package com.entities;

import java.io.IOException;
import java.util.UUID;
import com.menu.App;
import com.repo.Manager;
import com.repo.Repository;
import com.util.Debug;

/**
 * The entities class is an abstract class which all entity classes extend. 
 * In Hibernate, an entity is an object that can be stored in the database. 
 * Each entity has its own table, and entity objects can be 
 * persisted or merged into the database.
 * @since 1.0
 * @version 2
 */
public abstract class DataEntity extends App
{
    /**
     * A Repository object used by entity classes.
     * @since 1.1.0
     */
    protected static Repository repo;
    /**
     * A Manager object used by entity classes.
     * @since 1.1.1
     */
    protected static Manager manager;

    /**
     * Sets entity manager and repository to app manager.
     * Loads entity data from files.
     * @param debug true if in test mode, false if in normal mode
     * @throws IOException 
     * @since 1.1.2
     */
    public static void onStart(boolean debug) {
        Debug.write("DataEntity.onStart", debug);
        manager = getManager();
        repo = manager.getRepo();
        manager.load_data(debug);
    }

    public abstract Object getName();

    /**
     * Returns the name of type of entity plus their ID.
     * <p>Examples: fighter2, team30, game47
     * @return entitynameID
     * @since 1.1.2
     */
    public abstract String getID();
    
    /**
     * Generates a random name by getting a random UUID and making a String
     * out of the first eight digits.
     * @return randomized name
     * @since 1.0
     */
    protected String randomName() {return UUID.randomUUID().toString().substring(0, 8);}

    /**
     * Abstract which all entities implement their own method
     * of generating random objects.
     * @return auto-generated object
     * @since 1.0
     */
    protected abstract Object autogen();
    //public abstract Object defaultgen();
}
