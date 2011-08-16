package com.spiderflystudios.weatherwallpaper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

public class LiveWallpaperPainting extends Thread {
	
	public static enum CanvasType {PORTRAIT, LANDSCAPE, PREVIEW, PREVIEW_LAND};
	private static LiveWallpaperPainting mThis;
	private static boolean wait;
	
    private SurfaceHolder surfaceHolder;
    private Context context;
    private Paint paint;
 
    private WallpaperTimer mTouchTimer = new WallpaperTimer(null, 1000, 100);
    private CanvasType canvasType = CanvasType.PORTRAIT;
    
    public int numTaps = 0;
    private boolean run;
    private int width;
    private int height;
	private int touchPref;
	private float centerX;
	private float centerY;
	private float offset;
	
    public LiveWallpaperPainting(SurfaceHolder surfaceHolder, Context context) {
    	this.mThis = this;
        this.surfaceHolder = surfaceHolder;
        this.context = context;
        this.wait = true;
        this.paint = new Paint();
        updateTouchPref();
    }
    
    public boolean isRunning() {
    	return run;
    }
    
	public void updateTouchPref() {
		SharedPreferences prefs = context.getSharedPreferences(LiveWallpaperService.PREFERENCES, 0);
		touchPref = Integer.parseInt(prefs.getString(context.getString(R.string.touch_key), "0"));
	}
    
    /**
     * Pauses the live wallpaper animation
     */
    public void pausePainting() {
    	System.gc();
        this.wait = true;
        synchronized (this) {
			notify();
		}
    }
 
    /**
     * Resume the live wallpaper animation
     */
    public void resumePainting() {
        this.wait = false;
        synchronized (this) {
			notify();
		}
    }
 
    /**
     * Stop the live wallpaper animation
     */
    public void stopPainting() {
    	System.gc();
        this.run = false;
        synchronized (this) {
			notify();
		}
    }
    
    /**
     * Invoke when the surface dimension change
     */
    public void setSurfaceSize(int width, int height, CanvasType canvasType) {
    	this.canvasType = canvasType;
        this.width = width;
        this.height = height;
        centerX = width/2.0f;
        centerY = height/2.0f;
        synchronized (this) {
			notify();
		}
    }
    
    /**
     * Invoke while the screen is touched
     */
    public void doTouchEvent() {
		if (numTaps < 2) {
			mTouchTimer.start();
		} else {
			if (touchPref == 1 && mTouchTimer.isRunning) {
				showWeatherDisplay();
				mTouchTimer.onFinish();
			}
			numTaps = 0;
		}
        // if there is something to animate
        // then wake up
        this.wait = false;
        synchronized (this) {
			notify();
		}
    }
    
    /**
     * Invoke when offset changed
     */
    public void doOffsetChange(float xOffset, float yOffset) {
    	offset = xOffset;
    	this.wait = false;
    	synchronized (this) {
			notify();
		}
    }
 
    @Override
    public void run() {
        this.run = true;
        Canvas c = null;
        while (run) {
            try {
                c = this.surfaceHolder.lockCanvas();
                synchronized (this.surfaceHolder) {
                    doDraw(c);
                }
            } finally {
                if (c != null) {
                    this.surfaceHolder.unlockCanvasAndPost(c);
                }
            }
            // pause if no need to animate
            synchronized (this) {
                if (wait) {
                    try {
                        wait();
                    } catch (Exception e) {}
                }
            }
        }
    }
 
    /**
     * Do the actual drawing stuff
     */
    private void doDraw(Canvas c) {
    	Bitmap bitmap = WeatherAdapter.getInstance().getImage();
        if (c != null && bitmap != null) {
            // draw something
        	float transX = 0;
        	float transY = 0;
        	float scaleX = 1;
        	float scaleY = 1;
        	
            paint.setAntiAlias(true);
            paint.setDither(true);
            
            if (canvasType == CanvasType.PORTRAIT) {
                scaleY = (float) height / (float)bitmap.getHeight();
                scaleX = scaleY;
                transX = ((0.5f - offset) * 2.0f) * 100 + (-centerX*scaleY);
                transY = 0;
			} else if (canvasType == CanvasType.LANDSCAPE) {
//				scaleX = (float) width / (float)image.getWidth();
//				scaleY = scaleX;
				transX = (c.getWidth() - bitmap.getWidth())/2;
				transY = 0;
			} else if (canvasType == CanvasType.PREVIEW_LAND) {
//				scaleX = (float) width / (float)image.getWidth();
//				scaleY = scaleX;
				transX = (c.getWidth() - bitmap.getWidth())/2;
				transY = 0;
			} else if (canvasType == CanvasType.PREVIEW){
				scaleY = (float) height / (float)bitmap.getHeight();
                scaleX = scaleY;
				transX = -centerX*scaleX;
				transY = 0;
			}
            
        	c.scale(scaleX, scaleY);
			c.translate(transX, transY);
        	c.drawBitmap(bitmap, 0, 0, paint);
        } 
        else {
        	Log.w(LiveWallpaperService.DEBUG_TAG, "Drawing error: canvas=" + c + ", image=" + bitmap);
		}
        this.wait = true;
    }
    
    private void showWeatherDisplay() {
    	if (this.run) {
    		if (WeatherAdapter.getInstance().getCurrentCondition().length() <= 2) {
    			LiveWallpaperService.needUpdate = true;
    		}
    		Toast.makeText(context, WeatherAdapter.getInstance().getCurrentCondition() + "     " + WeatherAdapter.getInstance().getCurrentTemp(), Toast.LENGTH_LONG).show();
		}
    }
    
    public static void onBroadcastReceived() {
    	mThis.wait = false;
    	synchronized (mThis) {
    		mThis.notify();
		}
    }
    
    public class WallpaperTimer extends CountDownTimer{

    	public boolean isRunning = false;
    	public long timeLeft = 0;
    	public Runnable runnable;
    	
    	public WallpaperTimer(Runnable runnable, long millisInFuture, long countDownInterval) {
    		super(millisInFuture, countDownInterval);
    		this.runnable = runnable;
    	}

    	@Override
    	public void onFinish() {
    		isRunning = false;
    		if (runnable != null) {
    			new Thread(runnable).start();
    		}
    		numTaps = 0;
    	}

    	@Override
    	public void onTick(long millisUntilFinished) {
    		isRunning = true;
    		timeLeft = millisUntilFinished;
    	}

    }
    
    public static class PaintingReceiver extends BroadcastReceiver{
		
		public static final String ACTION_WALLPAPER_PAINT = "com.spiderflystudios.weatherwall.ACTION_WALLPAPER_PAINT";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			onBroadcastReceived();
		}

	}
}
