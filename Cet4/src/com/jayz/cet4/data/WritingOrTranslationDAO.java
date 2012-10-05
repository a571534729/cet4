package com.jayz.cet4.data;

import java.util.ArrayList;
import java.util.List;
import com.jayz.cet4.common.RegexUtil;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;

/**
 * 这个类属于写作和翻译的DAO，getSubjects返回的是一个Article
 * @author Jayz
 *
 */
public class WritingOrTranslationDAO {
	
	private String fileStr;
	
	/**
	 * 
	 * @param fileStr整个文件的String内容
	 */
	public WritingOrTranslationDAO(String fileStr) {
		this.fileStr = fileStr;
	}
	
	/**
	 * 通过文件内容获得文件所包含的问题
	 * @return
	 */
	public Article getSubjects(){
		Article article = new Article(0);
		//每篇文章内容的正则表达式
		String articleContentRegex = "(?<=\\<articleContent\\>)[\\s\\S]*?(?=\\</articleContent\\>)";
		//正确答案的正则表达式
		String answerRegex = "(?<=\\<answer\\>)[\\s\\S]*?(?=\\</answer\\>)";
		//答案解析的正则表达式
		String introductionRegex = "(?<=\\<introduction\\>)[\\s\\S]*?(?=\\</introduction\\>)";
		/** 组装Article对象 */
		//截取文章
		List<String> articleContentList = RegexUtil.parseFileStr(articleContentRegex, fileStr);
		if(articleContentList.size()>0&&articleContentList.get(0)!=null){
			article.setArticleContent(articleContentList.get(0));
		}
		List<Question> questionList = new ArrayList<Question>();
		Question q = new Question(0);
		//截取正确答案
		List<String> answersStr = RegexUtil.parseFileStr(answerRegex, fileStr);
		if(answersStr.size()>0&&answersStr.get(0)!=null){					
			q.setAnswer(answersStr.get(0));
		}
		//截取答案解析
		List<String> introductionsStr = RegexUtil.parseFileStr(introductionRegex, fileStr);
		if(introductionsStr.size()>0&&introductionsStr.get(0)!=null){
			q.setAnalysis(introductionsStr.get(0));
		}
		questionList.add(q);
		article.setQuestion(questionList);
		return article;
		
	}
		
}
