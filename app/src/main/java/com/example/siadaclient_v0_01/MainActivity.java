package com.example.siadaclient_v0_01;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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

    MqttAndroidClient client;
    MqttConnectOptions options;

    static String HOST = "tcp://10.147.20.12:1883";
    static String USERNAME = "mosquitto";
    static String PASSWORD = "202020";

    private int lightSwitch = 0;


    private ImageView light;
    private TextView temp;
    private TextView hum;
    private ImageView settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        light = findViewById(R.id.lightOffId);
        temp = findViewById(R.id.tempTextId);
        hum = findViewById(R.id.tempHumId);
        settings = findViewById(R.id.settingsButton);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), HOST, clientId);

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

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }) ;

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
                    temp.setText(data + "°C");

                }

                if (topic.equals("RoomHud")) {
                    String data = message.toString();
                    hum.setText(data + "%");

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });




    }

    public void SUBSCRIBE (MqttAndroidClient client, String topic) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}