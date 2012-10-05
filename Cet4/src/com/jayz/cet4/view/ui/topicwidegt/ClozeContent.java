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
import android.widget.TextView;
import android.widget.Toast;

import com.jayz.R;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.data.ClozeDAO;
import com.jayz.cet4.data.ListeningDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;
import com.jayz.cet4.view.ui.panel.EasingType.Type;
import com.jayz.cet4.view.ui.panel.ExpoInterpolator;
import com.jayz.cet4.view.ui.panel.Panel;
import com.jayz.cet4.view.ui.panel.Panel.OnPanelListener;

public class ClozeContent extends RelativeLayout implements OnPanelListener{
	
	private String clozeContent;
	private Context context;
	
	//定义存储类型变量
	private List<LinearLayout> clozeArticlesList;
	private List<LinearLayout> answerClozeStr;
	private List<LinearLayout> introductionClozeStr;
	private Map<Integer, List<LinearLayout>> questionClozeMap;
	//这个index是表示第几篇文章
	private int articleIndex = 0;
	private Map<Integer, Integer> questionIndex = new HashMap<Integer, Integer>();
	
	//初始化控件
	private LinearLayout llClozeContent;
	private RadioGroup  rgClozeAnswer;
	private RadioButton questionCloze;
	private LinearLayout llAnswerContent;
	private Button lastArticleCloze;
	private Button nextArticleCloze;
	private ImageButton lastQuestionBtn;
	private ImageButton nextQuestionBtn;
	private LinearLayout questionBtnGroup;
	
	private List<Article> clozeArticles;
	
	
	private final Handler questionSVHandler = new Handler();
	
