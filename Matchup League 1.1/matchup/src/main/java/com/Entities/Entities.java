package com.Entities;

import java.util.UUID;
import com.repo.Repository;

public abstract class Entities 
{
    protected static Repository entityRepo = new Repository();
    
    protected String randomName() {return UUID.randomUUID().toString().substring(0, 8);}

    //abstract classes
    protected abstract Object autogen();
    //public abstract Object defaultgen();
}
