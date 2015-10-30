package cz.cvut.fel.iss.integration.service;

import cz.cvut.fel.iss.integration.model.bo.OutputResponse;

/**
 * Created by Jan Srogl on 22.10.15.
 */
public class ResponseBuilder
{
    OutputResponse response;

    public void generateNewResponse()
    {
        this.response = new OutputResponse();
        //TODO - dodelat generovani odpovedi
    }
    public OutputResponse getResponse() //@ExchangeProperty("outputFormat")
    {
        return this.response;
    }
}