	public ClozeContent(Context context,String clozeContent) {
		super(context);
		this.clozeContent = clozeContent;
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.question_cloze, this,true);
		init();
		assemblyData();
		defaultView();
	}
	
	private void init(){
		ClozeDAO lDAO = new ClozeDAO(clozeContent);
		clozeArticles = lDAO.getSubjects();
		
		//定义存储类型变量
		clozeArticlesList = new ArrayList<LinearLayout>();
		answerClozeStr = new ArrayList<LinearLayout>();
		introductionClozeStr = new ArrayList<LinearLayout>();
		//问题与文章应该是多对一的关系
		questionClozeMap = new HashMap<Integer, List<LinearLayout>>();
		
		//初始化控件
		llClozeContent = (LinearLayout) this.findViewById(R.id.llArticleContent);
		rgClozeAnswer = (RadioGroup) this.findViewById(R.id.rgAnswerCloze);
		questionCloze = (RadioButton) this.findViewById(R.id.answerCloze);
		llAnswerContent = (LinearLayout) this.findViewById(R.id.llAnswerContent);
		lastArticleCloze = (Button) this.findViewById(R.id.lastArticleCloze);
		nextArticleCloze = (Button) this.findViewById(R.id.nextArticleCloze);
		lastQuestionBtn = (ImageButton) this.findViewById(R.id.lastQuestionBtn);
		nextQuestionBtn = (ImageButton) this.findViewById(R.id.nextQuestionBtn);
		questionBtnGroup = (LinearLayout) this.findViewById(R.id.questionBtn);
				
		//上拉控件设置
        Panel panel = (Panel)this.findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));
        
		//各个控件的监听
		rgClozeAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.questionCloze:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionClozeMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
					questionBtnGroup.setVisibility(View.VISIBLE);
					break;
				case R.id.answerCloze:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(answerClozeStr.get(articleIndex));
					questionBtnGroup.setVisibility(View.GONE);
					break;
				case R.id.introductionCloze:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(introductionClozeStr.get(articleIndex));
					questionBtnGroup.setVisibility(View.GONE);				
					break;					
				default:
					break;
				}
			}
			
		});
		
		
		lastArticleCloze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(articleIndex>0){
					llClozeContent.removeAllViews();
					articleIndex = articleIndex-1;
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
					llClozeContent.addView(clozeArticlesList.get(articleIndex),params);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionClozeMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一部分了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextArticleCloze.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					if(articleIndex<clozeArticlesList.size()-1){
						articleIndex = articleIndex+1;
						if(questionIndex.get(Integer.valueOf(articleIndex))==null){
							questionIndex.put(articleIndex, 0);
						}
						llClozeContent.removeAllViews();
						LinearLayout.LayoutParams params = new LinearLayout.
								LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
						llClozeContent.addView(clozeArticlesList.get(articleIndex),params);
						llAnswerContent.removeAllViews();
						llAnswerContent.addView(questionClozeMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
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
					llAnswerContent.addView(questionClozeMap.get(articleIndex).get(a));
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextQuestionBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(questionIndex.get(Integer.valueOf(articleIndex))<questionClozeMap.get(articleIndex).size()-1){
					int a =questionIndex.get(Integer.valueOf(articleIndex));
					a = a +1;
					questionIndex.remove(Integer.valueOf(articleIndex));
					questionIndex.put(Integer.valueOf(articleIndex), a);
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(questionClozeMap.get(articleIndex).get(a));
				}else{
					Toast.makeText(context, "亲，别点我了，没有下一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	
	private void assemblyData(){
		//数据包装
		if(clozeArticles.size()>0){
			for(int i = 0;i<clozeArticles.size();i++){
				LinearLayout llAnswerCloze = (LinearLayout) View.inflate(context, R.layout.answer_layout, null);
				LinearLayout llIntroductionCloze = (LinearLayout) View.inflate(context, R.layout.introduction_layout, null);
				
				Article clozeArticle = clozeArticles.get(i);
				String articleTitle = clozeArticle.getArticleTitle();
				String articleContent = clozeArticle.getArticleContent();
				TextView tvArticleNo = new TextView(context);
				tvArticleNo.setText("Part "+(clozeArticlesList.size()+1));
				setArticleTitleStyle(tvArticleNo);								
				LinearLayout linearArticle = new LinearLayout(context);
				setArticleLinearlayoutStyle(linearArticle);
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
				clozeArticlesList.add(linearArticle);
				
				if(clozeArticle.getQuestion().size()>0){
					List<Question> subjectsList = clozeArticle.getQuestion();
					List<LinearLayout> questionClozesList = new ArrayList<LinearLayout>();
					for(Question q:subjectsList){
						//拼装问题和选项内容
						LinearLayout llQuestion = (LinearLayout) View.inflate(context, R.layout.question_layout, null);
						RadioGroup questionRG = (RadioGroup) llQuestion.findViewById(R.id.choicesGroup);
						TextView txtQuestion = (TextView) llQuestion.findViewById(R.id.questionContent);
						txtQuestion.setText(q.getOrder()+1+".");
//						TextView tvQuestionNo = new TextView(context);
//						setCommonFontStyle(tvQuestionNo);
						List<String> choicesCloze = q.getChoice();
						questionRG.setOrientation(RadioGroup.VERTICAL);
						//拼装答案内容
						TextView txtAnswer = new TextView(context);
						setCommonFontStyle(txtAnswer);
						txtAnswer.setText(q.getAnswer());
						llAnswerCloze.addView(txtAnswer);
						//拼装详解内容
						TextView txtIntroduction = new TextView(context);
						setCommonFontStyle(txtIntroduction);
						txtIntroduction.setText(q.getAnalysis());
						llIntroductionCloze.addView(txtIntroduction);
						if(choicesCloze.size()>0){							
							for(String c:choicesCloze){
								RadioButton rb = new RadioButton(context);
								setChoiceRadioButtonStyle(rb);
//								if(choicesContainCon.get(0).equals(c)){
//									rb.setChecked(true);
//								}
								rb.setText(c);
								questionRG.addView(rb);
							}
						}
						
						questionClozesList.add(llQuestion);												
					}
					questionClozeMap.put(i, questionClozesList);
				}
				
				answerClozeStr.add(llAnswerCloze);
				introductionClozeStr.add(llIntroductionCloze);
				
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
		llClozeContent.addView(clozeArticlesList.get(articleIndex),params);
		llAnswerContent.addView(questionClozeMap.get(articleIndex).get(questionIndex.get(Integer.valueOf(articleIndex))));
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
