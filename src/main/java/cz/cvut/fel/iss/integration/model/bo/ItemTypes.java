/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.fel.iss.integration.model.bo;

/**
 *
 * @author Mira
 */
public enum ItemTypes {
    /**
     * Item should specify amount which supplierA has and price
     */
    SUPPLIER_A_INFO,
    SUPPLIER_B_INFO,
    
    /**
     * Item should specify amount available on local stock and it's price
     */
    LOCAL_STOCK_INFO,   
    
    /**
     * Item should specify ordered number of pieces of Item and corresponding price from local stock
     */
    ORDERED_ITEM,
    
    /**
     * Wanted amount of this Item is prepared to be picked from supplier A
     * and also it's price is changed to supplierA's price
     */
    SELECTED_SUPPLIER_A,
    SELECTED_SUPPLIER_B,
    
    /**
     * Wanted amount of this Item is prepared to be picked from local stock.
     */
    SELECTED_LOCAL_STOCK
    
}
