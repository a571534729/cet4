package com.jayz.cet4.view.ui.topicwidegt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jayz.R;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.data.ListeningDAO;
import com.jayz.cet4.data.ReadingDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;
import com.jayz.cet4.view.ui.panel.EasingType.Type;
import com.jayz.cet4.view.ui.panel.ExpoInterpolator;
import com.jayz.cet4.view.ui.panel.Panel;
import com.jayz.cet4.view.ui.panel.Panel.OnPanelListener;

public class QuestionContent extends RelativeLayout implements OnPanelListener{
	
	private String readingContent;
	private Context context;
	
	//定义存储类型变量
	private List<LinearLayout> readingArticlesList;
	private List<LinearLayout> answerReadingStr;
	private List<LinearLayout> introductionReadingStr;
	private Map<Integer, List<LinearLayout>> questionReadingMap;
	//这个index是表示第几篇文章
	private int articleIndex = 0;
	private Map<Integer, Integer> questionIndex = new HashMap<Integer, Integer>();
	
	//初始化控件
	private LinearLayout llReadingContent;
	private RadioGroup  rgReadingAnswer;
	private RadioButton questionReading;
	private LinearLayout llAnswerContent;
	private Button lastArticleReading;
	private Button nextArticleReading;
	private ImageButton lastQuestionBtn;
	private ImageButton nextQuestionBtn;
	private LinearLayout questionBtnGroup;
	
	private List<Article> readingArticles;
	
	
	private final Handler questionSVHandler = new Handler();
	
