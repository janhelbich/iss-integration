package cz.cvut.fel.iss.integration.model;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by Jan Srogl on 12.11.15.
 */
public class CheapestItemPickerProcessor implements Processor
{
    //header names
    final static String LOCAL = "localItem";
    final static String SUPPLIER_A = "supplierAItem";
    final static String SUPPLIER_B = "supplierBItem";

    @Override
    public void process(Exchange exchange) throws Exception
    {
        ItemBO loc = (ItemBO) exchange.getIn().getHeader(LOCAL);
        ItemBO supA = (ItemBO) exchange.getIn().getHeader(SUPPLIER_A);
        ItemBO supB = (ItemBO) exchange.getIn().getHeader(SUPPLIER_B);

        exchange.getIn().removeHeader(LOCAL);
        exchange.getIn().removeHeader(SUPPLIER_A);
        exchange.getIn().removeHeader(SUPPLIER_B);

        ItemBO result = null;
        if (supA.isAvailable() && supB.isAvailable())
        {
            switch (supA.getPrice().compareTo(supB.getPrice()))
            {
                case 1:
                {
                    result = supA;
                    break;
                }
                case 0:
                {
                    result = supA; // nebo result = supB; - je to jedno :-)
                    break;
                }
                case -1:
                {
                    result = supB;
                    break;
                }
            }
        }
        else
        {
            if (supA.isAvailable())
            {
                result = supA;
            }
            else /* jinak je dostupny supB*/
            {
                result = supB;

            }
        }
        
        //pokud je vetsi cena nez na lokalu -> je to VIP
        if (result.getPrice().compareTo(loc.getPrice()) == 1)
        {
            result.setVipStatus(true);
        }

        exchange.getIn().setBody(result,ItemBO.class);
    }
}
