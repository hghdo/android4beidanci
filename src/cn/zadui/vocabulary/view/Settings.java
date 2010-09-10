package cn.zadui.vocabulary.view;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import cn.zadui.vocabulary.R;
import cn.zadui.vocabulary.storage.PrefStore;

public class Settings extends PreferenceActivity implements OnPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager prefMgr = getPreferenceManager();		
		 addPreferencesFromResource(R.xml.settings);
		 //TODO fix hard code here
		 setTitle("Settings");
		 
		 Preference pref=prefMgr.findPreference(PrefStore.SP_KEY_MOTHER_TONGUE);
		 pref.setOnPreferenceChangeListener(this);
		 String lang=pref.getSharedPreferences().getString(PrefStore.SP_KEY_MOTHER_TONGUE,"");
		 pref.setSummary(lang);
		
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		preference.setSummary((String)newValue);
		return true;
	}

}
