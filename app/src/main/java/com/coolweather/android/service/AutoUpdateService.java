package com.coolweather.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;


import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;//8个小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /*更新天气信息
     * */
    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;

            String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                    //此处要加入获得的key bc0418b57b2d4918819d3974ac1285d9
                    weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    //由于账号秘钥问题，此处改为固定的信息
                    responseText = "{\"HeWeather\": [{\"basic\":{\"cid\":\"CN101230804\",\"location\":\"泰宁\",\"parent_city\":\"三明\",\"admin_area\":\"福建\",\"cnty\":\"中国\",\"lat\":\"26.897995\",\"lon\":\"117.17752075\",\"tz\":\"+8.00\",\"city\":\"泰宁\",\"id\":\"CN101230804\",\"update\":{\"loc\":\"2019-02-07 11:56\",\"utc\":\"2019-02-07 03:56\"}},\"update\":{\"loc\":\"2019-02-07 11:56\",\"utc\":\"2019-02-07 03:56\"},\"status\":\"ok\",\"now\":{\"cloud\":\"91\",\"cond_code\":\"305\",\"cond_txt\":\"小雨\",\"fl\":\"16\",\"hum\":\"99\",\"pcpn\":\"0.0\",\"pres\":\"1014\",\"tmp\":\"15\",\"vis\":\"8\",\"wind_deg\":\"339\",\"wind_dir\":\"西北风\",\"wind_sc\":\"1\",\"wind_spd\":\"4\",\"cond\":{\"code\":\"305\",\"txt\":\"小雨\"}},\"daily_forecast\":[{\"date\":\"2019-02-07\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"21\",\"min\":\"9\"}},{\"date\":\"2019-02-08\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"14\",\"min\":\"8\"}},{\"date\":\"2019-02-09\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"10\",\"min\":\"7\"}}],\"aqi\":{\"city\":{\"aqi\":\"55\",\"pm25\":\"29\",\"qlty\":\"良\"}},\"suggestion\":{\"comf\":{\"type\":\"comf\",\"brf\":\"舒适\",\"txt\":\"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。\"},\"sport\":{\"type\":\"sport\",\"brf\":\"较不宜\",\"txt\":\"有降水，推荐您在室内进行健身休闲运动；若坚持户外运动，须注意携带雨具并注意避雨防滑。\"},\"cw\":{\"type\":\"cw\",\"brf\":\"不宜\",\"txt\":\"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。\"}}}]}\n";

                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if (weather != null && "ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }

                }
            });

        }
    }

    /*更新必应每日一图
     * */
    private void updateBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
