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
import com.jayz.cet4.model.ReadingItem;
import com.jayz.cet4.model.ReadingSort;

public class ReadingItemsDAO {

		private List<ReadingSort> readingSorts;
		
		//获得所有读物的实例
		public List<ReadingSort> getAllReadingSorts(InputStream xmlStream){
			
			SaxParseService sax = new SaxParseService();			 
			readingSorts = sax.getReadingSorts(xmlStream);
			return readingSorts;
		}
			

	//解析XML数据
	class SaxParseService extends DefaultHandler{
		
		private List<ReadingSort> readingSorts;
		private ReadingSort readingSort;
		ReadingItem readingItem;
		private String perTag = null;//记录正在解析的节点
			
		public List<ReadingSort> getReadingSorts(InputStream xmlStream){
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
			return handler.readingSorts;
		}
		
		@Override
		public void startDocument() throws SAXException {
			readingSorts = new ArrayList<ReadingSort>();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if("ReadingSort".equals(localName)){
				readingSort = new ReadingSort();
				readingSort.setId(attributes.getValue(0));
			}else if("ReadingItem".equals(localName)){
				readingItem = new ReadingItem();
				readingItem.setId(attributes.getValue(0));
			}
			perTag = localName;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if("ReadingSort".equals(localName)){
				readingSorts.add(readingSort);
				readingSort = null;
			}else if("ReadingItem".equals(localName)){
				readingSort.getReadings().add(readingItem);
				readingItem = null;
			}
			perTag = null;
		};
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if(perTag!=null){
				String content = new String(ch,start,length);
				if("Name".equals(perTag)){
					readingSort.setName(content);
				}else if("ReadingItems".equals(perTag)){
					List<ReadingItem> readingItems = new ArrayList<ReadingItem>();
					readingSort.setReadings(readingItems);
				}else if("DownLoadAdd".equals(perTag)){
					readingItem.setDownLoadAdd(content);
				}else if("ItemName".equals(perTag)){
					readingItem.setName(content);
				}
			}
		}
		
		
	}
	
}

