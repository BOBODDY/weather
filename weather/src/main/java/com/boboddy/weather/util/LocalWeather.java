package com.boboddy.weather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class LocalWeather extends WwoApi {
    public static final String FREE_API_ENDPOINT = "http://api.worldweatheronline.com/free/v1/weather.ashx";

    public LocalWeather() {
        super();
        apiEndPoint = FREE_API_ENDPOINT;
    }

    public Data callAPI(String query) {
        return getLocalWeatherData(getInputStream(apiEndPoint + query));
    }

    public Data getLocalWeatherData(InputStream is) {
        Data weather = null;

        try {
            Log.d("WWO", "getLocalWeatherData");

            XmlPullParser xpp = getXmlPullParser(is);

            weather = new Data();
            CurrentCondition cc = new CurrentCondition();
            weather.current_condition = cc;

            cc.temp_C = getTextForTag(xpp, "temp_C");
            cc.weatherIconUrl = getDecode(getTextForTag(xpp, "weatherIconUrl"));
            cc.weatherDesc = getDecode(getTextForTag(xpp, "weatherDesc"));

            Log.d("WWO", "getLocalWeatherData:"+cc.temp_C);
            Log.d("WWO", "getLocalWeatherData:"+cc.weatherIconUrl);
            Log.d("WWO", "getLocalWeatherData:"+cc.weatherDesc);
        } catch (Exception e) {

        }

        return weather;
    }

    public class Params extends RootParams {
        String q;					//required
        String extra;
        String num_of_days="1";		//required
        String date;
        String fx="no";
        String cc;					//default "yes"
        String includeLocation;		//default "no"
        String format;				//default "xml"
        String show_comments="no";
        String callback;
        String key;					//required

        public Params(String key) {
            num_of_days = "1";
            fx = "no";
            show_comments = "no";
            this.key = key;
        }

        public Params setQ(String q) {
            this.q = q;
            return this;
        }

        public Params setExtra(String extra) {
            this.extra = extra;
            return this;
        }

        public Params setNumOfDays(String num_of_days) {
            this.num_of_days = num_of_days;
            return this;
        }

        public Params setDate(String date) {
            this.date = date;
            return this;
        }

        public Params setFx(String fx) {
            this.fx = fx;
            return this;
        }

        public Params setCc(String cc) {
            this.cc = cc;
            return this;
        }

        public Params setIncludeLocation(String includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }

        public Params setFormat(String format) {
            this.format = format;
            return this;
        }

        public Params setShowComments(String showComments) {
            this.show_comments = showComments;
            return this;
        }

        public Params setCallback(String callback) {
            this.callback = callback;
            return this;
        }

        public Params setKey(String key) {
            this.key = key;
            return this;
        }
    }

    public class Data {
        Request request;
        CurrentCondition current_condition;
        Weather weather;

        public CurrentCondition getCurrentCondition() {
            return this.current_condition;
        }

        public Weather getWeather() {
            return this.weather;
        }

        public Request getRequest() {
            return this.request;
        }
    }

    public class Request {
        String type;
        String query;
    }

    public class CurrentCondition {
        String observation_time;

        public String getTemp_C() {
            return temp_C;
        }

        String temp_C;
        String weatherCode;
        String weatherIconUrl;
        String weatherDesc;
        String windspeedMiles;
        String windspeedKmph;
        String winddirDegree;
        String winddir16Point;
        String precipMM;
        String humidity;
        String visibility;
        String pressure;
        String cloudcover;
    }

    public class Weather {
        String date;
        String tempMaxC;
        String tempMaxF;
        String tempMinC;
        String tempMinF;
        String windspeedMiles;
        String windspeedKmph;
        String winddirection;
        String weatherCode;
        String weatherIconUrl;
        String weatherDesc;
        String precipMM;

        public String getDate() {
            return date;
        }

        public String getTempMaxC() {
            return tempMaxC;
        }

        public String getTempMaxF() {
            return tempMaxF;
        }

        public String getTempMinC() {
            return tempMinC;
        }

        public String getTempMinF() {
            return tempMinF;
        }

        public String getWindspeedMiles() {
            return windspeedMiles;
        }

        public String getWindspeedKmph() {
            return windspeedKmph;
        }

        public String getWinddirection() {
            return winddirection;
        }

        public String getWeatherCode() {
            return weatherCode;
        }

        public String getWeatherIconUrl() {
            return weatherIconUrl;
        }

        public String getWeatherDesc() {
            return weatherDesc;
        }

        public String getPrecipMM() {
            return precipMM;
        }
    }
}
