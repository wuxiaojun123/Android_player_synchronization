package com.wxj.player;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wxj.player.backPlay.FuseMutilPlayerManager;
import com.wxj.player.backPlay.bean.UserBean;
import com.wxj.player.backPlay.utils.PlayerConstant;
import com.wxj.player.backPlay.utils.PlayerUtils;
import com.wxj.player.backPlay.view.IPlayerView;
import com.wxj.player.lifeCycler.AppRunningStatusCallbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/***
 * 首页
 */
public class MainActivity extends AppCompatActivity implements IPlayerView, SeekBar.OnSeekBarChangeListener {

	public static final String							TAG		= "MainActivity";

	private static final String							URL		= "http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4";

	private static final String							URL2	= "http://jzvd.nathen.cn/342a5f7ef6124a4a8faf00e738b8bee4/cf6d9db0bd4d41f59d09ea0a81e918fd-5287d2089db37e62345123a1be272f8b.mp4";

	@BindView(R.id.jz_video) FrameLayout				jzVideo;

	@BindView(R.id.jz_video2) FrameLayout				jzVideo2;

	@BindView(R.id.id_iv_play) ImageView				idIvPlay;

	@BindView(R.id.id_tv_play_time) TextView			idTvPlayTime;

	@BindView(R.id.id_tv_total_time) TextView			idTvTotalTime;

	@BindView(R.id.id_seekbar) SeekBar					idSeekBar;

	@BindView(R.id.id_rv1) RecyclerView					id_rv1;

	@BindView(R.id.id_rv2) RecyclerView					id_rv2;

	@BindView(R.id.loading) ImageView					idProgressBar;

	@BindView(R.id.id_rl_control_player) RelativeLayout	idRlControlPlayer;																															// 播放器的控制布局

	@BindView(R.id.id_ll_content) LinearLayout			id_ll_content;

	private FuseMutilPlayerManager						mMutilPlayerManager;

	private boolean										isBackground;																																// true:处于后台

	private long										mVideoDuration;																																// 视频总时长

	AnimationDrawable									mLoadingAnimation;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		initLoadingAnim();
		startLoading();
		initActivityLifeCyclerEvent();
		idSeekBar.setOnSeekBarChangeListener(this);
		id_ll_content.setOnClickListener(new View.OnClickListener() {

			@Override public void onClick(View v) {
				showControlView();
				startDismissControlViewTimer();
			}
		});

		List<ViewGroup> viewGroupList = new ArrayList<>();
		viewGroupList.add(jzVideo);
		viewGroupList.add(jzVideo2);

		List<UserBean> userList = new ArrayList<>();
		UserBean userBean1 = new UserBean(URL, 666, "");
		userList.add(userBean1);

		UserBean userBean2 = new UserBean(URL2, 777, "");
		userList.add(userBean2);

