package com.tsysinfo.billing;
/**
 * Created by tsysinfo on 10/30/2017.
 */
public class OrderEntity {

    String orderNo;
    String OrderDate;
    String CustomerName;
    String TotalAmt;
    String TransType;


    public String getOrderNo() {
        return orderNo;
    }
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
    public String getOrderDate() {
        return OrderDate;
    }
    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }
    public String getCustomerName() {
        return CustomerName;
    }
    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }
    public String getTotalAmt() {
        return TotalAmt;
    }
    public void setTotalAmt(String totalAmt) {
        TotalAmt = totalAmt;
    }
    public String getSalesMan() {
        return SalesMan;
    }

    public String getTransType() {
        return TransType;
    }

    public void setTransType(String transType) {
        TransType = transType;
    }

    public void setSalesMan(String salesMan) {
        SalesMan = salesMan;
    }

    String SalesMan;
}
