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
				// killMyself ������֮�������kill�����ǵ�Activity��������Դ���˷�;
//				android.os.Process.killProcess(android.os.Process.myPid());//��4.2�Ժ󣬻�����޷����ѵ�bug�����Ըĳ�finish();
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
		// ��ȡ�豸�������
		mPolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		// AdminReceiver �̳��� DeviceAdminReceiver
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
		if (isAdminActive()) {// ������˹���ԱȨ��
			lock3();// ֱ������
		}else{// ����Ȩ��
			activeManage();// ȥ��ȡȨ��
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
	
	//������ ���һ��û�������ɹ�������3�Ρ�
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
				mPolicyManager.lockNow();// ����
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
	
	/**���Ϊtrue�����ʾ��Ļ�������ˣ�������Ļ�������ˡ�*/
	private boolean isScreenOn(){
//		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);  
//	    mKeyguardManager.inKeyguardRestrictedInputMode(); 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		return pm.isScreenOn();
	}
	
	private void activeManage() {
		// �����豸����(��ʽIntent) - ��AndroidManifest.xml���趨��Ӧ������
		mIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		// Ȩ���б�
		mIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
		// ����(additional explanation)
		mIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "С���һ������");
		startActivityForResult(mIntent, 0);
		finishSelf();
	}
}