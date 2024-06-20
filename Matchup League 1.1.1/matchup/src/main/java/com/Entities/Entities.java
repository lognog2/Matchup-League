package com.Entities;

import java.util.UUID;
import com.repo.Manager;
import com.repo.Repository;

/**
 * The Entities class is an abstract class which all entity classes extend. 
 * In Hibernate, an entity is an object that can be stored in the database. 
 * Each entity has its own table, and entity objects can be 
 * persisted or merged into the database.
 * @since 1.0
 * @version 2
 */
public abstract class Entities 
{
    /**
     * A static Repository object used by entity classes.
     * @since 1.1.0
     */
    protected static Repository entityRepo = new Repository();
    /**
     * A static Manager object used by entity classes.
     * @since 1.1.1
     */
    protected static Manager entityManager = new Manager(entityRepo);

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
