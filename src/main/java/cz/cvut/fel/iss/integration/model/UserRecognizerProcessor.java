package cz.cvut.fel.iss.integration.model;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created by Jan Srogl on 12.11.15.
 */
public class UserRecognizerProcessor implements Processor
{

    @Override
    public void process(Exchange exchange) throws Exception
    {
        //TODO naparsovat jmeno uzivatele a zhodnotit, jestli je to VIP user -> nastavit hlavicku  'VIPuser' na true/false
    }
}
