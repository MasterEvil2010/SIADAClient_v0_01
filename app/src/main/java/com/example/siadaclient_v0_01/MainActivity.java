package com.example.siadaclient_v0_01;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences bufferPrefs;

    MqttAndroidClient client;
    MqttConnectOptions options;

    private String USERNAME;
    private String PASSWORD;

    final String TAG = "lifecycle";

    private int lightSwitch = 0;

    private ImageView light;
    private TextView temp;
    private TextView hum;


    private final String TempKey = "SavedTemp";
    private final String HumKey = "SavedHum";



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Activity створено");

        bufferPrefs = getSharedPreferences("showrooms", MODE_PRIVATE);


        light = findViewById(R.id.lightOffId);
        temp = findViewById(R.id.tempTextId);
        hum = findViewById(R.id.tempHumId);

        INIT();
        CONNECT();
        CALLBACK();
        LISTENCLICK();






    }


    public void LISTENCLICK() {

        ImageView settings = findViewById(R.id.settingsButton);

        light.setOnClickListener(view -> {

            lightSwitch++;
            String topic = "ledControl";
            byte[] encodePayload;
            String payload;

            if (lightSwitch%2==0)
            {
                payload = "1";
                light.setBackgroundResource(R.drawable.lighton);
            } else {
                payload = "0";
                light.setBackgroundResource(R.drawable.lightoff);
            }

            try {
                encodePayload = payload.getBytes(StandardCharsets.UTF_8);
                MqttMessage message = new MqttMessage(encodePayload);
                client.publish(topic, message);
            } catch (MqttException e) {
                e.printStackTrace();
            }

        });

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }) ;

    }

    public void SUBSCRIBE(MqttAndroidClient client, String topic) {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void CONNECT() {

        if (USERNAME == null && PASSWORD == null) {
            SharedPreferences.Editor edit = bufferPrefs.edit();
            edit.putString("MQTTLogin", "login");
            edit.putString("MQTTPass", "pass");
            edit.apply();
        }


        String HOST = bufferPrefs.getString("MQTTHost", "000.000.000.000");


        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(),"tcp://" + HOST + ":1883", clientId);

        options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_LONG).show();
                    SUBSCRIBE(client, "ledControlFeedback");
                    SUBSCRIBE(client, "RoomTemp");
                    SUBSCRIBE(client, "RoomHud");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "connection fell", Toast.LENGTH_LONG).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void CALLBACK() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                if (topic.equals("ledControlFeedback")) {
                    String data = message.toString();
                    if(data.equals("On")) {

                        lightSwitch = 0;
                    } else {

                        lightSwitch = 1;
                    }
                    Toast.makeText(MainActivity.this, "Light" + data, Toast.LENGTH_SHORT).show();
                }

                if (topic.equals("RoomTemp")) {
                    String data = message.toString();
                    String[] parts = data.split("\\.");
                    String part1 = parts[0];
                    SharedPreferences.Editor edit = bufferPrefs.edit();
                    edit.putString(TempKey, part1);
                    edit.apply();
                    temp.setText(part1 + "°C");

                }
                if (topic.equals("RoomHud")) {
                    String data = message.toString();
                    String[] parts = data.split("\\.");
                    String part1 = parts[0];
                    SharedPreferences.Editor edit = bufferPrefs.edit();
                    edit.putString(HumKey, part1);
                    edit.apply();
                    hum.setText(part1 + "%");
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    public void INIT() {
        temp.setText(new StringBuilder().append(bufferPrefs.getString(TempKey, "00")).append("°C").toString());
        hum.setText(new StringBuilder().append(bufferPrefs.getString(HumKey, "00")).append("%").toString());
        USERNAME = bufferPrefs.getString("MQTTLogin", "Login");
        PASSWORD = bufferPrefs.getString("MQTTPass", "Pass");


    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Activity з'являється");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity в фокусі");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity призупинено");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Activity зупинено");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity знищено");
    }
}