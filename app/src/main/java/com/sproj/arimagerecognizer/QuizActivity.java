package com.sproj.arimagerecognizer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QuizActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<String> transactionList;
    private static final String TAG = "QuizActivity";
    private ImageView quizImageView;
    private Handler mainHandler;
    private Translator translator;
    private List<Boolean> ignoranceList;

    private int randomIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mainHandler = new Handler(Looper.getMainLooper());
        transactionList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE); // Initially showing the loading indicator
        quizImageView = findViewById(R.id.item_image); // Shows the question image

        translator = Translation.getClient(
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.SPANISH)
                        .build());

        findViewById(R.id.submit_button).setOnClickListener(listener -> {
            ConstraintLayout loading = findViewById(R.id.loading_indicator);

            //TODo: optimize answer storage
            String word = ((TextView) findViewById(R.id.item_text)).getText().toString();

            RadioGroup answerRadioGroup = findViewById(R.id.answer_options);
            int selectedId = answerRadioGroup.getCheckedRadioButtonId();
            RadioButton selectedRadioButton = findViewById(selectedId);

            String selectedValue = selectedRadioButton.getText().toString();

            loading.setVisibility(View.VISIBLE); // Showing the loading indicator on submit

            translator.translate(word.trim()).addOnSuccessListener(translatedWord -> {

                Log.d(TAG, "Word is: " + word + ", correct answer is: " + translatedWord);

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle("Congratulations")
                        .setMessage("You correctly translated the word")
                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                        .setCancelable(false);

                loading.setVisibility(View.GONE); // Stop showing the loading indicator on submit

                if (translatedWord == null || selectedValue.trim().equalsIgnoreCase(translatedWord)) {
                    builder.setPositiveButton("Next", (dialog, which) -> {

                        updateStudyStatus(word);

                        dialog.dismiss();

                        startQuiz(0);

                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    });

                    builder.create().show();
                } else {
                    builder.setTitle("Sorry")
                            .setMessage("Your answer is incorrect, try again")
                            .create()
                            .show();
                }
            }).addOnFailureListener(failure -> loading.setVisibility(View.GONE));
        });

        ignoranceList = new ArrayList<>();

        loadContent();
    }

    private void populateRadioButtons(ArrayList<String> answerList) {
        if (answerList.get(0) != null)
            ((RadioButton) findViewById(R.id.radioButton1)).setText(answerList.get(0));
        else
            findViewById(R.id.radioButton1).setVisibility(View.GONE);

        if (answerList.get(1) != null)
            ((RadioButton) findViewById(R.id.radioButton2)).setText(answerList.get(1));
        else
            findViewById(R.id.radioButton2).setVisibility(View.GONE);

        if (answerList.get(2) != null)
            ((RadioButton) findViewById(R.id.radioButton3)).setText(answerList.get(2));
        else
            findViewById(R.id.radioButton3).setVisibility(View.GONE);

        if (answerList.get(3) != null)
            ((RadioButton) findViewById(R.id.radioButton4)).setText(answerList.get(3));
        else
            findViewById(R.id.radioButton4).setVisibility(View.GONE);
    }

    void loadContent() {
        CollectionReference transactionsRef = db.collection("labels/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "/recognisedLabels");
        transactionsRef
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadContent: size: " + queryDocumentSnapshots.size());

                    ignoranceList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
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
        loading.setVisibility(View.VISIBLE);

        if (loadCount > 10) return;

        String word = pickRandomDocument(ignoranceList, true);

        if (word == null) {
            startQuiz(loadCount + 1);
            return;
        }

        translator
                .translate(word)
                .addOnSuccessListener(translatedWord -> {
                    ((TextView) findViewById(R.id.item_text)).setText(word);
                    String alternateWord1 = pickRandomDocument(ignoranceList, false);
                    String alternateWord2 = pickRandomDocument(ignoranceList, false);
                    String alternateWord3 = pickRandomDocument(ignoranceList, false);

                    new Thread(() -> {
                        try {
                            ArrayList<String> answers = translateStrings(alternateWord1, alternateWord2, alternateWord3);
                            answers.add(translatedWord);
                            Collections.shuffle(answers);

                            mainHandler.post(() -> {
                                populateRadioButtons(answers);
                                loading.setVisibility(View.GONE);
                            });

                        } catch (InterruptedException | ExecutionException e) {
                            Log.e(TAG, "startQuiz: ", e);
                        }
                    }).start();
                }).addOnFailureListener(e -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to Load Translation", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error translating word: " + e.getMessage());
                });

        String imagePath = "images/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail() + "/" + word + ".jpg";
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
        storageRef
                .getFile(localFile)
                .addOnSuccessListener(it -> {
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

    private static String translateString(Translator translator, String text) throws InterruptedException {
        String[] result = new String[1];
        boolean[] done = new boolean[1];

        translator.translate(text)
                .addOnSuccessListener(translatedText -> {
                    result[0] = translatedText;
                    done[0] = true;
                })
                .addOnFailureListener(e -> {
                    result[0] = "";
                    done[0] = true;
                });

        while (!done[0]) {
            Thread.sleep(100);  // Wait for translation to complete
        }
        return result[0];
    }


    public ArrayList<String> translateStrings(String alternateWord1, String alternateWord2, String alternateWord3) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Create a list of tasks
        List<Callable<String>> tasks = List.of(
                () -> translateString(translator, alternateWord1),
                () -> translateString(translator, alternateWord2),
                () -> translateString(translator, alternateWord3)
        );

        // Submit the tasks and wait for all of them to complete
        List<Future<String>> futures = executor.invokeAll(tasks);

        // Collect the results
        ArrayList<String> results = new ArrayList<>();
        for (Future<String> future : futures) {
            results.add(future.get());  // This will wait for each task to complete
        }

        // Shut down the executor
        executor.shutdown();

        return results;
    }


    void updateStudyStatus(String itemName) {

    }


    String pickRandomDocument(List<Boolean> ignoranceList, boolean shouldIgnore) {
        if (transactionList == null || transactionList.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int size = transactionList.size();

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
        ignoranceList.set(randomIndex, shouldIgnore);
        return transactionList.get(randomIndex);
    }


}