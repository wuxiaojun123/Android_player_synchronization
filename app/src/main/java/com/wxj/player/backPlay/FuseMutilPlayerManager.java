package com.wxj.player.backPlay;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.wxj.player.backPlay.bean.UserBean;
import com.wxj.player.backPlay.base.BasePlayerController;
import com.wxj.player.backPlay.utils.PlayerConstant;
import com.wxj.player.backPlay.utils.PlayerUtils;
import com.wxj.player.backPlay.view.IPlayerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuxiaojun on 2019/6/17.
 */

public class FuseMutilPlayerManager {

	private static final String		TAG					= "MutilPlayerManager";

	private static final int		TIME_DIFFERENCE		= 2000;					// 每个视频音频容错时间

	private IPlayerView				mView;										// 更新界面

	private int						mStopTrackingTouchStatus;					// 拖动进度条时的播放器状态

	private List<FusePlayerBean>	mFusePlayerBeans	= new ArrayList<>();

	private int						mySelfUid;

	private int						mTeacherUid;

	private List<Integer>			mTeacherUids		= new ArrayList<>();	// 老师的uid集合，因为有替换老师的场景

	public FuseMutilPlayerManager(List<ViewGroup> containsList, IPlayerView view, Context context, List<UserBean> users, int mySelfUid) {
		this.mView = view;
		this.mySelfUid = mySelfUid;

		mTeacherUid = 777; // 测试数据
		initFusePlayerBeans(containsList, context, users, mySelfUid, users.size());
	}

	private void initFusePlayerBeans(List<ViewGroup> containsList, Context context, List<UserBean> users, int mySelfUid, int size) {
		MyPlayerState state = new MyPlayerState();
		for (int i = 0; i < size; i++) {
			UserBean userBean = users.get(i);

			FusePlayerBean fusePlayerBean;

			FuseVideoPlayerController videoController = new FuseVideoPlayerController(containsList.get(i), userBean.getVideoUrl(), state, userBean.getUid(), context);
			fusePlayerBean = new FusePlayerBean(userBean.getUid(), userBean.getVideoUrl(), PlayerConstant.CURRENT_STATE_NORMAL, 0, videoController, true, true);

			mFusePlayerBeans.add(fusePlayerBean);
		}
	}

	public void clickStart() { // 点击开始
		mStopTrackingTouchStatus = -1;
		for (FusePlayerBean bean : mFusePlayerBeans) {
			if (bean.isSynchronization()) {
				bean.getController().clickStart();
			}
		}
	}

	public void initVideoPlayer() {
		mStopTrackingTouchStatus = -1;
		for (FusePlayerBean bean : mFusePlayerBeans) {
			bean.getController().initVideoPlayer();
		}
	}

	public void setSynchronizedByUid(int uid, boolean isSync) {
		if (uid == mySelfUid) {
			return;
		}
		if (mTeacherUids.contains(uid)) {
			return;
		}

		BasePlayerController teacherPlayerController = getTeacherPlayerController();
		float volume = teacherPlayerController.getVolume();

		for (FusePlayerBean bean : mFusePlayerBeans) {
			if (bean.getUid() == uid) {
				if (isSync) {
					// bean.getController().setPreparation(true);
					// 快进当前播放器，并且快进完成的时候播放音频
					if (teacherPlayerController != null) {
						bean.getController().setVolume(volume);
					}
				} else {
					bean.getController().setVolume(0f);
				}
				break;
			}
		}
	}

	public BasePlayerController getTeacherPlayerController() {
		for (FusePlayerBean bean : mFusePlayerBeans) {
			if (bean.getUid() == mTeacherUid) {
				return bean.getController();
			}
		}
		return null;
	}

	public void onProgressChanged(int progress, boolean fromUser) {
		for (FusePlayerBean bean : mFusePlayerBeans) {
			bean.getController().onProgressChanged(progress, fromUser);
		}
	}

	public void onStartTrackingTouch() {
		for (FusePlayerBean bean : mFusePlayerBeans) {
			bean.getController().onStartTrackingTouch();
		}
	}

	public void onStopTrackingTouch(int progress) {
		FusePlayerBean bean = mFusePlayerBeans.get(0);
		if (bean.getState() == PlayerConstant.CURRENT_STATE_PAUSE) {
			mStopTrackingTouchStatus = bean.getState();
		}
		for (FusePlayerBean playerBean : mFusePlayerBeans) {
			playerBean.getController().seekTo(progress);
		}
		setCurrentTimeAndProgress(progress, PlayerUtils.stringForTime(progress));
	}

