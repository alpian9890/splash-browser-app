package alv.splash.browser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrowserTabFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
// BrowserTabFragment.java
public class BrowserTabFragment extends Fragment {
    private WebView mWebView;
    private String url;

    public static BrowserTabFragment newInstance(String url) {
        BrowserTabFragment fragment = new BrowserTabFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser_tab, container, false);
        mWebView = view.findViewById(R.id.mWebView);

        // Konfigurasi WebView
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (getArguments() != null) {
            url = getArguments().getString("url");
            mWebView.loadUrl(url);
        }

        return view;
    }
}