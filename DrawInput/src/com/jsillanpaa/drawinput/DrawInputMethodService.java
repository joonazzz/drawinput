package com.jsillanpaa.drawinput;

import java.util.ArrayList;

import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Button;

import com.jsillanpaa.drawinput.DrawInputCanvas.DrawInputCanvasListener;
import com.jsillanpaa.drawinput.char_recognizers.CharRecognitionResult;
import com.jsillanpaa.drawinput.char_recognizers.CharRecognizer;
import com.jsillanpaa.drawinput.char_recognizers.CharRecognizer.CharRecognizerListener;
import com.jsillanpaa.drawinput.char_recognizers.RbfSvmCharRecognizer;
import com.jsillanpaa.drawinput.hwr.InputMode;
import com.jsillanpaa.drawinput.widgets.InputModeToggleButton;

public class DrawInputMethodService extends InputMethodService {

	private static final String TAG = "DrawInputMethodService";

	private View mContainerView;

	private InputModeToggleButton mSmallAbcButton;
	private InputModeToggleButton mBigAbcButton;
	private InputModeToggleButton mNumbersButton;
	private InputModeToggleButton mSpecialCharsButton;
	private Button mEraseButton;
	private Button mSpaceButton;
	private Button mEnterButton;
	private Button mGoButton;
	private Button mLeftButton;
	private Button mClearButton;
	private Button mAcceptButton;
	private Button mRightButton;

	private ArrayList<InputModeToggleButton> mInputModeToggleButtons;
	private ArrayList<InputModeToggleButton> mValidInputModeButtons;
	private ArrayList<InputMode> mValidInputModes;

	private CharRecognizer mCharRecognizer;
	private CharRecognizerController mCharRecognizerController;
	private DrawInputCanvas mCanvas;
	private DrawInputCanvasController mCanvasController;

	private EditorInfo mEditorInfo;

	@Override
	public void onCreate() {
		Log.i(TAG, TAG + ".onCreate()");
		super.onCreate();
		mCharRecognizerController = new CharRecognizerController();
		mCharRecognizer = new RbfSvmCharRecognizer(this,
				mCharRecognizerController);

	}

	@Override
	public View onCreateInputView() {
		Log.i(TAG, TAG + ".onCreateInputView()");

		mContainerView = getLayoutInflater().inflate(R.layout.drawinput_gui,
				null);
		initReferences(mContainerView);
		return mContainerView;

	}

