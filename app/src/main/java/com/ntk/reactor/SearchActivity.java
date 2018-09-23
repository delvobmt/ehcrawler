package com.ntk.reactor;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.ntk.R;
import com.ntk.ehcrawler.EHConstants;
import com.ntk.ehcrawler.MyPreference;
import com.ntk.ehcrawler.activities.AppCompatPreferenceActivity;

public class SearchActivity extends AppCompatPreferenceActivity implements Preference.OnPreferenceChangeListener {

    private SharedPreferences mPreferences;
    private String mCurrentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mPreferences = getSharedPreferences(ReactorConstants.PREF_KEY, MODE_PRIVATE);
        addPreferencesFromResource(R.xml.reactor_pref_search);
        PreferenceManager.setDefaultValues(this, R.xml.reactor_pref_search, false);

        Preference tagPref = findPreference(ReactorConstants.TAG_KEY);
        tagPref.setOnPreferenceChangeListener(this);
        mCurrentTag = mPreferences.getString(ReactorConstants.TAG_KEY, null);
        tagPref.setSummary(mCurrentTag);
        tagPref.setDefaultValue(mCurrentTag);

        Preference indexPref = findPreference(ReactorConstants.INDEX_KEY);
        indexPref.setOnPreferenceChangeListener(this);
        int index = mPreferences.getInt(ReactorUtils.getCurrentIndexKey(mCurrentTag), 1);
        indexPref.setSummary(String.valueOf(index));
        indexPref.setDefaultValue(String.valueOf(index));
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
        SharedPreferences.Editor edit = mPreferences.edit();
        String key = preference.getKey();
        if (preference.getClass().equals(EditTextPreference.class)) {
            if(key.equals(ReactorConstants.TAG_KEY)) {
                preference.setSummary(newValue.toString());
                String nTag = String.valueOf(newValue);
                edit.putString(ReactorConstants.TAG_KEY, nTag);
                mCurrentTag = nTag;
                int nIndex = mPreferences.getInt(ReactorUtils.getCurrentIndexKey(mCurrentTag), 1);
                findPreference(ReactorConstants.INDEX_KEY).setSummary(String.valueOf(nIndex));
            } else if (key.equals(ReactorConstants.INDEX_KEY)) {
                preference.setSummary(newValue.toString());
                int index = Integer.valueOf(String.valueOf(newValue));
                edit.putInt(ReactorUtils.getCurrentIndexKey(mCurrentTag), index);
            }
        }
        boolean commit = edit.commit();
        return commit;
    }

}
