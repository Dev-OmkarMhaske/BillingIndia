package com.tsysinfo.billing;

public class Town {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Town( String id,String town) {
        this.town = town;
        this.id=id;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    private  String id,town,townname;
}
