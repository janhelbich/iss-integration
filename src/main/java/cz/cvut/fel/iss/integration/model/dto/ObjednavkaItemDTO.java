package cz.cvut.fel.iss.integration.model.dto;


/**
 * Created by Jan Srogl on 23.10.15.
 */
public class ObjednavkaItemDTO
{

    private String sku; // stock-keeping unit. google translation
    private int amount;

    public String getSku() {
        return sku;
    }

    public int getAmount() {
        return amount;
    }

    /**
     * Checks if Sku != null and ordered amount is bigger than zero.
     * @return TRUE if Sku != null and amount is bigger than zero. FALSE otherwise.
     */
    public boolean isValid(){
        return (this.sku != null && this.amount > 0);
    }
}
