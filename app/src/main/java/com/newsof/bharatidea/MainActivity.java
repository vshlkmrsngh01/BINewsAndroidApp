/*
* Crated by Oms web solution
* Contact- omswebsolution.official@gmail.com
*
*/

package com.newsof.bharatidea;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;



public class MainActivity extends AppCompatActivity {

    String website_url="https://app.bharatidea.com/";
    String website_domain="app.bharatidea.com"; //no http or https only domain name ,no '/' also

    //if not work (mean click on app redirect to browser)
    //then use add domain with www.domain_name(Like below)
    //String website_domain="www.omswebsolution.com";



    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private WebView webView;

    private SwipeRefreshLayout refreshLayout;
    private LinearLayout no_Internet_Layout;
    private TextView retry_txt;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intentBackgroundService=new Intent(this,FirebasePushNotification.class);
        startService(intentBackgroundService);

        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(String.valueOf(R.string.BANNER));
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;

            }


        });


            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4541093340043327/7333537569");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());



        webView = findViewById(R.id.webView);



        refreshLayout = findViewById(R.id.swipe_refresh);
        no_Internet_Layout = findViewById(R.id.noConnectionLayout);
        retry_txt = findViewById(R.id.retry_txt);

        webView.setWebViewClient(new myWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);


        //improve webView performance
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setSavePassword(true);
        CookieManager.getInstance().setAcceptCookie(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        //for save password
        webView.getSettings().setAppCachePath(getApplicationContext()
                .getFilesDir().getAbsolutePath() + "/cache");
        webView.getSettings().setDatabasePath(getApplicationContext()
                .getFilesDir().getAbsolutePath() + "/databases");


        //Implement download in web view
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
                Dexter.withActivity(MainActivity.this)
                        .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                                DownloadManager.Request request = new DownloadManager.Request(
                                        Uri.parse(url));
                                request.setMimeType(mimeType);
                                String cookies = CookieManager.getInstance().getCookie(url);
                                request.addRequestHeader("cookie", cookies);
                                request.addRequestHeader("User-Agent", userAgent);
                                request.setDescription("Downloading file...");
                                request.setTitle(URLUtil.guessFileName(url, contentDisposition,
                                        mimeType));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(
                                        Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(
                                                url, contentDisposition, mimeType));
                                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                if (dm != null) {
                                    dm.enqueue(request);
                                }
                                Toast.makeText(getApplicationContext(), "Downloading File",
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                                permissionToken.continuePermissionRequest();
                            }
                        }).check();
            }
        });

        //Check connection
        checkConnection();
        retry_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnection();
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(true);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        if (webView != null && webView.getUrl() != null) {
                            webView.reload();
                        } else {
                            checkConnection();
                        }
                    }
                }, 3000);
            }
        });

        refreshLayout.setColorSchemeColors(getResources()
                .getColor(android.R.color.holo_green_light));
    }

    private class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
           /* view.loadUrl(url);
            return true;*/
            //If any url from app then load otherwise open in different window
            if (website_domain.equals(Uri.parse(url).getHost())) {
                view.loadUrl(url);
                return true;
            }
            //Handle all url
            else {
                /*return super.shouldOverrideUrlLoading(view, url);*/
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
            webView.loadUrl("file:///android_asset/error.html");

            //For back button press
            webView.clearHistory();


        }


    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        exit_dialog();
                        mInterstitialAd.loadAd(new AdRequest.Builder().build());

                    }
                });
            } else {
                exit_dialog();
            }
        }
    }

    private void exit_dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.exit_txt)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Check internet connection
    private void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected()) {
            webView.setVisibility(View.VISIBLE);
            no_Internet_Layout.setVisibility(View.GONE);
            webView.loadUrl(website_url);
        } else if (mobileNetwork.isConnected()) {

            webView.setVisibility(View.VISIBLE);
            no_Internet_Layout.setVisibility(View.GONE);
            webView.loadUrl(website_url);
        } else {
            webView.setVisibility(View.GONE);
            no_Internet_Layout.setVisibility(View.VISIBLE);
        }
    }

}