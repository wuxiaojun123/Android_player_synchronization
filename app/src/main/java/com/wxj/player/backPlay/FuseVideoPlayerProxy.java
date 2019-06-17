package com.wxj.player.backPlay;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import com.wxj.player.backPlay.base.BasePlayerProxy;
import com.wxj.player.backPlay.view.MTextureView;


/**
 * Created by wuxiaojun on 2019/3/26.
 */

public class FuseVideoPlayerProxy extends BasePlayerProxy implements TextureView.SurfaceTextureListener {

	private static final String	TAG	= "PlayerProxy";

	private Surface				mSurface;

	private SurfaceTexture		mSurfaceTexture;

	private MTextureView		mTextureView;

	public FuseVideoPlayerProxy(FuseVideoPlayerController playerController, Context context, String url) {
		super(playerController, context, url);
	}

	@Override public void prepare() {
		super.prepare();
		if (mSurfaceTexture != null) {
			if (mSurface != null) {
				mSurface.release();
			}
			mSurface = new Surface(mSurfaceTexture);
			mPlayer.setSurface(mSurface);
		}
	}

	@Override public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
		if (mSurfaceTexture == null) {
			mSurfaceTexture = surface;
			prepare();
		} else {
			mTextureView.setSurfaceTexture(mSurfaceTexture);
		}
	}

	@Override public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

	}

	@Override public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return mSurfaceTexture == null;
	}

	@Override public void onSurfaceTextureUpdated(SurfaceTexture surface) {

	}

	public TextureView getTextureView() {
		return mTextureView;
	}

	public Surface getSurface() {
		return mSurface;
	}

	public SurfaceTexture getSurfaceTexture() {
		return mSurfaceTexture;
	}

	public void setSurfaceTexture(SurfaceTexture mSurfaceTexture) {
		this.mSurfaceTexture = mSurfaceTexture;
	}

	public void setTextureView(MTextureView mTextureView) {
		this.mTextureView = mTextureView;
	}
}
