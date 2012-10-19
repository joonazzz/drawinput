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
import com.jsillanpaa.drawinput.hwr.HwrTools;
import com.jsillanpaa.drawinput.hwr.InputMode;
import com.jsillanpaa.drawinput.R;

public class RbfSvmCharRecognizer extends CharRecognizer {
	private static final String TAG = "RbfSvmCharRecognizer";

	private static final int N_SAMPLES_PER_LABEL_1a = 15;
	private static final int N_SAMPLES_PER_LABEL_1b = 30;
	private static final int N_SAMPLES_PER_LABEL_1c = 30;

	private static final String NUMBER_MODEL_FILE = "rbf_svm_model_from_1a_"
			+ N_SAMPLES_PER_LABEL_1a + "_samples";
	private static final String BIG_LETTERS_MODEL_FILE = "rbf_svm_model_from_1b_"
			+ N_SAMPLES_PER_LABEL_1b + "_samples";
	private static final String SMALL_LETTERS_MODEL_FILE = "rbf_svm_model_from_1c_"
			+ N_SAMPLES_PER_LABEL_1c + "_samples";

	private svm_model mNumberModel;
	private svm_model mBigLettersModel;
	private svm_model mSmallLettersModel;
	private svm_model current_model;

	//private InputMode mLoadingInputMode = null;

	private AsyncTask<InputMode, Void, InputMode> mLoadingTask = null;

	public RbfSvmCharRecognizer(Context context, CharRecognizerListener listener) {
		super(context, listener);
	}

	@Override
	public void tryRecognition(HwrCharacter ch) {
		Log.i(TAG, "tryRecognition()");
		HwrCharacter preprocessed_ch = HwrAlgorithms.preProcessChar(ch);
		float[] feature_vec = HwrAlgorithms.extractFeatures(preprocessed_ch);
		classify(feature_vec);
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
			Log.i(TAG, "loadInputMode(), mode = " + imode);
			//HwrTools.listAssets(mContext);

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
				//mSmallLettersModel = loadModelFromText(SMALL_LETTERS_MODEL_FILE
				//		+ ".txt");
				// loadScaler(SMALL_LETTERS_MODEL_FILE+"_scaler.bin");
				Log.i(TAG, "PROFILE: loading small abc model from text took: "
								+ (System.currentTimeMillis() - startTime)
								+ " ms");
				current_model = mSmallLettersModel;
				break;
			case SPECIAL_CHARS:
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
			notifyNewInputModeLoaded(result);
		}

	}

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
			break;
		default:
			break;
		}
		if (model == null) {
			Log.i(TAG, "checkInputMode(), model == null, loading it...");
			//if( (mLoadingInputMode == null) || (mLoadingInputMode != input_mode)){
				this.notifyNewInputModeLoading(input_mode);
				//mLoadingInputMode = input_mode;
				if(mLoadingTask != null){
					Log.i(TAG, "checkInputMode(), there was previous loading task, canceling it...");
					mLoadingTask.cancel(true);
				}
				mLoadingTask = new LoadInputModeTask().execute(input_mode);	
			//}
			
			return 0;
		}
		return 1;
	}

}