package com.jayz.cet4.model;

public class TopicItem extends Model {

	private static final long serialVersionUID = -2834439826499774284L;
	
	private String id;
	private String name;
	private String downLoadAdd;
	private long fileSize;
	
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public String getDownloadAdd() {
		return downLoadAdd;
	}
	public void setDownLoadAdd(String downLoadAdd) {
		this.downLoadAdd = downLoadAdd;
	}
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
	


	
}
