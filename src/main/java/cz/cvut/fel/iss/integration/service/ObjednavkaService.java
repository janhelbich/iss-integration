package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.ObjednavkaBO;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaDTO;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaItemDTO;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import cz.cvut.fel.iss.integration.model.helper.ObjednavkaConverter;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.ExchangeProperty;
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
    static AtomicInteger lastOrderId = new AtomicInteger();
    static Map<Integer,ObjednavkaBO> repository = new ConcurrentHashMap<>();


    public static ObjednavkaService getInstance()
    {
        return singleton;
    }
    
    /**
     * Checks if it contains products. 
     * It also checks if products are valid.
     * @param o new Order
     * @return TRUE if everything is ok, FALSE if anything is not valid.
     * @throws InvalidObjednavkaDataFormat 
     */
    @Handler
    public void isValid(ObjednavkaDTO o) throws InvalidObjednavkaDataFormat{
        //check products
        if(o.getWantedItems() == null || o.getWantedItems().isEmpty()){
            throw new InvalidObjednavkaDataFormat();
        } else {
            for(ObjednavkaItemDTO item: o.getWantedItems()){
                if(!item.validate()){
                    throw new InvalidObjednavkaDataFormat();
                }
            }
        }

    }
    
    /**
     * Transforms DTO to BO object.
     * @param o
     * @return 
     */
    @Handler
    public ObjednavkaBO create(ObjednavkaDTO o){
        int newOrderId = lastOrderId.incrementAndGet();
        ObjednavkaConverter objConv = new ObjednavkaConverter();
        ObjednavkaBO obj = objConv.getObjednavka(o, newOrderId);
        repository.put(obj.getIdObjednavka(),obj);
        return obj;
    }

    @Handler
    public ObjednavkaBO get(@ExchangeProperty("id") Integer id)
    {
        return repository.get(id);
    }

    @Handler
    public ObjednavkaBO updateItems(@ExchangeProperty("id") Integer id,@ExchangeProperty("items")  ArrayList<ItemBO> items)
    {
        repository.get(id).setWantedItems(items);
        return repository.get(id);
    }

    @Handler
    public void remove(@ExchangeProperty("id") Integer id)
    {
        if (repository.containsKey(id)) repository.remove(id);
    }
}
