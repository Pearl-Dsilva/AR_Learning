package com.sproj.arimagerecognizer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.sproj.arimagerecognizer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<String> transactionList;
    private static final String TAG = "QuizActivity";
    private ImageView quizImageView;

    private TranslatorOptions options;
    private Translator englishGermanTranslator;
    private List<Boolean> ignoranceList;

    private int randomIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        db = FirebaseFirestore.getInstance();
        findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
        transactionList = new ArrayList<>();
        quizImageView = findViewById(R.id.item_image);
        Button submitButton = findViewById(R.id.submit_button);

        options = new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.GERMAN).build();
        englishGermanTranslator = Translation.getClient(options);

        submitButton.setOnClickListener(listener -> {
            ConstraintLayout loading = findViewById(R.id.loading_indicator);

            String word = ((TextView) findViewById(R.id.item_text)).getText().toString();
            String enteredVal = ((EditText) findViewById(R.id.answer)).getText().toString();

            if (enteredVal.trim().isEmpty()) {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }
            if (loading.getVisibility() == View.GONE) loading.setVisibility(View.VISIBLE);


            englishGermanTranslator.translate(word.trim()).addOnSuccessListener(translateListener -> {
                Log.d(TAG, "Word is: " + word + ", correct answer is: " + translateListener);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Congratulations").setMessage("You correctly translated the word").setPositiveButton("OK", (dialog, which) -> dialog.dismiss()).setCancelable(false);
                if (loading.getVisibility() == View.VISIBLE) loading.setVisibility(View.GONE);
                if (translateListener == null || enteredVal.trim().equalsIgnoreCase(translateListener)) {
                    builder.setPositiveButton("Next", (dialog, which) -> {
                        ignoranceList.set(randomIndex, true);
                        updateStudyStatus(word);
                        dialog.dismiss();
                        startQuiz(0);
                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    builder.setTitle("Sorry").setMessage("Your answer is incorrect, try again");
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }).addOnFailureListener(failure -> {
                if (loading.getVisibility() == View.VISIBLE) loading.setVisibility(View.GONE);
            });
        });

        ignoranceList = new ArrayList<>();
        loadContent();
    }

    void loadContent() {
        CollectionReference transactionsRef = db.collection("labels/" + FirebaseAuth.getInstance().getCurrentUser().getEmail() + "/recognisedLabels");
        transactionsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d(TAG, "loadContent: size: " + queryDocumentSnapshots.size());
            // Iterate through the documents
            ignoranceList = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                // Extract data from each document
                String transactionData = document.getString("label");
                transactionList.add(transactionData);
                ignoranceList.add(false);
            }

            Log.d(TAG, "Created a new Ignorance list with size: " + ignoranceList.size());
            if (queryDocumentSnapshots.isEmpty()) {
                Snackbar.make(findViewById(R.id.root), "No Data Available, Scan items first", Snackbar.LENGTH_SHORT).show();
            } else {
                startQuiz(0);
            }
        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    void startQuiz(int loadCount) {
        ConstraintLayout loading = findViewById(R.id.loading_indicator);

        if (loading.getVisibility() == View.GONE) loading.setVisibility(View.VISIBLE);

        Log.d(TAG, "startQuiz: " + loadCount);
        if (loadCount > 10) return;

        String word = pickRandomDocument(ignoranceList);
        if (word == null) {
            startQuiz(loadCount + 1);
            return;
        }

        String imagePath = "images/" + FirebaseAuth.getInstance().getCurrentUser().getEmail() + "/" + word + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(imagePath);
        File localFile = null;
        try {
            localFile = new File(this.getCacheDir(), "image_" + word + ".jpg");
            if (localFile.exists()) {
                Glide.with(this).load(localFile).into(quizImageView);
                return;
            }
            localFile = File.createTempFile("image_" + word, ".jpg", this.getCacheDir());
        } catch (IOException e) {
            Log.e(TAG, "Error creating temporary file: " + e.getMessage());
        }
        // Download the image file to local storage
        File finalLocalFile = localFile;
        storageRef.getFile(localFile).addOnSuccessListener(it -> {
            // Load image using Glide from the local file
            Glide.with(this).load(finalLocalFile).into(quizImageView);
            loading.setVisibility(View.GONE);

        }).addOnFailureListener(e -> {
            // Handle any errors
            loading.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to Load Image", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error downloading image from Firebase Storage: " + e.getMessage());

        });


        ((TextView) findViewById(R.id.item_text)).setText(word);
    }

    void updateStudyStatus(String itemName) {

    }


    String pickRandomDocument(List<Boolean> ignoranceList) {
        if (transactionList == null || transactionList.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int size = transactionList.size();

        Log.d(TAG, "transactionList size: " + size + ", ignoranceList: " + ignoranceList.size());

        // Create a list to store indices that can be chosen
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            // Add the index to availableIndices if the corresponding value in ignoranceList is false
            if (!ignoranceList.get(i)) {
                availableIndices.add(i);
            }
        }

        // Check if there are available indices to choose from
        if (availableIndices.isEmpty()) {
            return null; // No available indices, return null
        }

        // Pick a random index from availableIndices
        randomIndex = availableIndices.get(random.nextInt(availableIndices.size()));

        Log.d(TAG, "pickRandomDocument: " + randomIndex);

        return transactionList.get(randomIndex);
    }


}