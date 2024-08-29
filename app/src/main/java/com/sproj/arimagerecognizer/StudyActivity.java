package com.sproj.arimagerecognizer;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sproj.arimagerecognizer.adapter.LabelAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudyActivity extends AppCompatActivity  {

    LabelAdapter adapter;
    private static final String TAG = "StudyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        RecyclerView lists = findViewById(R.id.recyclerViewVocabularySets);
        SearchView searchView = findViewById(R.id.searchView);

        lists.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LabelAdapter(getApplicationContext(),getApplication().getSharedPreferences(getString(R.string.language_selection), MODE_PRIVATE));
        lists.setAdapter(adapter);
        loadDataFromFirestore();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.loadData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.loadData(newText);
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void loadDataFromFirestore() {
        Log.d(TAG, "loadDataFromFirestore: Flag 1");
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the collection
        CollectionReference collectionRef = db.collection("labels/" + email + "/recognisedLabels");

        List<Pair<String, Date>> recognisedLabels = new ArrayList<>();
        Log.d(TAG, "loadDataFromFirestore: Flag 2");

        collectionRef.get().addOnSuccessListener(querySnapshot -> {
            try {
                Log.d(TAG, "loadDataFromFirestore: " + querySnapshot.size());

                // Iterate over documents and extract data
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    // Extract label and timestamp from each document
                    String label = (String) document.get("label");
                    Date timestamp = document.getTimestamp("timestamp").toDate();

                    // Add the label and timestamp to the list
                    recognisedLabels.add(new Pair<>(label, timestamp));
                }
                adapter.setData(recognisedLabels);
            } catch (Exception e) {
                // Handle any errors
                Log.d(TAG, "loadDataFromFirestore: " + e.getMessage());
                System.out.println("Error fetching recognised labels: " + e.getMessage());
            }
        }).addOnFailureListener(e -> {
            // Handle failures
            Log.d(TAG, "Error fetching recognised labels: " + e.getMessage());
        });

    }
}