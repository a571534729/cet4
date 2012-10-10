package com.jayz.cet4.common;

import android.os.Environment;

public class Constants {
	
	/** SD卡路径 */
	public static final String PATH_SD=Environment.getExternalStorageDirectory().getAbsolutePath();
	/** 组织路径 */
	public static final String PATH_COM=PATH_SD+"/Coodroid";
	/** 应用路径 */
	public static final String PATH_APP=PATH_COM+"/CET4";
	/** 资源路径 */
	public static final String PATH_RES=PATH_APP+"/res";
	
	/**SD不存在或者加载出错提示*/
	public static final String SDERROR="您的SD卡不存在或者存在错误加载,请查看!!";
	/**网络不存在的提示*/
	public static final String NETERROR="亲,你的网络不大好,请稍后再试!!";
	/**下载出错的提示*/
	public static final String DOWNERROR="ORZ,下载出错,正在尝试重新下载!!";
	/**
	 * 真题里类型的标记
	 * */
	public static class TypeTopic{
		public static final int LISTENING_TYPE = 0;
		public static final int READING_TYPE = 1;
		public static final int SELECTING_TYPE =2;
		public static final int CLOZE_TYPE = 3;
		public static final int TRANSLATION_TYPE = 4;
		public static final int WRITING_TYPE = 5;
	}
	
	/**
	 *真题里类型所对应的文字
	 */
	public static class TypeTopicText{
		public static final String LISTENING_TXT = "听 力";
		public static final String READING_TXT = "阅 读";
		public static final String SELECTING_TXT ="填 空";
		public static final String CLOZE_TYPE_TXT = "完 型";
		public static final String TRANSLATION_TXT = "翻 译";
		public static final String WRITING_TXT = "写 作";
	}
	
	/** bundle传递数据时的key */
	public static class bundleKey{
		/** 真题信息 **/
		public static final String topicItem="topicItem";
		/** 异常信息 */
		public static final String errorMsg="errorMsg";
		/** 需要弹出的信息*/
		public static final String toastMag="toastMsg";
	}
	
	/** message.what */
	public static class msgWhat{
		/** 异常消息 */
		public static final int error=-200;
		/** 下载状态改变*/
		public static final int downstate_changed=2000;
		/** toast*/
		public static final int toast=3000;
	}
	
	/** sharedPreference 相关key */
	public static class sharedPreference{
		/** 真题配置信息 */
		public static class topicConfig{
			/** sharedPreference名称 */
			public static final String name="topicConfig";
			/** 书本大小 key的后缀*/
			public static final String size_suffix="_size";
			/** 是否需要积分 key的后缀*/
			public static final String needPoints_suffix="_needPoints";
		}
	}
}
