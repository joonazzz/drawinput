package com.jsillanpaa.drawinput.char_recognizers;

import java.io.IOException;
import java.io.InputStream;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jsillanpaa.drawinput.hwr.HwrAlgorithms;
import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.HwrStroke;
import com.jsillanpaa.drawinput.hwr.HwrTools;
import com.jsillanpaa.drawinput.hwr.InputMode;
import com.jsillanpaa.drawinput.R;

public class RbfSvmCharRecognizer extends CharRecognizer {
	private static final String TAG = "RbfSvmCharRecognizer";

	private static final int POINT_POINTS_MAX = 6;
	private static final int COMMA_POINTS_MAX = 18;

	private svm_model mNumberModel;
	private svm_model mBigLettersModel;
	private svm_model mSmallLettersModel;
	private svm_model mSpecialCharsModel;
	private svm_model current_model;

	private AsyncTask<InputMode, Void, InputMode> mLoadingTask = null;


	public RbfSvmCharRecognizer(Context context, CharRecognizerListener listener) {
		super(context, listener);
	}

	@Override
	public void tryRecognition(HwrCharacter ch) {
		if(current_model == mSpecialCharsModel){
			if(trySmallSpecialCharRecognition(ch)){
				return;
			}
		}
		Log.i(TAG, "tryRecognition()");
		HwrCharacter preprocessed_ch = HwrAlgorithms.preProcessChar(ch);
		float[] feature_vec = HwrAlgorithms.extractFeatures(preprocessed_ch);
		classify(feature_vec);
	}

	
	private boolean trySmallSpecialCharRecognition(HwrCharacter ch) {
		Log.i(TAG, "trySmallSpecialCharRecognition()");
		int num_strokes = ch.strokes.size();
		if(num_strokes==2){
			Log.i(TAG, "trySmallSpecialCharRecognition(), num_strokes = 2");
			HwrStroke upper = getUpperFromStrokes(ch.strokes.get(0), ch.strokes.get(1));
			HwrStroke lower = getLowerFromStrokes(ch.strokes.get(0), ch.strokes.get(1));
			
			Log.i(TAG, "upper.y = " + upper.points.get(0).y);
			Log.i(TAG, "lower.y = " + lower.points.get(0).y);
			Log.i(TAG, "isPoint(lower) = " + isPoint(lower));
			Log.i(TAG, "isPoint(upper) = " + isPoint(upper));
			Log.i(TAG, "isComma(lower) = " + isComma(lower));
			Log.i(TAG, "isComma(upper) = " + isComma(upper));
			
			
			if(isPoint(lower) && isPoint(upper)){
				notifyRecognizedChar(':');
				return true;
			}
			else if(isPoint(upper) && isComma(lower)){
				notifyRecognizedChar(';');
				return true;
			}
		}
		else if(num_strokes==1){
			Log.i(TAG, "trySmallSpecialCharRecognition(), num_strokes = 1");
			
			if(isPoint(ch.strokes.get(0))){
				notifyRecognizedChar('.');
				return true;
			}
			else if(isComma(ch.strokes.get(0))){
				notifyRecognizedChar(',');
				return true;
			}
			
		}
		return false;
	}

	private HwrStroke getLowerFromStrokes(HwrStroke left,
			HwrStroke right) {
		// y grows down
		if(left.points.get(0).y > right.points.get(0).y){
			return left;
		}
		else{
			return right;
		}
	}

	private HwrStroke getUpperFromStrokes(HwrStroke left,
			HwrStroke right) {
		// y grows down
		if(left.points.get(0).y <= right.points.get(0).y){
			return left;
		}
		else{
			return right;
		}
	}

	private boolean isComma(HwrStroke hwrStroke) {
		int num_points = hwrStroke.points.size();
		if( POINT_POINTS_MAX < num_points && num_points <= COMMA_POINTS_MAX){
			return true;
		}
		else{
			return false;	
		}
		
	}

	private boolean isPoint(HwrStroke hwrStroke) {
		int num_points = hwrStroke.points.size();
		if( 0 < num_points && num_points <= POINT_POINTS_MAX){
			return true;
		}
		else{
			return false;	
		}
		
	}

	private void classify(float[] feature_vec) {
		Log.i(TAG, "classify()");
		new ClassifyTask().execute(feature_vec);
	}