	public QuestionContent(Context context,String readingContent) {
		super(context);
		this.readingContent = readingContent;
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.question_reading, this,true);
		init();
		assemblyData();
		defaultView();
	}
	
	private void init(){
		ReadingDAO lDAO = new ReadingDAO(readingContent);
		readingArticles = lDAO.getSubjects();
		
		//定义存储类型变量
		readingArticlesList = new ArrayList<LinearLayout>();
		answerReadingStr = new ArrayList<LinearLayout>();
		introductionReadingStr = new ArrayList<LinearLayout>();
		//问题与文章应该是多对一的关系
		questionReadingMap = new HashMap<Integer, List<LinearLayout>>();
		
		//初始化控件
		llReadingContent = (LinearLayout) this.findViewById(R.id.llArticleContent);
		rgReadingAnswer = (RadioGroup) this.findViewById(R.id.rgAnswerReading);
		questionReading = (RadioButton) this.findViewById(R.id.answerReading);
		llAnswerContent = (LinearLayout) this.findViewById(R.id.llAnswerContent);
		lastArticleReading = (Button) this.findViewById(R.id.lastArticleReading);
		nextArticleReading = (Button) this.findViewById(R.id.nextArticleReading);
		lastQuestionBtn = (ImageButton) this.findViewById(R.id.lastQuestionBtn);
		nextQuestionBtn = (ImageButton) this.findViewById(R.id.nextQuestionBtn);
		questionBtnGroup = (LinearLayout) this.findViewById(R.id.questionBtn);
				
		//上拉控件设置
        Panel panel = (Panel)this.findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));
        
		//各个控件的监听
		rgReadingAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.questionReading:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionReadingMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
					questionBtnGroup.setVisibility(View.VISIBLE);
					break;
				case R.id.answerReading:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(answerReadingStr.get(articleIndex));
					questionBtnGroup.setVisibility(View.GONE);
					break;
				case R.id.introductionReading:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(introductionReadingStr.get(articleIndex));
					questionBtnGroup.setVisibility(View.GONE);				
					break;					
				default:
					break;
				}
			}
			
		});
		
		
		lastArticleReading.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(articleIndex>0){
					llReadingContent.removeAllViews();
					articleIndex = articleIndex-1;
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
					llReadingContent.addView(readingArticlesList.get(articleIndex),params);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionReadingMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一部分了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextArticleReading.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					if(articleIndex<readingArticlesList.size()-1){
						articleIndex = articleIndex+1;
						if(questionIndex.get(Integer.valueOf(articleIndex))==null){
							questionIndex.put(articleIndex, 0);
						}
						llReadingContent.removeAllViews();
						LinearLayout.LayoutParams params = new LinearLayout.
								LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
						llReadingContent.addView(readingArticlesList.get(articleIndex),params);
						llAnswerContent.removeAllViews();
						llAnswerContent.addView(questionReadingMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
					}else{
						Toast.makeText(context, "亲，别点我了，没有下一部分了。", Toast.LENGTH_SHORT).show();
					}

			}
		});
		lastQuestionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(questionIndex.get(Integer.valueOf(articleIndex))>0){
					llAnswerContent.removeAllViews();
					int a =questionIndex.get(Integer.valueOf(articleIndex));
					a = a -1;
					questionIndex.remove(Integer.valueOf(articleIndex));
					questionIndex.put(Integer.valueOf(articleIndex), a);
					llAnswerContent.addView(questionReadingMap.get(articleIndex).get(a));
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextQuestionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(questionIndex.get(Integer.valueOf(articleIndex))<questionReadingMap.get(articleIndex).size()-1){
					int a =questionIndex.get(Integer.valueOf(articleIndex));
					a = a +1;
					questionIndex.remove(Integer.valueOf(articleIndex));
					questionIndex.put(Integer.valueOf(articleIndex), a);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionReadingMap.get(articleIndex).get(a));
				}else{
					Toast.makeText(context, "亲，别点我了，没有下一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	
	private void assemblyData(){
		//数据包装
		if(readingArticles.size()>0){
			for(int i = 0;i<readingArticles.size();i++){
				LinearLayout llAnswerReading = (LinearLayout) View.inflate(context, R.layout.answer_layout, null);
				LinearLayout llIntroductionReading = (LinearLayout) View.inflate(context, R.layout.introduction_layout, null);
				
				Article readingArticle = readingArticles.get(i);
				String articleTitle = readingArticle.getArticleTitle();
				String articleContent = readingArticle.getArticleContent();
				TextView tvArticleNo = new TextView(context);
				TextView tvArticleTitle = new TextView(context);
				TextView tvArticleContent = new TextView(context);	
				tvArticleNo.setText(" Part "+(readingArticlesList.size()+1));
				tvArticleTitle.setText(articleTitle);
				tvArticleContent.setText(articleContent);
				setArticleTitleStyle(tvArticleNo);
				setArticleTitleStyle(tvArticleTitle);
				setArticleContentStyle(tvArticleContent);
				LinearLayout linearArticle = new LinearLayout(context);
				setArticleLinearlayoutStyle(linearArticle);
				linearArticle.addView(tvArticleNo);
				linearArticle.addView(tvArticleTitle);
				linearArticle.addView(tvArticleContent);
				readingArticlesList.add(linearArticle);
				
				if(readingArticle.getQuestion().size()>0){
					List<Question> subjectsList = readingArticle.getQuestion();
					List<LinearLayout> questionReadingsList = new ArrayList<LinearLayout>();
					for(Question q:subjectsList){
						//拼装问题和选项内容
						LinearLayout llQuestion = (LinearLayout) View.inflate(context, R.layout.question_layout, null);
						TextView txtQuestion = (TextView) llQuestion.findViewById(R.id.questionContent);
						RadioGroup questionRG = (RadioGroup) llQuestion.findViewById(R.id.choicesGroup);
						txtQuestion.setText(q.getQuestion());
						List<String> choicesReading = q.getChoice();
						questionRG.setOrientation(RadioGroup.VERTICAL);
						//拼装答案内容
						TextView txtAnswer = new TextView(context);
						setCommonFontStyle(txtAnswer);
						txtAnswer.setText(q.getAnswer());
						llAnswerReading.addView(txtAnswer);
						//拼装详解内容
						TextView txtIntroduction = new TextView(context);
						setCommonFontStyle(txtIntroduction);
						txtIntroduction.setText(q.getAnalysis());
						llIntroductionReading.addView(txtIntroduction);
						if(choicesReading.size()>0){							
							for(String c:choicesReading){
								RadioButton rb = new RadioButton(context);
								setChoiceRadioButtonStyle(rb);
//								if(choicesContainCon.get(0).equals(c)){
//									rb.setChecked(true);
//								}
								rb.setText(c);
								questionRG.addView(rb);
							}
						}
						
						questionReadingsList.add(llQuestion);												
					}
					questionReadingMap.put(i, questionReadingsList);
				}
				
				answerReadingStr.add(llAnswerReading);
				introductionReadingStr.add(llIntroductionReading);
				
			}
		}
	}
	
	
	//设置articleCotent和articleTitle的样式
	private void setArticleContentStyle(TextView articleContent){
		articleContent.setTextColor(getResources().getColor(R.color.black));
		articleContent.setTextSize(18);
	}
	
	private void setArticleTitleStyle(TextView articleTitle){
		articleTitle.setTextColor(getResources().getColor(R.color.black));
		articleTitle.setTextSize(20);
	}
	
	private void setArticleLinearlayoutStyle(LinearLayout article){
		article.setOrientation(LinearLayout.VERTICAL);	
		article.setPadding(8,4, 0, 0);
	}
	
	//设置radioButton样式
	private void setChoiceRadioButtonStyle(RadioButton rb){
		rb.setTextColor(getResources().getColor(R.color.black));
		rb.setTextSize(16);
		
	}
	
	//设置答案,详解字体样式
	private void setCommonFontStyle(TextView tv){
		tv.setTextColor(getResources().getColor(R.color.black));
		tv.setTextSize(16);
	}
	
	private void defaultView(){
		//首次载入页面显示第一题
		questionIndex.put(articleIndex, 0);
		LinearLayout.LayoutParams params = new LinearLayout.
				LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
		llReadingContent.addView(readingArticlesList.get(articleIndex),params);
		llAnswerContent.addView(questionReadingMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
		questionBtnGroup.setVisibility(View.VISIBLE);
	}
	
	Runnable r = new Runnable() {
		
		@Override
		public void run() {
			int y=0;
//			LogUtil.e(questionScrollView.getHeight()+"     "+llListentContent.getHeight()+"     "+questionScrollView.getScrollY()+"    "+y);
//			for(int i=0;i<index;i++){
//				y+=listenSubjects.get(i).getHeight();
//			}
//			y = y+listenSubjects.get(index-1).getHeight();
//			questionScrollView.scrollTo(0, y);
//			LogUtil.e("y"+y+"  height:"+listenSubjects.get(index-1).getHeight()+"  scrollY:"+questionScrollView.getScrollY()+"");
		}
	};

	@Override
	public void onPanelClosed(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("TestPanels", "Panel [" + panelName + "] closed");
	}

	@Override
	public void onPanelOpened(Panel panel) {
		String panelName = getResources().getResourceEntryName(panel.getId());
		Log.d("TestPanels", "Panel [" + panelName + "] closed");		
	}

}
