package com.jsillanpaa.drawinput;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.HwrStroke;
import com.jsillanpaa.drawinput.hwr.InputMode;

/**
 * This class represents a canvas area where user can draw characters.
 * 
 * Class listens to onTouch events and notifies DrawInputMethodService when a
 * new stroke has been drawn on canvas.
 * 
 * @author joonas
 */
public class DrawInputCanvas extends SurfaceView implements OnSharedPreferenceChangeListener {
	private static final String TAG = "DrawInputCanvas";
	private static final float LINE_WIDTH = 3.0f;
	private static final float DRAWING_LINE_WIDTH = 1.0f;

	private static final float END_POINT_RADIUS = 4.0f;
	private static final float TEXT_FONTSIZE = 24;
	private static final Typeface TEXT_TYPEFACE = Typeface.create("Serif", Typeface.ITALIC);
	private static final long ANIMATION_ROUND_TIME= 4000;
	private static final int ANIMATION_FPS = 15;
	private static final int ANIMATION_FRAME_TIME= (int) (1.0f/ANIMATION_FPS*1000);
	
	private static final float ANIMATION_CIRCLE_RADIUS = 40.0f;
	private static final int ANIMATION_N_DOTS = 3;
	private static final float MATH_2PI = (float) (2*Math.PI);
	private static final float ANIMATION_ANGLE_INCREMENT = MATH_2PI / ANIMATION_N_DOTS;
	private static final float CANVAS_INFO_TEXT_X = 40;
	private static final float CANVAS_INFO_TEXT_Y = 40;
	private static final float UPPER_LINE_Y = 0.20f;
	private static final float LOWER_LINE_Y = 0.80f;


	public interface DrawInputCanvasListener {
		public void onNewStroke(DrawInputCanvas canvas);
		public void onSwipeLeft(DrawInputCanvas canvas);
		public void onSwipeRight(DrawInputCanvas canvas);
		public void onCanvasSizeChanged(int w, int h);
	}

	private float previous_x;
	private float previous_y;

	private HwrStroke mStrokeBeingDrawn;
	private HwrCharacter mCharBeingDrawn;
	private ArrayList<DrawInputCanvasListener> mListeners;
	private Paint mLinePaint;
	private Paint mDotPaint;
	private Paint mTextPaint;
	private Paint mFirstPointPaint;
	private Paint mLastPointPaint;
	private Paint mUpperDrawingLinePaint;
	private Paint mLowerDrawingLinePaint;
	private InputMode mLoadingInputMode = null;
	private Paint mAnimationCirclePaint;
	private Paint mAnimationDotPaint;
	private Bitmap mDotBitmap;
	private long mPreviousExitTime;
	private Paint mMiddleDrawingLinePaint;
	private InputMode mInputMode;
	private SharedPreferences mSharedPrefs;
	private boolean mConnectPoints;
	private boolean mDrawEndPoints;
	private boolean mUseAnimations;
	
	public DrawInputCanvas(Context context) {
		super(context);
		init();
	}

	public DrawInputCanvas(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DrawInputCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		Log.d(TAG, "init()");
		mListeners = new ArrayList<DrawInputCanvas.DrawInputCanvasListener>();
		mCharBeingDrawn = new HwrCharacter('-');

		mLinePaint = new Paint();
		mLinePaint.setColor(getResources().getColor(R.color.canvas_line_color));
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(LINE_WIDTH);

		mUpperDrawingLinePaint = new Paint();
		mUpperDrawingLinePaint.setColor(getResources().getColor(R.color.canvas_drawing_line_color));
		mUpperDrawingLinePaint.setStyle(Style.STROKE);
		mUpperDrawingLinePaint.setStrokeWidth(DRAWING_LINE_WIDTH);
		
		mMiddleDrawingLinePaint = new Paint(mUpperDrawingLinePaint);
		mMiddleDrawingLinePaint.setStyle(Style.STROKE);
		mMiddleDrawingLinePaint.setStrokeMiter(0.5f);
		
		//mMiddleDrawingLinePaint.
		
		mLowerDrawingLinePaint = new Paint(mUpperDrawingLinePaint);
		
		mDotPaint = new Paint(mLinePaint);
		mDotPaint.setStyle(Style.FILL);
		
		mFirstPointPaint = new Paint();
		mFirstPointPaint.setColor(getResources().getColor(R.color.canvas_first_point_color));
		mFirstPointPaint.setStyle(Style.FILL);
		
		
		mLastPointPaint = new Paint();
		mLastPointPaint.setColor(getResources().getColor(R.color.canvas_last_point_color));
		mLastPointPaint.setStyle(Style.FILL);

		
		mTextPaint = new Paint();
		mTextPaint.setColor(getResources().getColor(R.color.canvas_text_color));
		mTextPaint.setStyle(Style.FILL);
		mTextPaint.setStrokeWidth(1.0f);
		mTextPaint.setSubpixelText(true);		// improves text quality
		mTextPaint.setAntiAlias(true);			// improves text quality
		mTextPaint.setTextSize(TEXT_FONTSIZE);
		mTextPaint.setTypeface(TEXT_TYPEFACE);
		
		setWillNotDraw(false);
		
		mAnimationCirclePaint = new Paint();
		mAnimationCirclePaint.setColor(getResources().getColor(R.color.canvas_circle_color));
		mAnimationCirclePaint.setStyle(Style.STROKE);
		mAnimationCirclePaint.setAntiAlias(true);
		mAnimationCirclePaint.setStrokeWidth(.5f);
		
		mDotBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.canvas_dot);
		
