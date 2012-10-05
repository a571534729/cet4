package com.jayz.cet4.view.ui.topicwidegt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jayz.R;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.common.ViewUtil;
import com.jayz.cet4.data.ListeningDAO;
import com.jayz.cet4.model.Article;
import com.jayz.cet4.model.Question;
import com.jayz.cet4.view.ui.Player;
import com.jayz.cet4.view.ui.panel.EasingType.Type;
import com.jayz.cet4.view.ui.panel.ExpoInterpolator;
import com.jayz.cet4.view.ui.panel.Panel;
import com.jayz.cet4.view.ui.panel.Panel.OnPanelListener;

public class ListeningContent extends RelativeLayout implements OnPanelListener{
	
	private String listeningContent;
	private Context context;
	
	//定义存储类型变量
	private List<LinearLayout> listenSubjects;
	private List<String> answerListensStr;
	private List<String> introductionListensStr;
	private List<String> conversationListensStr;
	private int index = 0;
	private String finalTimeJ;
	private String currentTimeJ;
	private boolean isPlaying = false;
	
	//初始化控件
	private LinearLayout llListentContent;
	private RadioGroup  rgListenAnswer;
	private RadioButton answerListen;
	private TextView tvAnswerContent;
	private Button lastSubjectListen;
	private Button nextSubjectListen;
	private ScrollView questionScrollView;
	private SeekBar mp3SeekBar;
	private TextView tvTotalTime;
	private TextView tvCurrentTime;
	private Button btnPlay;
	private MP3Player player;
	private ProgressDialog progressDialog;
	
	private List<Article> listenArticles;
	
	private View emptyView;
	
	private final Handler questionSVHandler = new Handler();
	
