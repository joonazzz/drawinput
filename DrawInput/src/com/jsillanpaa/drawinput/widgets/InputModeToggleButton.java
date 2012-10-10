package com.jsillanpaa.drawinput.widgets;

import com.jsillanpaa.drawinput.R;
import com.jsillanpaa.drawinput.hwr.InputMode;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ToggleButton;

/**
 * Represents InputMode toggle buttons. Acts like normal toggle button, but
 * has extra state: STATE_LOADED. This represents if input mode has been loaded
 * to classifier memory.
 * 
 * @author joonas
 *
 */
public class InputModeToggleButton extends ToggleButton {

	private static final int[] STATE_LOADED = { R.attr.state_loaded };
	private boolean mStateLoaded = false;
	private InputMode mInputMode;

	public InputModeToggleButton(Context context) {
		super(context);
	}

	public InputModeToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public InputModeToggleButton(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public void setStateLoaded(boolean isLoaded) {
		mStateLoaded = isLoaded;
	}

	public boolean getStateLoaded() {
		return mStateLoaded;
	}

	public InputMode getInputMode() {
		return mInputMode;
	}

	public void setInputMode(InputMode inputMode) {
		mInputMode = inputMode;
	}

	
	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.InputModeToggleButton);
		a.recycle();
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (mStateLoaded) {
			mergeDrawableStates(drawableState, STATE_LOADED);
		}
		return drawableState;
	}
}
