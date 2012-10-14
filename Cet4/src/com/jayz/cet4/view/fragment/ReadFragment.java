package com.jayz.cet4.view.fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jayz.R;
import com.jayz.cet4.common.Constants;
import com.jayz.cet4.common.DownLoader;
import com.jayz.cet4.common.IOUtil;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.common.NetWorkUtil;
import com.jayz.cet4.common.ReadingDownLoader;
import com.jayz.cet4.common.UnZipUtil;
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.data.ReadingItemsDAO;
import com.jayz.cet4.model.ReadingItem;
import com.jayz.cet4.model.ReadingSort;
import com.jayz.cet4.view.ReadingActivity;
import com.jayz.cet4.view.TopicActivity;
import com.jayz.cet4.view.base.BaseFragment;
import com.jayz.cet4.view.ui.CustAlertDialog;

public class ReadFragment extends BaseFragment{
	
	private Context context;
	private LayoutInflater mInflater;
	public static String Reading_XML = "config/reading_items.xml";
	
	public static String SUBSTRING_WORD = "=";
	
	private TextView sortNameTV;

	private List<ReadingSort> readingSorts;
	private Map<ReadingItem, ReadingDownLoader> downloaders;
	private Map<ReadingItem, Integer> downloadStates;
	private Map<ReadingItem, ViewGroup> readingViews;
	
	//储存于viewpage的viewlist
	private List<View> viewPageList;
	//储存底牌分屏标记的textview
	private TextView[] textViews;
	private TextView radioTextView;
	
	public ReadFragment(Context context) {
		this.context = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewPageList = new ArrayList<View>();
		readingViews = new HashMap<ReadingItem, ViewGroup>();
		getReadingSorts();
		initDownloaders();
		initDownloadStates();
	}
	
	//实例化ReadingItems
	private void getReadingSorts(){
		InputStream xmlStream = null;
		try {
			xmlStream =context.getAssets().open(Reading_XML);
			ReadingItemsDAO itemDAO = new ReadingItemsDAO();
			readingSorts = itemDAO.getAllReadingSorts(xmlStream);
		} catch (IOException e) {
			LogUtil.e(e);
		}finally{
			try {
				xmlStream.close();
			} catch (IOException e) {
				LogUtil.e(e);
			}
		}
	}
	