	public ListeningContent(Context context,String listeningContent) {
		super(context);
		this.listeningContent = listeningContent;
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.question_listening, this,true);
		init();
		new assemblyTask().execute();
//		assemblyData();
//		defaultView();
	}
	
	private void init(){
		emptyView=new View(context);
		emptyView.setLayoutParams(new ViewGroup.LayoutParams(-1, 200));
		
		//定义存储类型变量
		listenSubjects = new ArrayList<LinearLayout>();
		answerListensStr = new ArrayList<String>();
		introductionListensStr = new ArrayList<String>();
		conversationListensStr = new ArrayList<String>();
		
		//初始化控件
		llListentContent = (LinearLayout) this.findViewById(R.id.llListenContent);
		rgListenAnswer = (RadioGroup) this.findViewById(R.id.rgAnswerListen);
		answerListen = (RadioButton) this.findViewById(R.id.answerListen);
		tvAnswerContent = (TextView) this.findViewById(R.id.tvAnswerContent);
		lastSubjectListen = (Button) this.findViewById(R.id.lastSubjectListen);
		nextSubjectListen = (Button) this.findViewById(R.id.nextSubjectListen);
		questionScrollView = (ScrollView) this.findViewById(R.id.questionSV);
				
		//上拉控件设置
        Panel panel = (Panel)this.findViewById(R.id.bottomPanel);
        panel.setOnPanelListener(this);
        panel.setInterpolator(new ExpoInterpolator(Type.OUT));
        initController();
        
		//各个控件的监听
		rgListenAnswer.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.answerListen:
					tvAnswerContent.setText(answerListensStr.get(index));
					break;
				case R.id.introductionListen:
					tvAnswerContent.setText(introductionListensStr.get(index));
					break;
				case R.id.conversationListen:
					tvAnswerContent.setText(conversationListensStr.get(index));
					break;					
				default:
					break;
				}
			}
			
		});
		
		
		lastSubjectListen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(index>0){
					llListentContent.removeView(listenSubjects.get(index));
					index = index-1;
					tvAnswerContent.setText(answerListensStr.get(index));
					answerListen.setChecked(true);
					questionSVHandler.post(r);
				}else{
					Toast.makeText(context, "亲，别点我了，没有上一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		nextSubjectListen.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				llListentContent.removeView(emptyView);
				if(index<listenSubjects.size()-1){
					index = index +1;
					LinearLayout.LayoutParams params = new LinearLayout.
							LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					llListentContent.addView(listenSubjects.get(index),params);
						llListentContent.addView(emptyView);
					tvAnswerContent.setText(answerListensStr.get(index));
					answerListen.setChecked(true);
					questionSVHandler.post(r);
				}else{
					Toast.makeText(context, "亲，别点我了，没有下一题了。", Toast.LENGTH_SHORT).show();
				}
			}
		});
		

	}
	
	/** 初始化音乐播放器及其mp3信息 */
	private void initController() {
		mp3SeekBar  = (SeekBar) this.findViewById(R.id.sbAudio);
		tvTotalTime = (TextView) this.findViewById(R.id.tvTotalTime);
		tvCurrentTime = (TextView) this.findViewById(R.id.tvCurrentTime);
		btnPlay = (Button) this.findViewById(R.id.btnPlay);
		player = new MP3Player();		
		try {
			player.setDataSource(context.getAssets().openFd("cet420100619.mp3").getFileDescriptor());
		} catch (IOException e) {
			LogUtil.e(e);
		}
		this.currentTimeJ = "00:00";
		tvCurrentTime.setText("00:00");
		finalTimeJ = (String) tvTotalTime.getText();
		btnPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (ListeningContent.this.isPlaying) {
					pause();
					return;
				} else {
					v.setBackgroundResource(R.drawable.sel_pause);
					if (player == null) {
						Toast.makeText(context, R.string.initNotFinished,
								Toast.LENGTH_SHORT).show();
						return;
					}
					player.play();
					ListeningContent.this.isPlaying = true;
				}
			}

		});
		
	}
	
	private void pause() {
		if (!this.isPlaying || null==player)
			return;
		this.btnPlay.setBackgroundResource(R.drawable.sel_play);
		this.player.pause();
		this.isPlaying = false;
	}
	
	public void pauseMusic(){
		pause();
	}
	
	public void destroyMusic(){
		if(null!=player){
			player.stopmusic();
		}
	}
	
	private void assemblyData(){
		ListeningDAO lDAO = new ListeningDAO(listeningContent);
		listenArticles = lDAO.getSubjects();
		//数据包装
		if(listenArticles.size()>0){
			for(Article listenArticle:listenArticles){
				//一题对应一个对话
				if(listenArticle.getLongConversation()==null){
					List<Question> subjectsNoContainCon = listenArticle.getQuestion();
					for(Question q:subjectsNoContainCon){
						List<String> choicesContainCon = q.getChoice();
						RadioGroup rg = new RadioGroup(context);
						rg.setOrientation(RadioGroup.VERTICAL);
						answerListensStr.add(q.getAnswer());
						introductionListensStr.add(q.getAnalysis());
						conversationListensStr.add(q.getConversation());
						if(choicesContainCon.size()>0){							
							for(String c:choicesContainCon){
								RadioButton rb = new RadioButton(context);
								setChoiceRadioButtonStyle(rb);
//								if(choicesContainCon.get(0).equals(c)){
//									rb.setChecked(true);
//								}
								rb.setText(c);
								rg.addView(rb);
							}
						}
						LinearLayout linerContainCon =new LinearLayout(context);
						setQuestionLinearlayoutStyle(linerContainCon);
						TextView choiceNo = new TextView(context);
						choiceNo.setText(listenSubjects.size()+1+".");
						setChoiceNoTextViewStyle(choiceNo);
						linerContainCon.addView(choiceNo);
						linerContainCon.addView(rg);
						listenSubjects.add(linerContainCon);
					}
				}
				//多题对一个长对话
				if(listenArticle.getLongConversation()!=null&&listenArticle.getArticleContent()==null){
					List<Question> subjectsNoContainArtic = listenArticle.getQuestion();
					String longCoversationListen = listenArticle.getLongConversation();
					for(Question q:subjectsNoContainArtic){
						List<String> choicesContainCon = q.getChoice();
						RadioGroup rg = new RadioGroup(context);
						rg.setOrientation(RadioGroup.VERTICAL);
						answerListensStr.add(q.getAnswer());
						introductionListensStr.add(q.getAnalysis());
						conversationListensStr.add(longCoversationListen);
						if(choicesContainCon.size()>0){
							for(String c:choicesContainCon){
								RadioButton rb = new RadioButton(context);
//								if(choicesContainCon.get(0).equals(c)){
//									rb.setChecked(true);
//								}
								setChoiceRadioButtonStyle(rb);
								rb.setText(c);
								rg.addView(rb);
							}
							LinearLayout linerContainCon =new LinearLayout(context);
							setQuestionLinearlayoutStyle(linerContainCon);
							TextView choiceNo = new TextView(context);
							choiceNo.setText(listenSubjects.size()+1+".");
							setChoiceNoTextViewStyle(choiceNo);
							linerContainCon.addView(choiceNo);
							linerContainCon.addView(rg);
							listenSubjects.add(linerContainCon);
						}
					}
				}
				//听力填空
				if(listenArticle.getLongConversation()!=null&&listenArticle.getArticleContent()!=null){
					List<Question> subjectsContainAll = listenArticle.getQuestion();
					String articleContentListen = listenArticle.getArticleContent();
					String longCoversationListen = listenArticle.getLongConversation();
					for(Question q:subjectsContainAll){
						answerListensStr.add(q.getAnswer());
						introductionListensStr.add(q.getAnalysis());
						conversationListensStr.add(longCoversationListen);
						TextView tvClozeListen = new TextView(context);
						tvClozeListen.setText(articleContentListen);
						setChoiceNoTextViewStyle(tvClozeListen);
						LinearLayout linerContainCon =new LinearLayout(context);
						setQuestionLinearlayoutStyle(linerContainCon);
						TextView choiceNo = new TextView(context);
						choiceNo.setText(listenSubjects.size()+1+".");
						setChoiceNoTextViewStyle(choiceNo);
						linerContainCon.addView(choiceNo);
						linerContainCon.addView(tvClozeListen);
						listenSubjects.add(linerContainCon);
					}
									
				}
				
			}
		}
	}
	
	//设置radioButton样式
	private void setChoiceRadioButtonStyle(RadioButton rb){
		rb.setTextColor(getResources().getColor(R.color.black));
		rb.setTextSize(18);
		
	}
	
	private void setChoiceNoTextViewStyle(TextView tv){
		tv.setTextColor(getResources().getColor(R.color.black));
		tv.setTextSize(18);
	}
	
	private void setQuestionLinearlayoutStyle(LinearLayout ll){
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setPadding(8,4, 0, 0);
	}
	
	private void defaultView(){
		//首次载入页面显示第一题
		tvAnswerContent.setText(answerListensStr.get(index));
		LinearLayout.LayoutParams params = new LinearLayout.
				LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);		
		llListentContent.addView(listenSubjects.get(index),params);