		Context appContext = getContext().getApplicationContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(appContext); 
		mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
		
		mConnectPoints = mSharedPrefs.getBoolean(DrawInputPreferences.PREF_CANVAS_CONNECT_POINTS, true);
		mDrawEndPoints = mSharedPrefs.getBoolean(DrawInputPreferences.PREF_CANVAS_HIGHLIGHT_ENDPOINTS, true);
		mUseAnimations = mSharedPrefs.getBoolean(DrawInputPreferences.PREF_USE_ANIMATIONS, true);
	}

	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		if(key.equals(DrawInputPreferences.PREF_CANVAS_CONNECT_POINTS)){
			mConnectPoints = prefs.getBoolean(DrawInputPreferences.PREF_CANVAS_CONNECT_POINTS, true);
			invalidate();
		}
		else if(key.equals(DrawInputPreferences.PREF_CANVAS_HIGHLIGHT_ENDPOINTS)){
			mDrawEndPoints = prefs.getBoolean(DrawInputPreferences.PREF_CANVAS_HIGHLIGHT_ENDPOINTS, true);
			invalidate();
		}
		else if(key.equals(DrawInputPreferences.PREF_USE_ANIMATIONS)){
			mUseAnimations = prefs.getBoolean(DrawInputPreferences.PREF_USE_ANIMATIONS, true);
		}
		
	}
	
	@Override
	public void draw(Canvas canvas) {
		//Log.d(TAG, "onDraw()");
		if (canvas != null) {
			canvas.drawColor(getResources().getColor(R.color.canvas_bg_color));
			
			if(mLoadingInputMode != null){
				showInputModeLoadingText(canvas);
				if(mUseAnimations){
					drawLoadingAnimation(canvas);
				}
			}
			else{
				drawChar(canvas);
			}
		}
	}

	private void drawChar(Canvas canvas) {
		
		if(mInputMode == InputMode.SPECIAL_CHARS){
			canvas.drawLine(0, UPPER_LINE_Y*getHeight(), getWidth(), UPPER_LINE_Y*getHeight(), mUpperDrawingLinePaint);
			canvas.drawLine(0, LOWER_LINE_Y*getHeight(), getWidth(), LOWER_LINE_Y*getHeight(), mLowerDrawingLinePaint);
		}
		
		//canvas.drawLine(0, MIDDLE_LINE_Y*getHeight(), getWidth(), MIDDLE_LINE_Y*getHeight(), mMiddleDrawingLinePaint);
		
		
		for (HwrStroke stroke : mCharBeingDrawn.strokes) {
			PointF first_p = stroke.points.get(0);
			PointF last_p = stroke.points.get(stroke.points.size()-1);
			
			if(mDrawEndPoints){
				canvas.drawCircle(first_p.x, first_p.y, END_POINT_RADIUS, mFirstPointPaint);
			}
			
			for (int i = 1; i < stroke.points.size(); i++) {
				PointF previous = stroke.points.get(i - 1);
				PointF current = stroke.points.get(i);
				if(mConnectPoints){
					canvas.drawLine(previous.x, previous.y, current.x,
							current.y, mLinePaint);	
				}
				
				canvas.drawPoint(current.x, current.y, mDotPaint);

			}
			if(mDrawEndPoints){
				canvas.drawCircle(last_p.x, last_p.y, END_POINT_RADIUS, mLastPointPaint);
			}
		}
	}

	
	private void drawLoadingAnimation(Canvas canvas) {
		
		long entryTime = System.currentTimeMillis();
		
		if(mPreviousExitTime != 0){
			long sleep_time = ANIMATION_FRAME_TIME - (entryTime-mPreviousExitTime);
			if(sleep_time > 0){
				try {
					Log.d("animation", "sleeping for " + sleep_time + " ms");
					Thread.sleep(sleep_time);
				} catch (InterruptedException e) {
					Log.d("animation", "thread interrupted!");
					e.printStackTrace();
				}	
			}
			
		}
		
		long t_now = System.currentTimeMillis();
		float animation_progress = (float)(t_now % ANIMATION_ROUND_TIME) / ANIMATION_ROUND_TIME;
		
		//showInputModeLoadingText(canvas);
		
		
		float cx = getWidth()/2.0f;
		float cy = getHeight()/2.0f + 30;
		canvas.drawCircle(cx, cy, ANIMATION_CIRCLE_RADIUS, mAnimationCirclePaint);
		
		float angle = MATH_2PI*animation_progress;
		float x, y;
		float r = ANIMATION_CIRCLE_RADIUS;
		for (int i = 0; i < ANIMATION_N_DOTS; i++) {
			x = cx + r*FloatMath.cos(angle + i*ANIMATION_ANGLE_INCREMENT) - mDotBitmap.getWidth()/2.0f;
			y = cy + r*FloatMath.sin(angle + i*ANIMATION_ANGLE_INCREMENT) - mDotBitmap.getWidth()/2.0f;
			
			canvas.drawBitmap(mDotBitmap, x, y, mAnimationDotPaint);
		}

		invalidate();		
		mPreviousExitTime = System.currentTimeMillis();
	}

	private void showInputModeLoadingText(Canvas canvas) {
		canvas.drawText("Loading " + mLoadingInputMode, CANVAS_INFO_TEXT_X, CANVAS_INFO_TEXT_Y , mTextPaint);
		canvas.drawText("svm model ...", CANVAS_INFO_TEXT_X, CANVAS_INFO_TEXT_Y + mTextPaint.getFontSpacing(), mTextPaint);
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		for (DrawInputCanvasListener l : mListeners) {
			l.onCanvasSizeChanged(w,h);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		
		if(mLoadingInputMode != null){
			return true;
		}

		float event_x = motionEvent.getX();
		float event_y = motionEvent.getY();

		switch (motionEvent.getAction()) {
		case MotionEvent.ACTION_DOWN:
			previous_x = event_x;
			previous_y = event_y;

			mStrokeBeingDrawn = new HwrStroke();
			mStrokeBeingDrawn.points.add(new PointF(previous_x, previous_y));
			mCharBeingDrawn.strokes.add(mStrokeBeingDrawn);

			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			// For efficiency, motion events with ACTION_MOVE may batch together
			// multiple movement samples within a single object. The most current
			// pointer coordinates are available using getX(int) and getY(int).
			// Earlier coordinates within the batch are accessed using
			// getHistoricalX(int, int) and getHistoricalY(int, int). The
			// coordinates are "historical" only insofar as they are older than the
			// current coordinates in the batch; however, they are still distinct
			// from any other coordinates reported in prior motion events. To
			// process all coordinates in the batch in time order, first consume the
			// historical coordinates then consume the current coordinates.
			//
			// from: http://developer.android.com/reference/android/view/MotionEvent.html
			
			final int historySize = motionEvent.getHistorySize();
			for (int h = 0; h < historySize	; h++) {
				mStrokeBeingDrawn.points.add(new PointF(motionEvent.getHistoricalX(h), motionEvent.getHistoricalY(h)));
			}	
			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			if (previous_x != event_x && previous_y != event_y) {
				mStrokeBeingDrawn.points.add(new PointF(event_x, event_y));
			}
			invalidate(); // invalidate before try recognition
			notifyNewStroke();
			break;
		}

		return true;

	}

	private void notifyNewStroke() {
		for (DrawInputCanvasListener l : mListeners) {
			l.onNewStroke(this);
		}
	}


	public InputMode getInputMode() {
		return mInputMode;
	}

	public void setInputMode(InputMode mode) {
		if(mInputMode != mode){
			mInputMode = mode;
			invalidate();
		}
		
	}

	public void clear() {
		Log.d(TAG, "clear()");
		mCharBeingDrawn.strokes.clear();
		invalidate();
	}

	public void addCanvasListener(DrawInputCanvasListener l) {
		mListeners.add(l);
	}

	public HwrCharacter getCharBeingDrawn() {
		return mCharBeingDrawn;
	}

	public void showText(String string) {
		invalidate();
	}
	public void removeText() {
		invalidate();
	}

	public void showInputModeLoading(InputMode mode){
		mCharBeingDrawn.strokes.clear();
		mLoadingInputMode = mode;
		
		if(mUseAnimations){
			mPreviousExitTime = 0;
		}
		invalidate();
	}

	public void stopInputModeLoading(){
		mLoadingInputMode = null;
		invalidate();
	}
	
}