		// 拿到mPlayBackFacade
		mMutilPlayerManager = new FuseMutilPlayerManager(viewGroupList, this, this, userList, 666);
		mMutilPlayerManager.initVideoPlayer();
	}

	private void initLoadingAnim() {
		mLoadingAnimation = (AnimationDrawable) getResources().getDrawable(R.drawable.fuse_back_player_loading);
		idProgressBar.setBackground(mLoadingAnimation);
		// startLoading();
	}

	private void initActivityLifeCyclerEvent() { // 处于后台或熄屏然后再回来的监听
		AppRunningStatusCallbacks.getInstance(getApplication()).setOnSwitchForegoundAndBackgroundListener(new AppRunningStatusCallbacks.OnSwitchForegoundAndBackgroundListener() {

			@Override public void onSwitchForegoundAndBackgroundListener(boolean isForegound) { // isForegound:false
				// 用户按下home键
				Log.e(TAG, "isForegound=" + isForegound);

				if (isForegound) {
					continuePlayingWhenIsForegound();
				} else {
					pauseWhenIsBackground();
				}
			}
		});
	}

	private void continuePlayingWhenIsForegound() {
		if (mMutilPlayerManager != null && isBackground) {
			mMutilPlayerManager.clickStart();
			isBackground = false;
		}
	}

	private void pauseWhenIsBackground() {
		if (mMutilPlayerManager != null) {
			boolean isPlaying = mMutilPlayerManager.isPlaying();
			int progress = mMutilPlayerManager.getProgress();
			if (isPlaying) {
				isBackground = true;
				mMutilPlayerManager.clickStart();
			}
		}
	}

	@OnClick({ R.id.id_iv_play }) public void click(View view) {
		int id = view.getId();
		switch (id) {
			case R.id.id_iv_play:

				String playTime = idTvPlayTime.getText().toString();
				String totalTime = idTvTotalTime.getText().toString();
				if (mMutilPlayerManager != null && !TextUtils.isEmpty(totalTime)) {
					if (totalTime.replace(":", "").equals(playTime.replace(":", ""))) { // 容错处理，当播放时间等于总时长，这时候点击播放应该重置
						mMutilPlayerManager.onStopTrackingTouch(300);
					}

					mMutilPlayerManager.clickStart();
					boolean onPause = mMutilPlayerManager.isPause();
					int progress = mMutilPlayerManager.getProgress();

					showControlView();
					startDismissControlViewTimer();
				}

				break;
		}
	}

	@Override public void updateStartImage(int currentState) {
		if (currentState == PlayerConstant.CURRENT_STATE_PLAYING) {
			idIvPlay.setVisibility(View.VISIBLE);
			idIvPlay.setImageResource(R.mipmap.img_playback_pause);

		} else if (currentState == PlayerConstant.CURRENT_STATE_ERROR) {
			idIvPlay.setVisibility(View.INVISIBLE);

		} else if (currentState == PlayerConstant.CURRENT_STATE_AUTO_COMPLETE || currentState == PlayerConstant.CURRENT_STATE_PAUSE) {
			idIvPlay.setVisibility(View.VISIBLE);
			idIvPlay.setImageResource(R.mipmap.img_playback_play);

		}
	}

	@Override public void updateRecyclerView1(int status, int progress) {

	}

	@Override public void updateRecyclerView2(int status, int progress) {

	}

	@Override public void setDuration(long duration) {
		mVideoDuration = duration;
		idTvTotalTime.setText(PlayerUtils.stringForTime(duration));
	}

	@Override public void onPause(int progress) {}

	@Override public void setCurrentTime(String time) {
		idTvPlayTime.setText(time);
	}

	@Override public void setVideoProgress(int progress) {
		idSeekBar.setProgress(progress);
	}

	@Override public void setVideoMaxProgress(int maxProgress) {
		idSeekBar.setMax(maxProgress);
	}

	@Override public void resetProgressAndTime() {
		idTvPlayTime.setText(R.string.string_default_time);
		idSeekBar.setProgress(0);
	}

	@Override public void showLoading() {
		startLoading();
	}

	@Override public void hideLoading() {
		stopLoading();
	}

	private void startLoading() {
		if (mLoadingAnimation != null && !mLoadingAnimation.isRunning()) {
			idProgressBar.setVisibility(View.VISIBLE);
			mLoadingAnimation.start();
		}
	}

	private void stopLoading() {
		if (mLoadingAnimation != null && mLoadingAnimation.isRunning()) {
			mLoadingAnimation.stop();
			idProgressBar.setVisibility(View.GONE);
		}
	}

	@Override public void showControlView() {
		idRlControlPlayer.setVisibility(View.VISIBLE);
	}

	@Override public void dissmissControlView() { // 控制器隐藏
		idRlControlPlayer.setVisibility(View.INVISIBLE);
	}

	@Override public void onError(int flag) {
		// 播放失败
	}

	private Disposable mTimerDisposable; // 计时器

	private void startDismissControlViewTimer() {
		cancelDismissControlViewTimer();
		Flowable mFlowable = Flowable.timer(5, TimeUnit.SECONDS, Schedulers.io());
		mFlowable = mFlowable.observeOn(AndroidSchedulers.mainThread());
		mTimerDisposable = mFlowable.subscribe(new Consumer() {

			@Override public void accept(Object o) throws Exception {
				dissmissControlView();
			}
		});

	}

	private void cancelDismissControlViewTimer() {
		if (mTimerDisposable != null && !mTimerDisposable.isDisposed()) {
			mTimerDisposable.dispose();
			mTimerDisposable = null;
		}
	}

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (mMutilPlayerManager == null) {
			return;
		}
		mMutilPlayerManager.onProgressChanged(progress, fromUser);
	}

	@Override public void onStartTrackingTouch(SeekBar seekBar) {
		if (mMutilPlayerManager == null) {
			return;
		}
		mMutilPlayerManager.onStartTrackingTouch();
		cancelDismissControlViewTimer();
	}

	@Override public void onStopTrackingTouch(SeekBar seekBar) {
		if (mMutilPlayerManager == null) {
			return;
		}
		int progress = seekBar.getProgress();
		if (mMutilPlayerManager != null) {
			mMutilPlayerManager.onStopTrackingTouch(progress);
		}
		startDismissControlViewTimer();
	}

}
