package com.pb.app.lockscreen;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

public class LockScreenActivity extends Activity {
	
	private DevicePolicyManager mPolicyManager;
	private ComponentName mComponentName;
	private int mLockCount = 0;
	private Intent mIntent;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				// killMyself ，锁屏之后就立即kill掉我们的Activity，避免资源的浪费;
//				android.os.Process.killProcess(android.os.Process.myPid());//在4.2以后，会出现无法唤醒的bug。所以改成finish();
				finish();
				break;
			}
		}
		
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLockCount = 0;
		// 获取设备管理服务
		mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		// AdminReceiver 继承自 DeviceAdminReceiver
		mComponentName = new ComponentName(LockScreenActivity.this, AdminReceiver.class);
		
		try {
			startLock();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	protected void onDestroy() {
		mPolicyManager = null;
		mComponentName = null;
		mIntent = null;
		System.gc();
		super.onDestroy();
	}

	private void startLock() {
		if (isAdminActive()) {// 若获得了管理员权限
			lock3();// 直接锁屏
		}else{// 若无权限
			activeManage();// 去获取权限
		}
	}
	
	/**
	 * Return true if the given administrator component is currently 
	   active (enabled) in the system.
	 * */
	private boolean isAdminActive(){
		try {
			if(mPolicyManager == null)
				mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
			if(mComponentName == null)
				mComponentName = new ComponentName(LockScreenActivity.this, AdminReceiver.class);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if(mPolicyManager == null || mComponentName == null)
			return false;
		
		return mPolicyManager.isAdminActive(mComponentName);
	}
	
	//锁屏。 如果一次没有锁屏成功，尝试3次。
	private void lock3(){
		if (!isAdminActive()){
			return;
		}
		while(mLockCount < 3){
			if(!isScreenOn()){
				finishSelf();
				break;
			}
			
			try {
				mPolicyManager.lockNow();// 锁屏
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(300L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mLockCount++; 
		}
	}
	
	private void finishSelf(){
		mHandler.sendEmptyMessage(0);
	}
	
	/**如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。*/
	private boolean isScreenOn(){
//		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);  
//	    mKeyguardManager.inKeyguardRestrictedInputMode(); 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	private void activeManage() {
		// 启动设备管理(隐式Intent) - 在AndroidManifest.xml中设定相应过滤器
		mIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		// 权限列表
		mIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
		// 描述(additional explanation)
		mIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "小马哥一键锁屏");
		startActivityForResult(mIntent, 0);
		finishSelf();
	}
}