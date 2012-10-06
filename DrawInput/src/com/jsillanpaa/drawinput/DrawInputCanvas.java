package com.jsillanpaa.drawinput;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This class represents a canvas area where user can draw characters.
 * 
 * Class listens to onTouch events and notifies DrawInputMethodService
 * when a new stroke has been drawn on canvas.
 * 
 * @author joonas
 */
public class DrawInputCanvas extends SurfaceView implements OnTouchListener{
	
	public interface DrawInputCanvasListener{
		public void onNewStroke(DrawInputCanvas canvas);
		public void onSwipeLeft(DrawInputCanvas canvas);
		public void onSwipeRight(DrawInputCanvas canvas);
	}
	
	public DrawInputCanvas(Context context) {
		super(context);
		
		setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
