package com.jayz.cet4.common.exception;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.jayz.cet4.common.Constants;

public class ExceptionHandler extends Handler {
	Context context;
	public ExceptionHandler(Context context){
		this.context=context;
	}
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		/** 处理异常消息*/
		case Constants.msgWhat.exception:
			Toast.makeText(context, msg.getData().getString(Constants.bundleKey.exception), Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}
	/**
	 * 发送异常消息
	 * @param error
	 */
	public void sendExceptionMsg(String error){
		Message message=this.obtainMessage(Constants.msgWhat.exception);
		message.getData().putString(Constants.bundleKey.exception, error);
		this.sendMessage(message);
	}
}
