package com.entities;

import java.util.UUID;
import com.menu.App;

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
    public abstract String getName();

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
    protected abstract DataEntity autogen();
    //public abstract Object defaultgen();

    /**
     * Enum containing the name of each data entity
     * @since 1.2.1
     */
    public enum Entity {
        FIGHTER,
        TEAM,
        LEAGUE,
        GAME,
        PLAYER
    }
}
