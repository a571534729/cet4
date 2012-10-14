package com.jayz.cet4.view.fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
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
import com.jayz.cet4.common.UnZipUtil;
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.data.TopicItemsDAO;
import com.jayz.cet4.model.TopicItem;
import com.jayz.cet4.view.TopicActivity;
import com.jayz.cet4.view.base.BaseFragment;
import com.jayz.cet4.view.ui.CustAlertDialog;

public class TopicFragment extends BaseFragment{
	
	public static String TOPIC_XML = "config/topic_items.xml";
	private Context context;
	private LayoutInflater mInflater;

	public static String SUBSTRING_WORD = "=";
	
	private List<TopicItem> topicItems;
	private Map<Integer, DownLoader> downloaders;
	private Map<Integer, Integer> downloadStates;
	private Map<Integer, ViewGroup> topicViews;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTopicItems();
		topicViews = new HashMap<Integer, ViewGroup>();
		initDownloaders();
		initDownloadStates();
	}
	
	public TopicFragment(Context context) {
		this.context = context;
	}
	
	
	
	//实例化TopicItems
	private void getTopicItems(){
		InputStream xmlStream = null;
		try {
			xmlStream =context.getAssets().open(TOPIC_XML);
			TopicItemsDAO itemDAO = new TopicItemsDAO();
			topicItems = itemDAO.getAllTopicItems(xmlStream);
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
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		
		RelativeLayout fragmentTopic = (RelativeLayout) inflater.inflate(R.layout.fragment_topic, container, false);
//		articleContent = (TextView) fragmentTopic.findViewById(R.id.article_content);
//		articleContent.setText(content);
//		return fragmentTopic;
		ListView lvTopicContent = (ListView) fragmentTopic.findViewById(R.id.lvTopicContent);
		lvTopicContent.setAdapter(new TopicListAdapter());
		return fragmentTopic;
	}
	
	private void initDownloaders(){
		downloaders = new HashMap<Integer, DownLoader>();
		for(int i=0;i<topicItems.size();i++){
			final TopicItem topicItem=topicItems.get(i);
			//初始化下载地址
			String downloadAdd = topicItem.getDownloadAdd();
			//下载到本地的路径
			String targetPath = Constants.PATH_RES +"/"+
					downloadAdd.substring(downloadAdd.lastIndexOf(SUBSTRING_WORD)+1); 
			//初始化下载器
			DownLoader d = new DownLoader(context,handler,i,targetPath,downloadAdd){
				@Override
				protected void onDownloadBegin(long filesize) {
					super.onDownloadBegin(filesize);
					if(filesize>0){
						SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.sharedPreference.topicConfig.name, Context.MODE_PRIVATE);
						String key=topicItem.getId()+Constants.sharedPreference.topicConfig.size_suffix;
						sharedPreferences.edit().putLong(key, filesize).commit();
					}
				}
			};
			downloaders.put(i, d);
		}
	}
	
	private void initDownloadStates(){
		downloadStates=new HashMap<Integer, Integer>();
		for(int i=0;i<topicItems.size();i++){
			final TopicItem topicItem=topicItems.get(i);
			//初始化下载图标的状态
			SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.sharedPreference.topicConfig.name, Context.MODE_PRIVATE);
			//用于判断本地文件和文件长度是否相等传递初始化状态
			String key=topicItem.getId()+Constants.sharedPreference.topicConfig.size_suffix;
			DownLoader d=downloaders.get(i);
			d.setTotalFileSize(sharedPreferences.getLong(key, 0));
			long fileSize = d.getTotalFileSize();
			long localFileLength = d.getLocalFileLength();
			int status=DownLoader.DOWN_NONE;
			if(localFileLength<fileSize&&localFileLength!=0){
				status=DownLoader.DOWN_PAUSE;
			}else if(localFileLength == fileSize&&localFileLength!=0){
//				status=DownLoader.ZIP_BEGIN;	//TODO 考虑未解压情况处理
				status=DownLoader.DOWN_PAUSE;
			}else if(localFileLength==0){
				String zipDir = Constants.PATH_RES+"/"+topicItem.getDownloadAdd().substring(topicItem.getDownloadAdd().lastIndexOf(SUBSTRING_WORD)+1,topicItem.getDownloadAdd().lastIndexOf("."));
				File f = new File(zipDir);
				if(f.exists()){
					status = DownLoader.TASK_COMPLETE;
				}else{
					status=DownLoader.DOWN_NONE;
				}
			}
			downloadStates.put(i, status);
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
				int index=msg.arg1;
				int downState=msg.arg2;
				downloadStates.put(index, downState);
				updateTopicState(downState, index);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private class TopicListAdapter extends  BaseAdapter{

		@Override
		public int getCount() {
			return topicItems.size();
		}

		@Override
		public Object getItem(int position) {
			return topicItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TopicViewHolder holder;
			if(convertView==null){
				LinearLayout llTopicListItem = (LinearLayout) mInflater.inflate(R.layout.topic_list_item, null);
				convertView = llTopicListItem;
				holder = new TopicViewHolder();
				holder.tvTopicTitle = (TextView) llTopicListItem.findViewById(R.id.titleTopicListItem);
				holder.tvDownload = (TextView) llTopicListItem.findViewById(R.id.downloadTV);
				holder.ivDownload = (ImageView) llTopicListItem.findViewById(R.id.downloadImg);
				holder.topicView  = (ViewGroup) llTopicListItem.findViewById(R.id.llTopicListItem);
//				holder.topicView.setTag(position);
				convertView.setTag(holder);
			}else{
				holder = (TopicViewHolder) convertView.getTag();
			}
			holder.topicIndex=position;
			topicViews.put(position, holder.topicView);
			holder.tvTopicTitle.setText(topicItems.get(position).getName());
			holder.topicView.setOnClickListener(new TopicItemOnClickListenner(position));
			holder.ivDownload.setOnClickListener(new TopicItemOnClickListenner(position));			
			updateTopicState(downloadStates.get(position), holder.topicIndex, holder, true);
			return convertView;
		}
				
	}
	
	
	private class TopicItemOnClickListenner implements OnClickListener{

		private int index;
		public TopicItemOnClickListenner(int index){
			this.index=index;
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
			// 获取当前真题的下载器
			final DownLoader downloader = downloaders.get(index);
			// 获取本真题的对象
			final TopicItem topicItem = topicItems.get(index);
			// 判断是否是下载完的文件
			String downloadAdd = topicItem.getDownloadAdd();
			String zipDir = Constants.PATH_RES
					+ "/"
					+ downloadAdd.substring(
							downloadAdd.lastIndexOf(SUBSTRING_WORD) + 1,
							downloadAdd.lastIndexOf("."));
			File f = new File(zipDir);
			// 书本下载完成点击所做的操作
			if (f.exists()&&downloader.getLocalFileLength()==0) {
				Intent intent=new Intent(context, TopicActivity.class);
				intent.putExtra(Constants.bundleKey.topicItem, topicItem);
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
				downloadTopic(downloader, topicItem);
				return;
			}
		}
		
	}
	
	/**
	 * 下载操作 包含一系列逻辑判断
	 * @param downloader
	 * @param bookItem
	 */
	private void downloadTopic(final DownLoader downloader,
			final TopicItem topicItem) {
		if (!NetWorkUtil.isOnline(context)) {
			Toast.makeText(context,
					getString(R.string.exception_net), Toast.LENGTH_SHORT)
					.show();
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
	
	/**
	 * 根据下载状态更新ui
	 * @param status  下载状态 包括  {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param bookIndex  书本索引
	 */
	private void updateTopicState(int status, final int topicIndex) {
		TopicViewHolder topicViewHolder=new TopicViewHolder();
		ViewGroup topicView = topicViews.get(topicIndex);
		//若topicView的tag与书本索引不等 说明该topicView不是要更新的view 这里是为了防止回收再利用的topicView接收到下载线程的更新请求
		if(((TopicViewHolder)topicView.getTag()).topicIndex!=topicIndex){
			return;
		}
		topicViewHolder.topicView = topicView;
		topicViewHolder.ivDownload = (ImageView) topicView.findViewById(R.id.downloadImg);
		topicViewHolder.tvDownload = (TextView) topicView.findViewById(R.id.downloadTV);
		topicViewHolder.tvTopicTitle = (TextView) topicView.findViewById(R.id.titleTopicListItem);
		updateTopicState(status, topicIndex, topicViewHolder,true);
	}
	
	/**
	 * 根据下载状态更新ui  这个方法用于有topicViewHolder缓存的更新
	 * @param status  下载状态 包括 {@link DownLoader#DOWN_BEGIN}、{@link DownLoader#TASK_COMPLETE}、{@link DownLoader#DOWN_LOADING}、{@link DownLoader#DOWN_NONE}、{@link DownLoader#DOWN_PAUSE}
	 * @param topicIndex  书本索引
	 * @param bookViewHolder bookView视图缓存
	 * @param operation 是否进行非UI的操作,具体指下载解压等行为(实际上该参数是专为ListView更新UI用的)
	 */
	private void updateTopicState(int status, final int topicIndex,TopicViewHolder topicViewHolder,boolean operation) {
		final DownLoader downloader = downloaders.get(topicIndex);
		final TopicItem topicItem = topicItems.get(topicIndex);
		TextView percent = topicViewHolder.tvDownload;
		ImageView downloadImg = topicViewHolder.ivDownload;
		TextView topicTitle = topicViewHolder.tvTopicTitle;
		
		switch(status){
		case DownLoader.DOWN_PAUSE:
			percent.setText("继续下载");
			downloadImg.setBackgroundResource(R.drawable.pause1);
			topicTitle.setText(topicItem.getName());
			break;
		case DownLoader.DOWN_BEGIN:
			percent.setText("0%");
			downloadImg.setBackgroundResource(R.drawable.down);
			topicTitle.setText(topicItem.getName());
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
			topicTitle.setText(topicItem.getName());
			break;
		case DownLoader.DOWN_NONE:	
			percent.setText("下载");
			downloadImg.setBackgroundResource(R.drawable.down);
			topicTitle.setText(topicItem.getName());
			break;
		case DownLoader.DOWN_COMPLETE://开始解压
			if(operation){
				new Thread(){
					@Override
					public void run() {
						UnZipUtil zipUtil = new UnZipUtil();
						String downAdd = downloader.getUrl();
						String zipPath = Constants.PATH_RES+"/"+downAdd.substring(downAdd.lastIndexOf(SUBSTRING_WORD)+1);
						String destDir = Constants.PATH_RES+"/";
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
						msg.arg1=topicIndex;
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
	
	private static class TopicViewHolder{
		//整个真题的外容器
		ViewGroup topicView;
		//真题标题
		TextView tvTopicTitle;
		//下载百分比
		TextView tvDownload;
		//下载状态图标
		ImageView ivDownload;
		//标志当前Item的索引
		int topicIndex;
	}
		
}
