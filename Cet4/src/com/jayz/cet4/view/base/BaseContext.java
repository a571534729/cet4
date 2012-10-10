package com.jayz.cet4.view.base;

import com.jayz.cet4.common.LogUtil;
import com.jayz.cet4.common.exception.TradException;

import android.content.Context;

public class BaseContext {
	public static void handleException(Context context,TradException e){
		if(context==null){
			LogUtil.e(e);
		}else if(context instanceof BaseActivity){
			((BaseActivity)context).handleException(e);
		}else if(context instanceof BaseFragmentActivity){
			((BaseFragmentActivity)context).handleException(e);
		}else{
			LogUtil.e(e);
		}
	}
}
