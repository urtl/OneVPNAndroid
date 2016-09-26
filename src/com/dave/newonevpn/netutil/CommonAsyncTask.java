package com.dave.newonevpn.netutil;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;



import java.lang.ref.WeakReference;


/**
 * common async task for entire application.
 * this will be used for all UI blocking operations 
**/
public class CommonAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private asyncTaskListener mCallback = null;
	private ProgressHUD mProgressHud = null;
	private WeakReference<Activity> mActivityWeakRef;
	private Activity mActivity = null;
	private Fragment mFragment = null;
	private boolean showProgress = false;

	/** Interface for callback methods triggered on Async Task methods **/
	public interface asyncTaskListener {
		public Boolean onTaskExecuting();
		public void onTaskFinish(Boolean result);
	}
	
	/**
	 * Constructor for using Async Task in an Activity  
	**/
	public CommonAsyncTask(Activity activity, boolean showProgress, asyncTaskListener listener) {

		super();
		this.mActivity = activity;
		this.showProgress = showProgress;
		this.mActivityWeakRef = new WeakReference<Activity>(activity);
		mCallback = listener;


	}
	public CommonAsyncTask(Activity activity, boolean showProgress) {
	
		super();
		this.mActivity = activity;
		this.showProgress = showProgress;
		this.mActivityWeakRef = new WeakReference<Activity>(activity);
		mCallback = (asyncTaskListener) mActivity;


	}
	
	/**
	 * Constructor for using Async Task in a fragment
	**/
	public CommonAsyncTask(Fragment fragment, Context context, boolean showProgress) {
		
		super();
		this.mFragment = fragment;
		this.showProgress = showProgress;
		this.mActivityWeakRef = new WeakReference<Activity>((Activity) context);
		mCallback = (asyncTaskListener) mFragment;
		//mPrgDialog = new ProgressDialog(context);

	}

	@Override
	protected void onPreExecute() {
		if(showProgress )
		{
			mProgressHud = ProgressHUD.show(mActivity, "Loading...", true, false, null);
		}
	};
	
	@Override
	protected Boolean doInBackground(Void... params) {
		return mCallback.onTaskExecuting();
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if(mProgressHud != null){
			if(mProgressHud.isShowing()){
				mProgressHud.dismiss();
			}
		}
		if(mActivityWeakRef != null && !mActivityWeakRef.get().isFinishing()){
			mCallback.onTaskFinish(result);
		}
	};
	
	/** in case an error occured **/
	public void onError(){
		cancel(true);
	}
	
}
