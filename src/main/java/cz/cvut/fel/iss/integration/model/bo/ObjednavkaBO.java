package cz.cvut.fel.iss.integration.model.bo;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance objednavky
 * @author Jan Srogl
 */
public class ObjednavkaBO
{
    
    private Integer idObjednavka;
    private List<ItemBO> wantedItems;
    //private int customer; should it be defined here?
    
    public ObjednavkaBO(){
        this.wantedItems = new ArrayList<>();
    }

    public Integer getIDobjed() {
        return idObjednavka;
    }

    public void setIDobjed(Integer IDobjed) {
        this.idObjednavka = IDobjed;
    }

    public List<ItemBO> getWantedItems() {
        return wantedItems;
    }

    public void setWantedItems(List<ItemBO> wantedItems) {
        this.wantedItems = wantedItems;
    }
    public String toString()
    {
        String s = this.idObjednavka + " ";
        for (ItemBO b : this.wantedItems)
        {
            s += " " + b.toString();
        }
        return "[" + s + "]";
    }
    
}
