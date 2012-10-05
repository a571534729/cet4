package com.jayz.cet4.view.fragment;

import java.io.IOException;

import com.jayz.R;
import com.jayz.cet4.common.IOUtil;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.data.ClozeDAO;
import com.jayz.cet4.data.ListeningDAO;
import com.jayz.cet4.data.ReadingDAO;
import com.jayz.cet4.data.SelectingDAO;
import com.jayz.cet4.data.WritingOrTranslationDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TopicFragment extends Fragment{
	
	private TextView articleContent;
	private String content;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	/**
	 * 初始化对象
	 */
	private void init(){
		try {
			content = IOUtil.readInputStrem2Str(getActivity().getAssets().open("q_cloze.txt"),"gbk");
		} catch (IOException e) {
			LogUtil.e(e);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout fragmentTopic = (LinearLayout) inflater.inflate(R.layout.fragment_topic, container, false);
		articleContent = (TextView) fragmentTopic.findViewById(R.id.article_content);
		articleContent.setText(content);
		return fragmentTopic;
	}
	
	
	
}
