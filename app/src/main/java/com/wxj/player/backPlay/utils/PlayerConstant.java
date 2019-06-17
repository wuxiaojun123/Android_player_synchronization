package com.wxj.player.backPlay.utils;

/**
 * Created by wuxiaojun on 2019/3/14.
 */

public class PlayerConstant {

	public static int		VIDEO_IMAGE_DISPLAY_TYPE				= 0;

	public static final int	VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT	= 1;

	public static final int	VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP		= 2;

	public static final int	VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL		= 3;

	public static final int	CURRENT_STATE_NORMAL					= 0;	// 正常

	public static final int	CURRENT_STATE_PREPARING					= 1;	// 准备中

	// public static final int CURRENT_STATE_PREPARING_CHANGING_URL = 2;

	public static final int	CURRENT_STATE_PLAYING					= 3;	// 播放中

	public static final int	CURRENT_STATE_PAUSE						= 5;	// 暂停-用户暂停

	public static final int	CURRENT_STATE_PAUSE_MYSELF				= 50;	// 暂停-因为两个视频进度不一致自动暂停

	public static final int	CURRENT_STATE_AUTO_COMPLETE				= 6;	// 播放完成

	public static final int	CURRENT_STATE_ERROR						= 7;	// 错误

	public static final int	CURRENT_STATE_SEEKING					= 8;	// 快进快退加载中
}
