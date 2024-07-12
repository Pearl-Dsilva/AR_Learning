package com.sproj.arimagerecognizer;

import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;

public class ModelDownloader {
    private static final String TAG = "ModelDownloader";
    void downloader(String language, onDownloadStatus successFunction,onDownloadStatus failureFunction) {
        RemoteModelManager modelManager = RemoteModelManager.getInstance();

        // Define the French model to download
        TranslateRemoteModel translateRemoteModel = new TranslateRemoteModel.Builder(language).build();

        // Set conditions for downloading the model (requires Wi-Fi in this case)
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        // ToDo: check if model already exists in local db
        // Download the French model with the specified conditions
        // TODO: Before calling this method, show a user rational that the model is downloading, user is not allowed to access
        modelManager.download(translateRemoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Model downloaded successfully
                        // TODO: hide rational indicating the download was successful
                        // TODO: update local db that model was successfully downloaded
                        Log.d(TAG, "onSuccess: ");
                        successFunction.invoke();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle download error
                        // TODO: hide rational indicating the download was failure
                        Log.d(TAG, "onFailure");

                    }
                });
    }

    void deleteModel() {
        // Assuming you have already obtained an instance of RemoteModelManager
        RemoteModelManager modelManager = RemoteModelManager.getInstance();

        // Define the German model to delete
        TranslateRemoteModel germanModel = new TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build();

        // Delete the downloaded German model
        modelManager.deleteDownloadedModel(germanModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Model deleted successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle deletion error (e.g., model not found)
                    }
                });
    }
}
