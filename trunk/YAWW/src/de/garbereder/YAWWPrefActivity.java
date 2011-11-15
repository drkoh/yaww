package de.garbereder;

import java.io.IOException;
import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.RemoteViews;

public class YAWWPrefActivity extends PreferenceActivity {
	
	private Activity activity;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = this;
		addPreferencesFromResource(R.xml.preference);
		
		// LOCATION STUFF
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Geocoder geocoder = new Geocoder(this);
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		String city = "";
		if( location != null )
		{
	        try {
	                Address address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
	                city = address.getLocality();
	        } catch (IOException e) {
	                e.printStackTrace();
	        }
		}
		EditTextPreference cityPref = (EditTextPreference) findPreference("city");
		cityPref.setText(city);

		
		// COUNTRY CODE STUUF
		ListPreference countryCode = (ListPreference) findPreference("countryCode");
		Locale[] locales = Locale.getAvailableLocales();
		CharSequence[] strLocales = new String[locales.length];
		CharSequence[] strLocalesValues = new String[locales.length];
		int i = -1;
		for( Locale l : locales )
		{
			strLocales[++i] = l.getDisplayName();
			strLocalesValues[i] = l.toString();
		}
		countryCode.setEntries(strLocales);
		countryCode.setEntryValues(strLocalesValues);
		
		// COLOR PICKING
		((Preference) findPreference("bgColor")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(
						activity,
						((Preference) findPreference("bgColor")).getSharedPreferences().getInt("bgColor", 0xFF000000),
						new AmbilWarnaDialog.OnAmbilWarnaListener() {

							@Override
							public void onOk(AmbilWarnaDialog dialog, int color) {
								SharedPreferences bgColor = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
								SharedPreferences.Editor editor = bgColor.edit();
								editor.putInt("bgColor",color);
								editor.commit();
								System.out.println("Setting color");
							}

							@Override
							public void onCancel(AmbilWarnaDialog dialog) {
								//cancel was selected by the user
							}
						}
				);
				dialog.show();
				return true;
			}
		});
    }
    
    @Override
    public void onBackPressed() {
    	//super.onBackPressed();

		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		int weatherWidgetId = -1;
		if (extras != null) {
		    weatherWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
		final RemoteViews views = new RemoteViews(this.getPackageName(),
				R.layout.widget_layout);
		appWidgetManager.updateAppWidget(weatherWidgetId, views);
		
		final Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, weatherWidgetId);
		setResult(RESULT_OK, resultValue);
		System.out.println("CLOSING");
		Intent updateIntent = new Intent(this, YAWWWidget.class);
		updateIntent.setAction("PreferencesUpdated");
		updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, weatherWidgetId);
		sendBroadcast(updateIntent);
		finish();
    }
}
