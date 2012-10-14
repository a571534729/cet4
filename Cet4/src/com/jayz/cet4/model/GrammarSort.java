package com.jayz.cet4.model;

import java.util.List;

public class GrammarSort extends Model {

	private static final long serialVersionUID = -6558833558309535088L;
	
	private String id;
	private String name;
	private List<GrammarItem> grammars;
	
	public List<GrammarItem> getGrammars() {
		return grammars;
	}
	public void setGrammars(List<GrammarItem> grammars) {
		this.grammars = grammars;
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
