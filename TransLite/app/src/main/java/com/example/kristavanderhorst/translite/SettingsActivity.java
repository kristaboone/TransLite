package com.example.kristavanderhorst.translite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    private Spinner mOutputLang;
    private Spinner mInputLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up language input spinners
        mInputLang = (Spinner) findViewById(R.id.inLanguageSpinner);
        mOutputLang= (Spinner) findViewById(R.id.outLanguageSpinner);
        addOptions(mInputLang);
        addOptions(mOutputLang);
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                Intent data = new Intent();
                data.putExtra("inputLang", getInputLanguage());
                data.putExtra("outputLang", getOutputLanguage());
                setResult(RESULT_OK, data);
                super.finish();
        }
        return super.onKeyDown(keycode, e);
    }

    private void addOptions(Spinner spinner) {
        Locale[] locales = Locale.getAvailableLocales();
        List<String> readableList = new ArrayList<String>();

        for (Locale l:locales) {
            readableList.add(l.getDisplayLanguage());
        }
        removeDuplicatesInList(readableList);

        String userLang = Locale.getDefault().getDisplayLanguage();
        int defaultLang = 0;
        for (int i = 0; i < readableList.size(); ++i) {
            if (readableList.get(i).equals(userLang)) {
                defaultLang = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                                               android.R.layout.simple_spinner_item,
                                                               readableList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set default to user's language
        spinner.setSelection(defaultLang);
    }
    private void removeDuplicatesInList(List<String> list) {
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(list);
        list.clear();
        list.addAll(hashSet);
        Collections.sort(list);
    }
    public String getOutputLanguage() {
        return readable2ISO(mOutputLang.getSelectedItem().toString());
    }
    public String getInputLanguage() {
        return readable2ISO(mInputLang.getSelectedItem().toString());
    }

    private String readable2ISO(String readableName) {
        for (Locale locale : Locale.getAvailableLocales()) {
            if (readableName.equals(locale.getDisplayLanguage())) {
                return locale.getLanguage();
            }
        }
        return "";
    }
}
