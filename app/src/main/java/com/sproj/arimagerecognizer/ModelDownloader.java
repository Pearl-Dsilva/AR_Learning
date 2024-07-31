package com.sproj.arimagerecognizer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;


public class ModelDownloader {
    private static final String TAG = "ModelDownloader";
    private Boolean languageAvailable = Boolean.FALSE;

    void downloader(Context context, String language, onDownloadStatus successFunction, onDownloadStatus failureFunction) {
        if (!NetworkUtil.isInternetAvailable(context)){
            failureFunction.invoke("Download Failed, Network Not Available");
            Log.d(TAG, "downloader: download failed");
            return;
        }
        RemoteModelManager modelManager = RemoteModelManager.getInstance();

        // Define the French model to download
        TranslateRemoteModel translateRemoteModel = new TranslateRemoteModel.Builder(language).build();

        // Set conditions for downloading the model (requires Wi-Fi in this case)
        DownloadConditions conditions = new DownloadConditions.Builder()
//                .requireWifi()
                .build();


        modelManager.download(translateRemoteModel, conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Model downloaded successfully
                        Log.d(TAG, "onSuccess: ");
                        languageAvailable = true;
                        successFunction.invoke("Model Downloaded Successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle download error
                        Log.d(TAG, "onFailure");
                        languageAvailable = false;
                        failureFunction.invoke("Model Downloaded Failed, Retry?");
                    }
                }).addOnCanceledListener(() -> {
                    Log.d(TAG, "downloader: cancelled");

                }).addOnCompleteListener(listener -> {
                    Log.d(TAG, "downloader: completed"+listener);
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
