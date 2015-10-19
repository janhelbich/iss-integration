package cz.cvut.fel.iss.integration.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Instance objednavky
 * @author Jan Srogl
 */
public class Objednavka
{
    
    private int idObjednavka;
    private List<Item> wantedItems;
    //private int customer; should it be defined here?
    
    public Objednavka(){
        this.wantedItems = new ArrayList<>();
    }

    public int getIDobjed() {
        return idObjednavka;
    }

    public void setIDobjed(int IDobjed) {
        this.idObjednavka = IDobjed;
    }

    public List<Item> getWantedItems() {
        return wantedItems;
    }

    public void setWantedItems(List<Item> wantedItems) {
        this.wantedItems = wantedItems;
    }
    
}