	private void initReferences(View container) {
		mCanvas = (DrawInputCanvas) container.findViewById(R.id.canvas);
		mCanvasController = new DrawInputCanvasController();
		mCanvas.addCanvasListener(mCanvasController);

		mSmallAbcButton = (InputModeToggleButton) container
				.findViewById(R.id.button_small_abc);
		mBigAbcButton = (InputModeToggleButton) container
				.findViewById(R.id.button_big_abc);
		mNumbersButton = (InputModeToggleButton) container
				.findViewById(R.id.button_numbers);
		mSpecialCharsButton = (InputModeToggleButton) container
				.findViewById(R.id.button_special_chars);

		mSmallAbcButton.setInputMode(InputMode.SMALL_LETTERS);
		mBigAbcButton.setInputMode(InputMode.BIG_LETTERS);
		mNumbersButton.setInputMode(InputMode.NUMBERS);
		mSpecialCharsButton.setInputMode(InputMode.SPECIAL_CHARS);

		mEraseButton = (Button) container.findViewById(R.id.button_erase);
		mSpaceButton = (Button) container.findViewById(R.id.button_space);
		mEnterButton = (Button) container.findViewById(R.id.button_enter);
		mGoButton = (Button) container.findViewById(R.id.button_go);

		mLeftButton = (Button) container.findViewById(R.id.button_left);
		mClearButton = (Button) container.findViewById(R.id.button_clear);
		mAcceptButton = (Button) container.findViewById(R.id.button_accept);
		mRightButton = (Button) container.findViewById(R.id.button_right);

		mInputModeToggleButtons = new ArrayList<InputModeToggleButton>(4);
		mInputModeToggleButtons.add(mSmallAbcButton);
		mInputModeToggleButtons.add(mBigAbcButton);
		mInputModeToggleButtons.add(mNumbersButton);
		mInputModeToggleButtons.add(mSpecialCharsButton);

		/* There is no XML attribute for long key press. Have to attach
		 * it in code. */
		mEraseButton.setOnLongClickListener( new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				InputConnection ic = getCurrentInputConnection();
				CharSequence charsBeforeCursor = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
				ic.deleteSurroundingText(charsBeforeCursor.length(), 0);
				mEraseButton.setPressed(false); // weird that this has to be here
				cursorMove();
				return true;
			}
		});
	}

	private void initInputMode() {
		mValidInputModes = new ArrayList<InputMode>();
		
		switch (mEditorInfo.inputType & InputType.TYPE_MASK_CLASS) {
		case InputType.TYPE_CLASS_TEXT:
			mValidInputModes.add(InputMode.SMALL_LETTERS);
			mValidInputModes.add(InputMode.BIG_LETTERS);
			mValidInputModes.add(InputMode.NUMBERS);
			mValidInputModes.add(InputMode.SPECIAL_CHARS);
			setInputMode(InputMode.NUMBERS);

			break;
		case InputType.TYPE_CLASS_DATETIME:
		case InputType.TYPE_CLASS_NUMBER:
		case InputType.TYPE_CLASS_PHONE:
			mValidInputModes.add(InputMode.NUMBERS);
			setInputMode(InputMode.NUMBERS);
		default:
			break;
		}
	}


	private void setInputMode(InputMode inputmode) {
		updateButtonGroup(inputmode);
		mCharRecognizer.setInputMode(inputmode);
	}

	private void updateButtonGroup(InputMode inputmode) {
		for (int i = 0; i < mInputModeToggleButtons.size(); i++) {
			InputModeToggleButton b = mInputModeToggleButtons.get(i);
			b.setChecked(b.getInputMode() == inputmode);
			if (b.getInputMode() == inputmode) {
				b.setChecked(true);
				b.setEnabled(false);
			} else if (mValidInputModes.contains(b.getInputMode())) {
				// Re-enable previously pressed toggle button
				b.setEnabled(true);
			}

		}
	}

	private InputModeToggleButton getInputModeButton(InputMode mode) {
		for (InputModeToggleButton b : mInputModeToggleButtons) {
			if (b.getInputMode() == mode)
				return b;
		}
		return null;
	}

	private void appendText(String str) {
		InputConnection ic = getCurrentInputConnection();

		ic.commitText(str, 1);

		cursorMove();
	}

	private void removeChar() {
		InputConnection ic = getCurrentInputConnection();
		ic.deleteSurroundingText(1, 0);
		cursorMove();
	}

	private void cursorMove() {

		InputConnection ic = getCurrentInputConnection();
		int charsBeforeCursor = ic.getTextBeforeCursor(10, 0).length();
		int charsAfterCursor = ic.getTextAfterCursor(10, 0).length();

		mEraseButton.setEnabled(charsBeforeCursor > 0);
		mLeftButton.setEnabled(charsBeforeCursor > 0);
		mRightButton.setEnabled(charsAfterCursor > 0);
	}

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
    	Log.i(TAG, TAG + ".onInitializeInterface()" );
    	
    }
	@Override
	public void onStartInputView(EditorInfo info, boolean restarting) {
		Log.i(TAG, TAG + ".onStartInputView(), restarting = " + restarting);
		super.onStartInputView(info, restarting);
		mEditorInfo = info;
		initInputMode();
		initGoButton();
		
	}

	
	private void initGoButton() {
		Log.i(TAG, "initGoButton(), action = " + mEditorInfo.actionId);
		
		switch (mEditorInfo.imeOptions & EditorInfo.IME_MASK_ACTION) {

			case EditorInfo.IME_ACTION_GO:
				Log.i(TAG, "go button is Go!");
				mGoButton.setEnabled(true);
				mGoButton.setBackgroundResource(R.drawable.button_background);
				mGoButton.setText(R.string.button_go_text);
				break;
				
			case EditorInfo.IME_ACTION_SEARCH:
				Log.i(TAG, "go button is search");
				mGoButton.setEnabled(true);
				mGoButton.setBackgroundResource(R.drawable.button_search_background);
				mGoButton.setText("");
				break;
			case EditorInfo.IME_ACTION_NEXT:
				Log.i(TAG, "go button is next");
				mGoButton.setEnabled(true);
				mGoButton.setText("Next");
				mGoButton.setBackgroundResource(R.drawable.button_background);
				break;
			case EditorInfo.IME_ACTION_SEND:
				Log.i(TAG, "go button is send");
				mGoButton.setEnabled(true);
				mGoButton.setText("Send");
				mGoButton.setBackgroundResource(R.drawable.button_background);
			
			case EditorInfo.IME_ACTION_DONE:
				Log.i(TAG, "go button is done");
				mGoButton.setEnabled(true);
				mGoButton.setText("Done");
				mGoButton.setBackgroundResource(R.drawable.button_background);
				break;
			default:
				Log.i(TAG, "go button is disabled");
				mGoButton.setEnabled(false);
				mGoButton.setText("--");
				mGoButton.setBackgroundResource(R.drawable.button_background);
	
				break;
		}
	}

	public void onSmallAbcClicked(View v) {
		Log.i(TAG, "onSmallAbcClicked()");
		setInputMode(InputMode.SMALL_LETTERS);
	}

	public void onBigAbcClicked(View v) {
		Log.i(TAG, "onBigAbcClicked()");
		setInputMode(InputMode.BIG_LETTERS);
	}

	public void onNumbersClicked(View v) {
		Log.i(TAG, "onNumbersClicked()");
		setInputMode(InputMode.NUMBERS);
	}

	public void onSpecialCharsClicked(View v) {
		Log.i(TAG, "onSpecialCharsClicked()");
		setInputMode(InputMode.SPECIAL_CHARS);
	}

	public void onEraseClicked(View v) {
		Log.i(TAG, "onEraseClicked()");

		removeChar();

	}

	public void onSpaceClicked(View v) {
		Log.i(TAG, "onSpaceClicked()");

		appendText(" ");
		
		//Log.i(TAG, "initGoButton(), action = " + getCurrentInputEditorInfo().);
		
	}

	public void onEnterClicked(View v) {
		Log.i(TAG, "onEnterClicked()");
		appendText("\n");
	}

	public void onGoClicked(View v) {
		Log.i(TAG, "onGoClicked()");
		getCurrentInputConnection().performEditorAction(mEditorInfo.imeOptions & EditorInfo.IME_MASK_ACTION);
	}

	public void onLeftClicked(View v) {
		Log.i(TAG, "onLeftClicked()");
		InputConnection ic = getCurrentInputConnection();
		ic.commitText("", -1);
		cursorMove();
	}

	public void onClearClicked(View v) {
		Log.i(TAG, "onClearClicked()");

		mCanvas.clear();
		mClearButton.setEnabled(false);
		mAcceptButton.setText(R.string.button_accept_nochar);
		mAcceptButton.setEnabled(false);
	}

	public void onAcceptClicked(View v) {
		Log.i(TAG, "onAcceptClicked()");

		appendText(mAcceptButton.getText().toString());
		mCanvas.clear();
		mClearButton.setEnabled(false);
		mAcceptButton.setText(R.string.button_accept_nochar);
		mAcceptButton.setEnabled(false);
	}

	public void onRightClicked(View v) {
		Log.i(TAG, "onRightClicked()");
		InputConnection ic = getCurrentInputConnection();
		ic.commitText("", 2);
		cursorMove();

	}

	public class DrawInputCanvasController implements DrawInputCanvasListener {

		@Override
		public void onNewStroke(DrawInputCanvas canvas) {
			mClearButton.setEnabled(true);
			mCharRecognizer.tryRecognition(canvas.getCharBeingDrawn());
		}

		@Override
		public void onSwipeLeft(DrawInputCanvas canvas) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSwipeRight(DrawInputCanvas canvas) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onCanvasSizeChanged(int w, int h) {
			mCharRecognizer.setCanvasWidth(w);
			mCharRecognizer.setCanvasHeight(h);
		}

	}

	public class CharRecognizerController implements CharRecognizerListener {

		@Override
		public void onNoResult() {
			Log.i(TAG, "onNoResult()");
		}

		@Override
		public void onRecognizedChar(CharRecognitionResult result) {
			Log.i(TAG,
					"onRecognizedChar(), result.getChar() = "
							+ result.getChar());
			mAcceptButton.setText("" + result.getChar());
			mAcceptButton.setEnabled(true);
		}

		@Override
		public void onNewInputModeLoaded(InputMode mode) {
			Log.i(TAG, "onNewInputModeLoaded()");
			mCanvas.removeText();
			getInputModeButton(mode).setStateLoaded(true);
		}

		@Override
		public void onNewInputModeLoading(InputMode mode) {
			Log.i(TAG, "onNewInputModeLoading()");
			mCanvas.showText(mode + " "
					+ getResources().getString(R.string.inputmode_loading));
		}

	}

}
