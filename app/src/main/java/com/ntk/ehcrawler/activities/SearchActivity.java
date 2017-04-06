package com.ntk.ehcrawler.activities;


import android.content.SharedPreferences;
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

        Preference f_search = findPreference(EHConstants.SEARCH_KEY);
        f_search.setOnPreferenceChangeListener(this);
        Preference f_doujinshi = findPreference(EHConstants.SEARCH_DOUJINSHI_KEY);
        f_doujinshi.setOnPreferenceChangeListener(this);
        Preference f_manga = findPreference(EHConstants.SEARCH_MANGA_KEY);
        f_manga.setOnPreferenceChangeListener(this);
        Preference f_artistcg = findPreference(EHConstants.SEARCH_ARTISTCG_KEY);
        f_artistcg.setOnPreferenceChangeListener(this);
        Preference f_gamecg = findPreference(EHConstants.SEARCH_GAMECG_KEY);
        f_gamecg.setOnPreferenceChangeListener(this);
        Preference f_nonh = findPreference(EHConstants.SEARCH_NON_HENTAI_KEY);
        f_nonh.setOnPreferenceChangeListener(this);
        Preference f_western = findPreference(EHConstants.SEARCH_WESTERN_KEY);
        f_western.setOnPreferenceChangeListener(this);
        Preference f_imageset = findPreference(EHConstants.SEARCH_IMAGESET_KEY);
        f_imageset.setOnPreferenceChangeListener(this);
        Preference f_cosplay = findPreference(EHConstants.SEARCH_COSPLAY_KEY);
        f_cosplay.setOnPreferenceChangeListener(this);
        Preference f_asianporn = findPreference(EHConstants.SEARCH_ASIANPORN_KEY);
        f_asianporn.setOnPreferenceChangeListener(this);
        Preference f_misc = findPreference(EHConstants.SEARCH_MISC_KEY);
        f_misc.setOnPreferenceChangeListener(this);
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
        SharedPreferences pref = getSharedPreferences(EHConstants.SEARCH_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        if (preference.getClass().equals(EditTextPreference.class)) {
            preference.setSummary(newValue.toString());
            edit.putString(preference.getKey(), String.valueOf(newValue));
        } else if (preference.getClass().equals(SwitchPreference.class)) {
            edit.putBoolean(preference.getKey(), Boolean.valueOf(newValue.toString()));
        }
        return edit.commit();
    }
}
