package com.jayz.cet4.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.Message;

import com.jayz.R;
import com.jayz.cet4.common.exception.TradException;
import com.jayz.cet4.model.ReadingItem;

/**
 * 
 * @author Jayz
 *用于下载的下载类，包括下载，删除等操作
 */

public class ReadingDownLoader {
	/** 下载状态：暂停*/
	public static final int DOWN_PAUSE=0;
	/** 下载状态：正在下载*/
	public static final int DOWN_LOADING=1;
	/** 任务状态：完成(包括下载包括解压)*/
	public static final int TASK_COMPLETE=2;
	/** 下载状态：未下载*/
	public static final int DOWN_NONE=3;
	/** 下载状态：开始下载*/
	public static final int DOWN_BEGIN=4;
	/** 下载状态：完成  解压缩状态，开始解压*/
	public static final int DOWN_COMPLETE=5;
	/**标记某一个下载状态*/
	private ReadingItem downloadTag;
	private Handler handler;
	private boolean isStop=true;
	private String targetPath;
	private long currentFileSize;
	private long totalFileSize;
	/**下载的文件的链接*/
	private String url;
	private Context context;
	
	public ReadingDownLoader(Context context,Handler handler,ReadingItem downloadTag,String targetPath,String url) {
		this.context=context;
		this.handler = handler;
		this.downloadTag = downloadTag;
		this.targetPath = targetPath;
		this.url = url;
		File localFile = getLocalFile();
		if(null!=localFile){
			currentFileSize=localFile.length();
		}
	}
	
	/**下载方法
	 * @throws TradException 
	 * @throws NotFoundException */
	public void download() throws TradException{
		sendMsg(DOWN_BEGIN);
		isStop = false;
		File localFile = getLocalFile();
		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile accessFile = null;
		//判断本地文件是否存在，不存在新建文件
		if(localFile == null){
			localFile = new File(targetPath);
			if(!localFile.getParentFile().exists()){
				localFile.getParentFile().mkdirs();
			}
			try {
				localFile.createNewFile();
				currentFileSize = 0;
			} catch (IOException e) {
				throw new TradException(context.getResources().getString(R.string.exception_file_not_found),e);
			}
		}else{
			currentFileSize = localFile.length();
		}
		//用于获取输入流
		try {
			URL u = new URL(url);
			conn= (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5*1000);
			conn.setRequestProperty("Range","bytes="+currentFileSize+"-");
			is = conn.getInputStream();
			if(currentFileSize==0||totalFileSize==0){
				totalFileSize =conn.getContentLength();
				totalFileSize+=currentFileSize;
				onDownloadBegin(totalFileSize);
			}
			LogUtil.i("connected...........");
		} catch (IOException e) {
			conn.disconnect();
			throw new TradException(context.getResources().getString(R.string.exception_net),e);
		}
		//写输出流到本地文件中
		if(is!=null){
			try {
				accessFile = new RandomAccessFile(localFile, "rwd");
				BufferedInputStream bis = new BufferedInputStream(is);
				accessFile.seek(currentFileSize);
				byte[] buffer = new byte[1024];
				int len = 0;
				LogUtil.i("reading...........");
				int destPercent=1;
				while(isStop==false&&(len = bis.read(buffer))!=-1){
					accessFile.write(buffer,0,len);
					currentFileSize += len;
					if(isStop==false&&!handler.equals(null)){
						int percent=getPercent();
						if(percent>=destPercent){
							sendMsg(DOWN_LOADING);
							destPercent=percent+1;
						}
					}
				}
			} catch (IOException e) {
				throw new TradException(context.getResources().getString(R.string.exception_net),e);
			}finally{
				conn.disconnect();
				try {
					is.close();
					accessFile.close();
				} catch (IOException e) {
					LogUtil.e(e);
				}
			}
		}
		if(getPercent()==100){					
			if(!handler.equals(null)){
				isStop = true;
				sendMsg(DOWN_COMPLETE);
			}
		}
	}
	
