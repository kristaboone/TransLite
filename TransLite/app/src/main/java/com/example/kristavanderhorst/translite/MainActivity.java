package com.example.kristavanderhorst.translite;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.view.KeyEvent;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private final String VOICE_STRING_DEFAULT = "DEFAULT"; // for debugging

    private SpeechRecognizer mSpeechRecognizer;

    private TextView mTranslateTextView;
    private TextView mVoiceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up text views (voice text view is for debugging)
        mVoiceTextView = (TextView) findViewById(R.id.voiceTextView);
        mTranslateTextView = (TextView) findViewById(R.id.translateTextView);

        // Set up speech recognizer
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
    }

    // Connect to Google Cloud Translation API
    private void runTranslation() {
        mTranslateTextView.setText(mTranslateString);
    }

    // Capture voice when volume-down pressed
    // NOTE: Can replace with DPAD_CENTER to get glass dpad tap
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                // Set up intent
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());

                mSpeechRecognizer.startListening(intent);
                return true;
        }
        return super.onKeyDown(keycode, e);
    }

    // Our own RecognitionListener implementation to avoid pop-up from google..
    protected class SpeechRecognitionListener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);
            mVoiceTextView.setText("error " + error);
        }
        public void onResults(Bundle results)
        {
            Log.d(TAG, "onResults " + results);
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            mVoiceTextView.setText(matches.get(0));

            // translate text to new language
            runTranslation();
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }
}
