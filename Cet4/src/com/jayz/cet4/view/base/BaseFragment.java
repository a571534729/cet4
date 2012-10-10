package com.jayz.cet4.view.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.common.exception.ExceptionHandler;
import com.jayz.cet4.common.exception.TradException;

public class BaseFragment extends Fragment {
	protected ExceptionHandler eHandler;
	protected Context context;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		context=getActivity();
		eHandler=new ExceptionHandler(context);
		super.onCreate(savedInstanceState);
	}
	@Override
	public void onResume() {
		super.onResume();
		//UMeng统计
//		MobclickAgent.onResume(context);
	}
	
	@Override
	public void onPause() {
		//UMeng统计
//		MobclickAgent.onPause(context);
		super.onPause();
	}
	/**
	 * 处理异常 
	 * @param e
	 */
	public void handleException(TradException e){
		String msg=e.getMessage();
		msg=null!=msg?msg:context.getString(e.getResId());
		LogUtil.e(msg,e);
		eHandler.sendExceptionMsg(msg);
	}
	
}
