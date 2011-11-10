package de.garbereder;

import java.io.IOException;
import java.util.GregorianCalendar;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

public class YAWWWidget extends AppWidgetProvider {
	
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
            // To prevent any ANR timeouts, we perform the update in a service
            context.startService(new Intent(context, UpdateService.class));
    }
	
	@Override
	public void onReceive(Context context, Intent intent) {
	    if ("PreferencesUpdated".equals(intent.getAction())) {	        
            ComponentName thisWidget = new ComponentName(context, YAWWWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, buildUpdate(context));	        
	    }   
	}

	public class UpdateService extends Service {
    	
        @Override
        public void onStart(Intent intent, int startId) {
            // Build the widget update for today
            RemoteViews updateViews = buildUpdate(this);

            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(this, YAWWWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}
    }		

	RemoteViews buildUpdate(Context context) {
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		RemoteViews updateViews = null;
		updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		try {
			Weather w = new Weather(preferences.getString("city", ""),preferences.getString("countryCode", "de_DE").split("_")[0]);
			int color = preferences.getInt("bgColor", 0xFF000000);
			String iconSet = preferences.getString("iconSet", "colorfull");
			
			refreshWidget(updateViews,w,color,iconSet,context);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExceptionWithId e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return updateViews;
	}
    
    private void refreshWidget(RemoteViews updateViews, Weather w, int color, String iconSet, Context context) {
		Resources res = context.getResources();
		
		updateViews.setInt(R.id.widget, "setBackgroundColor", color);
		
		// CURRENT
		updateViews.setTextViewText( R.id.widget_day0,w.getCurrentCondition().getTemp() + "°");
		updateViews.setTextViewText( R.id.widget_temp0, w.getCurrentCondition().getCondition());
		String url = w.getCurrentCondition().getIconUrl();
		String iconName = url.substring(url.lastIndexOf("/")+1);
		iconName = iconName.substring(0, iconName.length()-4);
		updateViews.setImageViewResource(R.id.widget_imageIcon0, res.getIdentifier(iconSet +"_"+iconName, "drawable", context.getPackageName()));
		
		// TODAY
		updateViews.setTextViewText( R.id.widget_temp1,
				w.getForecast(Weather.Forecasts.TODAY).getLow() + "°|" +
				w.getForecast(Weather.Forecasts.TODAY).getHigh() + "°"
		);
		updateViews.setTextViewText( R.id.widget_day1, w.getForecast(Weather.Forecasts.TODAY).getDayOfWeek());
		url = w.getForecast(Weather.Forecasts.TODAY).getIconUrl();
		iconName = url.substring(url.lastIndexOf("/")+1);
		iconName = iconName.substring(0, iconName.length()-4);
		updateViews.setImageViewResource(R.id.widget_imageIcon1, res.getIdentifier(iconSet +"_"+iconName, "drawable", context.getPackageName()));
		
		
		// TOMORROW
		updateViews.setTextViewText( R.id.widget_temp2,
				w.getForecast(Weather.Forecasts.TOMORROW).getLow() + "°|" +
				w.getForecast(Weather.Forecasts.TOMORROW).getHigh() + "°"
		);
		updateViews.setTextViewText( R.id.widget_day2, w.getForecast(Weather.Forecasts.TOMORROW).getDayOfWeek());
		url = w.getForecast(Weather.Forecasts.TOMORROW).getIconUrl();
		iconName = url.substring(url.lastIndexOf("/")+1);
		iconName = iconName.substring(0, iconName.length()-4);
		updateViews.setImageViewResource(R.id.widget_imageIcon2, res.getIdentifier(iconSet +"_"+iconName, "drawable", context.getPackageName()));
		
		
		// DAY AFTER TOMORROW
		updateViews.setTextViewText( R.id.widget_temp3,
				w.getForecast(Weather.Forecasts.DAY_AFTER_TOMORROW).getLow() + "°|" +
				w.getForecast(Weather.Forecasts.DAY_AFTER_TOMORROW).getHigh() + "°"
		);
		updateViews.setTextViewText( R.id.widget_day3, w.getForecast(Weather.Forecasts.DAY_AFTER_TOMORROW).getDayOfWeek());
		url = w.getForecast(Weather.Forecasts.DAY_AFTER_TOMORROW).getIconUrl();
		iconName = url.substring(url.lastIndexOf("/")+1);
		iconName = iconName.substring(0, iconName.length()-4);
		updateViews.setImageViewResource(R.id.widget_imageIcon3, res.getIdentifier(iconSet +"_"+iconName, "drawable", context.getPackageName()));
		
		// UPDATE TIMESTAMP
		GregorianCalendar cal = new GregorianCalendar();
		int min = cal.get(GregorianCalendar.MINUTE);
		String strMin = String.valueOf(min);
		if(min<10)
			strMin = "0" + strMin;
		updateViews.setTextViewText(
				R.id.updateTime, res.getString(R.string.lastUpdate) + " " +
				cal.get(GregorianCalendar.HOUR_OF_DAY) + ":" + strMin
		);
	}
}
