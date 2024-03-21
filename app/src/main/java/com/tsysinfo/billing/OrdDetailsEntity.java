package com.tsysinfo.billing;

public class OrdDetailsEntity {
    private  String no;
    private String date;

    public String getUQC() {
        return UQC;
    }

    public void setUQC(String UQC) {
        this.UQC = UQC;
    }

    private String UQC;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuanti() {
        return quanti;
    }

    public void setQuanti(String quanti) {
        this.quanti = quanti;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getRot() {
        return rot;
    }

    public void setRot(String rot) {
        this.rot = rot;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    private String name;
    private String quanti;
    private String rate;
    private String rot;

    public String getGross() {
        return gross;
    }

    public void setGross(String gross) {
        this.gross = gross;
    }

    private String gross;
    private String total;
}