	public void releaseAllVideos() {
		for (FusePlayerBean bean : mFusePlayerBeans) {
			bean.getController().releaseAllVideos();
		}
	}

	private int getTeacherState() {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == mTeacherUid) {
				return fusePlayerBean.getState();
			}
		}
		return -1;
	}

	private BasePlayerController getPlayerControllerByFlag(int uid) {
		int size = mFusePlayerBeans.size();
		for (int i = 0; i < size; i++) {
			FusePlayerBean fusePlayerBean = mFusePlayerBeans.get(i);
			if (fusePlayerBean.getUid() == uid) {
				return fusePlayerBean.getController();
			}
		}
		return null;
	}

	private void seekToSameProgress(int uid) {
		BasePlayerController controller = getPlayerControllerByFlag(uid);
		// int otherUid = getOtherUid(uid);
		// int progress = getPlayerProgressByUid(otherUid);
		int progress = getPlayerProgressByUid(mTeacherUid); // 这里每次都取的是老师的进度，因为一般情况下快进完成最快的就是第一个播放器

		if (controller == null) {
			return;
		}
		controller.seekTo(progress);
	}

	private int getPlayerProgressByUid(int uid) {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (uid == fusePlayerBean.getUid()) {
				return fusePlayerBean.getProgress();
			}
		}
		return 0;
	}

	private void pauseAllPlayerByMyself() {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.isSynchronization()) {
				fusePlayerBean.getController().pauseByMyselfActive();
			}
		}
	}

	private void pausePlayerMyselfByUid(int uid) {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == uid) {
				fusePlayerBean.getController().pauseByMyselfActive();
			}
		}
	}

	/***
	 * 需要判断，音频是否需要同步
	 */
	private void playAllPlayerByMyself() {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.isSynchronization()) {
				fusePlayerBean.getController().playingWhenPauseByMyself();
			}
		}
	}

	private boolean judgePlayerStateSame(int currentUid) { // 没有用
		int otherUid = getOtherUid(currentUid);
		int currentState = -1;
		int otherState = -1;

		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == currentUid) {
				currentState = fusePlayerBean.getState();
			} else if (fusePlayerBean.getUid() == otherUid) {
				otherState = fusePlayerBean.getState();
			}
		}

		return currentState == otherState;
	}

	private boolean judgePlayerStateSame122(int currentUid) {
		int currentState = -1;

		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == currentUid) { // 为何只要是同步，就返回true？
				// if (fusePlayerBean.isSynchronization()) {
				// return true;
				// }
				currentState = fusePlayerBean.getState();
			}
		}

		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.isSynchronization()) {
				if (currentState != fusePlayerBean.getState()) {
					return false;
				}
			}
		}

		return true;
	}

	private boolean judgePlayerProgressSame(int currentUid) {
		if (currentUid == mTeacherUid) {
			return true;
		}
		int currentProgress = -1;
		int otherProgress = getProgress();

		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == currentUid) {
				currentProgress = fusePlayerBean.getProgress();
			}
		}

		int abs = Math.abs(currentProgress - otherProgress);

		return abs < TIME_DIFFERENCE;
	}

	private int getOtherUid(int uid) {
		if (uid == mTeacherUid) {
			return mySelfUid;
		} else {
			return mTeacherUid;
		}
	}

	public boolean isPlaying() {
		int status = mFusePlayerBeans.get(0).getState();
		return status == PlayerConstant.CURRENT_STATE_PLAYING;
	}

	public boolean isPause() {
		int status = mFusePlayerBeans.get(0).getState();
		return status == PlayerConstant.CURRENT_STATE_PAUSE || status == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF;
	}

	public int getProgress() {
		return mFusePlayerBeans.get(0).getProgress();
	}

	private void setCurrentTimeAndProgress(int videoProgress, String currentTime) {
		if (isPlaying()) {
			mView.setVideoProgress(videoProgress);
		} else if (isPause()) {
			mView.onPause(videoProgress);
		}
		mView.setCurrentTime(currentTime);
	}

	private void setPlayerStateByUid(int uid, int currentState) {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == uid) {
				fusePlayerBean.setState(currentState);
				break;
			}
		}
	}

	private void setPlayerProgressByUid(int uid, int currentProgress) {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == uid) {
				fusePlayerBean.setProgress(currentProgress);
				break;
			}
		}
	}

	private int getPlayerStateByUid(int uid) {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			if (fusePlayerBean.getUid() == uid) {
				return fusePlayerBean.getState();
			}
		}
		return -1;
	}

	public void addTeacherUid(int uid) {
		if (!mTeacherUids.contains(uid)) {
			mTeacherUids.add(uid);
		}
	}

	public void setMediaAutoCompletion() {
		for (FusePlayerBean fusePlayerBean : mFusePlayerBeans) {
			fusePlayerBean.getController().onMediaAutoCompletion();
		}
	}

	private class MyPlayerState implements IPlayerState {

		@Override public void getCurrentState(int uid, int currentState) { // 回调状态方法
			setPlayerStateByUid(uid, currentState);

			if (mFusePlayerBeans.size() > 1) {
				refreshRecyclerview();
				if (uid == mTeacherUid && currentState != PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
					mView.updateStartImage(currentState);
				}
				playAllByMyselfWhenStateEqualsPauseMyself(uid, currentState);

			} else {
				mView.updateStartImage(currentState);
			}
		}

		/***
		 * 当播放器的状态都是自己主动暂停的状态并且播放器的差值在允许范围内
		 *
		 * @param uid
		 *            当前用户id
		 * @param currentState
		 *            当前播放器状态
		 */
		private void playAllByMyselfWhenStateEqualsPauseMyself(int uid, int currentState) {
			int teacherState = getTeacherState();
			if (judgePlayerStateSame(uid)) {
				if (currentState != PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
					mView.updateStartImage(currentState);
				}
				if (teacherState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) { //
					// 开始播放
					boolean same = judgePlayerProgressSame(uid);
					if (same) {
						Log.e(TAG, "getCurrentState 当前状态是 自己暂停中的 true");
						playAllPlayerByMyself();
					}
				}
			}
		}

		@Override public void setVideoProgress(int uid, int videoProgress, String currentTime) { // 当前进度
			setPlayerProgressByUid(uid, videoProgress);

			if (mFusePlayerBeans.size() > 1) {
				refreshRecyclerview();
				if (judgePlayerStateSame(uid)) { // 播放器状态一致
					playerStateSame(uid);
				} else {
					playerStateDifferent(uid);
				}
			}

			if (uid == mTeacherUid) {
				setCurrentTimeAndProgress(videoProgress, currentTime);
			}
		}

		private void refreshRecyclerview() {
			// mView.updateRecyclerView1(mStateArray.get(0),
			// mProgressArray.get(0));
			// mView.updateRecyclerView2(mStateArray.get(1),
			// mProgressArray.get(1));
		}

		/***
		 * 多个播放器状态不一样
		 *
		 * @param uid
		 *            当前flag
		 */
		private void playerStateDifferent(int uid) {
			int otherUid = getOtherUid(uid);

			int currentState = getPlayerStateByUid(uid);
			int otherState = getPlayerStateByUid(otherUid);
			Log.e(TAG, "setVideoProgress 多个播放器状态不一致,当前uid=" + uid + "当前状态" + currentState + "老师或当前学生视频状态" + otherState);

			if (currentState == PlayerConstant.CURRENT_STATE_PLAYING) {
				if (otherState == PlayerConstant.CURRENT_STATE_PREPARING) {
					Log.e(TAG, "一个播放中，一个是准备中");
					BasePlayerController playerController = getPlayerControllerByFlag(uid);
					if (playerController == null) {
						return;
					}
					playerController.pauseByMyselfActive();

				} else if (otherState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
					Log.e(TAG, "一个播放中，一个是自我暂停");
					BasePlayerController playerController = getPlayerControllerByFlag(uid);
					if (playerController == null) {
						return;
					}
					playerController.pauseByMyselfActive();

				} else if (otherState == PlayerConstant.CURRENT_STATE_PAUSE) {
					Log.e(TAG, "一个播放中，一个是被动暂停");
					BasePlayerController playerController = getPlayerControllerByFlag(uid);
					if (playerController == null) {
						return;
					}
					playerController.pauseByPassive();

				}
			} else if (currentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
				if (otherState == PlayerConstant.CURRENT_STATE_PLAYING) {
					// 判断时间是否一致，如果一致，则直接播放，如果不一致，则暂停所有播放器，并且快进当前播放器
					boolean same = judgePlayerProgressSame(uid);
					if (!same) {
						Log.e(TAG, "setVideoProgress 两个播放器状态一个是自我暂停，一个是播放中,但是前后时间不一致");
						pauseAllPlayerByMyself();
						seekToSameProgress(uid);

					} else {
						BasePlayerController playerController = getPlayerControllerByFlag(uid);
						if (playerController == null) {
							return;
						}
						playerController.playingWhenPauseByMyself();
					}
				}

			}

		}

		/***
		 * 多个播放器状态相同
		 *
		 * @param uid
		 *            当前用户
		 */
		private void playerStateSame(int uid) {
			int currentPlayState = getPlayerStateByUid(uid);
			Log.e(TAG, "setVideoProgress 多个播放器状态一致,uid=" + uid + "当前状态" + currentPlayState);

			if (currentPlayState == PlayerConstant.CURRENT_STATE_PLAYING) { // 正在播放
				boolean same = judgePlayerProgressSame(uid);
				if (!same) {
					Log.e(TAG, "setVideoProgress 两个播放器状态都是播放中,但是前后时间不一致,当前uid=" + uid);
					pauseAllPlayerByMyself();
					seekToSameProgress(uid);
				}

			} else if (currentPlayState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) { // 开始播放
				boolean same = judgePlayerProgressSame(uid);
				Log.e(TAG, "setVideoProgress 两个播放器状态都是自我暂停中,但是前后时间是否相同" + same);
				if (same) {
					playAllPlayerByMyself();
				} else {
					seekToSameProgress(uid);
				}
			}
		}

		@Override public void onSeekComplete(int uid) {
			if (mFusePlayerBeans.size() > 1) {

				if (mStopTrackingTouchStatus == PlayerConstant.CURRENT_STATE_PAUSE) {
					BasePlayerController playerController = getPlayerControllerByFlag(uid);
					if (playerController != null) {
						playerController.pauseByPassive();
					}
					return;
				}

				int otherUid = getOtherUid(uid);
				if (getPlayerStateByUid(otherUid) == PlayerConstant.CURRENT_STATE_SEEKING) {
					Log.e(TAG,"当前是缓冲中，然后变为暂停 mflag=" + uid);
					pausePlayerMyselfByUid(uid);
				}

				if (judgePlayerStateSame(uid)) {
					int currentPlayState = getPlayerStateByUid(uid);
					if (currentPlayState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF && judgePlayerProgressSame(uid)) { // 开始播放
						Log.e(TAG, "快进完成，执行播放" + uid);
						playAllPlayerByMyself();
					}
				}
			}
		}

		@Override public void onError(int flag, int error, int extra) {
			switch (error) {
				case -1004:
					Log.e(TAG, "MEDIA_ERROR_IO");
					mView.onError(flag);
					break;
				case -1007:
					Log.e(TAG, "MEDIA_ERROR_MALFORMED");
					mView.onError(flag);
					break;
				case 200:
					Log.e(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
					mView.onError(flag);
					break;
				case 100:
					Log.e(TAG, "MEDIA_ERROR_SERVER_DIED");
					mView.onError(flag);
					break;
				case -110:
					Log.e(TAG, "MEDIA_ERROR_TIMED_OUT");
					mView.onError(flag);
					break;
				case 1:
					Log.e(TAG, "MEDIA_ERROR_UNKNOWN");
					mView.onError(flag);
					break;
				case -1010:
					Log.e(TAG, "MEDIA_ERROR_UNSUPPORTED");
					mView.onError(flag);
					break;
			}
		}

		@Override public void resetProgressAndTime() {
			mView.resetProgressAndTime();
		}

		@Override public void setDuration(long duration) {
			// 判断所有状态都处于5，那么则可以开始播放
			if (statusSame() == 0) {
				mView.setDuration(duration);
			}
		}

		@Override public void setVideoMaxProgress(int maxProgress) {
			if (statusSame() == 0) {
				mView.setVideoMaxProgress(maxProgress);
			}
		}

		@Override public void setCurrentTime(String currentTime) {}

		@Override public void showLoading() {
			mView.showLoading();
		}

		@Override public void hideLoading() {
			if (statusSame() == 0) {
				mView.hideLoading();
			}
		}

		private int statusSame() {
			int count = 0;

			for (FusePlayerBean bean : mFusePlayerBeans) {
				if (bean.isVideo() && bean.getState() != PlayerConstant.CURRENT_STATE_PAUSE) {
					count++;
				}
			}
			return count;
		}

	}

}
