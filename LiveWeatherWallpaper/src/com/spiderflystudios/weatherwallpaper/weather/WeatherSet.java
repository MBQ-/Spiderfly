package com.spiderflystudios.weatherwallpaper.weather;

import java.util.ArrayList;


/**
 * Combines one WeatherCurrentCondition with a List of
 * WeatherForecastConditions.
 */
public class WeatherSet {
       
        // ===========================================================
        // Fields
        // ===========================================================
 
		private String currentDateTime = null;
        private WeatherCurrentCondition myCurrentCondition = null;
        private ArrayList<WeatherForecastCondition> myForecastConditions =
                new ArrayList<WeatherForecastCondition>(4);
 
        // ===========================================================
        // Getter & Setter
        // ===========================================================
 
        public WeatherCurrentCondition getWeatherCurrentCondition() {
                return myCurrentCondition;
        }
 
        public void setWeatherCurrentCondition(
                        WeatherCurrentCondition myCurrentWeather) {
                this.myCurrentCondition = myCurrentWeather;
        }
 
        public ArrayList<WeatherForecastCondition> getWeatherForecastConditions() {
                return this.myForecastConditions;
        }
 
        public WeatherForecastCondition getLastWeatherForecastCondition() {
                return this.myForecastConditions
                                .get(this.myForecastConditions.size() - 1);
        }
        
        public String getCurrentDateTime() {
        	return this.currentDateTime;
        }
        
        public void setCurrentDateTime(String dateTime) {
        	this.currentDateTime = dateTime;
        }
}
