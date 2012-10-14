package com.jayz.cet4.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.jayz.R;
import com.jayz.cet4.common.Constants;
import com.jayz.cet4.common.IOUtil;
import com.jayz.cet4.common.ViewUtil;
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.model.ReadingItem;
import com.jayz.cet4.view.base.BaseActivity;
import com.jayz.cet4.view.ui.Player;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
/**
 * HorizontalScrollView和ViewPager联动效果
 * 上面为HorizontalScrollView,下面为ViewPager
 * 
 */
public class ReadingActivity extends BaseActivity implements OnCheckedChangeListener{
	private RadioGroup mRadioGroup;
	private RadioButton mRadioButton1;
	private RadioButton mRadioButton2;
	private RadioButton mRadioButton3;
	private RadioButton mRadioButton4;
	private ImageView mImageView;
	private float mCurrentCheckedRadioLeft;//当前被选中的RadioButton距离左侧的距离
	private HorizontalScrollView mHorizontalScrollView;//上面的水平滚动控件
	private ViewPager mViewPager;	//下方的可横向拖动的控件
	private ArrayList<View> mViews;//用来存放下方滚动的layout(layout_1,layout_2,layout_3)	
	public static String SUBSTRING_WORD = "=";
	
	//包含内容的schrollview
	private ScrollView readingContentSV;
	//标题
	private TextView readingItemTitle;
	//获取的GrammarItem
	private ReadingItem readingItem;
	
	private SeekBar mp3SeekBar;
	private TextView tvTotalTime;
	private TextView tvCurrentTime;
	private Button btnPlay;
	private MP3Player player;
	
