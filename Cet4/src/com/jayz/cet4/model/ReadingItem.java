package com.jayz.cet4.model;

public class ReadingItem extends Model {

	private static final long serialVersionUID = 3494835144386010970L;
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
