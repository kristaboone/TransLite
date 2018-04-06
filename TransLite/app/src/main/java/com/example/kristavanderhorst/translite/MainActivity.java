package com.example.kristavanderhorst.translite;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.KeyEvent;

import java.io.IOException;
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
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    // Speech recognition
    private SpeechRecognizer mSpeechRecognizer;
    private String mOperatorLang;
    private String mInteractLang;
    private String mVoiceInput;

    // Speech translation
    private SingletonRequestQueue mVolleyRequest;

    // Display item
    private TextView mTranslateTextView;

    private SurfaceHolder mCameraSurfHolder;
    private SurfaceView mCameraView;
    private int mCameraRotation;
    private Camera mCamera;
    private int mCameraID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up text view
        mTranslateTextView = (TextView) findViewById(R.id.translateTextView);
        mTranslateTextView.setText("\nWaiting for input...");

        // Set up camera
        mCameraView = (SurfaceView) findViewById(R.id.cameraView);
        mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCameraSurfHolder = mCameraView.getHolder();
        mCameraSurfHolder.addCallback(this);

        // Set up speech recognizer
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mOperatorLang = Locale.getDefault().getLanguage();
        mInteractLang = "es";

        // Set up volley singleton request queue
        // Using single instance will speed up access to translation API
        mVolleyRequest = SingletonRequestQueue.getInstance(this);
    }

    // Connect to Google Cloud Translation API
    private void runTranslation() {
        // Create Json URL
        String url = "https://translation.googleapis.com/language/translate/v2?";
        url += "q="+mVoiceInput;
        url += "&target="+mOperatorLang;
        url += "&format=text";
        url += "&source="+mInteractLang;
        url += "&key=AIzaSyC9NuYoZ0qSThz8qH-et-nhcmwYjgl8PPQ";

        // Create JsonObjectRequest
        JsonObjectRequest jsonRequest = new JsonObjectRequest
        (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject responseList = response.getJSONObject("data");
                    JSONArray transArray = responseList.getJSONArray("translations");
                    JSONObject textObj = transArray.getJSONObject(0);

                    // Format text
                    String outputStr = textObj.getString("translatedText");
                    outputStr = "\n" + outputStr.substring(0,1).toUpperCase() + outputStr.substring(1);
                    mTranslateTextView.setText(outputStr);
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
                // Set up speech recognizer in user's default language
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mOperatorLang);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, mOperatorLang);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());

                mSpeechRecognizer.startListening(intent);
                return true;
        }
        return super.onKeyDown(keycode, e);
    }

    // *****************************************************************************************
    // CAMERA SECTION
    // Source adjusted from following source:
    // https://www.c-sharpcorner.com/UploadFile/9e8439/how-to-make-a-custom-camera-ion-android/
    // *****************************************************************************************
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       if (!openCamera(Camera.CameraInfo.CAMERA_FACING_BACK)) {
           alertCameraDialog ();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    private void alertCameraDialog() {
        AlertDialog.Builder dialog = createAlert(MainActivity.this,
                "Camera info", "error to open camera");
        dialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        dialog.show();
    }
    private AlertDialog.Builder createAlert(Context context, String title, String message) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(
                new ContextThemeWrapper(context,
                        android.R.style.Theme_Holo_Light_Dialog));
        dialog.setIcon(R.drawable.ic_launcher_foreground);
        if (title != null)
            dialog.setTitle(title);
        else
            dialog.setTitle("Information");
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;

    }
    private boolean openCamera(int id) {
        boolean result = false;
        mCameraID = id;
        releaseCamera();
        try {
            mCamera = Camera.open(mCameraID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mCamera != null) {
            try {
                setUpCamera(mCamera);
                mCamera.setErrorCallback(new Camera.ErrorCallback() {

                    @Override
                    public void onError(int error, Camera camera) {
//to show the error message.
                    }
                });
                mCamera.setPreviewDisplay(mCameraSurfHolder);
                mCamera.startPreview();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
                releaseCamera();
            }
        }
        return result;
    }
    private void releaseCamera() {
        try {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mCamera.setErrorCallback(null);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("error", e.toString());
            mCamera = null;
        }
    }
    private void setUpCamera(Camera c) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, info);
        mCameraRotation = getWindowManager().getDefaultDisplay().getRotation();
        int degree = 0;
        switch (mCameraRotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;

            default:
                break;
        }

        // Back-facing
        mCameraRotation = (info.orientation - degree + 360) % 360;
        c.setDisplayOrientation(mCameraRotation);
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
        }
        public void onResults(Bundle results)
        {
            Log.d(TAG, "onResults " + results);
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            mVoiceInput = matches.get(0);

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
