package com.wxj.player.lifeCycler;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;


/**
 * 应用的生命周期 Created by wuxiaojun on 2018/11/21.
 */

public class AppRunningStatusCallbacks implements Application.ActivityLifecycleCallbacks {

	private int									appCount	= 0;

	private boolean								isForground	= true;

	private static AppRunningStatusCallbacks	mInstance;

	public static void registerActivityLifecycleCallbacks(Application application) {
		if (mInstance == null) {
			init(application);
		}
	}

	public static AppRunningStatusCallbacks getInstance(Application application) {
		if (mInstance == null) {
			init(application);
		}
		return mInstance;
	}

	private static void init(Application application) {
		mInstance = new AppRunningStatusCallbacks();
		application.registerActivityLifecycleCallbacks(mInstance);
	}

	@Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	@Override public void onActivityStarted(Activity activity) {
		appCount++;
		if (!isForground) {
			isForground = true;
			callback(true);
		}
	}

	@Override public void onActivityResumed(Activity activity) {

	}

	@Override public void onActivityPaused(Activity activity) {

	}

	@Override public void onActivityStopped(Activity activity) {
		appCount--;
		if (!isForgroundAppValue()) {
			isForground = false;
			callback(false);
		}
	}

	private boolean isForgroundAppValue() {
		return appCount > 0;
	}

	private void callback(boolean isForegound) {
		if (onSwitchForegoundAndBackgroundListener != null) {
			onSwitchForegoundAndBackgroundListener.onSwitchForegoundAndBackgroundListener(isForegound);
		}
	}

	@Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override public void onActivityDestroyed(Activity activity) {

	}

	private OnSwitchForegoundAndBackgroundListener onSwitchForegoundAndBackgroundListener;

	public void setOnSwitchForegoundAndBackgroundListener(OnSwitchForegoundAndBackgroundListener listener) {
		this.onSwitchForegoundAndBackgroundListener = listener;
	}

	public interface OnSwitchForegoundAndBackgroundListener {

		void onSwitchForegoundAndBackgroundListener(boolean isForegound);
	}

}
