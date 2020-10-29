package com.example.imageposting;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerResponse {
    @SerializedName("message")
    @Expose
    private String message;

    @SerializedName("error")
    @Expose
    private String error;

    /**
     * @return message
     */
    public String getMessage() {
        return this.message;
    }


    /**
     * @return error
     */
    public String getError() {
        return this.error;
    }

}