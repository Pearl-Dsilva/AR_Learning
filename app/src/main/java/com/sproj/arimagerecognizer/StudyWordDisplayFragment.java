package com.sproj.arimagerecognizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.File;


public class StudyWordDisplayFragment extends Fragment {


    public StudyWordDisplayFragment() {
        // Required empty public constructor
    }

    public static StudyWordDisplayFragment newInstance(String label, String translation, String imagePath) {
        StudyWordDisplayFragment fragment = new StudyWordDisplayFragment();
        Bundle args = new Bundle();
        args.putString("label", label);
        args.putString("translation", translation);
        args.putString("imagePath", imagePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_study_word_display, container, false);

        String label = getArguments().getString("label");
        String translation = getArguments().getString("translation");
        String imagePath = getArguments().getString("imagePath");

        TextView labelTextView = view.findViewById(R.id.labelTextView);
        TextView translationTextView = view.findViewById(R.id.translatnTextview);
        ImageView imageView = view.findViewById(R.id.objectImageView);
        WebView webView = view.findViewById(R.id.labelWebView);

        labelTextView.setText(label);
        translationTextView.setText(translation);

        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this).load(new File(imagePath)).into(imageView);
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.google.com/search?q=" + label);

        return view;
    }


}