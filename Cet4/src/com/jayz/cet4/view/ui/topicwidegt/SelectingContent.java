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
import android.widget.Button;
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
import com.jayz.cet4.data.SelectingDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;
import com.jayz.cet4.view.ui.panel.EasingType.Type;
import com.jayz.cet4.view.ui.panel.ExpoInterpolator;
import com.jayz.cet4.view.ui.panel.Panel;
import com.jayz.cet4.view.ui.panel.Panel.OnPanelListener;

public class SelectingContent extends RelativeLayout implements OnPanelListener{
	
	private String selectingContent;
	private Context context;
	
	//定义存储类型变量
	private List<LinearLayout> selectingArticlesList;
	private List<ScrollView> answerSelectingStr;
	private List<ScrollView> introductionSelectingStr;
	private Map<Integer, RadioGroup> questionSelectingMap;
	private Map<RadioButton, List<RadioGroup>> choicesSelectingMap;
	//这个index是表示第几篇文章
	private int articleIndex = 0;
	private int selectedQuestionNo = 0;
	private Map<Integer, Integer> questionIndex = new HashMap<Integer, Integer>();
	
	//初始化控件
	private LinearLayout llSelectingContent;
	private RadioGroup  rgSelectingAnswer;
	private LinearLayout llAnswerContent;
	private Button lastArticleSelecting;
	private Button nextArticleSelecting;	
	private LinearLayout llQuestionSelecting;
	private LinearLayout selectingQuestion;
	private LinearLayout selectingAnswers;
	private RadioGroup selectedArticleQuesitonRG;
	private RadioGroup selectedArticleAnswerRG;
	
	private List<Article> selectingArticles;
	
	
	private final Handler mHandler = new Handler();
	
