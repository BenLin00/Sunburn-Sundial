package com.example.test2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    public double lon;
    public double lat;

    public int skinType;
    public long minutesLeft;

    public TextView skinView;
    public TextView uvView;
    public TextView burnView;

    public Button skin1;
    public Button skin2;
    public Button skin3;
    public Button skin4;
    public Button skin5;
    public Button skin6;
    public Button[] buttonArray;

    public PopupWindow infoWindow;
    public double uvIndex;

//"#EFD1B6"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView LonView = (TextView)findViewById(R.id.LonView);
        final TextView LatView = (TextView)findViewById(R.id.LatView);
        skinView = (TextView)findViewById(R.id.SkinView);
        uvView = findViewById(R.id.uvView);
        burnView = findViewById(R.id.burnView);

        skin1 = findViewById(R.id.skin1);
        skin2 = findViewById(R.id.skin2);
        skin3 = findViewById(R.id.skin3);
        skin4 = findViewById(R.id.skin4);
        skin5 = findViewById(R.id.skin5);
        skin6 = findViewById(R.id.skin6);
        buttonArray = new Button[]{skin1,skin2,skin3,skin4,skin5,skin6};

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "NO PERMISSIONS", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        final Thread getUVData = new Thread(){
            @Override
            public void run(){
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.openuv.io/api/v1/uv?lat=" + lat + "&lng=" +lon)
                        .get()
                        .addHeader("x-access-token", "4059d065741aa54889980352126cb300")
                        .build();

                Log.d("JSON", "long:" + lon);

                try {
                    Response response = client.newCall(request).execute();
                    String resString = response.body().string();
                    JSONObject json = new JSONObject(resString);
                    Log.d("JSON",json.toString());
                    try {
                        uvIndex = (Double) json.getJSONObject("result").get("uv");
                    } catch (Exception e) {
                        uvIndex = 0;
                    }
                    uvView.setText("Current UV Index: "+uvIndex);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            //Toast.makeText(MainActivity.this, "LOCATION RECEIVED", Toast.LENGTH_LONG).show();
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                            //Toast.makeText(MainActivity.this, "Lat: " + lat + " Lon: "+ lon, Toast.LENGTH_LONG).show();

                            LatView.setText("latitude: "+ lat);
                            LonView.setText("longitude: "+lon);
                            getUVData.start();
                        }
                    }
                });

    }
    public void selectSkin(View v){
        for (Button buttona:buttonArray)
            buttona.setTextColor(Color.BLACK);
        Button button = findViewById(v.getId());
        button.setTextColor(Color.WHITE);
        //Toast.makeText(this, "id: " + button.getText(), Toast.LENGTH_SHORT).show();
        skinType=Integer.parseInt(button.getText().toString());
        skinView.setText("SkinType: "+skinType);

        double multiplier = 0;
        switch (skinType) {
            case 1:
                multiplier = 2.5;
                break;
            case 2:
                multiplier = 3;
                break;
            case 3:
                multiplier = 4;
                break;
            case 4:
                multiplier = 5;
                break;
            case 5:
                multiplier = 8;
                break;
            case 6:
                multiplier = 15;
                break;
            default:
                break;
        }

        double burntime = (200/3)*(multiplier/uvIndex);
        Toast.makeText(this,""+burntime, Toast.LENGTH_SHORT).show();
        minutesLeft = (long) burntime;
        int burnhours = (int) burntime/60;
        int burnminutes = ((int) (burntime+0.5))%60;

        String burnText = "Time to burn: ";
        if(burnhours > 1) burnText += burnhours + " hours ";
        else if(burnhours > 0) burnText += burnhours + " hour ";

        burnText += burnminutes + " minutes";

        if(uvIndex == 0) burnText = "Time to burn: N/A";
        burnView.setText(burnText);
    }
    public void startTimer(View v){
        Toast.makeText(this,""+minutesLeft, Toast.LENGTH_SHORT).show();
        new CountDownTimer(minutesLeft*60*1000, 1000){
            public void onTick(long millisecondsRemaining){
//                Toast.makeText(getApplicationContext(), "tick", Toast.LENGTH_SHORT).show();
                burnView.setText("Time until burn: \n" + millisecondsRemaining/60/1000 + " minutes " + (millisecondsRemaining % (60 * 1000)) / 1000 + " seconds");
            }
            @Override
            public void onFinish() {

            }
        }.start();
    }
    public void applySunscreen(View v){

    }

}
