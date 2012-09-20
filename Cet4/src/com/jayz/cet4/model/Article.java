package com.jayz.cet4.model;

import java.util.List;

public class Article extends Model{

	private static final long serialVersionUID = -4633676357768827444L;
	
	/**题目显示的序号*/
	private int order;
	/**题目文章的标题*/
	private String articleTitle;
	/**题目文章的内容*/
	private String articleContent;
	/**题目答题的问题*/
	private List<Question> question;
	private String analysis;
	
	public Article(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return order;
	}
	public String getArticleTitle() {
		return articleTitle;
	}
	public void setArticleTitle(String articleTitle) {
		this.articleTitle = articleTitle;
	}
	public String getArticleContent() {
		return articleContent;
	}
	public void setArticleContent(String articleContent) {
		this.articleContent = articleContent;
	}
	
	public List<Question> getQuestion() {
		return question;
	}

	public void setQuestion(List<Question> question) {
		this.question = question;
	}

	public String getAnalysis() {
		return analysis;
	}
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
	
	
	

}
