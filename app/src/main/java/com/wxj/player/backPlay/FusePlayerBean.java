package com.wxj.player.backPlay;


import com.wxj.player.backPlay.base.BasePlayerController;

/**
 * Created by wuxiaojun on 2019/5/30.
 */

public class FusePlayerBean {

	private int						mUid;				// 当前播放器对应的uid

	private String					mUrl;				// 当前播放的url

	private int						mState;			// 当前播放器状态

	private int						mProgress;			// 播放器的进度

	private BasePlayerController mController;		// 播放器对应的控制器

	private boolean					isSynchronization;	// true:同步 false:不同步

	private boolean					isVideo;			// true:为视频 false:为音频

	public FusePlayerBean(int mUid, String mUrl, int mState, int mProgress, BasePlayerController mController, boolean isSynchronization, boolean isVideo) {
		this.mUid = mUid;
		this.mUrl = mUrl;
		this.mState = mState;
		this.mProgress = mProgress;
		this.mController = mController;
		this.isSynchronization = isSynchronization;
		this.isVideo = isVideo;
	}

	public boolean isSynchronization() {
		return isSynchronization;
	}

	public void setSynchronization(boolean synchronization) {
		isSynchronization = synchronization;
	}

	public int getUid() {
		return mUid;
	}

	public String getUrl() {
		return mUrl == null ? "" : mUrl;
	}

	public int getState() {
		return mState;
	}

	public int getProgress() {
		return mProgress;
	}

	public BasePlayerController getController() {
		return mController;
	}

	public void setState(int state) {
		this.mState = state;
	}

	public void setProgress(int progress) {
		this.mProgress = progress;
	}

	public boolean isVideo() {
		return isVideo;
	}
}
