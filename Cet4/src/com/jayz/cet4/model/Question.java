package com.jayz.cet4.model;

import java.util.List;

public class Question extends Model{

	private static final long serialVersionUID = -2074978755514701841L;
	
	/**答题问题显示的序号*/
	private int order;
	/**答题的问题*/
	private String question;
	/**答题的选项*/
	private List<String> choice;
	/**问题的正确答案*/
	private String answer;
	/**问题的解析*/
	private String analysis;
	/**对话的原文(只适用于听力)*/
	private String conversation;
	
	public Question(int order) {
		this.order = order;
	}
	
	public int getOrder() {
		return order;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public List<String> getChoice() {
		return choice;
	}

	public void setChoice(List<String> choice) {
		this.choice = choice;
	}

	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getAnalysis() {
		return analysis;
	}
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	public String getConversation() {
		return conversation;
	}

	public void setConversation(String conversation) {
		this.conversation = conversation;
	}
	
	

}
