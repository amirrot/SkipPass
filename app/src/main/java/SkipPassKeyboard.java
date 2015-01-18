package com.google.android.skippass;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SkipPassKeyboard extends InputMethodService
    implements KeyboardView.OnKeyboardActionListener {
  static final boolean DEBUG = false;

  // These are the Contacts rows that we will retrieve
  static final String[] PROJECTION = new String[] {PasswordsContentProvider.Data.ID, PasswordsContentProvider.Data.NAME, PasswordsContentProvider.Data.DESCRIPTION};

  // This is the select criteria
  static final String SELECTION = ".*";

  /**
   * This boolean indicates the optional example code for performing
   * processing of hard keys in addition to regular text generation
   * from on-screen interaction.  It would be used for input methods that
   * perform language translations (such as converting text entered on
   * a QWERTY keyboard to Chinese), but may not be used for input methods
   * that are primarily intended to be used for on-screen text entry.
   */
  static final boolean PROCESS_HARD_KEYS = true;

  private static final String LOG_TAG = "SkipPassKeyboardLog";

  private InputMethodManager mInputMethodManager;
  private Cursor mCursor;

  private PinKeyboardView mKeyboardView;
  private CandidateView mCandidateView;
  private CompletionInfo[] mCompletions;

  private StringBuilder mComposing = new StringBuilder();
  private boolean mPredictionOn;
  private boolean mCompletionOn;
  private int mLastDisplayWidth;
  private long mMetaState;

  private PinKeyboard mKeyboard;

  /**
   * Main initialization of the input method component.  Be sure to call
   * to super class.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

    mCursor = getContentResolver().query(PasswordsContentProvider.CONTENT_URI, PROJECTION, SELECTION, null, null);
  }

  /**
   * This is the point where you can do all of your UI initialization.  It
   * is called after creation and any configuration change.
   */
  @Override
  public void onInitializeInterface() {
    if (mKeyboard != null) {
      // Configuration changes can happen after the keyboard gets recreated,
      // so we need to be able to re-build the keyboards if the available
      // space has changed.
      int displayWidth = getMaxWidth();
      if (displayWidth == mLastDisplayWidth) return;
      mLastDisplayWidth = displayWidth;
    }
    mKeyboard = new PinKeyboard(this, R.xml.qwerty);
  }

  /**
   * Called by the framework when your view for creating input needs to
   * be generated.  This will be called the first time your input method
   * is displayed, and every time it needs to be re-created such as due to
   * a configuration change.
   */
  @Override
  public View onCreateInputView() {
    mKeyboardView = (PinKeyboardView) getLayoutInflater().inflate(R.layout.input, null);
    mKeyboardView.setOnKeyboardActionListener(this);
    mKeyboardView.setKeyboard(mKeyboard);
    return mKeyboardView;
  }

  /**
   * Called by the framework when your view for showing candidates needs to
   * be generated, like {@link #onCreateInputView}.
   */
  @Override
  public View onCreateCandidatesView() {
    mCandidateView = new CandidateView(this);
    mCandidateView.setService(this);
    return mCandidateView;
  }

  /**
   * Helper method for getting the App name from its PID.
   * @param context
   * @param pid
   * @return
   */
  public static String getAppNameByPID(Context context, int pid){
    ActivityManager manager
        = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

    for(RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
      if(processInfo.pid == pid){
        return processInfo.processName;
      }
    }
    return "";
  }
  /**
   * This is the main point where we do our initialization of the input method
   * to begin operating on an application.  At this point we have been
   * bound to the client, and are now receiving all of the detailed information
   * about the target of our edits.
   */
  @Override
  public void onStartInput(EditorInfo attribute, boolean restarting) {
    super.onStartInput(attribute, restarting);
    StringBuilder log = new StringBuilder();

    InputBinding inputBinding = getCurrentInputBinding();
    log.append("application:");
    if (inputBinding == null) {
      log.append("null.");
    } else {
      log.append(getAppNameByPID(this, inputBinding.getPid()));
    }
    Log.d(LOG_TAG, log.toString());

    StringBuilder inputText = new StringBuilder();
    if (mCursor == null) {
      inputText.append("null");
    } else if (mCursor.getCount() == 0) {
        inputText.append("empty cursor");
    } else {
      mCursor.moveToFirst();  // Move to first row.
      inputText.append(mCursor.getString(1));  // Get column 1
      getCurrentInputConnection().commitText(
          inputText.subSequence(0, inputText.length() - 1), inputText.length());
    }

    // Reset our state.  We want to do this even if restarting, because
    // the underlying state of the text editor could have changed in any way.
    mComposing.setLength(0);
    updateCandidates();

    if (!restarting) {
      // Clear shift states.
      mMetaState = 0;
    }

    mPredictionOn = false;
    mCompletionOn = false;
    mCompletions = null;

    // Update the label on the enter key, depending on what the application
    // says it will do.
    mKeyboard.setImeOptions(getResources(), attribute.imeOptions);
  }

  /**
   * This is called when the user is done editing a field.  We can use
   * this to reset our state.
   */
  @Override
  public void onFinishInput() {
    super.onFinishInput();

    // Clear current composing text and candidates.
    mComposing.setLength(0);
    updateCandidates();

    // We only hide the candidates window when finishing input on
    // a particular editor, to avoid popping the underlying application
    // up and down if the user is entering text into the bottom of
    // its window.
    setCandidatesViewShown(false);

    // Return the last keyboard used.
    mInputMethodManager.switchToLastInputMethod(getToken());

  if (mKeyboardView != null) {
      mKeyboardView.closing();
    }
  }

  private IBinder getToken() {
    final Dialog dialog = getWindow();
    if (dialog == null) {
      return null;
    }
    final Window window = dialog.getWindow();
    if (window == null) {
      return null;
    }
    return window.getAttributes().token;
  }

  @Override
  public void onStartInputView(EditorInfo attribute, boolean restarting) {
    super.onStartInputView(attribute, restarting);
    // Apply the selected keyboard to the input view.
    mKeyboardView.closing();
    final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
    mKeyboardView.setSubtypeOnSpaceKey(subtype);
  }

  @Override
  public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
    mKeyboardView.setSubtypeOnSpaceKey(subtype);
  }

  /**
   * Deal with the editor reporting movement of its cursor.
   */
  @Override
  public void onUpdateSelection(int oldSelStart, int oldSelEnd,
      int newSelStart, int newSelEnd,
      int candidatesStart, int candidatesEnd) {
    super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
        candidatesStart, candidatesEnd);

    // If the current selection in the text view changes, we should
    // clear whatever candidate text we have.
    if (mComposing.length() > 0 && (newSelStart != candidatesEnd
        || newSelEnd != candidatesEnd)) {
      mComposing.setLength(0);
      updateCandidates();
      InputConnection ic = getCurrentInputConnection();
      if (ic != null) {
        ic.finishComposingText();
      }
    }
  }

  /**
   * This tells us about completions that the editor has determined based
   * on the current text in it.  We want to use this in fullscreen mode
   * to show the completions ourself, since the editor can not be seen
   * in that situation.
   */
  @Override
  public void onDisplayCompletions(CompletionInfo[] completions) {
    if (mCompletionOn) {
      mCompletions = completions;
      if (completions == null) {
        setSuggestions(null, false, false);
        return;
      }

      List<String> stringList = new ArrayList<String>();
      for (int i = 0; i < completions.length; i++) {
        CompletionInfo ci = completions[i];
        if (ci != null) stringList.add(ci.getText().toString());
      }
      setSuggestions(stringList, true, true);
    }
  }

  /**
   * This translates incoming hard key events in to edit operations on an
   * InputConnection.  It is only needed when using the
   * PROCESS_HARD_KEYS option.
   */
  private boolean translateKeyDown(int keyCode, KeyEvent event) {
    mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
        keyCode, event);
    int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
    mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
    InputConnection ic = getCurrentInputConnection();
    if (c == 0 || ic == null) {
      return false;
    }

    if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
      c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
    }

    if (mComposing.length() > 0) {
      char accent = mComposing.charAt(mComposing.length() -1 );
      int composed = KeyEvent.getDeadChar(accent, c);

      if (composed != 0) {
        c = composed;
        mComposing.setLength(mComposing.length()-1);
      }
    }

    onKey(c, null);

    return true;
  }

  /**
   * Use this to monitor key events being delivered to the application.
   * We get first crack at them, and can either resume them or let them
   * continue to the app.
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        // The InputMethodService already takes care of the back
        // key for us, to dismiss the input method if it is shown.
        // However, our keyboard could be showing a pop-up window
        // that back should dismiss, so we first allow it to do that.
        if (event.getRepeatCount() == 0 && mKeyboardView != null) {
          if (mKeyboardView.handleBack()) {
            return true;
          }
        }
        break;

      case KeyEvent.KEYCODE_DEL:
        // Special handling of the delete key: if we currently are
        // composing text for the user, we want to modify that instead
        // of let the application to the delete itself.
        if (mComposing.length() > 0) {
          onKey(Keyboard.KEYCODE_DELETE, null);
          return true;
        }
        break;

      case KeyEvent.KEYCODE_ENTER:
        // Let the underlying text editor always handle these.
        return false;

      default:
        // For all other keys, if we want to do transformations on
        // text being entered with a hard keyboard, we need to process
        // it and do the appropriate action.
        if (PROCESS_HARD_KEYS) {
          if (keyCode == KeyEvent.KEYCODE_SPACE
              && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
            // A silly example: in our input method, Alt+Space
            // is a shortcut for 'android' in lower case.
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
              // First, tell the editor that it is no longer in the
              // shift state, since we are consuming this.
              ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
              keyDownUp(KeyEvent.KEYCODE_A);
              keyDownUp(KeyEvent.KEYCODE_N);
              keyDownUp(KeyEvent.KEYCODE_D);
              keyDownUp(KeyEvent.KEYCODE_R);
              keyDownUp(KeyEvent.KEYCODE_O);
              keyDownUp(KeyEvent.KEYCODE_I);
              keyDownUp(KeyEvent.KEYCODE_D);
              // And we consume this event.
              return true;
            }
          }
          if (mPredictionOn && translateKeyDown(keyCode, event)) {
            return true;
          }
        }
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * Use this to monitor key events being delivered to the application.
   * We get first crack at them, and can either resume them or let them
   * continue to the app.
   */
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    // If we want to do transformations on text being entered with a hard
    // keyboard, we need to process the up events to update the meta key
    // state we are tracking.
    if (PROCESS_HARD_KEYS) {
      if (mPredictionOn) {
        mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
            keyCode, event);
      }
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * Helper function to commit any text being composed in to the editor.
   */
  private void commitTyped(InputConnection inputConnection) {
    if (mComposing.length() > 0) {
      inputConnection.commitText(mComposing, mComposing.length());
      mComposing.setLength(0);
      updateCandidates();
    }
  }

  /**
   * Helper to send a key down / key up pair to the current editor.
   */
  private void keyDownUp(int keyEventCode) {
    getCurrentInputConnection().sendKeyEvent(
        new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
    getCurrentInputConnection().sendKeyEvent(
        new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
  }

  // Implementation of KeyboardViewListener
  public void onKey(int primaryCode, int[] keyCodes) {
    if (primaryCode == Keyboard.KEYCODE_DELETE) {
      handleBackspace();
    } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
      handleClose();
    } else {
      handleCharacter(primaryCode);
    }
  }

  public void onText(CharSequence text) {
    InputConnection ic = getCurrentInputConnection();
    if (ic == null) {
      return;
    }
    ic.beginBatchEdit();
    if (mComposing.length() > 0) {
      commitTyped(ic);
    }
    ic.commitText(text, 0);
    ic.endBatchEdit();
  }

  /**
   * Update the list of available candidates from the current composing
   * text.  This will need to be filled in by however you are determining
   * candidates.
   */
  private void updateCandidates() {
    if (!mCompletionOn) {
      if (mComposing.length() > 0) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(mComposing.toString());
        setSuggestions(list, true, true);
      } else {
        setSuggestions(null, false, false);
      }
    }
  }

  public void setSuggestions(List<String> suggestions, boolean completions,
      boolean typedWordValid) {
    if (suggestions != null && suggestions.size() > 0) {
      setCandidatesViewShown(true);
    } else if (isExtractViewShown()) {
      setCandidatesViewShown(true);
    }
    if (mCandidateView != null) {
      mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
    }
  }

  private void handleBackspace() {
    final int length = mComposing.length();
    if (length > 1) {
      mComposing.delete(length - 1, length);
      getCurrentInputConnection().setComposingText(mComposing, 1);
      updateCandidates();
    } else if (length > 0) {
      mComposing.setLength(0);
      getCurrentInputConnection().commitText("", 0);
      updateCandidates();
    } else {
      keyDownUp(KeyEvent.KEYCODE_DEL);
    }
  }

  private void handleCharacter(int primaryCode) {
    if (isInputViewShown()) {
      if (mKeyboardView.isShifted()) {
        primaryCode = Character.toUpperCase(primaryCode);
      }
    }
    if (Character.isLetter(primaryCode) && mPredictionOn) {
      mComposing.append((char) primaryCode);
      getCurrentInputConnection().setComposingText(mComposing, 1);
      updateCandidates();
    } else {
      getCurrentInputConnection().commitText(
          String.valueOf((char) primaryCode), 1);
    }
  }

  private void handleClose() {
    commitTyped(getCurrentInputConnection());
    requestHideSelf(0);
    mKeyboardView.closing();
  }

  public void pickDefaultCandidate() {
    pickSuggestionManually(0);
  }

  public void pickSuggestionManually(int index) {
    if (mCompletionOn && mCompletions != null && index >= 0
        && index < mCompletions.length) {
      CompletionInfo ci = mCompletions[index];
      getCurrentInputConnection().commitCompletion(ci);
      if (mCandidateView != null) {
        mCandidateView.clear();
      }
    } else if (mComposing.length() > 0) {
      // If we were generating candidate suggestions for the current
      // text, we would commit one of them here.  But for this sample,
      // we will just commit the current text.
      commitTyped(getCurrentInputConnection());
    }
  }

  public void swipeRight() {
    if (mCompletionOn) {
      pickDefaultCandidate();
    }
  }

  public void swipeLeft() {
    handleBackspace();
  }

  public void swipeDown() {
    handleClose();
  }

  public void swipeUp() {}

  public void onPress(int primaryCode) {}

  public void onRelease(int primaryCode) {}
}
