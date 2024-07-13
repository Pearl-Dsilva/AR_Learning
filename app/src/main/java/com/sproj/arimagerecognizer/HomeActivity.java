package com.sproj.arimagerecognizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.sproj.arimagerecognizer.arhelper.ml.ARActivity;
import com.sproj.arimagerecognizer.R;


public class HomeActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CardView openARCamera = findViewById(R.id.cardViewAR);
        CardView buttonQuiz = findViewById(R.id.cardTest);
        CardView buttonStudy = findViewById(R.id.cardStudy);
        ImageView imageViewSettings = findViewById(R.id.imageViewSettings);
        ImageView imageViewHelp = findViewById(R.id.imageViewHelp);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
//        SharedPreferences sharedPreferences;

        // Set up the click listeners for each button
        openARCamera.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, ARActivity.class);
            startActivity(intent);
        });

        buttonQuiz.setOnClickListener(view -> {
            // TODO: Start the Quiz Activity
            Intent intent = new Intent(HomeActivity.this, QuizActivity.class);
            startActivity(intent);
        });

        buttonStudy.setOnClickListener(view -> {
            // TODO: Start the Study Activity
            Intent intent = new Intent(HomeActivity.this, StudyActivity.class);
            startActivity(intent);
        });

        imageViewSettings.setOnClickListener(view -> {
            // TODO: Show settings options
            Intent intent = new Intent(HomeActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        imageViewHelp.setOnClickListener(view -> {
            // TODO: Show help information
            Intent intent = new Intent(HomeActivity.this, HelpActivity.class);
            startActivity(intent);
        });

        logAppStarted();
        selectStartingLanguage();
    }

    void logAppStarted(){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, FirebaseAuth.getInstance().getUid());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
    }

    void selectStartingLanguage(){
        //Load LanguageManager
//        LanguageManager languageManager = new LanguageManager();
        //check if user first time, call isSetupCompleted, continue if false
        //show dialog to select language (first time)
        //save lang preference, call SetupCompleted
    }
}