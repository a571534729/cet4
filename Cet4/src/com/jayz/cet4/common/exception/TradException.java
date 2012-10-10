/**
 * 
 */
package com.jayz.cet4.common.exception;

import com.jayz.R;


/**
 * @author alkaid
 * 业务异常类
 */
public class TradException extends Exception {
	private int resId=0;
	public TradException(Throwable e) {
		super(null,e);
	}
	public TradException(String msg,Throwable e) {
		super(msg, e);
	}
	public TradException(int resId,Throwable e) {
		super(null, e);
		this.resId=resId;
	}
	public int getResId(){
		return this.resId!=0?resId:R.string.exception_unknow;
	}
	
}
