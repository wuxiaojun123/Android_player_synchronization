package com.wxj.player.backPlay;

/**
 * 单个播放器的状态 Created by wuxiaojun on 2019/4/1.
 */

public interface IPlayerState {

	void getCurrentState(int hashcode, int currentState);

	void resetProgressAndTime();

	void setDuration(long duration);

	void setCurrentTime(String currentTime);

	void setVideoProgress(int hashcode, int videoProgress, String currentTime);

	void setVideoMaxProgress(int maxProgress);

	void showLoading();

	void hideLoading();

	void onSeekComplete(int flag);

	void onError(int flag, int error, int extra);
}