//		llListentContent.addView(emptyView);
	}
	
	Runnable r = new Runnable() {
		
		@Override
		public void run() {
			int y=0;
//			LogUtil.e(questionScrollView.getHeight()+"     "+llListentContent.getHeight()+"     "+questionScrollView.getScrollY()+"    "+y);
			for(int i=0;i<index;i++){
				y+=listenSubjects.get(i).getHeight();
			}
//			y = y+listenSubjects.get(index-1).getHeight();
			questionScrollView.scrollTo(0, y);
//			LogUtil.e("y"+y+"  height:"+listenSubjects.get(index-1).getHeight()+"  scrollY:"+questionScrollView.getScrollY()+"");
		}
	};
	
	/**
	 * 异步加载数据，解析数据
	 *
	 */
	private class assemblyTask extends AsyncTask<Void, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(Void... params) {
			assemblyData();
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			if (ListeningContent.this.progressDialog == null) {
				ListeningContent.this.progressDialog = new ProgressDialog(
						context);
				progressDialog
						.setMessage(context.getText(R.string.loading));
			}
			ListeningContent.this.progressDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			defaultView();
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
			super.onPostExecute(result);
		}
		
	}

	
	private class MP3Player extends Player{

		public MP3Player() {
			super(mp3SeekBar);
		}
		
		@Override
		protected void onSetedDataSource(MediaPlayer mp) {
			super.onSetedDataSource(mp);
			if (TextUtils.isEmpty(finalTimeJ))
				tvTotalTime
						.setText(ViewUtil.formatTimeInmmss(mp.getDuration()));
			else
				tvTotalTime.setText(finalTimeJ);
			tvCurrentTime.setText(currentTimeJ);
		}
		@Override
		protected void onUpdateProgress(MediaPlayer mp) {
			super.onUpdateProgress(mp);
			tvCurrentTime.setText(ViewUtil.formatTimeInmmss(mp
					.getCurrentPosition()));
			tvCurrentTime.invalidate();
 		}
		@Override
		protected void onCompletion(MediaPlayer mp) {
			super.onCompletion(mp);
			btnPlay.setBackgroundResource(R.drawable.sel_pause);
		}
				
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
