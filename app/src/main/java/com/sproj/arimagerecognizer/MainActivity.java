package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sproj.arimagerecognizer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.sproj.arimagerecognizer.arhelper.ml.ARActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        findViewById(R.id.button_open_camera).setOnClickListener(l ->
                startActivity(new Intent(MainActivity.this, ARActivity.class))
        );
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            startActivity(new Intent(this, HomeActivity.class));
        }
    }

}
