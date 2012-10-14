package com.jayz.cet4.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.model.GrammarItem;
import com.jayz.cet4.model.GrammarSort;
import com.jayz.cet4.model.TopicItem;

public class GrammarItemsDAO {

		private List<GrammarSort> grammarSorts;
		
		//获得所有语法的实例
		public List<GrammarSort> getAllGrammarSorts(InputStream xmlStream){
			
			SaxParseService sax = new SaxParseService();			 
			grammarSorts = sax.getGrammarSorts(xmlStream);
			return grammarSorts;
		}
			

	//解析XML数据
	class SaxParseService extends DefaultHandler{
		
		private List<GrammarSort> grammarSorts;
		private GrammarSort grammarSort;
		GrammarItem grammarItem;
		private String perTag = null;//记录正在解析的节点
			
		public List<GrammarSort> getGrammarSorts(InputStream xmlStream){
			SaxParseService handler = null;
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parse = factory.newSAXParser();
				handler = new SaxParseService();
				parse.parse(xmlStream, handler);
			} catch (FactoryConfigurationError e) {
				LogUtil.e(e);
			} catch (ParserConfigurationException e) {
				LogUtil.e(e);
			} catch (SAXException e) {
				LogUtil.e(e);
			} catch (IOException e) {
				LogUtil.e(e);
			}
			return handler.grammarSorts;
		}
		
		@Override
		public void startDocument() throws SAXException {
			grammarSorts = new ArrayList<GrammarSort>();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("GrammarSort".equals(localName)){
				grammarSort = new GrammarSort();
				grammarSort.setId(attributes.getValue(0));
			}else if("GrammarItem".equals(localName)){
				grammarItem = new GrammarItem();
				grammarItem.setId(attributes.getValue(0));
			}
			perTag = localName;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("GrammarSort".equals(localName)){
				grammarSorts.add(grammarSort);
				grammarSort = null;
			}else if("GrammarItem".equals(localName)){
				grammarSort.getGrammars().add(grammarItem);
				grammarItem = null;
			}
			perTag = null;
		};
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(perTag!=null){
				String content = new String(ch,start,length);
				if("Name".equals(perTag)){
					grammarSort.setName(content);
				}else if("GrammarItems".equals(perTag)){
					List<GrammarItem> grammarItems = new ArrayList<GrammarItem>();
					grammarSort.setGrammars(grammarItems);
				}else if("DownLoadAdd".equals(perTag)){
					grammarItem.setDownLoadAdd(content);
				}else if("ItemName".equals(perTag)){
					grammarItem.setName(content);
				}
			}
		}
		
		
	}
	
}

