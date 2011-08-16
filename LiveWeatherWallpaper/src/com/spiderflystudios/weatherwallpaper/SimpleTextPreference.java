package com.spiderflystudios.weatherwallpaper;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SimpleTextPreference extends Preference{

	private TextView monitorBox;
	
	public SimpleTextPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public SimpleTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SimpleTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		LinearLayout layout = new LinearLayout(getContext());

		LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params1.gravity = Gravity.LEFT;
		params1.weight = 1.0f;

		layout.setPadding(15, 5, 10, 5);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView view = new TextView(getContext());
		view.setText(getTitle());
		view.setTextSize(18);
		view.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		view.setGravity(Gravity.LEFT);
		view.setLayoutParams(params1);

		this.monitorBox = new TextView(getContext());
		this.monitorBox.setTextSize(12);
		this.monitorBox.setLayoutParams(params1);
		this.monitorBox.setPadding(2, 5, 0, 0);
		if (LiveWallpaperService.lastUpdate != null) {
			this.monitorBox.setText(LiveWallpaperService.lastUpdate.toLocaleString());
		}

		layout.addView(view);
		layout.addView(this.monitorBox);
		layout.setId(android.R.id.widget_frame);

		return layout;
	}
}
