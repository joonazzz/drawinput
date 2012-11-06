package com.jsillanpaa.drawinput.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

import com.jsillanpaa.drawinput.R;

public class ActionButton extends Button {

	private static final String TAG = "ActionButton";
	
	public static final String UNSPECIFIED = "unspecified";
	public static final String NONE = "none";
	public static final String GO = "go";
	public static final String SEARCH = "search";
	public static final String SEND = "send";
	public static final String NEXT = "next";
	public static final String DONE = "done";

	private static final int LAYER0 = 0;
	private static final int LAYER1 = 1;
	
	private String mActionId = ActionButton.UNSPECIFIED;
	
	private Drawable[] mLayers = new Drawable[2];
	private LayerDrawable mBackgroundDrawable; 
	
	private Drawable mButtonPressedDrawable = null;
	private Drawable mButtonDefaultDrawable = null;
	private Drawable mButtonDisabledDrawable = null;
	
	private Drawable mUnspecifiedDrawable = null;
	private Drawable mNoneDrawable = null;
	private Drawable mGoDrawable = null;
	private Drawable mSearchDrawable = null;
	private Drawable mSendDrawable = null;
	private Drawable mDoneDrawable = null;
	private Drawable mNextDrawable = null;
	
	public ActionButton(Context context) {
		super(context);
		init();
	}

	public ActionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public ActionButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private Drawable getButtonDefaultDrawable() {
		if(mButtonDefaultDrawable == null){
			mButtonDefaultDrawable = getResources().getDrawable(R.drawable.button_normal);
		}
		return mButtonDefaultDrawable;
	}

	private Drawable getButtonPressedDrawable() {
		if(mButtonPressedDrawable == null){
			mButtonPressedDrawable = getResources().getDrawable(R.drawable.button_pressed);
		}
		return mButtonPressedDrawable;
	}

	private Drawable getButtonDisabledDrawable() {
		if(mButtonDisabledDrawable == null){
			mButtonDisabledDrawable = getResources().getDrawable(R.drawable.button_disabled);
		}
		return mButtonDisabledDrawable;
	}

	
	public String getActionId() {
		return mActionId;
	}

	public void setActionId(String actionId) {
		if(!mActionId.equals(actionId)){
			this.mActionId = actionId;
			onNewActionId();
		}
	
	}

	private void onNewActionId() {
		mLayers[0] = getButtonDefaultDrawable();
		
		if(mActionId.equals(NONE)){
			mLayers[1] = getNoneDrawable();
			mLayers[0] = getButtonDisabledDrawable();
		}
		else if(mActionId.equals(GO)){
			mLayers[1] = getGoDrawable();
		}
		else if(mActionId.equals(SEARCH)){
			mLayers[1] = getSearchDrawable();
		}
		else if(mActionId.equals(SEND)){
			mLayers[1] = getSendDrawable();
		}
		else if(mActionId.equals(NEXT)){
			mLayers[1] = getNextDrawable();
		}
		else if(mActionId.equals(DONE)){
			mLayers[1] = getDoneDrawable();
		}
		else{
			mLayers[1] = getUnspecifiedDrawable();
		}
		
		mBackgroundDrawable = new LayerDrawable(mLayers);
		this.setBackgroundDrawable(mBackgroundDrawable);
	}

	
	

	private Drawable getDoneDrawable() {
		if(mDoneDrawable == null){
			mDoneDrawable = getResources().getDrawable(R.drawable.button_action_done);
		}
		return mDoneDrawable;
	}

	private Drawable getNextDrawable() {
		if(mNextDrawable == null){
			mNextDrawable = getResources().getDrawable(R.drawable.button_action_next);
		}
		return mNextDrawable;
	}

	private Drawable getSendDrawable() {
		if(mSendDrawable == null){
			mSendDrawable = getResources().getDrawable(R.drawable.button_action_send);
		}
		return mSendDrawable;
	}

	private Drawable getSearchDrawable() {
		if(mSearchDrawable == null){
			mSearchDrawable = getResources().getDrawable(R.drawable.button_action_search);
		}
		return mSearchDrawable;
	}

	private Drawable getGoDrawable() {
		if(mGoDrawable == null){
			mGoDrawable = getResources().getDrawable(R.drawable.button_action_go);
		}
		return mGoDrawable;
	}

	private Drawable getUnspecifiedDrawable() {
		if(mUnspecifiedDrawable == null){
			mUnspecifiedDrawable = getResources().getDrawable(R.drawable.button_action_next);
		}
		return mUnspecifiedDrawable;
	}

	private Drawable getNoneDrawable() {
		if(mNoneDrawable == null){
			mNoneDrawable = getResources().getDrawable(R.drawable.button_action_none);
		}
		return mNoneDrawable;
	}

	private void init(AttributeSet attrs) {

		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ActionButton);

		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.ActionButton_action_id:
				mActionId = a.getString(attr);
				break;
			}
		}
		a.recycle();
		init();

	}

	private void init() {
		Log.i(TAG, "ActionButton()");
		
		mLayers[0] = getButtonDefaultDrawable();
		mLayers[1] = getNextDrawable();
		mBackgroundDrawable = new LayerDrawable(mLayers);
		mBackgroundDrawable.setId(0, LAYER0);
		mBackgroundDrawable.setId(1, LAYER1);
		this.setBackgroundDrawable(mBackgroundDrawable);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		
		if(this.isPressed()){
			mLayers[0] = getButtonPressedDrawable();
		}
		else if(!this.isEnabled()){
			mLayers[0] = getButtonDisabledDrawable();
		}
		else{
			mLayers[0] = getButtonDefaultDrawable();
		}
		mBackgroundDrawable = new LayerDrawable(mLayers);
		this.setBackgroundDrawable(mBackgroundDrawable);
	}
	
	
}
