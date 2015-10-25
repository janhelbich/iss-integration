package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.ObjednavkaBO;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.camel.Handler;

/**
 * Hlavni trida pro komunikaci se systemy
 * @author Jan Srogl && Mira Nedved
 */
@Startup
@Singleton
public class ObjednavkaService
{
    static ObjednavkaService singleton = new ObjednavkaService();

    public static ObjednavkaService getInstance()
    {
        return singleton;
    }
    
    /**
     * Checks if IDobjed != 0 and if it contains products. 
     * It also checks if products are valid.
     * @param o new Order
     * @return TRUE if everything is ok, FALSE if anything is not valid.
     * @throws InvalidObjednavkaDataFormat 
     */
    @Handler
    public boolean isValid(ObjednavkaBO o) throws InvalidObjednavkaDataFormat{
        //check ID
        if(o.getIDobjed() != 0){
            throw new InvalidObjednavkaDataFormat();
        }
        //check products
        if(o.getWantedItems() == null || o.getWantedItems().isEmpty()){
            //TODO HonzaŠ zatim chyta vyjimky
            throw new InvalidObjednavkaDataFormat();
        } else {
            for(ItemBO item: o.getWantedItems()){
                if(!item.isValid()){
                    throw new InvalidObjednavkaDataFormat();
                }
            }
        }
        
        return true;
    }
}
