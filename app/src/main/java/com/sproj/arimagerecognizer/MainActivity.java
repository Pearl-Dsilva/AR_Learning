package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SplashScreen splashScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null) {
            splashScreen.setKeepOnScreenCondition(() -> false);
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            splashScreen.setKeepOnScreenCondition(() -> false);
            startActivity(new Intent(this, HomeActivity.class));
        }
        finish();
    }
}
