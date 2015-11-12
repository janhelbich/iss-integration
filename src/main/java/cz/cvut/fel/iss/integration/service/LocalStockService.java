/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.ItemTypes;
import cz.cvut.fel.iss.integration.model.exceptions.InvalidObjednavkaDataFormat;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;

/**
 *
 * @author Mira
 */
@Startup
@Singleton
public class LocalStockService {
    static LocalStockService singleton = new LocalStockService();
    private static Map<String, ItemBO> localStock;

    public LocalStockService() {
        localStock = new HashMap<>();
        localStock.put("fedora", new ItemBO("fedora",50, 10, ItemTypes.LOCAL_STOCK_INFO));
        localStock.put("rhel", new ItemBO("rhel",30, 10, ItemTypes.LOCAL_STOCK_INFO));
        localStock.put("ubuntu", new ItemBO("ubuntu",15, 10, ItemTypes.LOCAL_STOCK_INFO));
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
    @Handler
    public boolean isInStock(@ExchangeProperty("item") ItemBO wantedItem){
        
        if(this.localStock.containsKey(wantedItem.getSku())){
            ItemBO localItem = this.localStock.get(wantedItem.getSku());

            //return TRUE if localStock contains atleast wanted amount of items.
            if (localItem.getAmount() >= wantedItem.getAmount()) wantedItem.setIsAvailable(true);
            else wantedItem.setIsAvailable(false);
            return (localItem.getAmount() >= wantedItem.getAmount());
                
        } else {
            //TODO throw some Not-exist exception?
            return false;
        }
    }
    
    /**
     * Selects cheaper supplier which have wanted amount of Item.
     * It also checks if wantedItem is the same item as items from 
     * supplierA and supplierB
     * Sets vipStatus of item to TRUE, if supplier's price is higher than on local stock.
     * @param wantedItem Wanted Item with defined wanted amount
     * @param itemA item state at supplierA, price and amount 
     * @param itemB item state at supplierB, price and amount
     * @return Selected cheaper Item or null if no supplier have wanted amount.
     * @throws InvalidObjednavkaDataFormat If given items are not the same.
     */
    @Handler
    public ItemBO selectCheapestItem(ItemBO wantedItem, ItemBO itemA, ItemBO itemB) throws InvalidObjednavkaDataFormat{
        
        if( !wantedItem.getSku().equals(itemA.getSku()) && !itemA.getSku().equals(itemB.getSku()) ){
            //not the same items
            throw new InvalidObjednavkaDataFormat();
        }
        
        //it is info about state on supplier's stock
        itemA.setItemType(ItemTypes.SUPPLIER_A_INFO);
        itemB.setItemType(ItemTypes.SUPPLIER_B_INFO);
        
        int wantedAmount = wantedItem.getAmount();
        if(itemA.getAmount() >= wantedAmount && itemB.getAmount() >= wantedAmount){
            if(itemA.getPrice().compareTo(itemB.getPrice()) <= 0){
                return compareSupplierPriceAndLocalPrice(itemA);
            } else {
                return compareSupplierPriceAndLocalPrice(itemB);
            }
        } else if(itemA.getAmount() >= wantedAmount){
            return compareSupplierPriceAndLocalPrice(itemA);
        } else if(itemB.getAmount() >= wantedAmount){
            return compareSupplierPriceAndLocalPrice(itemB);
        }
        //both of suppliers do not have wanted amount
        return null;
    }
    
    /**
     * Checks if price of given item is higher than price in local stock.
     * @param supplierItem Selected item from supplier.
     * @return given item with vipStatus variable adjusted.
     */
    private ItemBO compareSupplierPriceAndLocalPrice(ItemBO supplierItem ){
        BigDecimal priceInLocalStock = this.getItem(supplierItem.getSku()).getPrice();
        supplierItem.setVipStatus(supplierItem.getPrice().compareTo(priceInLocalStock) > 0);
        return supplierItem;
    }
    
    /**
     * Removes wanted amount of Item from local stock.
     * @param processedItem item which amount should be decreased in local stock
     * @return True if it was removed, False if there is not enough pieces.
     */
    @Handler
    public boolean removeNumberOfItemsFromStock(ItemBO processedItem){
        //TODO do it in some transaction
        ItemBO itemInStock = getItem(processedItem.getSku());
        if(itemInStock.getAmount() >= processedItem.getAmount()){
            itemInStock.setAmount( itemInStock.getAmount() - processedItem.getAmount());
            
            //I am not sure if this step is neccessary
            this.localStock.put(itemInStock.getSku(), itemInStock);
            return true;
        } else {
            //not enough items in local stock
            return false;
        }
    } 
    
    /**
     * Gets item from local stock based on it's identifier (.sku)
     * @param itemId item's identifier
     * @return wanted Item or null
     */
    private ItemBO getItem(String itemId){
        return this.localStock.get(itemId);
    }
}
