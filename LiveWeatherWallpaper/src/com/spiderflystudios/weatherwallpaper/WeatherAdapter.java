package com.spiderflystudios.weatherwallpaper;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.util.ByteArrayBuffer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.spiderflystudios.weatherwallpaper.LiveWallpaperService.LiveWallpaperReceiver;
import com.spiderflystudios.weatherwallpaper.weather.GoogleWeatherHandler;
import com.spiderflystudios.weatherwallpaper.weather.WeatherSet;
import com.spiderflystudios.weatherwallpaper.weather.WeatherUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class WeatherAdapter {
	
	private static WeatherAdapter singleton;
	
	private Context context;
	private String imageURL = "http://www.spiderflystudios.com/Weather/Grass/";
	private HashMap<String, String> conditionImageMap_day = new HashMap<String, String>();
	private HashMap<String, String> conditionImageMap_night = new HashMap<String, String>();
	private Date[] sunTimes;
	private Date currentTime;
	
	public int locationOffset;
	public int userOffset;
	
	// Weather view vars
	//public boolean completedLastUpdate = true;
	private String location;
	private String currentCondition = "";
	private String currentTemp = "";
	private String forecastHigh = "";
	private String forecastLow = "";
	private Bitmap image = null;
	
	private WeatherAdapter() {
		initHashMaps();
	}
	
	public static WeatherAdapter getInstance() {
		if (singleton == null) {
			singleton = new WeatherAdapter();
		}
		return singleton;
	}
	
	private void initHashMaps() {
		// <imageName, condition>
		conditionImageMap_day.put("n/a", "na.jpg");
		conditionImageMap_day.put("chance of rain", "chance_rain_d.jpg");
		conditionImageMap_day.put("chance of snow", "chance_snow_d.jpg");
		conditionImageMap_day.put("chance of storms", "chance_storm_d.jpg");
		conditionImageMap_day.put("chance of thunderstorms", "chance_tstorm_d.jpg");
		conditionImageMap_day.put("clear", "sunny.jpg");
		conditionImageMap_day.put("cloudy", "cloudy_d.jpg");
		conditionImageMap_day.put("dust", "dust_d.jpg");
		conditionImageMap_day.put("flurries", "flurries_d.jpg");
		conditionImageMap_day.put("fog", "fog_d.jpg");
		conditionImageMap_day.put("freezing drizzle", "freezing_drizzle_d.jpg");
		conditionImageMap_day.put("freezing rain", "freezing_drizzle_d.jpg");
		conditionImageMap_day.put("hail", "hail_d.jpg");
		conditionImageMap_day.put("haze", "haze_d.jpg");
		conditionImageMap_day.put("ice", "ice_d.jpg");
		conditionImageMap_day.put("ice and snow", "ice_snow_d.jpg");
		conditionImageMap_day.put("ice/snow", "ice_snow_d.jpg");
		conditionImageMap_day.put("light rain", "light_rain_d.jpg");
		conditionImageMap_day.put("light snow", "light_snow_d.jpg");
		conditionImageMap_day.put("light snow mist", "light_snow_d.jpg");
		conditionImageMap_day.put("mist", "mist_d.jpg");
		conditionImageMap_day.put("mostly cloudy", "mostly_cloudy_d.jpg");
		conditionImageMap_day.put("mostly clear", "mostly_sunny.jpg");
		conditionImageMap_day.put("mostly sunny", "mostly_sunny.jpg");
		conditionImageMap_day.put("overcast", "overcast_d.jpg");
		conditionImageMap_day.put("partly cloudy", "partly_cloudy_d.jpg");
		conditionImageMap_day.put("partly clear", "partly_sunny.jpg");
		conditionImageMap_day.put("partly sunny", "partly_sunny.jpg");
		conditionImageMap_day.put("rain", "rain_d.jpg");
		conditionImageMap_day.put("rain & Snow", "rain_snow_d.jpg");
		conditionImageMap_day.put("scattered showers", "scattered_showers_d.jpg");
		conditionImageMap_day.put("scattered thunderstorms", "scattered_tstorms_d.jpg");
		conditionImageMap_day.put("showers", "showers_d.jpg");
		conditionImageMap_day.put("sleet", "sleet_d.jpg");
		conditionImageMap_day.put("smoke", "smoke_d.jpg");
		conditionImageMap_day.put("snow", "snow_d.jpg");
		conditionImageMap_day.put("snow showers", "snow_d.jpg");
		conditionImageMap_day.put("storms", "storms_d.jpg");
		conditionImageMap_day.put("sunny", "sunny.jpg");
		conditionImageMap_day.put("thunderstorm", "tstorm_d.jpg");
		
		conditionImageMap_night.put("n/a", "na.jpg");
		conditionImageMap_night.put("chance of rain", "chance_rain_n.jpg");
		conditionImageMap_night.put("chance of snow", "chance_snow_n.jpg");
		conditionImageMap_night.put("chance of storms", "chance_storm_n.jpg");
		conditionImageMap_night.put("chance of thunderstorms", "chance_tstorm_n.jpg");
		conditionImageMap_night.put("clear", "clear.jpg");
		conditionImageMap_night.put("cloudy", "cloudy_n.jpg");
		conditionImageMap_night.put("dust", "dust_n.jpg");
		conditionImageMap_night.put("flurries", "flurries_n.jpg");
		conditionImageMap_night.put("fog", "fog_n.jpg");
		conditionImageMap_night.put("freezing drizzle", "freezing_drizzle_n.jpg");
		conditionImageMap_night.put("freezing rain", "freezing_drizzle_n.jpg");
		conditionImageMap_night.put("hail", "hail_n.jpg");
		conditionImageMap_night.put("haze", "haze_n.jpg");
		conditionImageMap_night.put("ice", "ice_n.jpg");
		conditionImageMap_night.put("ice and snow", "ice_snow_n.jpg");
		conditionImageMap_night.put("ice/snow", "ice_snow_n.jpg");
		conditionImageMap_night.put("light rain", "light_rain_n.jpg");
		conditionImageMap_night.put("light snow", "light_snow_n.jpg");
		conditionImageMap_night.put("light snow mist", "light_snow_n.jpg");
		conditionImageMap_night.put("mist", "mist_n.jpg");
		conditionImageMap_night.put("mostly cloudy", "mostly_cloudy_n.jpg");
		conditionImageMap_night.put("mostly clear", "mostly_clear.jpg");
		conditionImageMap_night.put("mostly sunny", "mostly_clear.jpg");
		conditionImageMap_night.put("overcast", "overcast_n.jpg");
		conditionImageMap_night.put("partly cloudy", "partly_cloudy_n.jpg");
		conditionImageMap_night.put("partly clear", "partly_clear.jpg");
		conditionImageMap_night.put("partly sunny", "partly_clear.jpg");
		conditionImageMap_night.put("rain", "rain_n.jpg");
		conditionImageMap_night.put("rain & Snow", "rain_snow_n.jpg");
		conditionImageMap_night.put("scattered showers", "scattered_showers_n.jpg");
		conditionImageMap_night.put("scattered thunderstorms", "scattered_tstorms_n.jpg");
		conditionImageMap_night.put("showers", "showers_n.jpg");
		conditionImageMap_night.put("sleet", "sleet_n.jpg");
		conditionImageMap_night.put("smoke", "smoke_n.jpg");
		conditionImageMap_night.put("snow", "snow_n.jpg");
		conditionImageMap_night.put("snow showers", "snow_n.jpg");
		conditionImageMap_night.put("storms", "storms_n.jpg");
		conditionImageMap_night.put("sunny", "clear.jpg");
		conditionImageMap_night.put("thunderstorm", "tstorm_n.jpg");
	}
	
	public void setContext(Context cntxt) {
		this.context = cntxt;
		if (image == null) {
			image = BitmapFactory.decodeResource(context.getResources(), R.drawable.na);
		}
	}
	
	public void updateWeatherData() {
		//completedLastUpdate = true;
		checkWeather();
		updateTimes();
		updateImage();
	}
	
	public String getCurrentCondition() {
		return currentCondition;
	}
	
	public String getCurrentTemp() {
		return currentTemp;
	}
	
	public String getForecastHigh() {
		return forecastHigh;
	}
	
	public String getForecastLow() {
		return forecastLow;
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public Date[] getSunTimes() {
		return sunTimes;
	}
	public Date getCurrentTime() {
		return currentTime;
	}
	
	private void checkWeather() {
		SharedPreferences prefs = context.getSharedPreferences(LiveWallpaperService.PREFERENCES, 0);
		location = prefs.getString(context.getString(R.string.location_key), "Location not set");
		Log.e("LOCATION", location);
		URL url;
		String queryString = "http://www.google.com/ig/api?weather=" + location;
		
		try {
			if (location.equalsIgnoreCase("Location not set")) {
				currentCondition = "na";
				throw new IllegalArgumentException();
			}
			/* Replace blanks with HTML-Equivalent. */ 
			url = new URL(queryString.replace(" ", "%20"));
			
			/* Get a SAXParser from the SAXPArserFactory. */ 
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			
			/* Get the XMLReader of the SAXParser we created. */ 
			XMLReader xr = sp.getXMLReader();
			
			/* Create a new ContentHandler and apply it to the XML-Reader*/ 
			GoogleWeatherHandler gwh = new GoogleWeatherHandler();
			xr.setContentHandler(gwh);
			
			/* Parse the xml-data our URL-call returned. */ 
			xr.parse(new InputSource(url.openStream()));

			/* Our Handler now provides the parsed weather-data to us. */ 
			WeatherSet ws = gwh.getWeatherSet();
			
			/* Update the Info with the parsed data. */
			currentCondition = ws.getWeatherCurrentCondition().getCondition();
			currentTemp = String.valueOf(ws.getWeatherCurrentCondition().getTempFahrenheit()) + "\u00b0";
			forecastHigh = String.valueOf(WeatherUtils.celsiusToFahrenheit(ws.getWeatherForecastConditions().get(0).getTempMaxCelsius())) + "\u00b0";
			forecastLow = String.valueOf(WeatherUtils.celsiusToFahrenheit(ws.getWeatherForecastConditions().get(0).getTempMinCelsius())) + "\u00b0";
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			//completedLastUpdate = false;
			LiveWallpaperService.needUpdate = false;
			Intent intent = new Intent(context, LiveWallpaperPreferences.class);
			showNotification("Set your location to get weather information.", intent);
		} catch (SocketException e) {
			e.printStackTrace();
			//completedLastUpdate = false;
			LiveWallpaperService.needUpdate = true;
			Intent intent = new Intent(context, LiveWallpaperPreferences.class);
			showNotification("Connection failed. Please try again later.", intent);
		} catch (Exception e) {
			e.printStackTrace();
			//completedLastUpdate = false;
			LiveWallpaperService.needUpdate = true;
			Intent intent = new Intent(context, LiveWallpaperPreferences.class);
			showNotification("The location you entered was not found, please try again.", intent);
		}
	}
	
	public void updateImage() {
		try { 
			// Sunrise > current < sunset (DAY)
			Log.e("HERE", currentTime.toLocaleString());
	    	long currentLong = currentTime.getTime();
	    	long sunriseLong = sunTimes[0].getTime();
	    	long sunsetLong = sunTimes[1].getTime();
	    	if (!currentCondition.equalsIgnoreCase("na")) {
				if ((currentLong > sunriseLong) && (currentLong < sunsetLong)) {
					if (conditionImageMap_day.containsKey(currentCondition.toLowerCase())) {
						image = downloadImage(conditionImageMap_day.get(currentCondition.toLowerCase()));
					} else
						throw new FileNotFoundException();
				} else {
					if (conditionImageMap_night.containsKey(currentCondition.toLowerCase())) {
						image = downloadImage(conditionImageMap_night.get(currentCondition.toLowerCase()));
					} else
						throw new FileNotFoundException();
				}
	    	} else {
	    		image = BitmapFactory.decodeResource(context.getResources(), R.drawable.na);
	    	}
			Intent paintingIntent = new Intent(LiveWallpaperPainting.PaintingReceiver.ACTION_WALLPAPER_PAINT);
			context.sendBroadcast(paintingIntent);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"android_bugs@spiderflystudios.com"});
            emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Live Weather Wallpaper Bug Report");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, 
            		"Could not download image for: " + currentCondition + "\n" +
            		"Location: " + location + "\n" + 
            		"Time: " + currentTime);
			showNotification("An image for the current weather condition was not found.  Click to notify the developer.", emailIntent);
		} 
	}
	
	public void updateTimes() {
		try {
			SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(context, location);
			sunTimes = calculator.timeOfDay();
			currentTime =  Calendar.getInstance().getTime();
			/*int dst = 0;
			if (calculator.timeZone.inDaylightTime(currentTime)) {
				dst = 1;
			}*/
			userOffset = currentTime.getTimezoneOffset() / 60; // hours
			locationOffset = ((calculator.timeZone.getRawOffset()/1000)/60)/60; // hours
			int netOffset = (userOffset + locationOffset);
			Log.e("HERE", userOffset + " " + locationOffset + " " + netOffset);
			int day = currentTime.getDate();
			currentTime = new Date(currentTime.getTime() + (netOffset * 60 * 60 * 1000));
			currentTime.setDate(day);
		} catch (Exception e) {
			LiveWallpaperService.needUpdate = true;
			Intent intent = new Intent(LiveWallpaperReceiver.ACTION_WALLPAPER_RETRY);
			context.sendBroadcast(intent);
		}
	}
	
	private Bitmap downloadImage(String fileName) {
		if (fileName != null && fileName.length() > 0) {
			try {
				URL url = new URL(imageURL + fileName); 
	
				long startTime = System.currentTimeMillis();
				Log.d(LiveWallpaperService.DEBUG_TAG, "Downloading file name:" + fileName);
				// Open a connection to that URL. 
				URLConnection ucon = url.openConnection();
	
				// Define InputStreams to read from the URLConnection.
				InputStream is = ucon.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(is);
	
				// Read bytes to the Buffer until there is nothing more to read(-1).
				ByteArrayBuffer baf = new ByteArrayBuffer(50);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				Log.d(LiveWallpaperService.DEBUG_TAG, "Download finished in"
						+ ((System.currentTimeMillis() - startTime) / 1000)
						+ " sec");
	
				Bitmap downloadedImage = BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.length());
				if (downloadedImage == null) {
					LiveWallpaperService.needUpdate = true;
					throw new Exception("Image was null");
				}
				return BitmapFactory.decodeByteArray(baf.toByteArray(), 0, baf.length());
			} catch (Exception e) {
				e.printStackTrace();
				//completedLastUpdate = false;
				LiveWallpaperService.needUpdate = true;
				Intent intent = new Intent(LiveWallpaperReceiver.ACTION_WALLPAPER_RETRY);
				context.sendBroadcast(intent);
				return BitmapFactory.decodeResource(context.getResources(), R.drawable.na);
			}
		} else {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.na);
		}
	}
	
	public void showNotification(String text, Intent intent) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.na_notification;
		CharSequence contentTitle = "Live Weather Wallpaper";
		long when = System.currentTimeMillis();

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		Notification notification = new Notification(icon,text,when);
		notification.setLatestEventInfo(context, contentTitle, text, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(LiveWallpaperPreferences.NOTIFICATION_ID, notification);
	}
}
