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
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

// TODO: Make sure user has correct permissions set before doing anything...
public class MainActivity extends AppCompatActivity {
    // Speech recognition
    private SpeechRecognizer mSpeechRecognizer;

    // Speech translation
    private SingletonRequestQueue mVolleyRequest;

    // Text display
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

        // Set up volley singleton request queue
        // Using single instance will speed up access to translation API
        mVolleyRequest = SingletonRequestQueue.getInstance(this);
    }

    // Connect to Google Cloud Translation API
    private void runTranslation() {
        // Get operator language- use default for now
        String opLang = Locale.getDefault().getLanguage();
        // Get interacter language- use spanish for now to test
        String inLang = "es";

        // Create Json URL
        String url = "https://translation.googleapis.com/language/translate/v2?";
        url += "q="+mVoiceTextView.getText();
        url += "&target="+opLang;
        url += "&format=text";
        url += "&source="+inLang;
        url += "&key=";

        // Create JsonObjectRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseList = response.getJSONObject("data");
                    JSONArray transArray = responseList.getJSONArray("translations");
                    JSONObject textObj = transArray.getJSONObject(0);
                    mTranslateTextView.setText(textObj.getString("translatedText"));
                } catch (JSONException e) {
                    mTranslateTextView.setText(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        // Add Json request to request queue
        mVolleyRequest.addToRequestQueue(jsonRequest);
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
