package com.tsysinfo.billing;

import java.io.Serializable;

public class ExpenseTown implements Serializable {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    private  String id,town,townname;
}
