package com.wxj.player.backPlay;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wxj.player.backPlay.base.BasePlayerController;
import com.wxj.player.backPlay.base.BasePlayerProxy;
import com.wxj.player.backPlay.utils.PlayerConstant;
import com.wxj.player.backPlay.view.MTextureView;


/**
 * 播放器控制类 Created by wuxiaojun on 2019/3/26.
 */

public class FuseVideoPlayerController extends BasePlayerController {

	private final static String	TAG	= "FuseVideoPlayerControll";

	private ViewGroup			mContainer;

	private FuseVideoPlayerProxy mProxy;

	public FuseVideoPlayerController(ViewGroup viewGroup, String url, IPlayerState playerState, int flag, Context context) {
		super(url, playerState, flag, context);
		this.mContainer = viewGroup;
		mProxy = (FuseVideoPlayerProxy) initPlayerProxy();
		this.mPlayerProxy = mProxy;
		// mProxy = (FuseVideoPlayerProxy) mPlayerProxy;
	}

	@Override public BasePlayerProxy initPlayerProxy() {
		return new FuseVideoPlayerProxy(this, mContext, mUrl);
	}

	private void initTextureView() {
		removeTextureView();
		mProxy.setTextureView(new MTextureView(mContext));
		mProxy.getTextureView().setSurfaceTextureListener(mProxy);
	}

	private void addTextureView() {
		Log.e(TAG, "addTextureView [" + mUid + "] ");
		if (mContainer == null) {
			return;
		}
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		mContainer.addView(mProxy.getTextureView(), layoutParams);
	}

	private void removeTextureView() {
		mProxy.setSurfaceTexture(null);
		if (mProxy.getTextureView() != null && mProxy.getTextureView().getParent() != null) {
			((ViewGroup) mProxy.getTextureView().getParent()).removeView(mProxy.getTextureView());
		}
	}

	@Override public void startVideo() {
		onCompletion();
		Log.e(TAG, "startVideo [" + mUid + "] ");
		initTextureView();
		addTextureView();
		onStatePreparing();
	}

	public void playingWhenPauseByMyself() {
		if (mPlayerProxy != null) {
			Log.e(TAG, "playingWhenPauseByMyself 当前状态是" + mCurrentState+"当前uid="+mUid);
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

	private void onCompletion() {
		Log.e(TAG, "onCompletion");

		updateStartImage();
		// 取消计时器
		cancelProgressTimer();
		if (mContainer == null) {
			return;
		}
		mContainer.removeView(mProxy.getTextureView());
		if (mProxy.getSurface() != null) {
			mProxy.getSurface().release();
		}
		if (mProxy.getSurfaceTexture() != null) {
			mProxy.getSurfaceTexture().release();
		}
		mProxy.setTextureView(null);
		mProxy.setSurfaceTexture(null);
	}

}
