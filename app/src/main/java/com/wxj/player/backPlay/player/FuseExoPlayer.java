package com.wxj.player.backPlay.player;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;
import com.wxj.player.R;
import com.wxj.player.backPlay.OnMediaPlayerStateListener;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;


/**
 * exoplayer播放器 Created by wuxiaojun on 2019/3/26.
 */

public class FuseExoPlayer extends MediaInterface implements Player.EventListener, VideoListener {

	private static final String			TAG				= "ExoPlayer";

	private Context						mContext;

	private String						mUrl;

	private SimpleExoPlayer mSimpleExoPlayer;

	private Handler						mHandler;

	private int							mBufferProgress;				// 缓冲百分比

	private OnBufferingUpdate			mOnBufferingUpdate;

	private long						mPreviousSeek	= -1;

	private OnMediaPlayerStateListener onMediaPlayerStateListener;

	public FuseExoPlayer(Context context, String url, OnMediaPlayerStateListener onMediaPlayerStateListener) {
		this.mContext = context;
		this.mUrl = url;
		this.onMediaPlayerStateListener = onMediaPlayerStateListener;
	}

	@Override public void prepare() {
		mHandler = new Handler();

		BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
		TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
		TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

		LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE), 360000, 600000, 1000, 5000, C.LENGTH_UNSET, false);

		// 2. Create the player

		RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
		mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, renderersFactory, trackSelector, loadControl);
		// Produces PlayerDataSource instances through which media data is
		// loaded.
		DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, mContext.getResources().getString(R.string.app_name)));

		MediaSource mMediaSource;
		if (mUrl.contains(".m3u8")) {
			mMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mUrl), mHandler, null);
		} else {
			mMediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mUrl));
		}
		mSimpleExoPlayer.addVideoListener(this);
		Log.e(TAG, "URL Link = " + mUrl);

		mSimpleExoPlayer.addListener(this);

		mSimpleExoPlayer.prepare(mMediaSource);
		mSimpleExoPlayer.setPlayWhenReady(true);

		mOnBufferingUpdate = new OnBufferingUpdate();
	}

	private class OnBufferingUpdate implements Runnable {

		@Override public void run() {
			int percent = mSimpleExoPlayer.getBufferedPercentage();
			mBufferProgress = percent;
			// Log.e(TAG, "OnBufferingUpdate:" + percent + "hashcode=" +
			// hashCode());
			if (percent < 100) {
				mHandler.postDelayed(mOnBufferingUpdate, 300);
			} else {
				mHandler.removeCallbacks(mOnBufferingUpdate);
			}
		}

	}

	@Override public void start() {
		Log.e(TAG, "start");
		mSimpleExoPlayer.setPlayWhenReady(true);
	}

	@Override public void pause() {
		mSimpleExoPlayer.setPlayWhenReady(false);
	}

	@Override public boolean isPlaying() {
		return mSimpleExoPlayer.getPlayWhenReady();
	}

	@Override public void seekTo(long time) {
		if (time != mPreviousSeek) {
			mSimpleExoPlayer.seekTo(time);
			mPreviousSeek = time;
		}
	}

	@Override public void release() {
		if (mSimpleExoPlayer != null) {
			mSimpleExoPlayer.release();
		}
		if (mHandler != null) {
			mHandler.removeCallbacks(mOnBufferingUpdate);
		}
	}

	@Override public long getCurrentPosition() {
		if (mSimpleExoPlayer != null) {
			return mSimpleExoPlayer.getCurrentPosition();
		}
		return 0;
	}

	@Override public long getDuration() {
		if (mSimpleExoPlayer != null) {
			return mSimpleExoPlayer.getDuration();
		}
		return 0;
	}

	@Override public void setSurface(Surface surface) {
		mSimpleExoPlayer.setVideoSurface(surface);
	}

	@Override public void setVolume(float leftVolume, float rightVolume) {
		if (mSimpleExoPlayer != null) {
			mSimpleExoPlayer.setVolume(leftVolume);
			mSimpleExoPlayer.setVolume(rightVolume);
		}
	}

	@Override public float getVolume() {
		return mSimpleExoPlayer.getVolume();
	}

	@Override public void setSpeed(float speed) {
		PlaybackParameters playbackParameters = new PlaybackParameters(speed, 1.0f);
		mSimpleExoPlayer.setPlaybackParameters(playbackParameters);
	}

	@Override public int getBufferProgress() {
		return mBufferProgress;
	}

	@Override public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
		Log.e(TAG, "onVideoSizeChanged width=" + width + "height=" + height);
	}

	@Override public void onLoadingChanged(boolean isLoading) {
		Log.e(TAG, "onLoadingChanged=" + isLoading + "hashcode=" + hashCode());
		if (!isLoading) {
			onMediaPlayerStateListener.onMediaLoadingComplete();
		}
	}

	@Override public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) { // playwhenready:是否准备好了
		// playbackstate:播放状态
		Log.e(TAG, "onPlayerStateChanged playWhenReady=" + playWhenReady + "playbackState=" + playbackState + "hashcode=" + hashCode());
		mHandler.post(new Runnable() {

			@Override public void run() {
				switch (playbackState) {
					case Player.STATE_IDLE:// 播放错误吗
						break;
					case Player.STATE_BUFFERING:
						// 开始缓冲 playWhenReady=trueplaybackState=2
						mHandler.post(mOnBufferingUpdate);

						break;
					case Player.STATE_READY:
						if (playWhenReady) { // 准备好了，先暂停
							Log.e(TAG, "onPlayerStateChanged 准备好了");
							onMediaPlayerStateListener.onMediaReadyPlaying();
						}

						break;
					case Player.STATE_ENDED:
						// 播放完成
						onMediaPlayerStateListener.onMediaAutoCompletion();

						break;
				}
			}
		});
	}

}
