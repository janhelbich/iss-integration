package cz.cvut.fel.iss.integration.model.helper;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.ItemTypes;
import cz.cvut.fel.iss.integration.model.bo.ObjednavkaBO;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaDTO;
import cz.cvut.fel.iss.integration.model.dto.ObjednavkaItemDTO;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jan Srogl on 23.10.15.
 */
public class ObjednavkaConverter
{
    /**
     * Transforms Objednavka, adds unique ID and also transforms list of items.
     * @param dto goven Order
     * @param newOrderId new unique identifier which will be used for transformed OrderBO
     * @return Given order transformed to BO.
     */
    public ObjednavkaBO getObjednavka(ObjednavkaDTO dto, int newOrderId)
    {
        ObjednavkaBO newOrder = new ObjednavkaBO();
        newOrder.setIDobjed(newOrderId);
        newOrder.setWantedItems(getItemsBO(dto.getWantedItems()));
        return newOrder;
    }
    
    /**
     * Transforms Items from given list into ItemsBO and adds them to list.
     * It sums amount of the same items to only one element per each Item in list.
     * @param itemsDTO list of Items from OrderDTO
     * @return Corresponding transformed and reduced List of ItemsBO
     */
    private List<ItemBO> getItemsBO(List<ObjednavkaItemDTO> itemsDTO){
        List<ItemBO> items = new ArrayList<>();
        for(ObjednavkaItemDTO iDTO : itemsDTO){
            ItemBO item = getItemBO(iDTO);
            addItemBOToList(item, items);
        }
        
        return items;
    }
    
    /**
     * Adds given item into given list. If list already contains item with the same id,
     * method just adds amount of given item to the item in list. If the same item is not
     * in list, it adds given item into list.
     * @param newItem Item which should be added to list
     * @param items List of items
     */
    private void addItemBOToList(ItemBO newItem, List<ItemBO> items){
        for(ItemBO itemInList:items){
            if(itemInList.getSku().equals(newItem.getSku())){
                itemInList.addAmount(newItem.getAmount());
                return;
            }
        }
        items.add(newItem);
    }
    
    /**
     * Makes ItemBO from ItemDTO. Uses given sku and amount, sets 
     * ItemType to ORDERED_ITEM, price should remain at null.
     * @param itemDTO given Item
     * @return Transformed Item
     */
    private ItemBO getItemBO(ObjednavkaItemDTO itemDTO){
        ItemBO item = new ItemBO(itemDTO.getSku(), itemDTO.getAmount());
        item.setItemType(ItemTypes.ORDERED_ITEM);
        return item;
    }
        
    public ObjednavkaDTO getObjednavkaDTO(ObjednavkaBO obj)
    {
        //TODO
        return null;
    }
    

}
