package com.spiderflystudios.weatherwallpaper;

import java.util.Calendar;
import java.util.Date;

import com.spiderflystudios.weatherwallpaper.LiveWallpaperPainting.CanvasType;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaperService extends WallpaperService implements Runnable{
	
	public static final String DEBUG_TAG = "WeatherWallpaperService";
	public static final String PREFERENCES = "com.spiderflystudios.weatherwallpaper";
	public static final String ACTION_UPDATE = "com.spiderflystudios.weatherwallpaper.update";
	public final String SYSTEM_TIME = "LiveWallpaperService.SYSTEM_TIME";
	public final String TIME_LEFT = "LiveWallpaperService.TIME_LEFT";
	public static Date lastUpdate = null;
	public static boolean updateRunning = false;
	public static boolean needUpdate = false;
	
	private static LiveWallpaperService mThis;
	private static int numRetries = 0;
	private NotificationManager notificationManager;
	public static long updateFreq;
	
	private Runnable checkStatus = new Runnable() {
		public void run() {
			Log.d(DEBUG_TAG, "Running checkStatus...");
			Date now = new Date();
			if ( (lastUpdate != null) &&
					((now.getTime()-lastUpdate.getTime()) > updateFreq) ) {
				needUpdate = true;
			}
			if (needUpdate || !(WeatherAdapter.getInstance().getCurrentCondition().length() > 2)) {
				mThis.run();
				needUpdate = false;
			}
		}
	};
	
	@Override
    public void onCreate() {
        super.onCreate();
        mThis = this;
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        updateFreq = Long.parseLong(getSharedPreferences(PREFERENCES, 0).getString(getApplicationContext().getString(R.string.frequency_key), "3600000"));
        WeatherAdapter.getInstance().setContext(getApplicationContext());
        new Thread(this).start();
    }
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }
	
	@Override
	public Engine onCreateEngine() {
		return new WallpaperEngine();
	}
	
	@Override
	public void run() {
		if (!updateRunning) {
			Log.d(DEBUG_TAG, "Running update...");
			updateRunning = true;
			if (isOnline()) {
				notificationManager.cancel(LiveWallpaperPreferences.NOTIFICATION_ID);
				WeatherAdapter.getInstance().updateWeatherData(); 
				setNextUpdate(updateFreq);
				numRetries = 0;
			} else {
				setNextUpdate(30000); // 30 seconds 
			}
			lastUpdate = Calendar.getInstance().getTime();
		} else {
			Log.d(DEBUG_TAG, "Update already running");
		}
		updateRunning = false;
	}
	
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    } 
    
    public static void setNextUpdate(long delay) {
    	WeatherAdapter wa = WeatherAdapter.getInstance();
    	if (!wa.getCurrentCondition().equalsIgnoreCase("na") && (wa.getSunTimes()!= null)) {
	    	Date[] sunTimes = wa.getSunTimes();
	    	Date sunrise = new Date(sunTimes[0].getTime() - (wa.locationOffset * 60 * 60 * 1000) - (wa.userOffset * 60 * 60 * 1000));
	    	Date sunset = new Date(sunTimes[1].getTime() - (wa.locationOffset * 60 * 60 * 1000) - (wa.userOffset * 60 * 60 * 1000));
	    	long nextUpdate = Calendar.getInstance().getTime().getTime() + delay;
	    	long nextSunrise = sunrise.getTime();
	    	long nextSunset = sunset.getTime();
	    	if (nextSunrise < nextUpdate && (nextSunrise > (nextUpdate-delay))) {
				nextUpdate = nextSunrise;
			} else if (nextSunset < nextUpdate && (nextSunset > (nextUpdate-delay))) {
				nextUpdate = nextSunset;
			}
	    	Log.w("NextUpdate", new Date(nextUpdate).toLocaleString());
			Intent intent = new Intent(LiveWallpaperReceiver.ACTION_WALLPAPER_UPDATE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mThis, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager)mThis.getSystemService(Context.ALARM_SERVICE);
			alarmManager.set(AlarmManager.RTC_WAKEUP, nextUpdate, pendingIntent);
    	} else {
    		needUpdate = true;
    	}
	}
	
	class WallpaperEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener{
		
		private LiveWallpaperPainting enginePainting;
		
        WallpaperEngine() {
        	mThis.getApplicationContext().getSharedPreferences(PREFERENCES, 0).registerOnSharedPreferenceChangeListener(this);
        }
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            enginePainting.stopPainting();
            System.gc();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            enginePainting = new LiveWallpaperPainting(holder, getApplicationContext());
            enginePainting.start();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
        	super.onSurfaceDestroyed(holder);
            enginePainting.stopPainting();
            boolean retry = true;
            while (retry) {
                try {
                	enginePainting.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
        }
        
        @Override
        public void onVisibilityChanged(boolean visible) {
            setTouchEventsEnabled(visible);
        	if (visible) {
                enginePainting.resumePainting();
                new Thread(checkStatus).start();
            } else {
                enginePainting.pausePainting();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (isPreview()) {
            	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    				enginePainting.setSurfaceSize(width, height, CanvasType.PREVIEW_LAND);
    			} else {
    				enginePainting.setSurfaceSize(width, height, CanvasType.PREVIEW);
    			}
			} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				enginePainting.setSurfaceSize(width, height, CanvasType.LANDSCAPE);
			} else {
				enginePainting.setSurfaceSize(width, height, CanvasType.PORTRAIT);
			}
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
            enginePainting.doOffsetChange(xOffset, yOffset);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (isPreview() && event.getAction() == MotionEvent.ACTION_UP) {
            	enginePainting.numTaps++;
            	enginePainting.doTouchEvent();
			}
        }
        
        @Override
        public Bundle onCommand(String action, int x, int y, int z, Bundle extras, boolean resultRequested) {
            if (action.equals(WallpaperManager.COMMAND_TAP)) {
            	enginePainting.numTaps++;
            	enginePainting.doTouchEvent();
            	Log.w("CommandTap", "Point: " + x + ", " + y + ", " + z + " taps: " + enginePainting.numTaps);
            } 
            return null;
        }
        
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			updateFreq = Long.parseLong(prefs.getString(getApplicationContext().getString(R.string.frequency_key), "3600000"));
			setNextUpdate(1000);
        	enginePainting.updateTouchPref();
        	enginePainting.resumePainting();
        }
    }
	
	public static class LiveWallpaperReceiver extends BroadcastReceiver{
		
		public static final String ACTION_WALLPAPER_UPDATE = "com.spiderflystudios.weatherwall.ACTION_WALLPAPER_UPDATE";
		public static final String ACTION_WALLPAPER_RETRY = "com.spiderflystudios.weatherwall.ACTION_WALLPAPER_RETRY";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_WALLPAPER_UPDATE) && !updateRunning) {
				new Thread(mThis).start();
			} else {
				Log.e(DEBUG_TAG, "Failed attempt: " + numRetries+1);
				numRetries++;
				LiveWallpaperService.needUpdate = true;
				LiveWallpaperService.setNextUpdate(60000);
			} 
		}

	}
}
