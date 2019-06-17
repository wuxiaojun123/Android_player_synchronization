package com.wxj.player.backPlay.bean;

/**
 * Created by wuxiaojun on 2019/6/17.
 */

public class UserBean {

	private String	videoUrl;

	private int		uid;

	private String	audioUrl;

	public UserBean(String videoUrl, int uid, String audioUrl) {
		this.videoUrl = videoUrl;
		this.uid = uid;
		this.audioUrl = audioUrl;
	}

	public String getVideoUrl() {
		return videoUrl == null ? "" : videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getAudioUrl() {
		return audioUrl == null ? "" : audioUrl;
	}

	public void setAudioUrl(String audioUrl) {
		this.audioUrl = audioUrl;
	}
}
