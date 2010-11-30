package com.burpen.awumpus;

import com.burpen.awumpus.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private ListPreference difficultyPreference;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    difficultyPreference = (ListPreference) getPreferenceScreen().findPreference("difficulty");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
        // TODO fix this
        difficultyPreference.setSummary("Currently set to " + sharedPreferences.getString("difficulty", ""));
	}

}
