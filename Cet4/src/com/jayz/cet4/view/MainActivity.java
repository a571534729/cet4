package com.jayz.cet4.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.jayz.R;
import com.jayz.cet4.view.base.BaseFragmentActivity;
import com.jayz.cet4.view.fragment.GrammarFragment;
import com.jayz.cet4.view.fragment.MoreFragment;
import com.jayz.cet4.view.fragment.TopicFragment;
import com.jayz.cet4.view.fragment.WordsFragment;

public class MainActivity extends BaseFragmentActivity{

    private TabHost tabhost;
    private RadioGroup tabGroup;
    private FragmentManager fragmentManager;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);
        init();
        
    }
    
    /**
     * 初始化对象
     */
    private void init(){
    	tabhost = (TabHost) findViewById(android.R.id.tabhost);
    	tabGroup = (RadioGroup) findViewById(R.id.radio_group_bottom);
    	
    	fragmentManager = getSupportFragmentManager();
    	
    	tabGroup.setOnCheckedChangeListener(checkChangerListener);
    }
    
    /**
     * 监听切换卡事件
     */
    private OnCheckedChangeListener checkChangerListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			
			switch (checkedId) {
			
			case R.id.radio_words:
				tabhost.setCurrentTabByTag("words");
				changeFragment(new WordsFragment(), fragmentManager);
				break;
			case R.id.radio_topic:
				tabhost.setCurrentTabByTag("topic");
				changeFragment(new TopicFragment(MainActivity.this), fragmentManager);
				break;
			case R.id.radio_grammar:
				tabhost.setCurrentTabByTag("grammar");
				changeFragment(new GrammarFragment(), fragmentManager);				
				break;
			case R.id.radio_read:
				tabhost.setCurrentTabByTag("read");
				changeFragment(new GrammarFragment(), fragmentManager);				
				break;
			case R.id.radio_more:
				tabhost.setCurrentTabByTag("more");
				changeFragment(new MoreFragment(), fragmentManager);				
				break;				
			default:
				break;
			}
		}
		
		/**
		 * 
		 * @param fragment 需要切换的fragment
		 * @param fragmentManager fragment管理类
		 */
		private void changeFragment(Fragment fragment,FragmentManager fragmentManager){
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.replace(R.id.content_fragment, fragment);
			fragmentTransaction.commit();
		}
	};



	
	
    
    
}