	private void initDownloaders(){
		downloaders = new HashMap<ReadingItem, ReadingDownLoader>();
		for(int i=0;i<readingSorts.size();i++){
			ReadingSort readingSort=readingSorts.get(i);
			List<ReadingItem> readingItems = readingSort.getReadings();
			for(int j=0;j<readingItems.size();j++){
				final ReadingItem readingItem = readingItems.get(j);
				//初始化下载地址
				String downloadAdd = readingItem.getDownLoadAdd();
				//下载到本地的路径
				String targetPath = Constants.PATH_RES +"/"+
						downloadAdd.substring(downloadAdd.lastIndexOf(SUBSTRING_WORD)+1); 
				//初始化下载器
				ReadingDownLoader d = new ReadingDownLoader(context,handler,readingItem,targetPath,downloadAdd){
					@Override
					protected void onDownloadBegin(long filesize) {
						super.onDownloadBegin(filesize);
						if(filesize>0){
							SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.sharedPreference.topicConfig.name, Context.MODE_PRIVATE);
							String key=readingItem.getId()+Constants.sharedPreference.topicConfig.size_suffix;
							sharedPreferences.edit().putLong(key, filesize).commit();
						}
					}
				};
				downloaders.put(readingItem, d);
			}
		}
	}
	
	private void initDownloadStates(){
		downloadStates=new HashMap<ReadingItem, Integer>();
		for(int i=0;i<readingSorts.size();i++){
			ReadingSort readingSort=readingSorts.get(i);
			List<ReadingItem> readingItems = readingSort.getReadings();
			for(int j=0;j<readingItems.size();j++){
				ReadingItem readingItem = readingItems.get(j);
				//初始化下载图标的状态
				SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.sharedPreference.topicConfig.name, Context.MODE_PRIVATE);
				//用于判断本地文件和文件长度是否相等传递初始化状态
				String key=readingItem.getId()+Constants.sharedPreference.topicConfig.size_suffix;
				ReadingDownLoader d=downloaders.get(readingItem);
				d.setTotalFileSize(sharedPreferences.getLong(key, 0));
				long fileSize = d.getTotalFileSize();
				long localFileLength = d.getLocalFileLength();
				int status=DownLoader.DOWN_NONE;
				if(localFileLength<fileSize&&localFileLength!=0){
					status=DownLoader.DOWN_PAUSE;
				}else if(localFileLength == fileSize&&localFileLength!=0){
//					status=DownLoader.ZIP_BEGIN;	//TODO 考虑未解压情况处理
					status=DownLoader.DOWN_PAUSE;
				}else if(localFileLength==0){
					String zipDir = Constants.PATH_RES+"/"+readingItem.getDownLoadAdd().substring
							(readingItem.getDownLoadAdd().lastIndexOf(SUBSTRING_WORD)+1,readingItem.getDownLoadAdd().lastIndexOf("."));
					File f = new File(zipDir);
					if(f.exists()){
						status = DownLoader.TASK_COMPLETE;
					}else{
						status=DownLoader.DOWN_NONE;
					}
				}
				downloadStates.put(readingItem, status);
			}
		}

	}
	
	//定义一个Handle用于处理线程和UI的通讯
	//定义消息的what值0表示暂停状态,1表示进度条进行更新,2表示完成下载所做的操作,3表示刚开始下载,-1错误处理
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
//			case msg_what_exit:
//				isExit = false;
//				return;
			case Constants.msgWhat.downstate_changed:
				ReadingItem readingItem=(ReadingItem) msg.obj;
				int downState=msg.arg2;
				downloadStates.put(readingItem, downState);
				updateReadingState(downState, readingItem);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		RelativeLayout fragmentReading = (RelativeLayout) inflater.inflate(R.layout.fragment_read, container, false);
		sortNameTV = (TextView) fragmentReading.findViewById(R.id.sortReading);
		ViewPager viewPageReading = (ViewPager) fragmentReading.findViewById(R.id.readingViewPager);
		LinearLayout group = (LinearLayout) fragmentReading.findViewById(R.id.viewGroup);
		textViews=new TextView[readingSorts.size()];
		for(int i=0;i<readingSorts.size();i++){
			RelativeLayout mRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.reading_list_rl, null);
			viewPageList.add(mRelativeLayout);
			radioTextView=new TextView(context);			
			radioTextView.setLayoutParams(new LayoutParams(30,30));
			radioTextView.setPadding(0, 0, 2, 0);			
			textViews[i]=radioTextView;
			if (i==0)
			{	
//				默认进入程序后第一张图片被选中;
				textViews[i].setBackgroundResource(R.drawable.radio_sel);						
			}else {
				textViews[i].setBackgroundResource(R.drawable.radio);
			}
			group.addView(textViews[i]);
		}
		assemblyListView();
		viewPageReading.setAdapter(new ReadingViewPageAdapter());
		viewPageReading.setOnPageChangeListener(new ReadingViewPageListener());
		
		return fragmentReading;
	}
	
	//进行拼装viewPage 将viewPage里的所有ListView拼装好
	private void assemblyListView(){
		for(int i=0;i<viewPageList.size();i++){
			View viewPageItem = viewPageList.get(i);
			ListView viewPageItemLV = (ListView) viewPageItem.findViewById(R.id.lvReadingContent);
			viewPageItemLV.setAdapter(new viewPageItemAdapter(i));			
		}
	}
	
	/**
	 * 根据下载状态更新ui
	 * @param status  下载状态 包括  {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param bookIndex  书本索引
	 */
	private void updateReadingState(int status, final ReadingItem readingItem) {
		ReadingViewHolder readingViewHolder=new ReadingViewHolder();
		ViewGroup readingView = readingViews.get(readingItem);
		//若topicView的tag与书本索引不等 说明该topicView不是要更新的view 这里是为了防止回收再利用的topicView接收到下载线程的更新请求
		if(((ReadingViewHolder)readingView.getTag()).readingItem!=readingItem){
			return;
		}
		readingViewHolder.downloadImg = (ImageView) readingView.findViewById(R.id.downloadImg);
		readingViewHolder.downloadTV = (TextView) readingView.findViewById(R.id.downloadTV);
		readingViewHolder.tvReadingTitle = (TextView) readingView.findViewById(R.id.titleReadingListItem);
		updateReadingState(status, readingItem, readingViewHolder,true);
	}
	
	/**
	 * 根据下载状态更新ui  这个方法用于有topicViewHolder缓存的更新
	 * @param status  下载状态 包括 {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param topicIndex  书本索引
	 * @param bookViewHolder bookView视图缓存
	 * @param operation 是否进行非UI的操作,具体指下载解压等行为(实际上该参数是专为ListView更新UI用的)
	 */
	private void updateReadingState(int status, final ReadingItem readingItem,ReadingViewHolder readingViewHolder,boolean operation) {
		final ReadingDownLoader downloader = downloaders.get(readingItem);
		TextView percent = readingViewHolder.downloadTV;
		ImageView downloadImg = readingViewHolder.downloadImg;
		TextView readingTitle = readingViewHolder.tvReadingTitle;
		
		switch(status){
		case DownLoader.DOWN_PAUSE:
			percent.setText("继续下载");
			downloadImg.setBackgroundResource(R.drawable.pause1);
			readingTitle.setText(readingItem.getName());
			break;
		case DownLoader.DOWN_BEGIN:
			percent.setText("0%");
			downloadImg.setBackgroundResource(R.drawable.down);
			readingTitle.setText(readingItem.getName());
			break;
		case DownLoader.DOWN_LOADING:
//			if(image1.getAnimation()==null){
//				downAnimation(image1, image2,bookIndex);
//			}
			downloadImg.setBackgroundResource(R.drawable.downloading);
			AnimationDrawable _animation = downloadAnimation(downloadImg);
			if(_animation.isRunning()){
				_animation.stop();
			}
			_animation.start();
			if(downloader.getPercent()>=0&&downloader.getPercent()<=100){
				percent.setText(downloader.getPercent()+"%");
			}else{
				Toast.makeText(context, getString(R.string.exception_download),Toast.LENGTH_LONG ).show();
				downloader.deleteFile();
				downloader.setCurrentFileSize(0);
				if(operation){
					if(IOUtil.checkSDCard()==false){
						Toast.makeText(context,getString(R.string.exception_sdcard), Toast.LENGTH_LONG).show();
					}else if(NetWorkUtil.isOnline(context)==false){
						Toast.makeText(context, getString(R.string.exception_net), Toast.LENGTH_LONG).show();
					}else{
						new Thread(){								
							@Override
							public void run() {
								try {
									downloader.download();
								} catch (TradException e) {
									handleException(e);
								}												
							};								
						}.start();
					}
				}
			}
			break;
		case DownLoader.TASK_COMPLETE:
			percent.setText("进入");
			downloadImg.setBackgroundResource(R.drawable.ok);
			readingTitle.setText(readingItem.getName());
			break;
		case DownLoader.DOWN_NONE:	
			percent.setText("下载");
			downloadImg.setBackgroundResource(R.drawable.down);
			readingTitle.setText(readingItem.getName());
			break;
		case DownLoader.DOWN_COMPLETE://开始解压
			if(operation){
				new Thread(){
					@Override
					public void run() {
						UnZipUtil zipUtil = new UnZipUtil();
						String downAdd = downloader.getUrl();
						String zipPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf(SUBSTRING_WORD)+1);
						String destDir = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf(SUBSTRING_WORD)+1,downAdd.lastIndexOf("/"));
						try {
							zipUtil.readByApacheZipFile(zipPath, destDir);
						} catch (IOException e) {
							try {
								throw new TradException(getString(R.string.exception_decompression), e);
							} catch (TradException e1) {
								handleException(e1);
							}
						}
						Message msg=handler.obtainMessage(Constants.msgWhat.downstate_changed);
						msg.obj=readingItem;
						msg.arg2=DownLoader.TASK_COMPLETE;
						handler.sendMessage(msg);
					};
				}.start();
			}
			break;
		default:
			break;
		}
	}
	
	//下载动画，渐变动画 
	private AnimationDrawable downloadAnimation(ImageView image){
		image.setBackgroundResource(R.anim.download_animation);
		AnimationDrawable _animation = (AnimationDrawable)image.getBackground();
		return _animation;
	}
	
	
	
	//用于显示listview的adapter
	private class viewPageItemAdapter extends BaseAdapter{
		
		int index;
		List<ReadingItem> readingItems;
		
		public viewPageItemAdapter(int index) {
			this.index = index;
			readingItems = readingSorts.get(index).getReadings();
		}

		@Override
		public int getCount() {
			return readingItems.size();
		}

		@Override
		public Object getItem(int position) {
			return readingItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ReadingViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.reading_list_item, null);
				holder = new ReadingViewHolder();
				holder.tvReadingTitle = (TextView) convertView.findViewById(R.id.titleReadingListItem);
				holder.readingView = (ViewGroup) convertView.findViewById(R.id.llReadingListItem);
				holder.downloadImg = (ImageView) convertView.findViewById(R.id.downloadImg);
				holder.downloadTV = (TextView) convertView.findViewById(R.id.downloadTV);
				holder.readingItem = readingItems.get(position);
				holder.llReadingListItem = (LinearLayout) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ReadingViewHolder) convertView.getTag();
			}
			readingViews.put(readingItems.get(position), holder.readingView);
			holder.tvReadingTitle.setText(readingItems.get(position).getName());
			holder.llReadingListItem.setOnClickListener(new ReadingItemOnClickListenner(
					readingItems.get(position),downloaders.get(readingItems.get(position))));
			updateReadingState(downloadStates.get(readingItems.get(position)), readingItems.get(position), holder, true);
			return convertView;
		}
		
	}
	
	private class ReadingItemOnClickListenner implements OnClickListener{
		
		ReadingItem readingItem;
		ReadingDownLoader downloader;
		
		public ReadingItemOnClickListenner(ReadingItem readingItem,ReadingDownLoader downloader) {
			this.readingItem = readingItem;
			this.downloader = downloader;
		}
		
		@Override
		public void onClick(View v) {
			// 测试解压
			/*
			 * if(i==1){ Message
			 * msg=handle.obtainMessage(DownLoader.ZIP_BEGIN);
			 * msg.obj=i; handle.sendMessage(msg); return; }
			 */
			// 判断SD卡操作，错误返回提示
			if (!IOUtil.checkSDCard()) {
				Toast.makeText(context, getResources().getString(R.string.exception_sdcard),
						Toast.LENGTH_SHORT).show();
				return;
			}
		
			// 判断是否是下载完的文件
			String downloadAdd = readingItem.getDownLoadAdd();
			String zipDir = Constants.PATH_RES
					+ "/"
					+ downloadAdd.substring(
							downloadAdd.lastIndexOf(SUBSTRING_WORD) + 1,
							downloadAdd.lastIndexOf("."));
			File f = new File(zipDir);
			// 书本下载完成点击所做的操作
			if (f.exists()&&downloader.getLocalFileLength()==0) {
				Intent intent=new Intent(context, ReadingActivity.class);
				intent.putExtra(Constants.bundleKey.readingItem, readingItem);
//				global.putData(Constants.bundleKey.bookItem,bookItem);
				startActivity(intent);
				return;
			}
			//开始下载或暂停
			if(!downloader.isStop()){
				// 做暂停操作
				downloader.pause();
				return;
			}
			// 做下载操作
			if (downloader.isStop()) {
				downloadReading(downloader);
				return;
			}
		}
		
	}
	
	/**
	 * 下载操作 包含一系列逻辑判断
	 * @param downloader
	 * @param bookItem
	 */
	private void downloadReading(final ReadingDownLoader downloader){
		// 判断SD卡操作，错误返回提示
		if (!IOUtil.checkSDCard()) {
			Toast.makeText(context, getResources().getString(R.string.exception_sdcard),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		//当网络不是wifi时提示是否下载
		if (NetWorkUtil.getAPNType(context) != 1) {
			String cancel="算了";
			String title="您所在的网络下载会产生流量费用，建议您通过WIFI下载后观看";
			String downText="继续";
			new CustAlertDialog.Builder(context)
				.setMessage(title)
				.setPositiveButton(cancel, new DialogInterface.OnClickListener() {		
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.setNegativeButton(downText, new DialogInterface.OnClickListener() {				
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						new Thread(){								
							@Override
							public void run() {
								try {
									downloader.download();
								} catch (TradException e) {
									handleException(e);
								}												
							};								
						}.start();
					}
				})
				.create().show();
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						downloader.download();
					} catch (TradException e) {
						handleException(e);
					}
				};
			}.start();
		}
		
	}
	
	
	private static class ReadingViewHolder{
		//真题标题
		TextView tvReadingTitle;
		//下载标签
		LinearLayout llReadingListItem;
		//下载文字提示
		TextView downloadTV;
		//下载图片提示
		ImageView downloadImg;
		//整个reanding容器
		ViewGroup readingView;
		//对应的ReadingItem
		ReadingItem readingItem;
	}
	
	//用于viewPage的adapter
	class ReadingViewPageAdapter extends PagerAdapter{

		@Override
		public int getCount()
		{
			return viewPageList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1)
		{
			return arg0==arg1;
		}

		@Override
		public void destroyItem(ViewGroup container,int position, Object object)
		{
			((ViewPager)container).removeView(viewPageList.get(position));			
		}

		@Override
		public void finishUpdate(ViewGroup container)
		{
			super.finishUpdate(container);
		}

		@Override
		public int getItemPosition(Object object)
		{
			return super.getItemPosition(object);
		}
		
		@Override
		public CharSequence getPageTitle(
				int position)
		{
			return super.getPageTitle(position);
		}

		@Override
		public Object instantiateItem(
				ViewGroup container, int position)
		{
			((ViewPager)container).addView(viewPageList.get(position));
			return viewPageList.get(position);
		}		
	}
	
	class ReadingViewPageListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0){}

		@Override
		public void onPageScrolled(int arg0,float arg1, int arg2){}

		@Override
		public void onPageSelected(int arg0)
		{	
			sortNameTV.setText(readingSorts.get(arg0).getName());
			for(int i=0;i<textViews.length;i++) {				
				textViews[arg0].setBackgroundResource(R.drawable.radio_sel);
				if (arg0!=i)
				{					
					textViews[i].setBackgroundResource(R.drawable.radio);
				}
			}
			
		}
		
	}
	
	
	
	
}
