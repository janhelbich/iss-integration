package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.bo.OutputResponse;
import cz.cvut.fel.iss.integration.model.helper.ResponseFormatRepository;
import org.apache.camel.Exchange;
import org.apache.camel.Message;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created by Jan Srogl on 22.10.15.
 */
public class ResponseBuilder
{

    public OutputResponse generateNewResponse(Exchange exch)  {
        OutputResponse response = new OutputResponse();

        //Get Response type from response repository based on incoming status
        ResponseFormatRepository dec = ResponseFormatRepository.getFromStatus((String) exch.getIn().getHeader("status"));

        response.setStatus(dec.getStatus());
        response.setResponse(dec.getDescription());
        response.setVars(new TreeMap<>());

        //TODO - dodelat generovani odpovedii
        switch (dec)
        {
            case OK: {
                if (exch.getIn().getHeader("objednavka") != null)
                {
                    response.getVars().put("objednavka", (String) exch.getIn().getHeader("status"));
                }
                else response.getVars().put("objednavka","UNDEFINED");
                break;
            }
            case BAD_INPUT_FORMAT:
                break;
            case BAD_OUTPUT_DATA_FORMAT: {
                if (exch.getIn().getHeader("status") != null)
                {
                    response.getVars().put("status", (String) exch.getIn().getHeader("status"));
                }
                else response.getVars().put("status","UNDEFINED");
                break;
            }
        }
        return response;
    }
}
