package com.jayz.cet4.common;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ViewUtil {
	/**
	 * 格式化时间为   分：秒   格式
	 * @param timeMs 单位 milliseconds
	 * @return
	 */
	public static String formatTimeInmmss(int timeMs) {
		StringBuilder timeDisplay = new StringBuilder();
		int timeMin = timeMs / 60000;
		int timeRestSec = timeMs / 1000 % 60;
		if (timeMin <= 0) {
			timeDisplay.append("00");
		} else if (timeMin < 10) {
			timeDisplay.append("0");
			timeDisplay.append(timeMin);
		} else {
			timeDisplay.append(timeMin);
		}
		timeDisplay.append(":");
		if (timeRestSec <= 0) {
			timeDisplay.append("00");
		} else if (timeRestSec < 10) {
			timeDisplay.append("0");
			timeDisplay.append(timeRestSec);
		} else {
			timeDisplay.append(timeRestSec);
		}
		return timeDisplay.toString();
	}
	
}
