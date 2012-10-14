package com.jayz.cet4.view.fragment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
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
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.data.GrammarItemsDAO;
import com.jayz.cet4.model.GrammarItem;
import com.jayz.cet4.model.GrammarSort;
import com.jayz.cet4.view.GrammarActivity;
import com.jayz.cet4.view.TopicActivity;
import com.jayz.cet4.view.base.BaseFragment;

public class GrammarFragment extends BaseFragment{
	
	private Context context;
	private LayoutInflater mInflater;
	public static String Grammar_XML = "config/grammar_items.xml";
	
	public static String SUBSTRING_WORD = "=";
	
	private TextView sortNameTV;

	private List<GrammarSort> grammarSorts;
	//储存于viewpage的viewlist
	private List<View> viewPageList;
	//储存底牌分屏标记的textview
	private TextView[] textViews;
	private TextView radioTextView;
	
	public GrammarFragment(Context context) {
		this.context = context;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewPageList = new ArrayList<View>();
		getGrammarSorts();

	}
	
	
	//实例化TopicItems
	private void getGrammarSorts(){
		InputStream xmlStream = null;
		try {
			xmlStream =context.getAssets().open(Grammar_XML);
			GrammarItemsDAO itemDAO = new GrammarItemsDAO();
			grammarSorts = itemDAO.getAllGrammarSorts(xmlStream);
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
		RelativeLayout fragmentGrammar = (RelativeLayout) inflater.inflate(R.layout.fragment_grammar, container, false);
		sortNameTV = (TextView) fragmentGrammar.findViewById(R.id.sortGrammar);
		ViewPager viewPageGrammar = (ViewPager) fragmentGrammar.findViewById(R.id.grammarViewPager);
		LinearLayout group = (LinearLayout) fragmentGrammar.findViewById(R.id.viewGroup);
		textViews=new TextView[grammarSorts.size()];
		for(int i=0;i<grammarSorts.size();i++){
			RelativeLayout mRelativeLayout = (RelativeLayout) inflater.inflate(R.layout.grammar_list_rl, null);
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
		viewPageGrammar.setAdapter(new grammarViewPageAdapter());
		viewPageGrammar.setOnPageChangeListener(new grammarViewPageListener());
		
		return fragmentGrammar;
	}
	
	//进行拼装viewPage 将viewPage里的所有ListView拼装好
	private void assemblyListView(){
		for(int i=0;i<viewPageList.size();i++){
			View viewPageItem = viewPageList.get(i);
			ListView viewPageItemLV = (ListView) viewPageItem.findViewById(R.id.lvTopicContent);
			viewPageItemLV.setAdapter(new viewPageItemAdapter(i));			
		}
	}
	
	
	//用于显示listview的adapter
	private class viewPageItemAdapter extends BaseAdapter{
		
		int index;
		List<GrammarItem> grammarItems;
		
		public viewPageItemAdapter(int index) {
			this.index = index;
			grammarItems = grammarSorts.get(index).getGrammars();
		}

		@Override
		public int getCount() {
			return grammarItems.size();
		}

		@Override
		public Object getItem(int position) {
			return grammarItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GrammarViewHolder holder;
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.grammar_list_item, null);
				holder = new GrammarViewHolder();
				holder.tvGrammarTitle = (TextView) convertView.findViewById(R.id.titleGrammarListItem);
				holder.llGrammarListItem = (LinearLayout) convertView;
				convertView.setTag(holder);
			}else{
				holder = (GrammarViewHolder) convertView.getTag();
			}
			holder.tvGrammarTitle.setText(grammarItems.get(position).getName());
			holder.llGrammarListItem.setOnClickListener(new GrammarItemOnClickListenner(grammarItems.get(position)));
			return convertView;
		}
		
	}
	
	private class GrammarItemOnClickListenner implements OnClickListener{
		
		GrammarItem grammarItem;
		
		public GrammarItemOnClickListenner(GrammarItem grammarItem) {
			this.grammarItem = grammarItem;
		}

		@Override
		public void onClick(View v) {
			// 判断SD卡操作，错误返回提示
			if (!IOUtil.checkSDCard()) {
				Toast.makeText(context, getResources().getString(R.string.exception_sdcard),
						Toast.LENGTH_SHORT).show();
				return;
			}
			
			// 判断是否是下载完的文件
			String downloadAdd = grammarItem.getDownLoadAdd();
//			String zipDir = Constants.PATH_RES
//					+ "/"
//					+ downloadAdd.substring(
//							downloadAdd.lastIndexOf(SUBSTRING_WORD) + 1,
//							downloadAdd.lastIndexOf("."));

			//下载到本地的路径
			String targetPath = Constants.PATH_RES +"/"+
					downloadAdd.substring(downloadAdd.lastIndexOf(SUBSTRING_WORD)+1); 
			File f = new File(targetPath);
			// 点击所做的操作
			if (f.exists()) {
				Intent intent=new Intent(context, GrammarActivity.class);
				intent.putExtra(Constants.bundleKey.grammarItem, grammarItem);
				startActivity(intent);
				return;
			}else{
				new downloadTask(targetPath, downloadAdd).execute();
				Intent intent=new Intent(context, TopicActivity.class);
				intent.putExtra(Constants.bundleKey.grammarItem, grammarItem);
				startActivity(intent);

			}
		}
	
	}
	
	private class downloadTask extends AsyncTask<Void, Void, Boolean>{

		String targetPath;
		String downloadAdd;
		ProgressDialog progressDialog;
		
		public downloadTask(String targetPath,String downloadAdd) {
			this.targetPath = targetPath;
			this.downloadAdd = downloadAdd;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			new Thread(){								
				@Override
				public void run() {
					try {
						DownLoader downloader = new DownLoader(context, null, -1, targetPath, downloadAdd);
						downloader.downloadNotHandlerMessage();
					} catch (TradException e) {
						handleException(e);
					}							
				};								
			}.start();
					
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			if (this.progressDialog == null) {
				this.progressDialog = new ProgressDialog(
						context);
				progressDialog
						.setMessage(context.getText(R.string.loading));
			}
			this.progressDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
			super.onPostExecute(result);
		}
		
	}
	
	private static class GrammarViewHolder{
		//真题标题
		TextView tvGrammarTitle;
		//下载标签
		LinearLayout llGrammarListItem;
	}
	
	//用于viewPage的adapter
	class grammarViewPageAdapter extends PagerAdapter{

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
	
	class grammarViewPageListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0){}

		@Override
		public void onPageScrolled(int arg0,float arg1, int arg2){}

		@Override
		public void onPageSelected(int arg0)
		{	
			sortNameTV.setText(grammarSorts.get(arg0).getName());
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
