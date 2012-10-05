package com.jayz.cet4.view;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.jayz.R;
import com.jayz.cet4.common.Constants;
import com.jayz.cet4.common.IOUtil;
import com.jayz.cet4.view.ui.topicwidegt.ClozeContent;
import com.jayz.cet4.view.ui.topicwidegt.ListeningContent;
import com.jayz.cet4.view.ui.topicwidegt.QuestionContent;
import com.jayz.cet4.view.ui.topicwidegt.SelectingContent;
import com.jayz.cet4.view.ui.topicwidegt.WritingOrTranslationContent;

public class TopicActivity extends Activity{
	
	public static final int OpenAnimation = 100;
	public static final int CloseAnimation = 101;
	
	private Button topicTypeBtn;
	private RelativeLayout sortTopicTL;
	private LinearLayout titleTopic;
	private RadioGroup typesTopicRG;
	private LinearLayout articleContent;
	
	//当前所操作的模块
	private int currentTopicType = -1;
	
	
	private ListeningContent listeningContent;
	private QuestionContent readingContent;
	private SelectingContent selectingContent;
	private ClozeContent clozeContent;
	private WritingOrTranslationContent translationContent;
	private WritingOrTranslationContent writingContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_topic);
        
        //初始化view
        topicTypeBtn = (Button) findViewById(R.id.topicType);
        sortTopicTL = (RelativeLayout) findViewById(R.id.sortTopic);
        titleTopic = (LinearLayout) findViewById(R.id.titleTopic);
        typesTopicRG = (RadioGroup) findViewById(R.id.typesTopic);
        articleContent = (LinearLayout) findViewById(R.id.articleCotent);
        //实现分类动画
        final Animation anClose = AnimationUtils.loadAnimation(TopicActivity.this, R.anim.close);
        final Animation anOpen = AnimationUtils.loadAnimation(TopicActivity.this, R.anim.open);
        anClose.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				sortTopicTL.setVisibility(View.GONE);
			}
		});

        topicTypeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(sortTopicTL.getVisibility()==View.GONE){
					sortTopicTL.setVisibility(View.VISIBLE);
					sortTopicTL.startAnimation(anOpen);
				}else if(sortTopicTL.getVisibility()==View.VISIBLE){
					sortTopicTL.startAnimation(anClose);
				}
			}
		});
        
        typesTopicRG.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	        	        	        	        	
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(currentTopicType!=-1){
					stopMusic();
				}
				switch (checkedId) {
				case R.id.radioTypeListening:
					currentTopicType = Constants.TypeTopic.LISTENING_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.LISTENING_TXT);
					articleContent.removeAllViews();
					if(listeningContent==null){
						listeningContent = 
							new ListeningContent(TopicActivity.this, getContent(Constants.TypeTopic.LISTENING_TYPE));
					}
					articleContent.addView(listeningContent);
					break;
				case R.id.radioTypeReading:
					currentTopicType = Constants.TypeTopic.READING_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.READING_TXT);
					articleContent.removeAllViews();
					if(readingContent == null){
						readingContent = 
						new QuestionContent(TopicActivity.this, getContent(Constants.TypeTopic.READING_TYPE));
					}
					articleContent.addView(readingContent);
					break;
				case R.id.radioTypeSelecting:
					currentTopicType = Constants.TypeTopic.SELECTING_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.SELECTING_TXT);
					articleContent.removeAllViews();
					if(selectingContent == null){
						selectingContent = 
						new SelectingContent(TopicActivity.this, getContent(Constants.TypeTopic.SELECTING_TYPE));
					}
					articleContent.addView(selectingContent);
					break;
				case R.id.radioTypeCloze:
					currentTopicType = Constants.TypeTopic.CLOZE_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.CLOZE_TYPE_TXT);
					articleContent.removeAllViews();
					if(clozeContent == null){
						clozeContent = 
						new ClozeContent(TopicActivity.this, getContent(Constants.TypeTopic.CLOZE_TYPE));
					}
					articleContent.addView(clozeContent);
					break;
				case R.id.radioTypeTranslation:
					currentTopicType = Constants.TypeTopic.TRANSLATION_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.TRANSLATION_TXT);
					articleContent.removeAllViews();
					if(translationContent == null){
						translationContent = 
						new WritingOrTranslationContent(TopicActivity.this, getContent(Constants.TypeTopic.TRANSLATION_TYPE),R.layout.question_writeortran);
					}
					articleContent.addView(translationContent);
					break;
				case R.id.radioTypeWriting:
					currentTopicType = Constants.TypeTopic.WRITING_TYPE;
					topicTypeBtn.setText(Constants.TypeTopicText.WRITING_TXT);
					articleContent.removeAllViews();
					if(writingContent == null){
						writingContent = 
						new WritingOrTranslationContent(TopicActivity.this, getContent(Constants.TypeTopic.WRITING_TYPE),R.layout.question_writeortran);
					}
					articleContent.addView(writingContent);
					break;					
				}
			}
		});
	}
	
	/**判断是不是listenTopic模块，是的话，需要停止音乐*/
	private void stopMusic(){
		if(currentTopicType==Constants.TypeTopic.LISTENING_TYPE){
			listeningContent.pauseMusic();
		}
	}
	
	@Override
	protected void onDestroy() {
		if(listeningContent!=null){
			listeningContent.destroyMusic();
		}
		super.onDestroy();
	}
	
	/**根据获得所需要文件的内容*/
	private String getContent(int topicType){
		String content = null;
		try {
			switch (topicType) {
			case Constants.TypeTopic.LISTENING_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_listen.txt"),"gbk");
				break;
			case Constants.TypeTopic.READING_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_reading.txt"),"gbk");
				break;
			case Constants.TypeTopic.SELECTING_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_select.txt"),"gbk");
				break;
			case Constants.TypeTopic.CLOZE_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_cloze.txt"),"gbk");
				break;
			case Constants.TypeTopic.TRANSLATION_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_translation.txt"),"gbk");
				break;				
			case Constants.TypeTopic.WRITING_TYPE:
				content = IOUtil.readInputStrem2Str(this.getAssets().open("q_writing.txt"),"gbk");
				break;				
			default:
				break;
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	
	
}
