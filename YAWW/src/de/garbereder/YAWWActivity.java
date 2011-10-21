package de.garbereder;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

import de.garbereder.ColorPicker.ColorPickerDialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;

public class YAWWActivity extends Activity {

    private Weather weather = null;
    private Activity activity = null;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.main);

        ((Button)this.findViewById(R.id.bSave)).setOnClickListener(new OnClickListener() {
			//@Override
        	public void onClick(View v) {
        		getWeatherFromGoogle();
        	}
        });
        
        ((Button)this.findViewById(R.id.bColor)).setOnClickListener(new OnClickListener() {
			//@Override
        	public void onClick(View v) {
        		new ColorPickerDialog(
        			activity,
        			new BigInteger(((TextView)activity.findViewById(R.id.tColor)).getText().toString().substring(2), 16).intValue()
    			).show();
        	}
        });        

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		((TextView) this.findViewById(R.id.tLocation)).setText(city);
        
		String localeLang = preferences.getString("locale", null);
		String localeCountry = preferences.getString("localeCountry", null);
        Locale[] locales = Locale.getAvailableLocales();
        String[] strLocales = new String[locales.length];
        int i = -1;
        int markIdx = 0;
        
        for( Locale l : locales )
        {
        	strLocales[++i] = l.getDisplayName();
        	if( (localeLang != null && localeCountry != null && localeCountry.equals(l.getCountry()) && localeLang.equals(l.getLanguage())) ||
        			((localeLang == null || localeCountry == null) && l.equals(Locale.getDefault())) )
        		markIdx = i;
        }
        ((Spinner) this.findViewById(R.id.sLocale)).setAdapter(
        		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strLocales)
        );
        ((Spinner) this.findViewById(R.id.sLocale)).setSelection(markIdx);
        
        int iconIdx = preferences.getInt("iconSet", 0);
        ((Spinner) this.findViewById(R.id.sIcons)).setAdapter(
        		new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, YAWWWidget.iconSets)
        );
        ((Spinner) this.findViewById(R.id.sIcons)).setSelection(iconIdx);
    }

	protected void getWeatherFromGoogle() {
		try {
			String city = ((EditText) this.findViewById(R.id.tLocation)).getText().toString();
			Locale locale = Locale.getAvailableLocales()[((Spinner) this.findViewById(R.id.sLocale)).getSelectedItemPosition()];
			String localeLang = locale.getLanguage();
			weather = new Weather(
					city,
					localeLang
			);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putString("city", city);
			editor.putString("localeLang", localeLang);
			editor.putString("localeCountry", locale.getCountry());
			editor.putInt("bgColor", new BigInteger(((TextView)activity.findViewById(R.id.tColor)).getText().toString().substring(2), 16).intValue());
			editor.putInt("iconSet", ((Spinner) this.findViewById(R.id.sIcons)).getSelectedItemPosition());
			editor.commit();
			repaintWeather();
			updateWidget();
		} catch (ClientProtocolException e) {
			showDialog(e.getMessage());
		} catch (IllegalStateException e) {
			showDialog(e.getMessage());
		} catch (IOException e) {
			showDialog(e.getMessage());
		} catch (ParserConfigurationException e) {
			showDialog(e.getMessage());
		} catch (SAXException e) {
			showDialog(e.getMessage());
		} catch (ExceptionWithId e) {
			showDialog(getString(e.getId()));
		}
	}

	private void updateWidget() {
		
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
		YAWWWidget.refreshWidget(views, weather, new BigInteger(((TextView)activity.findViewById(R.id.tColor)).getText().toString().substring(2), 16).intValue(),
				YAWWWidget.ICONSETS.values()[((Spinner) this.findViewById(R.id.sIcons)).getSelectedItemPosition()], this);
		appWidgetManager.updateAppWidget(weatherWidgetId, views);
		
		final Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, weatherWidgetId);
		setResult(RESULT_OK, resultValue);
		finish();
	}

	private void repaintWeather() {
    	// DEBUG
		
		String deg = "°C";
		if( "US".equals(weather.getInfo().getUnitSystem()) )
			deg = "°F";
		
    	((TextView)this.findViewById(R.id.condition)).setText(weather.getCurrentCondition().getCondition());
    	((TextView)this.findViewById(R.id.temp)).setText(String.valueOf(weather.getCurrentCondition().getTemp()) + deg);
    	((TextView)this.findViewById(R.id.humidity)).setText(weather.getCurrentCondition().getHumidity());
    	
    	String url = "http://www.google.com"+weather.getCurrentCondition().getIconUrl();
		HttpClient hc = new DefaultHttpClient();
		try{		
			HttpResponse rp = hc.execute(new HttpPost(url));
			if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
		    	((ImageView)this.findViewById(R.id.imageIcon))
		    		.setImageDrawable(new BitmapDrawable(rp.getEntity().getContent()));
			}
		}catch(Exception e){
        	showDialog("Error: " + e.getMessage());
        	e.printStackTrace();
        }

	}

	private void showDialog(String msg){
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(msg);
		AlertDialog alert = dialogBuilder.create();
		alert.show();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.credit:
	        showDialog("GGWeatherWidget by\n\tGerrit Garbereder\n\thttp://www.Garbereder.de\n\nUsing\n\tGoogle Weather API\n\twww.BestIcon.com");
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}