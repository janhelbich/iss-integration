package cz.cvut.fel.iss.integration.service;

import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * Hlavni trida pro komunikaci se systemy
 * @author Jan Srogl
 */
@Startup
@Singleton
public class ObjednavkaService extends AbstractObjednavkaService
{
    static ObjednavkaService singleton = new ObjednavkaService();

    public static ObjednavkaService getInstance()
    {
        return singleton;
    }
}
