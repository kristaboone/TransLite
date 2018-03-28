package com.example.kristavanderhorst.translite;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final String VOICE_STRING_DEFAULT = "DEFAULT"; // for debugging
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView mVoiceTextView;
    private String mVoiceString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVoiceTextView = (TextView) findViewById(R.id.voiceTextView);
        mVoiceString = VOICE_STRING_DEFAULT;
    }
    
    // TODO: Get input language from App settings?
    public void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            mVoiceString = VOICE_STRING_DEFAULT;
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mVoiceString = result.get(0);
                    mVoiceTextView.setText(mVoiceString);
                }
                break;
            }
        }
    }

    // Capture voice when volume-down pressed
    // NOTE: Can replace with DPAD_CENTER to get glass dpad tap
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                promptSpeechInput();
                return true;
        }
        return super.onKeyDown(keycode, e);
    }
}
