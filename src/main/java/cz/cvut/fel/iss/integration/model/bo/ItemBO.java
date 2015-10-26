/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cvut.fel.iss.integration.model.bo;

/**
 * copy of https://github.com/qa/course-sys-int-systems-exam/blob/master/common/src/main/java/com/redhat/brq/integration/examination/common/Item.java
 * @author Mira
 */
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ItemBO {
    @Id @Column(name = "id")
    private String sku; // stock-keeping unit. google translation

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "count")
    private int amount;

    private ItemTypes itemType;
    
    public ItemBO() {
    }

    public ItemBO(final String sku, final int price, final int amount, ItemTypes type) {
            this.sku = sku;
            this.price = new BigDecimal(price);
            this.amount = amount;
            this.itemType = type;
    }

    public ItemBO(final String sku, final int amount) {
            this.sku = sku;
            this.amount = amount;
            this.price = null;
            this.itemType = null;
    }

    /**
     * Checks if Sku != null and price is bigger than zero and amount is bigger than zero.
     * @return TRUE if Sku != null and price is bigger than zero and amount is bigger than zero. FALSE otherwise.
     */
    public boolean isValid(){
        return (this.sku != null 
                && this.price.compareTo(BigDecimal.ZERO) > 0
                && this.amount > 0);
    }
    
    
    public BigDecimal getPrice() {
            return price;
    }
    public void setPrice(final BigDecimal price) {
            this.price = price;
    }
    public int getAmount() {
            return amount;
    }
    public void setAmount(final int amount) {
            this.amount = amount;
    }

    /**
     * Adds given amount to this amount.
     * @param amount 
     */
    public void addAmount(int amount){
        this.amount +=amount;
    }
    
    
    public String getSku() {
            return sku;
    }

    public void setSku(String sku) {
            this.sku = sku;
    }	

    @Override
    public String toString() {
            return "Item [sku=" + sku + ", price=" + price + ", amount=" + amount
                            + "]";
    }

    public ItemTypes getItemType() {
        return itemType;
    }

    public void setItemType(ItemTypes itemType) {
        this.itemType = itemType;
    }
        
    
    
}

