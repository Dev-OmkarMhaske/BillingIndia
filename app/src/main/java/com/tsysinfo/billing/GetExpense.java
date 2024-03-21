package com.tsysinfo.billing;

public class GetExpense {


    private String Date = "";
    private String MainCategory = "";
    private String SubCategory = "";
    private String Remarks = "";
    private String Amount = "";




    public String getDate()
    {
        return Date;
    }
    public void setDate(String Date)
    {
        this.Date = Date;
    }
    public String getMainCategory() {
        return MainCategory;
    }
    public void setMainCategory(String MainCategory)
    {
        this.MainCategory = MainCategory;
    }
    public String getSubCategory()
    {
        return SubCategory;
    }
    public void setSubCategory(String SubCategory) {
        this.SubCategory = SubCategory;
    }
    public String getRemarks() {
        return Remarks;
    }
    public void setRemarks(String Remarks) {
        this.Remarks = Remarks;
    }
    public String getAmount() {
        return Amount;
    }
    public void setAmount(String Amount) {
        this.Amount = Amount;
    }
}
