package com.example.siadaclient_v0_01;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;




public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private EditText MQTTHost;
    private EditText LOGIN;
    private EditText PASS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        prefs = getSharedPreferences("showrooms", MODE_PRIVATE);

        MQTTHost = findViewById(R.id.MQTT_host);
        LOGIN = findViewById(R.id.MQTT_Login);
        PASS = findViewById(R.id.MQTT_Pass);
        Button backToMain = findViewById(R.id.SaveAndExit);

        MQTTHost.setText(prefs.getString("MQTTHost", "000.000.000.000"));
        LOGIN.setText(prefs.getString("MQTTLogin", "Логін"));
        PASS.setText(prefs.getString("MQTTPass", "Пароль"));

//вихід з сторінки

        backToMain.setOnClickListener(view -> {

            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("MQTTHost", MQTTHost.getText().toString());
            edit.putString("MQTTLogin", LOGIN.getText().toString());
            edit.putString("MQTTPass", PASS.getText().toString());
            edit.apply();

            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}