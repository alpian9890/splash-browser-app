package alv.splash.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;
import java.util.Map;

public class AdBlockerWebView {





    static Map<String, Boolean> loadedUrls = new HashMap<>();

    public static boolean blockAds(WebView view, String url) {
        boolean ad;
        if (!loadedUrls.containsKey(url)) {
            ad = AdBlocker.isAd(url);
            loadedUrls.put(url, ad);
        } else {
            ad = loadedUrls.get(url);
        }
        return ad;
    }

    public static class init {
        Context context;

        public init(Context context) {
            AdBlocker.init(context);
            this.context = context;
        }

        private Handler handler = new Handler();

        private String pageTitle;
        private String viewLink;
        private String urlResources;

        public void initializeWebView(final WebView view) {


            // Mengatur WebSettings
            WebSettings webSettings = view.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setDisplayZoomControls(false);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // Sesuaikan dengan kebutuhan

            // Mengatur dukungan untuk zoom
            webSettings.setSupportZoom(true);

            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);



            // Mengatur WebViewClient
            view.setWebViewClient(new WebViewClient() {


                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    // Tindakan saat halaman mulai dimuat
                }

                @Override
                public void onPageFinished(final WebView view, String url) {
                    super.onPageFinished(view, url);
                    // Tindakan saat halaman selesai dimuat


                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    // Implementasi AdBlocker atau intercept request
                    String url = request.getUrl().toString();

                    urlResources = url;

                    return AdBlockerWebView.blockAds(view,url) ? AdBlocker.createEmptyResource() :
                            super.shouldInterceptRequest(view, url);

                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    // Tangani URL di sini, misalnya:
                    String url = request.getUrl().toString();

                    viewLink = url;

                    if (AdBlocker.isAd(url)) {
                        // Block the ad by returning true
                        return true;
                    } else {
                        // Allow regular URLs to be loaded
                        return false;
                    }
                }

            });

            // Mengatur WebChromeClient untuk dukungan fungsi tambahan
            view.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    viewLink = view.getUrl();
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    pageTitle = title;

                }


            });
        }
    }
}
