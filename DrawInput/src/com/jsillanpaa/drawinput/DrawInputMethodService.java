package com.jsillanpaa.drawinput;

import com.jsillanpaa.drawinput.R;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;

public class DrawInputMethodService extends InputMethodService {
	private static final String TAG = "DrawInputMethodService";
	private View mContainerView;

	@Override
	public void onCreate() {
		Log.i(TAG, TAG + ".onCreate()");
		super.onCreate();

	}

	@Override
	public View onCreateInputView() {
		Log.i(TAG, TAG + ".onCreateInputView()");

		mContainerView = (View) getLayoutInflater().inflate(
				R.layout.drawinput_gui, null);

		
		return mContainerView;

	}

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		Log.i(TAG, TAG + ".onStartInputView(), restarting = " + restarting);
		super.onStartInputView(info, restarting);
	}

}