	private String finalTimeJ;
	private String currentTimeJ;
	private boolean isPlaying = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_reading);
		readingItem = (ReadingItem) getIntent().getExtras().get(Constants.bundleKey.readingItem);
        
        iniController();
        iniListener();
        iniVariable();
        
        mRadioButton1.setChecked(true);
        mViewPager.setCurrentItem(1);
        mCurrentCheckedRadioLeft = getCurrentCheckedRadioLeft();
        
		setData();
        
    }
    
    private void iniVariable() {
    	mViews = new ArrayList<View>();
    	mViews.add(getLayoutInflater().inflate(R.layout.blank_layout, null));
    	mViews.add(getLayoutInflater().inflate(R.layout.reading_content_item, null));
    	mViews.add(getLayoutInflater().inflate(R.layout.reading_content_item, null));
    	mViews.add(getLayoutInflater().inflate(R.layout.reading_content_item, null));
    	mViews.add(getLayoutInflater().inflate(R.layout.reading_content_item, null));
    	mViews.add(getLayoutInflater().inflate(R.layout.blank_layout, null));
    	
    	mViewPager.setAdapter(new MyPagerAdapter());//设置ViewPager的适配器
	}
    
    /**
	 * RadioGroup点击CheckedChanged监听
	 */
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		
		AnimationSet _AnimationSet = new AnimationSet(true);
		TranslateAnimation _TranslateAnimation;
		
		if (checkedId == R.id.btn1) {
			_TranslateAnimation = new TranslateAnimation(mCurrentCheckedRadioLeft, getResources().getDimension(R.dimen.rdo1), 0f, 0f);
			_AnimationSet.addAnimation(_TranslateAnimation);
			_AnimationSet.setFillBefore(false);
			_AnimationSet.setFillAfter(true);
			_AnimationSet.setDuration(100);
			mImageView.startAnimation(_AnimationSet);//开始上面蓝色横条图片的动画切换
			mViewPager.setCurrentItem(1);//让下方ViewPager跟随上面的HorizontalScrollView切换
		}else if (checkedId == R.id.btn2) {
			_TranslateAnimation = new TranslateAnimation(mCurrentCheckedRadioLeft, getResources().getDimension(R.dimen.rdo2), 0f, 0f);

			_AnimationSet.addAnimation(_TranslateAnimation);
			_AnimationSet.setFillBefore(false);
			_AnimationSet.setFillAfter(true);
			_AnimationSet.setDuration(100);
			mImageView.startAnimation(_AnimationSet);
			
			mViewPager.setCurrentItem(2);
		}else if (checkedId == R.id.btn3) {
			_TranslateAnimation = new TranslateAnimation(mCurrentCheckedRadioLeft, getResources().getDimension(R.dimen.rdo3), 0f, 0f);
			
			_AnimationSet.addAnimation(_TranslateAnimation);
			_AnimationSet.setFillBefore(false);
			_AnimationSet.setFillAfter(true);
			_AnimationSet.setDuration(100);
			mImageView.startAnimation(_AnimationSet);
			
			mViewPager.setCurrentItem(3);
		}else if (checkedId == R.id.btn4) {
			_TranslateAnimation = new TranslateAnimation(mCurrentCheckedRadioLeft, getResources().getDimension(R.dimen.rdo4), 0f, 0f);
			
			_AnimationSet.addAnimation(_TranslateAnimation);
			_AnimationSet.setFillBefore(false);
			_AnimationSet.setFillAfter(true);
			_AnimationSet.setDuration(100);
			mImageView.startAnimation(_AnimationSet);
			
			mViewPager.setCurrentItem(4);
		}
		
		mCurrentCheckedRadioLeft = getCurrentCheckedRadioLeft();//更新当前蓝色横条距离左边的距离			
		mHorizontalScrollView.smoothScrollTo((int)mCurrentCheckedRadioLeft-(int)getResources().getDimension(R.dimen.rdo2), 0);
	}
	
    
	/**
     * 获得当前被选中的RadioButton距离左侧的距离
     */
	private float getCurrentCheckedRadioLeft() {
		if (mRadioButton1.isChecked()) {
			return getResources().getDimension(R.dimen.rdo1);
		}else if (mRadioButton2.isChecked()) {
			return getResources().getDimension(R.dimen.rdo2);
		}else if (mRadioButton3.isChecked()) {
			return getResources().getDimension(R.dimen.rdo3);
		}else if (mRadioButton4.isChecked()) {
			return getResources().getDimension(R.dimen.rdo4);
		}
		return 0f;
	}

	private void iniListener() {	
		mRadioGroup.setOnCheckedChangeListener(this);		
		mViewPager.setOnPageChangeListener(new MyPagerOnPageChangeListener());
	}

	private void iniController() {
		mRadioGroup = (RadioGroup)findViewById(R.id.radioGroup);
		mRadioButton1 = (RadioButton)findViewById(R.id.btn1);
		mRadioButton2 = (RadioButton)findViewById(R.id.btn2);
		mRadioButton3 = (RadioButton)findViewById(R.id.btn3);
		mRadioButton4 = (RadioButton)findViewById(R.id.btn4);
		
		mImageView = (ImageView)findViewById(R.id.tagImg);
		
		mHorizontalScrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView);
		readingItemTitle = (TextView) findViewById(R.id.readingItemTitle);
		
		mViewPager = (ViewPager)findViewById(R.id.readingPager);
		
		initController();
	}
	
	private void setData(){
		
		readingItemTitle.setText(readingItem.getName());
		
		for(int i=1;i<mViews.size()-1;i++){
			ScrollView articleContentSV = (ScrollView) mViews.get(i);
			TextView readingContentTV = (TextView) articleContentSV.findViewById(R.id.readingItemContent);
			
				String targetPath = Constants.PATH_RES +"/"+
						readingItem.getDownLoadAdd().substring(readingItem.getDownLoadAdd().
								lastIndexOf(SUBSTRING_WORD)+1,readingItem.getDownLoadAdd().lastIndexOf("."));
				if(i==1){
					targetPath = targetPath + "/article.txt";
				}else if(i==2){
					targetPath = targetPath + "/translation.txt";
				}else if(i==3){
					targetPath = targetPath + "/word.txt";
				}else if(i==4){
					targetPath = targetPath + "/sentence.txt";
				}
				File f = new File(targetPath);
				String content = null;
				if(f.exists()){
					try {
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
				}
				if(content!=null){
					readingContentTV.setText(content);
				}else{
					readingContentTV.setText("该文章无此部分内容,可能会在后续增加，敬请谅解");
				}
			
		}		
}

	/**
	 * ViewPager的适配器
	 */
	private class MyPagerAdapter extends PagerAdapter{

		@Override
		public void destroyItem(View v, int position, Object obj) {
			((ViewPager)v).removeView(mViews.get(position));
		}

		@Override
		public void finishUpdate(View arg0) {}

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public Object instantiateItem(View v, int position) {
			((ViewPager)v).addView(mViews.get(position));
			return mViews.get(position);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {}
		
	}
	/**
	 * ViewPager的PageChangeListener(页面改变的监听器)
	 */
	private class MyPagerOnPageChangeListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {}
		/**
		 * 滑动ViewPager的时候,让上方的HorizontalScrollView自动切换
		 */
		@Override
		public void onPageSelected(int position) {

			if (position == 0) {
				mViewPager.setCurrentItem(1);
			}else if (position == 1) {
				mRadioButton1.performClick();
			}else if (position == 2) {
				mRadioButton2.performClick();
			}else if (position == 3) {
				mRadioButton3.performClick();
			}else if (position == 4) {
				mRadioButton4.performClick();
			}else if (position == 5) {
				mViewPager.setCurrentItem(3);
			}
		}
		
	}
	
	/** 初始化音乐播放器及其mp3信息 */
	private void initController() {
		mp3SeekBar  = (SeekBar) this.findViewById(R.id.sbAudio);
		tvTotalTime = (TextView) this.findViewById(R.id.tvTotalTime);
		tvCurrentTime = (TextView) this.findViewById(R.id.tvCurrentTime);
		btnPlay = (Button) this.findViewById(R.id.btnPlay);
		player = new MP3Player();		
		String mp3Path = Constants.PATH_RES +"/"+
				readingItem.getDownLoadAdd().substring(readingItem.getDownLoadAdd().
						lastIndexOf(SUBSTRING_WORD)+1,readingItem.getDownLoadAdd().lastIndexOf("."))+"/mp3.mp3";
		File f = new File(mp3Path);
		if(f.exists()){
			player.setDataSource(f);
		}
		this.currentTimeJ = "00:00";
		tvCurrentTime.setText("00:00");
		finalTimeJ = (String) tvTotalTime.getText();
		btnPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (ReadingActivity.this.isPlaying) {
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
					ReadingActivity.this.isPlaying = true;
				}
			}

		});
		
	}
	
	@Override
	protected void onDestroy() {
		destroyMusic();
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		pauseMusic();
		super.onPause();
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
	
}