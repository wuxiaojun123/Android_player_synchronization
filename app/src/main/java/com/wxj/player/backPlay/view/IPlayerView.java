package com.wxj.player.backPlay.view;

/**
 * Created by wuxiaojun on 2019/3/26.
 */

public interface IPlayerView {

	void updateStartImage(int currentState);

	void updateRecyclerView1(int status, int progress);

	void updateRecyclerView2(int status, int progress);

	void setDuration(long duration);

	void onPause(int progress);

	void setCurrentTime(String time);

	void setVideoProgress(int progress);

	void setVideoMaxProgress(int maxProgress);

	void resetProgressAndTime();

	void showLoading();

	void hideLoading();

	void showControlView();

	void dissmissControlView();

	void onError(int flag);

}
