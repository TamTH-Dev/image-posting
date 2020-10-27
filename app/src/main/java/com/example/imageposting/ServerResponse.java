package com.example.imageposting;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ServerResponse {
    @SerializedName("message")
    @Expose
    private String message;

    /**
     * @return message
     */
    public String getMessage() {
        return this.message;
    }
}
