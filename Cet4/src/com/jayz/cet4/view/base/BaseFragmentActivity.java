package com.jayz.cet4.view.base;

import com.jayz.R;
import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.common.exception.ExceptionHandler;
import com.jayz.cet4.common.exception.TradException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseFragmentActivity extends FragmentActivity {
	protected ExceptionHandler eHandler;
	protected Context context;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		context=this;
		eHandler=new ExceptionHandler(context);
		super.onCreate(savedInstanceState);
	}
	@Override
	protected void onResume() {
		super.onResume();
		//UMeng统计
//		MobclickAgent.onResume(context);
	}
	
	@Override
	protected void onPause() {
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
	
    protected void exit() {
    	new AlertDialog.Builder(this)
    		.setMessage("是否确定要退出"+context.getResources().getString(R.string.app_name))
    		.setNegativeButton("取消", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.setPositiveButton("退出", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
	                android.os.Process.killProcess(android.os.Process.myPid());
	                System.exit(0);
				}
			}).create().show();
    };
}
