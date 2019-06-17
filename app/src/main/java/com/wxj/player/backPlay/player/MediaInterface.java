package com.wxj.player.backPlay.player;

import android.view.Surface;

/**
 * Created by wuxiaojun on 2019/3/26.
 */

public abstract class MediaInterface {

	public abstract void start();

	public abstract void prepare();

	public abstract void pause();

	public abstract boolean isPlaying();

	public abstract void seekTo(long time);

	public abstract void release();

	public abstract long getCurrentPosition();

	public abstract long getDuration();

	public abstract void setSurface(Surface surface);

	public abstract void setVolume(float leftVolume, float rightVolume);

	public abstract void setSpeed(float speed);

	public abstract int getBufferProgress();

	public abstract float getVolume();

}
