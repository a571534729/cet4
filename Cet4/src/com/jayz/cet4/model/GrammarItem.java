package com.jayz.cet4.model;

public class GrammarItem extends Model {

	private static final long serialVersionUID = 9088075029851186755L;
	
	private String id;
	private String name;
	private String downLoadAdd;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId(){
		return id;
	}
	public String getDownLoadAdd() {
		return downLoadAdd;
	}
	public void setDownLoadAdd(String downLoadAdd) {
		this.downLoadAdd = downLoadAdd;
	}
	
	


	
}
