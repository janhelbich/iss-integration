package cz.cvut.fel.iss.integration.model.bo;

import java.util.Map;

/**
 * Created by Jan Srogl on 16.10.15.
 */

public class OutputResponse
{
    String status;
    String response;
    Map<String,String> vars;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Map<String, String> getVars() {
        return vars;
    }

    public void setVars(Map<String, String> vars) {
        this.vars = vars;
    }
}
