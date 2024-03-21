package com.tsysinfo.billing;

public class ReceiptSearchResults {
	private String date = "";
	private String billid = "";
	private String totalamt = "";
	private String outstandingamt = "";
    private String PartPayment;
	private String days;

	private String SoShortName;
	private String NetDue;

	public String getSoShortName() {
		return SoShortName;
	}

	public void setSoShortName(String soShortName) {
		SoShortName = soShortName;
	}

	public String getNetDue() {
		return NetDue;
	}

	public void setNetDue(String netDue) {
		NetDue = netDue;
	}

	public String getPartPayment() {
		return PartPayment;
	}

	public void setPartPayment(String partPayment) {
		PartPayment = partPayment;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getNotApproval() {
		return NotApproval;
	}

	public void setNotApproval(String notApproval) {
		NotApproval = notApproval;
	}

	private String NotApproval;
	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	private String custName;
	public String KEY_custNo = "";
	public String getKEY_custNo() {
		return KEY_custNo;
	}
	public void setKEY_custNo(String KEY_custNo) {
		this.KEY_custNo = KEY_custNo;
	}
	public String getKEY_dbkey() {
		return KEY_dbkey;
	}
	public void setKEY_dbkey(String KEY_dbkey) {
		this.KEY_dbkey = KEY_dbkey;
	}
	public String getKEY_dbprfx() {
		return KEY_dbprfx;
	}
	public void setKEY_dbprfx(String KEY_dbprfx) {
		this.KEY_dbprfx = KEY_dbprfx;
	}
	public String getKEY_dbno() {
		return KEY_dbno;
	}
	public void setKEY_dbno(String KEY_dbno) {
		this.KEY_dbno = KEY_dbno;
	}
	public String getKEY_dbdate() {
		return KEY_dbdate;
	}
	public void setKEY_dbdate(String KEY_dbdate) {
		this.KEY_dbdate = KEY_dbdate;
	}
	public String getKEY_dbsofar() {
		return KEY_dbsofar;
	}
	public void setKEY_dbsofar(String KEY_dbsofar) {
		this.KEY_dbsofar = KEY_dbsofar;
	}
	public  String KEY_dbkey = "";
	public  String KEY_dbprfx= "";
	public  String KEY_dbno = "";
	public  String KEY_dbdate = "";
	public  String KEY_dbsofar = "";





	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	
	public String getBillid() {
		return billid;
	}
	public void setBillid(String billid) {
		this.billid = billid;
	}
	public String getTotalamt() {
		return totalamt;
	}
	public void setTotalamt(String totalamt) {
		this.totalamt = totalamt;
	}
	public String getOutstandingamt() {
		return outstandingamt;
	}
	public void setOutstandingamt(String outstandingamt) {
		this.outstandingamt = outstandingamt;
	}
	
	
}