	private class ClassifyTask extends
			AsyncTask<float[], Void, CharRecognitionResult> {

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected CharRecognitionResult doInBackground(float[]... params) {
			Log.i(TAG, "ClassifyTask.doInBackground()");
			return predict(params[0]);
		}

		private CharRecognitionResult predict(float[] feature_vector) {
			svm_node[] z = new svm_node[feature_vector.length];
			for (int i = 0; i < feature_vector.length; i++) {
				z[i] = new svm_node();
				z[i].index = i + 1;
				z[i].value = feature_vector[i];
			}
			double result = svm.svm_predict(current_model, z);
			return new CharRecognitionResult((char) result, 1.0f);
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(CharRecognitionResult result) {
			Log.i(TAG, "ClassifyTask.onPostExecute()");
			notifyRecognizedChar(result);
		}
	}

	private class LoadInputModeTask extends
			AsyncTask<InputMode, Void, InputMode> {
		private static final String TAG = "LoadInputModeTask";

		/**
		 * The system calls this to perform work in a worker thread and delivers
		 * it the parameters given to AsyncTask.execute()
		 */
		@Override
		protected InputMode doInBackground(InputMode... params) {
			Log.i(TAG, "LoadInputModeTask.doInBackground()");
			loadInputMode(params[0]);
			return params[0];
		}

		private void loadInputMode(InputMode imode) {
			Log.i(TAG, "loadInputMode, mode = " + imode);

			long startTime;
			switch (imode) {
			case NUMBERS:
				startTime = System.currentTimeMillis();
				mNumberModel = loadModelFromResource(R.raw.rbf_svm_model_from_1a_15_samples);

				Log.i(TAG, "PROFILE: loading number model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				current_model = mNumberModel;
				break;
			case BIG_LETTERS:
				startTime = System.currentTimeMillis();
				mBigLettersModel = loadModelFromResource(R.raw.rbf_svm_model_from_1b_30_samples);
				Log.i(TAG, "PROFILE: loading BIG ABC model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				current_model = mBigLettersModel;
				break;
			case SMALL_LETTERS:
				startTime = System.currentTimeMillis();
				mSmallLettersModel = loadModelFromResource(R.raw.rbf_svm_model_from_1c_30_samples);
				Log.i(TAG, "PROFILE: loading small abc model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				current_model = mSmallLettersModel;
				break;
			case SPECIAL_CHARS:
				startTime = System.currentTimeMillis();
				mSpecialCharsModel = loadModelFromResource(R.raw.rbf_svm_model_from_1d_15_samples);
				Log.i(TAG, "PROFILE: loading special chars model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				current_model = mSpecialCharsModel;
				break;
			default:
				break;
			}

		}

		private svm_model loadModelFromResource(int resid) {
			
			InputStream is;
			svm_model retval = null;
			try {
				is = mContext.getResources().openRawResource(resid);
				retval = HwrTools.loadModelFromSvmText(is);
			} catch (IOException e) {
				e.printStackTrace();
			}

			return retval;
		}

		

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(InputMode result) {
			Log.i(TAG, "LoadInputModeTask.onPostExecute()");
			mLoadingTask = null;
			notifyNewInputModeLoaded(result);
		}

	}

	@Override
	public void setInputMode(InputMode input_mode) {
		Log.i(TAG, "setInputMode(), mode = " + input_mode);
		
		if (checkInputMode(input_mode) == 0) {
			return;
		}

		switch (input_mode) {
		case NUMBERS:
			current_model = mNumberModel;
			break;
		case BIG_LETTERS:
			current_model = mBigLettersModel;
			break;
		case SMALL_LETTERS:
			current_model = mSmallLettersModel;
			break;
		case SPECIAL_CHARS:
			current_model = mSpecialCharsModel;
			break;
		default:
			break;
		}

	}

	private int checkInputMode(InputMode input_mode) {
		Log.i(TAG, "checkInputMode(), mode = " + input_mode);
		svm_model model = null;
		switch (input_mode) {
		case NUMBERS:
			model = mNumberModel;
			break;
		case BIG_LETTERS:
			model = mBigLettersModel;
			break;
		case SMALL_LETTERS:
			model = mSmallLettersModel;
			break;
		case SPECIAL_CHARS:
			model = mSpecialCharsModel;
			break;
		default:
			break;
		}
		if (model == null) {
			Log.i(TAG, "checkInputMode(), model == null, maybe loading it...");
			
			if( mInputMode == null){ // This was no previous inputmode
				Log.i(TAG, "checkInputMode(), no previous inputmode, loading " + input_mode);
				mInputMode = input_mode;
				notifyNewInputModeLoading(input_mode);
				mLoadingTask = new LoadInputModeTask().execute(input_mode);
			}
			else if(mInputMode != input_mode){ // There as previous inputmode which was different
				Log.i(TAG, "checkInputMode(), previous inputmode was different, loading " + input_mode);
				mInputMode = input_mode;	
				notifyNewInputModeLoading(input_mode);
				// If there was previous loading task, cancel it
				if(mLoadingTask != null){
					Log.i(TAG, "checkInputMode(), there was previous loading task, canceling it...");
					mLoadingTask.cancel(true);
					
				}
				mLoadingTask = new LoadInputModeTask().execute(input_mode);
			}
			else{ // There was previous inputmode which was same, no need to do anything
				Log.i(TAG, "checkInputMode(), previous inputmode was same, no action");
				// FIXME TODO: I should really check why
//				android calls initialization twice. I think it is unnecessary and
//				causes annoying code. In the first init() drawing surface is created
//				and inputmode is loaded and drawing surface gets text "loading xxx inputmode"
//				In second call, it is recreated with empy surface and here we dont
//				start new loading, and thus drawing surface is left empty although
//				we are really still loading input mode!
				notifyNewInputModeLoading(input_mode);

				
			}
			
			return 0;
		}
		return 1;
	}

}
