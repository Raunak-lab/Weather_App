package com.hfad.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.hfad.weatherapp.WeatherRVAdapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.Manifest;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout homeRl;
    private ProgressBar loadingPB;
    private TextView cityNameTV,temperatureTV,conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdit;
    private ImageView backIV,iconIV,searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;
    public static void main(String[] args) {
        FileReader fileReader = null;
        FileWriter fileWriter = null;
        try {
            // Perform file reading operations
            fileReader = new FileReader("input.txt");
            // Perform file writing operations
            fileWriter = new FileWriter("output.txt");
            // ...
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRl = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdit = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);

        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null){cityName = getCityName(location.getLongitude(),location.getLatitude());
            getWeatherInfo(cityName);
        } else {
            cityName = "London";
            getWeatherInfo(cityName);
        }


        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityEdit.getText().toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
                }
                else{
                    cityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please Provide The Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude)
    {
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude,longitude,10);
            for(Address adr:addresses)
            {
                if(adr!=null)
                {
                    String city = adr.getLocality();
                    if(city!=null&&!city.equals("")){
                        cityName = city;

                    }
                    else{
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found..", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        return cityName;
    }
    private void getWeatherInfo(String cityName)
    {
      String url = "http://api.weatherapi.com/v1/forecast.json?key=c49040ad90c849a1a0643910230704&q="+cityName+"&days=1&aqi=yes&alerts=yes";
      cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRl.setVisibility(View.VISIBLE);

                weatherRVModalArrayList.clear();
                try{
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    temperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    conditionTV.setText(condition);
                    if(isDay==1)
                    {   //morning
                        Picasso.get().load("https://previews.123rf.com/images/sarapon/sarapon1803/sarapon180300003/97285057-portrait-of-a-blue-sky-and-white-cloud-in-a-clear-summer-sky-to-serve-as-a-background-for-text-or.jpg").into(backIV);

                    }else{
                        Picasso.get().load("https://unsplash.com/photos/8FU_MZ2r0EQ").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecastO.getJSONArray("hour");
                    for(int i =0;i<hourArray.length();i++)
                    {
                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = hourObj.getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherRVModalArrayList.add(new WeatherRVModal(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();


                }
                catch(JSONException e){
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please Enter Valid City name", Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}