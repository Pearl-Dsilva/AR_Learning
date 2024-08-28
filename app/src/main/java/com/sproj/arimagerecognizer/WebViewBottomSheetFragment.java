package com.sproj.arimagerecognizer;

import android.os.Bundle;


import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class WebViewBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_URL = "url";

    public WebViewBottomSheetFragment() {
        // Required empty public constructor
    }

    public static WebViewBottomSheetFragment newInstance(String url) {
        WebViewBottomSheetFragment fragment = new WebViewBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view_bottom_sheet, container, false);

        WebView webView = view.findViewById(R.id.searchWebview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // Load the URL passed as an argument
        if (getArguments() != null) {
            String url = getArguments().getString(ARG_URL);
            webView.loadUrl(url);
        }

        return view;
    }
}