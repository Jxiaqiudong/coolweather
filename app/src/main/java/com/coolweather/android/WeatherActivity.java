package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        //Android5.0及以上系统才支持，即版本号大于等于21
        if(Build.VERSION.SDK_INT>=21){
            //调用getWindow().getDecorView()拿到当前活动的DecorView
            View decorView = getWindow().getDecorView();
            //改变系统的UI显示，传入的两个值表示活动的布局会显示在状态栏上面
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明色
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        //初始化控件
        weatherLayout = (ScrollView)findViewById(R.id.weahter_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if(weatherString != null){
            //若有缓存直接解析天气数据
            Log.d(TAG, "WeatherActivity : 开始直接解析数据 ");
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{
            //无缓存时去服务器查询天气
            Log.d(TAG, "WeatherActivity : 去服务器查询数据");
            String weatherId = getIntent().getStringExtra("weather_id");
            Log.d(TAG, "weather_id is " + weatherId);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            Log.d(TAG, "服务器查询天气数据结束");
        }

        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic !=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

    }

    /*
     * 根据天气的Id向服务器请求天气信息*/
    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+
                //此处要加入获得的key bc0418b57b2d4918819d3974ac1285d9
                weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable(){
                    public void run(){
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败1",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //final String responseText = response.body().string();
                //由于账号秘钥问题，此处改为固定的信息
                final String responseText = "{\"HeWeather\": [{\"basic\":{\"cid\":\"CN101230804\",\"location\":\"泰宁\",\"parent_city\":\"三明\",\"admin_area\":\"福建\",\"cnty\":\"中国\",\"lat\":\"26.897995\",\"lon\":\"117.17752075\",\"tz\":\"+8.00\",\"city\":\"泰宁\",\"id\":\"CN101230804\",\"update\":{\"loc\":\"2019-02-07 11:56\",\"utc\":\"2019-02-07 03:56\"}},\"update\":{\"loc\":\"2019-02-07 11:56\",\"utc\":\"2019-02-07 03:56\"},\"status\":\"ok\",\"now\":{\"cloud\":\"91\",\"cond_code\":\"305\",\"cond_txt\":\"小雨\",\"fl\":\"16\",\"hum\":\"99\",\"pcpn\":\"0.0\",\"pres\":\"1014\",\"tmp\":\"15\",\"vis\":\"8\",\"wind_deg\":\"339\",\"wind_dir\":\"西北风\",\"wind_sc\":\"1\",\"wind_spd\":\"4\",\"cond\":{\"code\":\"305\",\"txt\":\"小雨\"}},\"daily_forecast\":[{\"date\":\"2019-02-07\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"21\",\"min\":\"9\"}},{\"date\":\"2019-02-08\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"14\",\"min\":\"8\"}},{\"date\":\"2019-02-09\",\"cond\":{\"txt_d\":\"小雨\"},\"tmp\":{\"max\":\"10\",\"min\":\"7\"}}],\"aqi\":{\"city\":{\"aqi\":\"55\",\"pm25\":\"29\",\"qlty\":\"良\"}},\"suggestion\":{\"comf\":{\"type\":\"comf\",\"brf\":\"舒适\",\"txt\":\"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。\"},\"sport\":{\"type\":\"sport\",\"brf\":\"较不宜\",\"txt\":\"有降水，推荐您在室内进行健身休闲运动；若坚持户外运动，须注意携带雨具并注意避雨防滑。\"},\"cw\":{\"type\":\"cw\",\"brf\":\"不宜\",\"txt\":\"不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。\"}}}]}\n";
                Log.d(TAG, responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather !=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败2",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        loadBingPic();
    }

    /*处理并展示Weather实体类中的数据
     * */
    private void showWeatherInfo(Weather weather){
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carwash = "洗车指数："+weather.suggestion.carWash.info;
        String sport = "运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

    /*
     * 加载必应图片，每日一图*/
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });
    }

}