	public void downloadNotHandlerMessage() throws TradException{
		File localFile = getLocalFile();
		HttpURLConnection conn = null;
		InputStream is = null;
		RandomAccessFile accessFile = null;
		//判断本地文件是否存在，不存在新建文件
		if(localFile == null){
			localFile = new File(targetPath);
			if(!localFile.getParentFile().exists()){
				localFile.getParentFile().mkdirs();
			}
			try {
				localFile.createNewFile();
				currentFileSize = 0;
			} catch (IOException e) {
				throw new TradException(context.getResources().getString(R.string.exception_file_not_found),e);
			}
		}else{
			currentFileSize = localFile.length();
		}
		//用于获取输入流
		try {
			URL u = new URL(url);
			conn= (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5*1000);
			conn.setRequestProperty("Range","bytes="+currentFileSize+"-");
			is = conn.getInputStream();
			if(currentFileSize==0||totalFileSize==0){
				totalFileSize =conn.getContentLength();
				totalFileSize+=currentFileSize;
				onDownloadBegin(totalFileSize);
			}
			LogUtil.i("connected...........");
		} catch (IOException e) {
			conn.disconnect();
			throw new TradException(context.getResources().getString(R.string.exception_net),e);
		}
		//写输出流到本地文件中
		if(is!=null){
			try {
				accessFile = new RandomAccessFile(localFile, "rwd");
				BufferedInputStream bis = new BufferedInputStream(is);
				accessFile.seek(currentFileSize);
				byte[] buffer = new byte[1024];
				int len = 0;
				LogUtil.i("reading...........");
				while((len = bis.read(buffer))!=-1){
					accessFile.write(buffer,0,len);
					currentFileSize += len;
				}
			} catch (IOException e) {
				throw new TradException(context.getResources().getString(R.string.exception_net),e);
			}finally{
				conn.disconnect();
				try {
					is.close();
					accessFile.close();
				} catch (IOException e) {
					LogUtil.e(e);
				}
			}
		}
	}
		
		/** 获取本地文件*/
	public File getLocalFile(){
			if(null==targetPath)
				return null;
			File localFile = new File(targetPath);
			if(localFile.exists()){
				return localFile;
			}else{
				return null;
			}
	}
	
	/** 下载时所响应的操作消息*/
	private void sendMsg(int downState){
		Message msg = handler.obtainMessage(Constants.msgWhat.downstate_changed);
		msg.obj = downloadTag;
		msg.arg2=downState;
		handler.sendMessage(msg);
	}
	
	/** 取得本地文件长度*/
	public long getLocalFileLength(){
		if(getLocalFile()!=null){
			return getLocalFile().length();
		}else{
			return 0;
		}
		
	}
	
	/** 暂停下载 */
	public void pause(){
//LogUtil.e("暂停"+curSize+"-"+fileSize);
		if(!handler.equals(null)){
			sendMsg(DOWN_PAUSE);
		}
		isStop = true;
	}
	
	/** 删除文件*/
	public boolean deleteFile(){
		File file = getLocalFile();
		if(file.exists()){
			file.delete();
			if(!handler.equals(null)){
				sendMsg(DOWN_NONE);
			}
			isStop = true;
			currentFileSize = 0;
			return true;
		}else{
			return false;
		}	
	}
	
	/**用于获得下载进度的百分比*/
	public int getPercent(){
		int percent = totalFileSize==0? 0 : (int) (100*(currentFileSize)/totalFileSize);
		return percent;
	}
	
	public long getCurrentFileSize() {
		return currentFileSize;
	}

	public void setCurrentFileSize(long currentFileSize) {
		this.currentFileSize = currentFileSize;
	}

	public long getTotalFileSize() {
		return totalFileSize;
	}

	public void setTotalFileSize(long totalFileSize) {
		this.totalFileSize = totalFileSize;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isStop() {
		return isStop;
	}

	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}
	
	/**
	 * 下载前操作 子类覆盖此方法
	 * @param filesize 文件长度 {@link #fileSize}
	 */
	protected void onDownloadBegin(long filesize) {}
	
	
}
