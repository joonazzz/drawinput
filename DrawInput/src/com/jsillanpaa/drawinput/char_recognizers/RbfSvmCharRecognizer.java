package com.jsillanpaa.drawinput.char_recognizers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import libsvm.svm;
import libsvm.svm_model;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.jsillanpaa.drawinput.R;
import com.jsillanpaa.drawinput.hwr.BigLetterLogicRecognizer;
import com.jsillanpaa.drawinput.hwr.HwrAlgorithms;
import com.jsillanpaa.drawinput.hwr.HwrCharacter;
import com.jsillanpaa.drawinput.hwr.InputMode;
import com.jsillanpaa.drawinput.hwr.LogicRecognizer;
import com.jsillanpaa.drawinput.hwr.NumberLogicRecognizer;
import com.jsillanpaa.drawinput.hwr.SmallLetterLogicRecognizer;
import com.jsillanpaa.drawinput.hwr.SpecialCharLogicRecognizer;

public class RbfSvmCharRecognizer extends CharRecognizer {
	private static final String TAG = "RbfSvmCharRecognizer";



	private svm_model mNumberModel;
	private svm_model mBigLettersModel;
	private svm_model mSmallLettersModel;
	private svm_model mSpecialCharsModel;
	private svm_model mSvmModel;

	private LogicRecognizer mCurrenLogicRecognizer = null;
	private LogicRecognizer mNumberLogicRecognizer;
	private LogicRecognizer mBigLettersLogicRecognizer;
	private LogicRecognizer mSmallLettersLogicRecognizer;
	private LogicRecognizer mSpecialCharLogicRecognizer;

	private AsyncTask<InputMode, Void, InputMode> mLoadingTask = null;



	public RbfSvmCharRecognizer(Context context, CharRecognizerListener listener) {
		super(context, listener);
		init();
		
	}

	// These are fast to initialize, no need to init in background thread
	private void init() {
		//mSpecialCharLogicRecognizer = new SpecialCharLogicRecognizer();
		
	}

	@Override
	public void tryRecognition(HwrCharacter ch) {		
		Log.d(TAG, "tryRecognition()");		
		new ClassifyTask().execute( ch );
	}




