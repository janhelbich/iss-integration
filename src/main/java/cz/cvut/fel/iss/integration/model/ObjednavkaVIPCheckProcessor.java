package cz.cvut.fel.iss.integration.model;

import cz.cvut.fel.iss.integration.model.bo.ItemBO;
import cz.cvut.fel.iss.integration.model.bo.ObjednavkaBO;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by Jan Srogl on 12.11.15.
 */
public class ObjednavkaVIPCheckProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception
    {
        ObjednavkaBO bo = exchange.getIn().getBody(ObjednavkaBO.class);
        

        for (ItemBO item : bo.getWantedItems())
        {
            if (item.getVipStatus())
            {
                exchange.getIn().setHeader("VIPOrder",true);
                return;
            }
        }
        exchange.getIn().setHeader("VIPOrder",false);
    }
}
