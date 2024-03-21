package com.tsysinfo.billing;

import java.io.Serializable;

public class ExpenseType1 implements Serializable {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getexpensetype()
    {
        return expensetype;
    }

    public void setexpensetype(String expensetype)
    {
        this.expensetype = expensetype;
    }

    private  String id,expensetype;
}
