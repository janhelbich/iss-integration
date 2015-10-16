package cz.cvut.fel.iss.integration.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Created by Jan Srogl on 16.10.15.
 */
@Startup
@Singleton
public class VIPObjednavkaService
{
    static VIPObjednavkaService singleton = new VIPObjednavkaService();

    public static VIPObjednavkaService getInstance()
    {
        return singleton;
    }
}
