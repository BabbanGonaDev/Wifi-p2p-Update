package com.example.wifi_p2p.Model;

import java.io.Serializable;

public class IntentModel implements Serializable {
    private String result;
    private String type;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
