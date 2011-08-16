package com.spiderflystudios.weatherwallpaper;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.geonames.WebService;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import uk.me.jstott.coordconv.LatitudeLongitude;
import uk.me.jstott.sun.*;
import uk.me.jstott.util.JulianDateConverter;

public class SunriseSunsetCalculator {
	
	public TimeZone timeZone;
	public Locale locale;
	private Context contxt;
	private String loc;
	
	public SunriseSunsetCalculator(Context context, String location) {
		WebService.setUserName("azvargas87");
		contxt = context;
		loc = location;
	}
	
	public Date[] timeOfDay() {
		Date[] sunTimes = new Date[2];
		try {
			Address address = getAddressFromString();
			if(address != null){
				LatitudeLongitude ll = new LatitudeLongitude(address.getLatitude(), address.getLongitude());
				timeZone = TimeZone.getTimeZone(WebService.timezone(address.getLatitude(), address.getLongitude()).getTimezoneId());
				locale = address.getLocale();
				Calendar cal = Calendar.getInstance(timeZone);
				double julian = JulianDateConverter.dateToJulian(cal);
				boolean dst = false;
				Time sunrise = Sun.sunriseTime(julian, ll, timeZone, dst);
				Time sunset = Sun.sunsetTime(julian, ll, timeZone, dst);
				sunTimes[0] = new Date();
				sunTimes[0].setHours(sunrise.getHours());
				sunTimes[0].setMinutes(sunrise.getMinutes());
				sunTimes[0].setSeconds((int) sunrise.getSeconds());
				sunTimes[1] = new Date();
				sunTimes[1].setHours(sunset.getHours());
				sunTimes[1].setMinutes(sunset.getMinutes());
				sunTimes[1].setSeconds((int) sunset.getSeconds());
			} else {
				throw new Exception("Address from geolocation is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			sunTimes[0] = new Date();
			sunTimes[0].setHours(5);
			sunTimes[1] = new Date();
			sunTimes[1].setHours(18);
		}
		Log.w("SUNTIME", sunTimes[0].toString() + " - " + sunTimes[1].toString());
		return sunTimes;
	}
	
	private Address getAddressFromString() throws Exception{
		Geocoder geo = new Geocoder(contxt);
		List<Address> addressList = geo.getFromLocationName(loc, 1);
		if (addressList.size() < 1) {
			Log.w(LiveWallpaperService.DEBUG_TAG, "Geocoding error trying again...");
			Thread.sleep(1000);
			geo = new Geocoder(contxt);
			addressList = geo.getFromLocationName(loc, 1);
		}
		return addressList.get(0);
	}
	
}