	public SelectingContent(Context context,String selectingContent) {
		super(context);
		this.selectingContent = selectingContent;
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.question_selecting, this,true);
		init();
		assemblyData();
		defaultView();
		monitor();
	}
	
	private void init(){
		SelectingDAO lDAO = new SelectingDAO(selectingContent);
		selectingArticles = lDAO.getSubjects();
		
		//定义存储类型变量
		selectingArticlesList = new ArrayList<LinearLayout>();
		answerSelectingStr = new ArrayList<ScrollView>();
		introductionSelectingStr = new ArrayList<ScrollView>();
		//问题与文章应该是多对一的关系
		questionSelectingMap = new HashMap<Integer, RadioGroup>();
		choicesSelectingMap = new HashMap<RadioButton, List<RadioGroup>>();

		
		//初始化控件
		llSelectingContent = (LinearLayout) this.findViewById(R.id.llArticleContent);
		rgSelectingAnswer = (RadioGroup) this.findViewById(R.id.rgAnswerSelecting);
		llAnswerContent = (LinearLayout) this.findViewById(R.id.llAnswerContent);
		lastArticleSelecting = (Button) this.findViewById(R.id.lastArticleSelecting);
		nextArticleSelecting = (Button) this.findViewById(R.id.nextArticleSelecting);
		llQuestionSelecting = (LinearLayout) View.inflate(context, R.layout.selecting_subject_layout, null);
		selectingQuestion = (LinearLayout) llQuestionSelecting.findViewById(R.id.selectingQuestions);
		selectingAnswers = (LinearLayout) llQuestionSelecting.findViewById(R.id.selctingAnswers);
				
		//上拉控件设置
        Panel panel = (Panel)this.findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));
        

	}
	
	private void monitor(){
		//各个控件的监听
		rgSelectingAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.questionSelecting:
					llAnswerContent.removeAllViews();
					selectedArticleQuesitonRG = questionSelectingMap.get(articleIndex);
					mHandler.post(selectedAnswerRunnable);
					mHandler.post(selectedQuestionRunnable);
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					aseemblyQuestionll(articleIndex,questionIndex.get(articleIndex));
					llAnswerContent.addView(llQuestionSelecting,params);
					break;
				case R.id.answerSelecting:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(answerSelectingStr.get(articleIndex));
					break;
				case R.id.introductionSelecting:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(introductionSelectingStr.get(articleIndex));			
					break;					
				default:
					break;
				}
			}
			
		});
		
		
		lastArticleSelecting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(articleIndex>0){
					llSelectingContent.removeAllViews();
					articleIndex = articleIndex-1;
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
					llSelectingContent.addView(selectingArticlesList.get(articleIndex),params);
					llAnswerContent.removeAllViews();
					selectedArticleQuesitonRG = questionSelectingMap.get(articleIndex);
					mHandler.post(selectedQuestionRunnable);
					mHandler.post(selectedAnswerRunnable);
					aseemblyQuestionll(articleIndex,0);		
					llAnswerContent.addView(llQuestionSelecting,params);
					selectedArticleAnswerRG = choicesSelectingMap.get(selectedArticleQuesitonRG.findViewById(selectedArticleQuesitonRG.getCheckedRadioButtonId())).get(0);
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一部分了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextArticleSelecting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(articleIndex<selectingArticlesList.size()-1){
					articleIndex = articleIndex+1;
					if(questionIndex.get(Integer.valueOf(articleIndex))==null){
						questionIndex.put(articleIndex, 0);
					}
					llSelectingContent.removeAllViews();
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
					llSelectingContent.addView(selectingArticlesList.get(articleIndex),params);
					llAnswerContent.removeAllViews();
					selectedArticleQuesitonRG = questionSelectingMap.get(articleIndex);
					mHandler.post(selectedQuestionRunnable);
					mHandler.post(selectedAnswerRunnable);
					aseemblyQuestionll(articleIndex,0);		
					llAnswerContent.addView(llQuestionSelecting,params);
					selectedArticleAnswerRG = choicesSelectingMap.get(selectedArticleQuesitonRG.findViewById(selectedArticleQuesitonRG.getCheckedRadioButtonId())).get(0);
				}else{
					Toast.makeText(context, "亲，别点我了，没有下一部分了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		
	}
	
	
	private void assemblyData(){
		//数据包装
		if(selectingArticles.size()>0){
			for(int i = 0;i<selectingArticles.size();i++){
				
				ScrollView svAnswerSelecting = (ScrollView) View.inflate(context, R.layout.selecting_answer_layout, null);
				LinearLayout llAnswerSelecting = (LinearLayout) svAnswerSelecting.findViewById(R.id.llSelectingAnswers);
				ScrollView svIntroductionSelecting = (ScrollView) View.inflate(context, R.layout.selecting_introduction_layout, null);
				LinearLayout llIntroductionSelecting = (LinearLayout) svIntroductionSelecting.findViewById(R.id.llSelectingIntroductions);
				RadioGroup rgQuestion = new RadioGroup(context);
				List<RadioGroup> rgChoicesSelecting = new ArrayList<RadioGroup>();
				
				Article selectingArticle = selectingArticles.get(i);
				String articleTitle = selectingArticle.getArticleTitle();
				String articleContent = selectingArticle.getArticleContent();
				LinearLayout linearArticle = new LinearLayout(context);
				setArticleLinearlayoutStyle(linearArticle);
				TextView tvArticleNo = new TextView(context);
				tvArticleNo.setText(" Part "+(selectingArticlesList.size()+1));
				setArticleTitleStyle(tvArticleNo);				
				linearArticle.addView(tvArticleNo);
				if(articleTitle!=null){
					TextView tvArticleTitle = new TextView(context);
					tvArticleTitle.setText(articleTitle);
					setArticleTitleStyle(tvArticleTitle);
					linearArticle.addView(tvArticleTitle);
				}
				if(articleContent!=null){
					TextView tvArticleContent = new TextView(context);	
					tvArticleContent.setText(articleContent);
					setArticleContentStyle(tvArticleContent);
					linearArticle.addView(tvArticleContent);
				}
				selectingArticlesList.add(linearArticle);
				
				if(selectingArticle.getQuestion().size()>0){
					List<Question> subjectsList = selectingArticle.getQuestion();
					for(int j=0;j<subjectsList.size();j++){
						Question q = subjectsList.get(j);
						RadioGroup rgAnswer = new RadioGroup(context);
						//拼装问题
						RadioButton rbQuestion = new RadioButton(context);
						setChoiceRadioButtonStyle(rbQuestion);
						rbQuestion.setButtonDrawable(android.R.color.transparent);
						rbQuestion.setText(q.getOrder()+"._____");	
						rbQuestion.setBackgroundResource(R.drawable.selecting_question_selector);
						rgQuestion.addView(rbQuestion);
						if(j==0){
							rgQuestion.check(rbQuestion.getId());
						}
						//拼装答案
						List<String> choicesSelecting = q.getChoice();
						if(choicesSelecting.size()>0){
							for(int K=0;K<choicesSelecting.size();K++){
										String c = choicesSelecting.get(K);
										RadioButton rb = new RadioButton(context);
										setChoiceRadioButtonStyle(rb);
										rb.setText(c);
										rgAnswer.addView(rb);
//										if(j==0){
//											rgAnswer.check(rb.getId());
//										}
							}
						}
						rgChoicesSelecting.add(rgAnswer);
						choicesSelectingMap.put(rbQuestion, rgChoicesSelecting);
						//拼装答案内容
						TextView txtAnswer = new TextView(context);
						setCommonFontStyle(txtAnswer);
						txtAnswer.setText(q.getAnswer());
						llAnswerSelecting.addView(txtAnswer);
						//拼装详解内容
						TextView txtIntroduction = new TextView(context);
						setCommonFontStyle(txtIntroduction);
						txtIntroduction.setText(q.getAnalysis());
						llIntroductionSelecting.addView(txtIntroduction);										
					}
				}
				
				questionSelectingMap.put(i, rgQuestion);
				
				answerSelectingStr.add(svAnswerSelecting);
				introductionSelectingStr.add(svIntroductionSelecting);
				
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
	
	private String formatQuestionText(String text){
		return "__"+text.substring(0, 1)+"__";
	}
	
	private void defaultView(){
		//首次载入页面显示第一题
		questionIndex.put(articleIndex, 0);
		LinearLayout.LayoutParams params = new LinearLayout.
				LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		
		llSelectingContent.addView(selectingArticlesList.get(articleIndex),params);
		selectedArticleQuesitonRG = questionSelectingMap.get(articleIndex);
		mHandler.post(selectedQuestionRunnable);
		mHandler.post(selectedAnswerRunnable);
		aseemblyQuestionll(articleIndex,0);		
		llAnswerContent.addView(llQuestionSelecting,params);
		selectedArticleAnswerRG = choicesSelectingMap.get(selectedArticleQuesitonRG.findViewById(selectedArticleQuesitonRG.getCheckedRadioButtonId())).get(0);
	}
	
	private void aseemblyQuestionll(int articleIndex,int questionIndex){
		selectingQuestion.removeAllViews();
		selectingAnswers.removeAllViews();
		selectingQuestion.addView(questionSelectingMap.get(articleIndex)) ;		
		selectingAnswers.addView(choicesSelectingMap.get(selectedArticleQuesitonRG.findViewById(selectedArticleQuesitonRG.getCheckedRadioButtonId())).get(questionIndex));
	}
	
	Runnable selectedAnswerRunnable = new Runnable() {
		
		@Override
		public void run() {
			selectedArticleAnswerRG.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					View  view1 = group.findViewById(checkedId);
					RadioButton rb1 = (RadioButton) view1;
					RadioGroup rg = questionSelectingMap.get(articleIndex);
					View  view2 = rg.findViewById(rg.getCheckedRadioButtonId());
					RadioButton rb2 = (RadioButton) view2;
					rb2.setText(((String)rb2.getText()).substring(0, 1)+"."+formatQuestionText((String)rb1.getText()));
					aseemblyQuestionll(articleIndex, selectedQuestionNo);
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(llQuestionSelecting,params);
					
				}
			});
		}
	};
	
	Runnable selectedQuestionRunnable = new Runnable() {
		
		@Override
		public void run() {
			selectedArticleQuesitonRG.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					selectedQuestionNo = group.indexOfChild(group.findViewById(checkedId));
					selectedArticleAnswerRG = choicesSelectingMap.get(group.findViewById(checkedId)).get(selectedQuestionNo);
					aseemblyQuestionll(articleIndex, selectedQuestionNo);
					mHandler.post(selectedAnswerRunnable);
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(llQuestionSelecting,params);
				}
			});
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
