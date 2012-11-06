package com.jsillanpaa.drawinput;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class DrawInputPreferences extends PreferenceActivity {
	private static final String TAG = "DrawInputPreferences";

	public static final String PREF_CANVAS_CONNECT_POINTS = "PREF_CANVAS_CONNECT_POINTS";
	public static final String PREF_CANVAS_HIGHLIGHT_ENDPOINTS = "PREF_CANVAS_HIGHLIGHT_ENDPOINTS";
	public static final String PREF_USE_ANIMATIONS = "PREF_USE_ANIMATIONS";

	SharedPreferences mSharedPrefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.drawinput_preferences);
		
		Context context = getApplicationContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

	}
	
	public SharedPreferences getSharedPrefs() {
		return mSharedPrefs;
	}

}
