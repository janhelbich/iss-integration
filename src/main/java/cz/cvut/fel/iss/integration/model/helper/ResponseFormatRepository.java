package cz.cvut.fel.iss.integration.model.helper;

import java.util.Objects;

/**
 * Created by Jan Srogl on 30.10.15.
 */
public enum ResponseFormatRepository
{
    OK("OK","Order has been successfully generated"),
    BAD_INPUT_FORMAT("BAD_REQUEST","Invalid data format was recieved"),
    BAD_OUTPUT_DATA_FORMAT("BadOutputDataFormat"," Response build FAILED- Following errors occured");


    private final String status;
    private final String description;

    ResponseFormatRepository(String status, String description) {
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public static ResponseFormatRepository getFromStatus(String s)
    {
        if (s == null) return ResponseFormatRepository.BAD_OUTPUT_DATA_FORMAT;

        for (ResponseFormatRepository r : ResponseFormatRepository.values())
        {
            if (Objects.equals(r.getStatus(), s)) return r;
        }
        return ResponseFormatRepository.BAD_OUTPUT_DATA_FORMAT;
    }
}
