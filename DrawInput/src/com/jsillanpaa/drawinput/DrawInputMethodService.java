package com.jsillanpaa.drawinput;

import android.inputmethodservice.InputMethodService;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;

public class DrawInputMethodService extends InputMethodService {
	private static final String TAG = "DrawInputMethodService";
	private View mContainerView;
	private View mCanvas;
	private Button mSmallAbcButton;
	private Button mBigAbcButton;
	private Button mNumbersButton;
	private Button mSpecialCharsButton;
	
	@Override
	public void onCreate() {
		Log.i(TAG, TAG + ".onCreate()");
		super.onCreate();

	}

	@Override
	public View onCreateInputView() {
		Log.i(TAG, TAG + ".onCreateInputView()");

		mContainerView = getLayoutInflater().inflate(
				R.layout.drawinput_gui, null);
		
		init_references(mContainerView);
		
		return mContainerView;

	}

	private void init_references(View container) {
		mCanvas = container.findViewById(R.id.canvas);
		
		
		mSmallAbcButton = (Button) container.findViewById(R.id.button_small_abc);
		mBigAbcButton = (Button) container.findViewById(R.id.button_big_abc);
		mNumbersButton = (Button) container.findViewById(R.id.button_numbers);
		mSpecialCharsButton = (Button) container.findViewById(R.id.button_special_chars);
		
	}

	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		Log.i(TAG, TAG + ".onStartInputView(), restarting = " + restarting);
		super.onStartInputView(info, restarting);
	}

}
