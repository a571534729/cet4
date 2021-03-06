package com.jayz.cet4.data;

import java.util.ArrayList;
import java.util.List;
import com.jayz.cet4.common.RegexUtil;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;

/**
 * 这个类属于选词填空的DAO,getSubjects返回的是多个题目的对象
 * @author Jayz
 *
 */
public class SelectingDAO {
	
	private String fileStr;
	
	/**
	 * 
	 * @param fileStr整个文件的String内容
	 */
	public SelectingDAO(String fileStr) {
		this.fileStr = fileStr;
	}
	
	/**
	 * 通过文件内容获得文件所包含的问题
	 * @return
	 */
	public List<Article> getSubjects(){
		List<Article> articles = new ArrayList<Article>();
		//整篇文章的正则表达式
		String articleRegex = "(?<=\\<article\\>)[\\s\\S]*?(?=\\</article\\>)";
		//每篇文章标题的正则表达式
		String articleTitleRegex = "(?<=\\<articleTitle\\>)[\\s\\S]*?(?=\\</articleTitle\\>)";
		//每篇文章内容的正则表达式
		String articleContentRegex = "(?<=\\<articleContent\\>)[\\s\\S]*?(?=\\</articleContent\\>)";
		//每篇文章整个问题(包括答案)的正则表达式
		String subjectRegex = "(?<=\\<subject\\>)[\\s\\S]*?(?=\\</subject\\>)";
		//答题的正则表达式
		String choiceRegex = "(?<=\\<choice\\>)[\\s\\S]*?(?=\\</choice\\>)";
		//正确答案的正则表达式
		String answerRegex = "(?<=\\<answer\\>)[\\s\\S]*?(?=\\</answer\\>)";
		//整篇文章解析的正则表达式
		String articleIntroductionRegex = "(?<=\\<articleIntroduction\\>)[\\s\\S]*?(?=\\</articleIntroduction\\>)";
		//答案解析的正则表达式
		String introductionRegex = "(?<=\\<introduction\\>)[\\s\\S]*?(?=\\</introduction\\>)";
		/** 组装Article对象 */
		List<String> articlesStr = RegexUtil.parseFileStr(articleRegex, fileStr);
		if(articlesStr.size()!=0){
			/** 此部分不包括正确答案和解析*/
			for(int i = 0;i<articlesStr.size();i++){
				Article article = new Article(i);
				//截取文章标题
				List<String> articleTitleList = RegexUtil.parseFileStr(articleTitleRegex, articlesStr.get(i));
				if(articleTitleList.size()>0&&articleTitleList.get(0)!=null){
					article.setArticleTitle(articleTitleList.get(0));
				}
				//截取文章
				List<String> articleContentList = RegexUtil.parseFileStr(articleContentRegex, articlesStr.get(i));
				if(articleContentList.size()>0&&articleContentList.get(0)!=null){
					article.setArticleContent(articleContentList.get(0));
				}
				//截取正确答案集
				List<String> answersStr = RegexUtil.parseFileStr(answerRegex, articlesStr.get(i));
				//截取答案解析集
				List<String> introductionsStr = RegexUtil.parseFileStr(introductionRegex, articlesStr.get(i));
				//截取题目集
				List<String> subjectsStr = RegexUtil.parseFileStr(subjectRegex, articlesStr.get(i));
				if(subjectsStr.size()!=0){
					List<Question> questionList = new ArrayList<Question>();
					for(int j = 0;j<subjectsStr.size();j++){
						Question q = new Question(j);
						//截取选项
						List<String> choicesStr = RegexUtil.parseFileStr(choiceRegex,articlesStr.get(i) );
						if(subjectsStr.size()!=0){
							q.setChoice(choicesStr);
						}
						//插入正确答案
						if(answersStr.get(j)!=null){
							q.setAnswer(answersStr.get(j));
						}
						//插入答案解析
						if(introductionsStr.get(j)!=null){
							q.setAnalysis(introductionsStr.get(j));
						}
						questionList.add(q);
					}
					article.setQuestion(questionList);
				}
				//截取整个题目的解析
				List<String> articleIntroductionStr = RegexUtil.parseFileStr(articleIntroductionRegex, articlesStr.get(i));
				article.setAnalysis(articleIntroductionStr.get(0));
				articles.add(article);
			}
		}
		
		return articles;
		
	}
		
}
