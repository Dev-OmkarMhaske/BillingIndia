package com.tsysinfo.billing;

public class CustomerSearchResults {
	private String id = "";
	private String name = "";
	private String address = "";
	private String phone = "";
	private String Log;

	public String getLog() {
		return Log;
	}

	public void setLog(String log) {
		Log = log;
	}

	public String getLatl() {
		return Latl;
	}

	public void setLatl(String latl) {
		Latl = latl;
	}

	private String Latl;

	public void setID(String id) {
		this.id = id;
	}

	public String getID() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPhone() {
		return phone;
	}
}
