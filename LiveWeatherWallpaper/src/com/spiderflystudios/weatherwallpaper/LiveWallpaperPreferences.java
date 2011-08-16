package com.spiderflystudios.weatherwallpaper;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class LiveWallpaperPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	public static final int NOTIFICATION_ID = 18092;
	private EditTextPreference locationPref;
	private ListPreference freqPref;
	private ListPreference touchPref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(LiveWallpaperService.PREFERENCES);
		addPreferencesFromResource(R.xml.preferences);
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		setContentView(R.layout.pref_additions);
		
		locationPref = (EditTextPreference) findPreference(getResources().getString(R.string.location_key));
		freqPref = (ListPreference) findPreference(getResources().getString(R.string.frequency_key));
		touchPref = (ListPreference) findPreference(getResources().getString(R.string.touch_key));
		
		locationPref.setSummary(locationPref.getText());
		freqPref.setSummary(freqPref.getEntry());
		if (touchPref != null && touchPref.getEntry() != null) {
			if (touchPref.getEntry().toString().equalsIgnoreCase("Enable")) {
				touchPref.setSummary("Double tap action to display weather details is enabled.");
			} else {
				touchPref.setSummary("Double tap action to display weather details is disabled.");
			}
		}
		
		/*about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.custom_dialog);
				dialog.setTitle("About");
				((TextView) dialog.findViewById(R.id.dialogText)).setText("This is the about text");
				((ImageView) dialog.findViewById(R.id.dialogImage)).setImageResource(R.drawable.dialog_icon);
				dialog.show();
				return false;
			}
		});*/
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}
	
	@Override
	protected void onDestroy() {
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		locationPref.setSummary(locationPref.getText());
		freqPref.setSummary(freqPref.getEntry());
		if (touchPref != null && touchPref.getEntry() != null) {
			if (touchPref.getEntry().toString().equalsIgnoreCase("Enable")) {
				touchPref.setSummary("Double tap action to display weather details is enabled.");
			} else {
				touchPref.setSummary("Double tap action to display weather details is disabled.");
			}
		}
	}
}
