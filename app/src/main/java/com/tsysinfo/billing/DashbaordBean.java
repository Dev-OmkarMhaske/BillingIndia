package com.tsysinfo.billing;

import java.io.Serializable;

public class DashbaordBean implements Serializable {
    String  RowNo,Head1,Particulars,Amount;

    public String getRowNo() {
        return RowNo;
    }

    public void setRowNo(String rowNo) {
        RowNo = rowNo;
    }

    public String getHead1() {
        return Head1;
    }

    public void setHead1(String head1) {
        Head1 = head1;
    }

    public String getParticulars() {
        return Particulars;
    }

    public void setParticulars(String particulars) {
        Particulars = particulars;
    }

    public String getAmount() {
        return Amount;
    }

    public void setAmount(String amount) {
        Amount = amount;
    }
}
