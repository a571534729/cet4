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
import com.jayz.cet4.model.TopicItem;

public class TopicItemsDAO {

		private List<TopicItem> topicItems;
		
		//获得所有真题的实例
		public List<TopicItem> getAllTopicItems(InputStream xmlStream){
			
			SaxParseService sax = new SaxParseService();			 
			topicItems = sax.getTopicItems(xmlStream);
			return topicItems;
		}
			

	//解析XML数据
	class SaxParseService extends DefaultHandler{
		
		private List<TopicItem> topicItems;
		private TopicItem topicItem;
		private String perTag = null;//记录正在解析的节点
			
		public List<TopicItem> getTopicItems(InputStream xmlStream){
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
			return handler.topicItems;
		}
		
		@Override
		public void startDocument() throws SAXException {
			topicItems = new ArrayList<TopicItem>();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("TopicItem".equals(localName)){
				topicItem = new TopicItem();
				topicItem.setId(attributes.getValue(0));
			}
			perTag = localName;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("TopicItem".equals(localName)){
				topicItems.add(topicItem);
				topicItem = null;
			}
			perTag = null;
		};
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(perTag!=null){
				String content = new String(ch,start,length);
				if("Name".equals(perTag)){
					topicItem.setName(content);
				}else if("DownLoadAdd".equals(perTag)){
					topicItem.setDownLoadAdd(content);
				}
			}
		}
		
		
	}
	
}

