package com.wxj.player.backPlay.base;

import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.wxj.player.R;
import com.wxj.player.backPlay.IPlayerState;
import com.wxj.player.backPlay.OnMediaPlayerStateListener;
import com.wxj.player.backPlay.utils.PlayerConstant;
import com.wxj.player.backPlay.utils.PlayerUtils;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by wuxiaojun on 2019/5/30.
 */

public abstract class BasePlayerController implements OnMediaPlayerStateListener {

	private final static String	TAG					= "BasePlayerController";

	protected int				mCurrentState;

	protected BasePlayerProxy	mPlayerProxy;									// 播放器代理类

	protected Context			mContext;

	protected IPlayerState		iPlayerState;

	protected String			mUrl;

	protected int				seekToManulPosition	= -1;						// 快进的位置

	protected Disposable		mDisposable;

	protected int				mUid;

	protected boolean			mPreparation;									// 准备中

	public BasePlayerController(String url, IPlayerState playerState, int uid, Context context) {
		this.mUrl = url;
		this.mContext = context;
		this.iPlayerState = playerState;
		this.mUid = uid;
		this.mPlayerProxy = initPlayerProxy();
	}

	public abstract BasePlayerProxy initPlayerProxy();

	public void clickStart() {
		Log.e(TAG, "onClick start [" + mUid + "] ");
		if (TextUtils.isEmpty(mUrl)) {
			Toast.makeText(mContext, "播放路径错误", Toast.LENGTH_SHORT).show();
			onError(-1004, 0);
			return;
		}
		if (mCurrentState == PlayerConstant.CURRENT_STATE_NORMAL) {
			if (!mUrl.startsWith("file") && !mUrl.startsWith("/") && !PlayerUtils.isWifiConnected(mContext)) {
				// 显示wifi弹窗
				showWifiDialog();
				return;
			}
			startVideo();

		} else if (mCurrentState == PlayerConstant.CURRENT_STATE_PREPARING) { // 准备中
			showLoading();
			mPreparation = false;
			playing();

		} else if (mCurrentState == PlayerConstant.CURRENT_STATE_PLAYING) {
			Log.e(TAG, "pauseVideo [" + mUid + "] ");
			pause();

		} else if (mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE || mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
			mPreparation = false;
			playing();

		} else if (mCurrentState == PlayerConstant.CURRENT_STATE_AUTO_COMPLETE) {
			playing();

		} else if (mCurrentState == PlayerConstant.CURRENT_STATE_SEEKING) {
			if (mPreparation) {
				mPreparation = false;
			}
			mCurrentState = PlayerConstant.CURRENT_STATE_PLAYING;
			playing();
		}
	}

