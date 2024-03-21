package com.tsysinfo.billing;

import java.io.Serializable;

public class ChequeIn implements Serializable {

   String Cust_Key,txnKey,DateOfReceipt,ChequeNo,ChequeDate,ChequeAmount,Billno,BillDate,BillAmount,AllocatedAmount,Rdays;

    public String getRdays() {
        return Rdays;
    }

    public void setRdays(String rdays) {
        Rdays = rdays;
    }

    public String getCust_Key() {
        return Cust_Key;
    }
    public void setCust_Key(String cust_Key) {
        Cust_Key = cust_Key;
    }

    public String getTxnKey() {
        return txnKey;
    }

    public void setTxnKey(String txnKey) {
        this.txnKey = txnKey;
    }

    public String getDateOfReceipt() {
        return DateOfReceipt;
    }

    public void setDateOfReceipt(String dateOfReceipt) {
        DateOfReceipt = dateOfReceipt;
    }

    public String getChequeNo() {
        return ChequeNo;
    }

    public void setChequeNo(String chequeNo) {
        ChequeNo = chequeNo;
    }

    public String getChequeDate() {
        return ChequeDate;
    }

    public void setChequeDate(String chequeDate) {
        ChequeDate = chequeDate;
    }

    public String getChequeAmount() {
        return ChequeAmount;
    }

    public void setChequeAmount(String chequeAmount) {
        ChequeAmount = chequeAmount;
    }

    public String getBillno() {
        return Billno;
    }

    public void setBillno(String billno) {
        Billno = billno;
    }

    public String getBillDate() {
        return BillDate;
    }

    public void setBillDate(String billDate) {
        BillDate = billDate;
    }

    public String getBillAmount() {
        return BillAmount;
    }

    public void setBillAmount(String billAmount) {
        BillAmount = billAmount;
    }

    public String getAllocatedAmount() {
        return AllocatedAmount;
    }

    public void setAllocatedAmount(String allocatedAmount) {
        AllocatedAmount = allocatedAmount;
    }
}
