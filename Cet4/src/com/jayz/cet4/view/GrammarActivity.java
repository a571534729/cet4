package com.jayz.cet4.view;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.jayz.R;
import com.jayz.cet4.common.Constants;
import com.jayz.cet4.common.IOUtil;
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.model.GrammarItem;
import com.jayz.cet4.view.base.BaseActivity;

import android.os.Bundle;
import android.widget.TextView;

public class GrammarActivity extends BaseActivity{
	
	//标题
	private TextView grammarItemTitle;
	//内容
	private TextView grammarItemContent;
	//获取的GrammarItem
	private GrammarItem grammarItem;
	
	public static String SUBSTRING_WORD = "=";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_grammar);
		initView();
		grammarItem = (GrammarItem) getIntent().getExtras().get(Constants.bundleKey.grammarItem);
		setData();
		
	}
	
	private void initView(){
		grammarItemTitle = (TextView) findViewById(R.id.grammarItemTitle);
		grammarItemContent = (TextView) findViewById(R.id.grammarItemContent);
	}
	
	private void setData(){
		grammarItemTitle.setText(grammarItem.getName());
		String content = null;
		try {
			String targetPath = Constants.PATH_RES +"/"+
					grammarItem.getDownLoadAdd().substring(grammarItem.getDownLoadAdd().lastIndexOf(SUBSTRING_WORD)+1);
			content = IOUtil.readFile2Str(targetPath, "gbk");
		} catch (FileNotFoundException e) {
			try {
				throw new TradException(e);
			} catch (TradException e1) {
				handleException(e1);
			}
		} catch (IOException e) {
			try {
				throw new TradException(e);
			} catch (TradException e1) {
				handleException(e1);
			}
		}
		
		grammarItemContent.setText(content);
	}
}
