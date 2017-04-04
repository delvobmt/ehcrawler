package com.ntk.ehcrawler.activities;


import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.R;

public class SearchActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.pref_search);
        findPreference(EHConstants.SEARCH_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_DOUJINSHI_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_MANGA_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_ARTISTCG_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_GAMECG_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_NON_HENTAI_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_WESTERN).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_IMAGESET_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_COSPLAY_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_ASIANPORN_KEY).setOnPreferenceChangeListener(this);
        findPreference(EHConstants.SEARCH_MISC_KEY).setOnPreferenceChangeListener(this);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference.getClass().equals(EditTextPreference.class)) {
            preference.setSummary(newValue.toString());
            getSharedPreferences(EHConstants.SEARCH_PREFERENCES, MODE_PRIVATE).edit()
                    .putString(preference.getKey(), String.valueOf(newValue));
        }else if(preference.getClass().equals(SwitchPreference.class)){
            getSharedPreferences(EHConstants.SEARCH_PREFERENCES, MODE_PRIVATE).edit()
                    .putBoolean(preference.getKey(), Boolean.valueOf(newValue.toString()));
        }
        return true;
    }
}