	public void initVideoPlayer() {
		Log.e(TAG, "onClick start [" + mUid + "] ");
		if (TextUtils.isEmpty(mUrl)) {
			Toast.makeText(mContext, "播放路径为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (mCurrentState == PlayerConstant.CURRENT_STATE_NORMAL) {
			if (!mUrl.startsWith("file") && !mUrl.startsWith("/") && !PlayerUtils.isWifiConnected(mContext)) {
				// 显示wifi弹窗
				showWifiDialog();
				return;
			}
			mPreparation = true;
			startVideo();
		}
	}

	private void showWifiDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(mContext.getString(R.string.string_tips_not_wifi));
		builder.setPositiveButton(mContext.getResources().getString(R.string.string_tips_not_wifi_confirm), new DialogInterface.OnClickListener() {

			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startVideo();
			}
		});
		builder.setNegativeButton(mContext.getResources().getString(R.string.string_tips_not_wifi_cancel), new DialogInterface.OnClickListener() {

			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	protected void playing() {
		mPlayerProxy.start();
		onStatePlaying();
	}

	protected void pause() {
		mPlayerProxy.pause();
		onStatePause();
	}

	public void startVideo() {
		onCompletion();
		Log.e(TAG, "startVideo [" + mUid + "] ");
		onStatePreparing();
	}

	public void releaseAllVideos() {
		Log.e(TAG, "releaseAllVideos");
		onCompletion();
		mPlayerProxy.releaseMediaPlayer();
	}

	/***
	 * 人为控制-被动暂停
	 */
	public void pauseByPassive() {
		if (mPlayerProxy != null) {
			if (mCurrentState == PlayerConstant.CURRENT_STATE_AUTO_COMPLETE || mCurrentState == PlayerConstant.CURRENT_STATE_NORMAL || mCurrentState == PlayerConstant.CURRENT_STATE_ERROR) {
				return;
			}
			onStatePause();
			mPlayerProxy.pause();

		}
	}

	/***
	 * 播放器自己主动暂停，这是为了同步播放器
	 */
	public void pauseByMyselfActive() {
		if (mPlayerProxy != null) {
			if (mCurrentState == PlayerConstant.CURRENT_STATE_AUTO_COMPLETE || mCurrentState == PlayerConstant.CURRENT_STATE_NORMAL || mCurrentState == PlayerConstant.CURRENT_STATE_ERROR) {
				return;
			}
			onStatePauseByMyself();
			mPlayerProxy.pause();
		}
	}

	public void playingWhenPauseByMyself() {
		if (mPlayerProxy != null) {
			Log.e(TAG, "playingWhenPauseByMyself 当前状态是" + mCurrentState);
			if (mCurrentState == PlayerConstant.CURRENT_STATE_AUTO_COMPLETE || mCurrentState == PlayerConstant.CURRENT_STATE_NORMAL || mCurrentState == PlayerConstant.CURRENT_STATE_ERROR) {
				return;
			}
			mPlayerProxy.start();
			mCurrentState = PlayerConstant.CURRENT_STATE_PLAYING;
			startProgressTimer();
			updateStartImage();
			hideLoading();
		}
	}

	private void onStatePauseByMyself() {
		Log.e(TAG, "自我暂停 onStatePauseByMyself " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_PAUSE_MYSELF;
		startProgressTimer();
		updateStartImage();
		hideLoading();
	}

	private void onCompletion() {
		Log.e(TAG, "onCompletion");

		updateStartImage();
		// 取消计时器
		cancelProgressTimer();
	}

	private void onStateNormal() {
		Log.e(TAG, "onStateNormal " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_NORMAL;
		cancelProgressTimer();
		updateStartImage();
	}

	protected void onStatePreparing() {
		Log.e(TAG, "onStatePreparing " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_PREPARING;
		resetProgressAndTime(); // 重置进度条和时间
		updateStartImage();
	}

	private void onStatePlaying() {
		Log.e(TAG, "onStatePlaying " + " [" + mUid + "] ");
		if (mCurrentState == PlayerConstant.CURRENT_STATE_SEEKING) {
			onSeekComplete();
			return;
		}
		mCurrentState = PlayerConstant.CURRENT_STATE_PLAYING;
		startProgressTimer();
		updateStartImage();
		hideLoading();
	}

	private void onStatePause() {
		Log.e(TAG, "onStatePause " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_PAUSE;
		startProgressTimer();
		updateStartImage();
		hideLoading();
	}

	public void onStateError() {
		Log.e(TAG, "onStateError " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_ERROR;
		cancelProgressTimer();
		updateStartImage();
		hideLoading();
	}

	private void onStateAutoComplete() {
		Log.e(TAG, "onStateAutoComplete " + " [" + mUid + "] ");
		mCurrentState = PlayerConstant.CURRENT_STATE_AUTO_COMPLETE;
		cancelProgressTimer();
		iPlayerState.resetProgressAndTime();
		updateStartImage();
		hideLoading();

		pause();
	}

	protected void updateStartImage() {
		iPlayerState.getCurrentState(mUid, mCurrentState);
	}

	protected void startProgressTimer() {
		Log.e(TAG, "startProgressTimer: " + " [" + mUid + "] ");
		cancelProgressTimer();
		interval();
	}

	protected void cancelProgressTimer() {
		if (mDisposable != null && !mDisposable.isDisposed()) {
			mDisposable.dispose();
			mDisposable = null;
		}
	}

	private void resetProgressAndTime() {
		iPlayerState.resetProgressAndTime();
	}

	protected void showLoading() {
		iPlayerState.showLoading();
	}

	protected void hideLoading() {
		iPlayerState.hideLoading();
	}

	private long getCurrentPositionWhenPlaying() {
		long position = 0;
		// TODO 这块的判断应该根据MediaPlayer来
		if (mCurrentState == PlayerConstant.CURRENT_STATE_PLAYING || mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE || mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF
				|| mCurrentState == PlayerConstant.CURRENT_STATE_SEEKING) {
			try {
				position = mPlayerProxy.getCurrentPosition();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return position;
			}
		}
		return position;
	}

	public long getDuration() {
		long duration = 0;
		try {
			duration = mPlayerProxy.getDuration();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return duration;
		}
		return duration;
	}

	private void onProgress(long position, long duration) {
		Log.e(TAG, "seekToManulPosition=" + seekToManulPosition + " position=" + position + " duration=" + duration + "mUid=" + mUid);
		// 如果是触摸了seekbar的时候，应该如何处理
		if (Math.abs(seekToManulPosition - position) < 200) {
			seekToManulPosition = -1;
		}

		if (seekToManulPosition == -1) {
			iPlayerState.setVideoProgress(mUid, (int) position, PlayerUtils.stringForTime(position));
		}

		if (Math.abs(position - duration) < 200) {
			seekTo(300);
		}

		if (position != 0) {
			iPlayerState.setCurrentTime(PlayerUtils.stringForTime(position));
		}
	}

	private void setDuration(long duration) {
		iPlayerState.setDuration(duration);
	}

	private void setMaxProgress(long duration) {
		iPlayerState.setVideoMaxProgress((int) duration);
	}

	public void playingWhenLoadingComplete() {
		Log.e(TAG, "playingWhenLoadingComplete " + mCurrentState);
		if (mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
			mPlayerProxy.start();
			onStatePlaying();
		}
	}

	public void seekTo(long progress) {
		long tempProgress = Math.abs(progress - getCurrentPositionWhenPlaying());
		Log.e(TAG, "seekTo方法 目标进度" + progress + "当前进度是" + getCurrentPositionWhenPlaying() + "进度差是" + tempProgress + "当前状态是" + mCurrentState + "mUid=" + mUid);
		if (tempProgress < 1000) {
			seekToManulPosition = -1;
			return;
		}
		startProgressTimer();
		if (mCurrentState != PlayerConstant.CURRENT_STATE_PLAYING && mCurrentState != PlayerConstant.CURRENT_STATE_PAUSE && mCurrentState != PlayerConstant.CURRENT_STATE_PAUSE_MYSELF
				&& mCurrentState != PlayerConstant.CURRENT_STATE_SEEKING) {
			return;
		}
		mCurrentState = PlayerConstant.CURRENT_STATE_SEEKING;
		seekToManulPosition = (int) progress;
		mPlayerProxy.seekTo(progress);
	}

	public void onStartTrackingTouch() {
		Log.e(TAG, "bottomProgress onStartTrackingTouch [" + mUid + "] ");
		cancelProgressTimer();
	}

	public void onProgressChanged(int progress, boolean fromUser) {
		if (fromUser) {
			// 设置这个progres对应的时间，给textview
			String currentTime = PlayerUtils.stringForTime(progress);
			iPlayerState.setCurrentTime(currentTime);
		}
	}

	protected void onSeekComplete() { // 快进完成
		iPlayerState.onSeekComplete(mUid);
		Log.e(TAG, "快进完成 当前进度是" + getCurrentPositionWhenPlaying() + "目标进度是" + seekToManulPosition + "当前状态是" + mCurrentState + "flag=" + mUid);
		seekToManulPosition = -1;
	}

	public void onError(int what, int extra) {
		iPlayerState.onError(mUid, what, extra);
		Log.e("BasePlayerController", "播放出现错误 what" + what + "extra=" + extra + "mUid=" + mUid);
	}

	private void interval() {
		Flowable flowable = Flowable.interval(1, TimeUnit.SECONDS, Schedulers.io());
		flowable = flowable.observeOn(AndroidSchedulers.mainThread());
		mDisposable = flowable.subscribe(new Consumer<Long>() {

			@Override public void accept(Long o) throws Exception {
				if (mCurrentState == PlayerConstant.CURRENT_STATE_PLAYING || mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE || mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF
						|| mCurrentState == PlayerConstant.CURRENT_STATE_SEEKING) {

					long position = getCurrentPositionWhenPlaying();
					long duration = getDuration();
					onProgress(position, duration);
				}
			}
		});
	}

	protected int getCurrentState() {
		return mCurrentState;
	}

	@Override public void onMediaReadyPlaying() { // 准备好了开始播放
		Log.e(TAG, "onMediaReadyPlaying " + " [" + mUid + "] mPreparation=" + mPreparation);

		if (!mPreparation) {
			onStatePlaying();
		} else {
			pause();

			long duration = getDuration();
			setDuration(duration);
			setMaxProgress(duration);
		}
	}

	@Override public void onMediaAutoCompletion() { // 自动播放完成
		// Runtime.getRuntime().gc();
		Log.e(TAG, "onMediaAutoCompletion " + " [" + mUid + "] ");
		onStateAutoComplete();
	}

	@Override public void onMediaLoadingComplete() { // 加载中
		Log.e(TAG, "onMediaLoadingComplete " + mCurrentState);
		if (mCurrentState == PlayerConstant.CURRENT_STATE_PAUSE_MYSELF) {
			mPlayerProxy.start();
			onStatePlaying();
		}
	}

	public void setVolume(float volume) {
		mPlayerProxy.setVolume(volume);
	}

	public float getVolume() {
		return mPlayerProxy.getVolume();
	}

	public void setPreparation(boolean preparation) {
		this.mPreparation = preparation;
	}

}
