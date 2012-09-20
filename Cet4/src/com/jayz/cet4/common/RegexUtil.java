package com.jayz.cet4.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
	/**
	 * 
	 * @param regex 需要解析的正则表达式，
	 * @param data 需要解析的数据
	 * @return
	 */
	public static  List<String> parseFileStr(String regex,String data){
		List<String> texts = new ArrayList<String>();
		Pattern p = Pattern.compile(regex);
		Matcher matcher = p.matcher(data);
		while(matcher.find()){
			texts.add(matcher.group());
		}
		return texts;		
	}
}