	private class ClassifyTask extends
	AsyncTask<HwrCharacter, Void, CharRecognitionResult> {
		

		private static final String MYTAG = "ClassifyTask";

		@Override
		protected CharRecognitionResult doInBackground(HwrCharacter... params) {
			Log.d(MYTAG, "doInBackground()");

			HwrCharacter ch = params[0];
			CharRecognitionResult result = null;

			/* First try recognition with logic. */
			if (mCurrenLogicRecognizer != null) {
				result = mCurrenLogicRecognizer
						.tryRecognition(ch);
				
			}
			if(result == null){
				/* Second try recognition with rbf svm. */
				HwrCharacter preprocessed_ch = HwrAlgorithms.preProcessChar(ch);
				float[] feature_vec = HwrAlgorithms
						.extractFeatures(preprocessed_ch);
				result = HwrAlgorithms.predict(feature_vec, mSvmModel);
			}
			return result;
			
		}

		/**
		 * The system calls this to perform work in the UI thread and delivers
		 * the result from doInBackground()
		 */
		@Override
		protected void onPostExecute(CharRecognitionResult result) {
			Log.d(MYTAG, "onPostExecute()");
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
			Log.d(TAG, "LoadInputModeTask.doInBackground()");
			loadInputMode(params[0]);
			return params[0];
		}

		private void loadInputMode(InputMode imode) {
			Log.d(TAG, "loadInputMode, mode = " + imode);

			long startTime;
			switch (imode) {
			case NUMBERS:
				startTime = System.currentTimeMillis();
				mNumberModel = loadModelFromResource(R.raw.rbf_svm_model_from_1a_15_samples);
				mNumberLogicRecognizer = new NumberLogicRecognizer(mCanvasWidth, mCanvasHeight);
				Log.d(TAG, "PROFILE: loading number model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				break;
			case BIG_LETTERS:
				startTime = System.currentTimeMillis();
				mBigLettersModel = loadModelFromResource(R.raw.rbf_svm_model_from_1b_15_samples);
				Log.d(TAG, "PROFILE: loading BIG ABC model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				mBigLettersLogicRecognizer = new BigLetterLogicRecognizer(mCanvasWidth, mCanvasHeight);
				break;
			case SMALL_LETTERS:
				startTime = System.currentTimeMillis();
				mSmallLettersModel = loadModelFromResource(R.raw.rbf_svm_model_from_1c_15_samples);
				Log.d(TAG, "PROFILE: loading small abc model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				mSmallLettersLogicRecognizer = new SmallLetterLogicRecognizer(mCanvasWidth, mCanvasHeight);
				break;
			case SPECIAL_CHARS:
				startTime = System.currentTimeMillis();
				mSpecialCharsModel = loadModelFromResource(R.raw.rbf_svm_model_from_1d_15_samples);
				mSpecialCharLogicRecognizer = new SpecialCharLogicRecognizer(mCanvasWidth, mCanvasHeight);
				Log.d(TAG, "PROFILE: loading special chars model from text took: "
						+ (System.currentTimeMillis() - startTime)
						+ " ms");
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
				retval = svm.svm_load_model( new BufferedReader( new InputStreamReader(is), 8192));
				 
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
			Log.d(TAG, "LoadInputModeTask.onPostExecute()");
			mLoadingTask = null;
			notifyNewInputModeLoaded(result);
			setInputMode(mInputMode);
		}

	}

	@Override
	public void setInputMode(InputMode input_mode) {
		Log.d(TAG, "setInputMode(), mode = " + input_mode);
		
		if (checkInputModeIsLoaded(input_mode) == false) {
			return;
		}

		switch (input_mode) {
		case NUMBERS:
			mSvmModel = mNumberModel;
			mCurrenLogicRecognizer = mNumberLogicRecognizer;
			break;
		case BIG_LETTERS:
			mSvmModel = mBigLettersModel;
			mCurrenLogicRecognizer = mBigLettersLogicRecognizer;
			break;
		case SMALL_LETTERS:
			mSvmModel = mSmallLettersModel;
			mCurrenLogicRecognizer = mSmallLettersLogicRecognizer;
			break;
		case SPECIAL_CHARS:
			mSvmModel = mSpecialCharsModel;
			mCurrenLogicRecognizer = mSpecialCharLogicRecognizer;
			break;
		default:
			break;
		}

	}

	/**
	 * @param input_mode
	 * @return true if input mode has already been loaded
	 */
	private boolean checkInputModeIsLoaded(InputMode input_mode) {
		Log.d(TAG, "checkInputModeIsLoaded(), mode = " + input_mode);
		
		if (!isLoaded(input_mode)) {
			Log.d(TAG, "checkInputMode(), model == null, maybe loading it...");
			
			if( mInputMode == null){ // There was no previous inputmode
				Log.d(TAG, "checkInputMode(), no previous inputmode, loading " + input_mode);
				mInputMode = input_mode;
				notifyNewInputModeLoading(input_mode);
				mLoadingTask = new LoadInputModeTask().execute(input_mode);
			}
			else if(mInputMode != input_mode){ // There was previous inputmode which was different
				Log.d(TAG, "checkInputMode(), previous inputmode was different, loading " + input_mode);
				mInputMode = input_mode;	
				notifyNewInputModeLoading(input_mode);
				// If there was previous loading task, cancel it
				if(mLoadingTask != null){
					Log.d(TAG, "checkInputMode(), there was previous loading task, canceling it...");
					mLoadingTask.cancel(true);
					
				}
				mLoadingTask = new LoadInputModeTask().execute(input_mode);
			}
			else{ // There was previous inputmode which was same, no need to do anything
				Log.d(TAG, "checkInputMode(), previous inputmode was same, no action");
				// FIXME TODO: I should really check why
//				android calls initialization twice. I think it is unnecessary and
//				causes annoying code. In the first init() drawing surface is created
//				and inputmode is loaded and drawing surface gets text "loading xxx inputmode"
//				In second call, it is recreated with empy surface and here we dont
//				start new loading, and thus drawing surface is left empty although
//				we are really still loading input mode!
				notifyNewInputModeLoading(input_mode);

				
			}
			
			return false;
		}
		return true;
	}

	@Override
	public boolean isLoaded(InputMode input_mode) {
		
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
		return model != null;
		
	}

}
