package com.wxj.player.backPlay.base;

import android.content.Context;
import android.os.Handler;

import com.wxj.player.backPlay.player.FuseExoPlayer;
import com.wxj.player.backPlay.player.MediaInterface;


/**
 * Created by wuxiaojun on 2019/5/30.
 */

public abstract class BasePlayerProxy {

	protected MediaInterface mPlayer;

	protected Handler				mMainHandler;

	protected BasePlayerController	mController;

	public BasePlayerProxy(BasePlayerController playerController, Context context, String url) {
		this.mPlayer = new FuseExoPlayer(context, url, playerController);
		this.mController = playerController;

		mMainHandler = new Handler();
	}

	public long getCurrentPosition() {
		return mPlayer.getCurrentPosition();
	}

	public long getDuration() {
		return mPlayer.getDuration();
	}

	public void seekTo(long time) {
		mPlayer.seekTo(time);
	}

	public void pause() {
		mPlayer.pause();
	}

	public void start() {
		mPlayer.start();
	}

	public boolean isPlaying() {
		return mPlayer.isPlaying();
	}

	public void setSpeed(float speed) {
		mPlayer.setSpeed(speed);
	}

	public void setVolume(float volume) {
		mPlayer.setVolume(volume, volume);
	}

	public float getVolume() {
		return mPlayer.getVolume();
	}

	public void releaseMediaPlayer() {
		mPlayer.release();
	}

	public void prepare() {
		mPlayer.prepare(); // 播放器准备
	}

	public BasePlayerController getPlayerController() {
		return mController;
	}

	public Handler getMainHandler() {
		return mMainHandler;
	}

}
