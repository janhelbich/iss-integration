/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.Item;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author Mira
 */
@Startup
@Singleton
public class LocalStockService {
    static LocalStockService singleton = new LocalStockService();
    private static Map<String, Item> localStock;

    public LocalStockService() {
        this.localStock = new HashMap<>();
        this.localStock.put("fedora", new Item(50, 10));
        this.localStock.put("rhel", new Item(30, 10));
        this.localStock.put("ubuntu", new Item(15, 10));
    }
    
    
    public static LocalStockService getInstance()
    {
        return singleton;
    }
    
    /**
     * Checks if wanted item is in local store and if here is atleast wanted
     * amount of items.
     * @param wantedItem Item with specified amount value and Sku identifier
     * @return TRUE if here is atleast wanted amount of wanted Item in local stock.
     * FALSE otherwise.
     */
    public boolean isInStock(Item wantedItem){
        
        if(this.localStock.containsKey(wantedItem.getSku())){
            Item localItem = this.localStock.get(wantedItem.getSku());
            
            //return TRUE if localStock contains atleast wanted amount of items.
            return (localItem.getAmount() >= wantedItem.getAmount());
                
        } else {
            //TODO throw some Not-exist exception?
            return false;
        }
    }
    
}
