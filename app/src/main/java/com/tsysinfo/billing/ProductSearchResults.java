package com.tsysinfo.billing;

public class ProductSearchResults {
	private String id = "";
	private String name = "";
	private String description = "";
	private String price = "";
	private String image = "";
	private String act = "";
	private String Stcok=" ";


	public String getStcok() {
		return Stcok;
	}

	public void setStcok(String stcok) {
		Stcok = stcok;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	private  String remark="";

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	private String unit = "";

	public String getCsz() {
		return csz;
	}

	public void setCsz(String csz) {
		this.csz = csz;
	}

	private String csz = "";



	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	private  boolean checked =true;
	private String quantity = "";

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDp() {
		return dp;
	}

	public void setDp(String dp) {
		this.dp = dp;
	}

	private  String dp="";
	private String rot = "";

	public String getRot() {
		return rot;
	}

	public void setRot(String rot) {
		this.rot = rot;
	}

	public String getPrate() {
		return prate;
	}

	public void setPrate(String prate) {
		this.prate = prate;
	}

	private String prate = "";
	
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

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPrice() {
		return price;
	}
	
	public void setImage(String image) {
		this.image = image;
	}

	public String getImage() {
		return image;
	}
	
	public void setAct(String act) {
		this.act = act;
	}

	public String getAct() {
		return act;
	}
	
	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getQuantity() {
		return quantity;
	}


	@Override
	public String toString() {
		return "ProductSearchResults{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", price='" + price + '\'' +
				", image='" + image + '\'' +
				", act='" + act + '\'' +
				", Stcok='" + Stcok + '\'' +
				", remark='" + remark + '\'' +
				", unit='" + unit + '\'' +
				", csz='" + csz + '\'' +
				", checked=" + checked +
				", quantity='" + quantity + '\'' +
				", dp='" + dp + '\'' +
				", rot='" + rot + '\'' +
				", prate='" + prate + '\'' +
				'}';
	}
}
