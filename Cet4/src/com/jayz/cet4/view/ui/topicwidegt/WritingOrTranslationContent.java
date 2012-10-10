package com.jayz.cet4.view.ui.topicwidegt;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jayz.R;
import com.jayz.cet4.data.WritingOrTranslationDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.view.ui.panel.EasingType.Type;
import com.jayz.cet4.view.ui.panel.ExpoInterpolator;
import com.jayz.cet4.view.ui.panel.Panel;
import com.jayz.cet4.view.ui.panel.Panel.OnPanelListener;

public class WritingOrTranslationContent extends RelativeLayout implements OnPanelListener{
	
	private String WOTContent;
	private Context context;
	
	//定义存储类型变量
	private String articleContentWOTStr;
	private String answerWOTStr;
	private String introductionWOTStr;
	private Article WOTArticle;
	
	//初始化控件
	private LinearLayout llWOTContent;
	private LinearLayout llAnswerContent;
	private RadioGroup  rgWOTAnswer;
	private EditText etAnswer;

	
	public WritingOrTranslationContent(Context context,String WOTContent,int layoutResource) {
		super(context);
		this.WOTContent = WOTContent;
		this.context = context;
		LayoutInflater.from(context).inflate(layoutResource, this,true);
		init();
		assemblyData();
		defaultView();
	}
	
	private void init(){
		WritingOrTranslationDAO lDAO = new WritingOrTranslationDAO(WOTContent);
		WOTArticle = lDAO.getSubjects();
		
		
		//初始化控件
		llWOTContent = (LinearLayout) this.findViewById(R.id.llArticleContent);
		llAnswerContent = (LinearLayout) this.findViewById(R.id.llAnswerContent);
		rgWOTAnswer = (RadioGroup) this.findViewById(R.id.rgAnswerWOT); 
				
		//上拉控件设置
        Panel panel = (Panel)this.findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));
        
		//各个控件的监听
		rgWOTAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.questionWOT:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(etAnswer);
					break;
				case R.id.answerWOT:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(createTextView(answerWOTStr));
					break;
				case R.id.introductionWOT:
					llAnswerContent.removeAllViews();
					llAnswerContent.addView(createTextView(introductionWOTStr));		
					break;					
				default:
					break;
				}
			}
			
		});		
	}	
	
	private void assemblyData(){
		//数据包装
				
				if(WOTArticle!=null){
					articleContentWOTStr = WOTArticle.getArticleContent();
					answerWOTStr = WOTArticle.getQuestion().get(0).getAnswer();
					introductionWOTStr = WOTArticle.getQuestion().get(0).getAnalysis();
				}										
	}
	
	private TextView createTextView(String tvContent){
		TextView tv = new TextView(context);
		tv.setText(tvContent);
		setCommonFontStyle(tv);
		return tv;
	}
	
	private EditText createEditText(){
		EditText et = new EditText(context);
		et.setTextColor(getResources().getColor(R.color.black));
		et.setTextSize(16);
		et.setLines(5);
		et.setGravity(Gravity.TOP);
		et.setHint("哟，此输入框用于填写答案，方便与答案匹配!!");
		return et;
	}
	
	//设置答案,详解字体样式
	private void setCommonFontStyle(TextView tv){
		tv.setTextColor(getResources().getColor(R.color.black));
		tv.setTextSize(16);
	}
	
	
	private void defaultView(){
		TextView tvArticleContent = createTextView(articleContentWOTStr);
		tvArticleContent.setPadding(8,4, 0, 0);
		llWOTContent.addView(tvArticleContent);
		etAnswer = createEditText();
		llAnswerContent.addView(etAnswer);
	}
	

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
