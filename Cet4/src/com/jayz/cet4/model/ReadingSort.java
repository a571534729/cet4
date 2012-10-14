package com.jayz.cet4.model;

import java.util.List;

public class ReadingSort extends Model {

	private static final long serialVersionUID = 4967791252375636549L;
	
	private String id;
	private String name;
	private List<ReadingItem> readings;

	public List<ReadingItem> getReadings() {
		return readings;
	}
	public void setReadings(List<ReadingItem> readings) {
		this.readings = readings;
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
