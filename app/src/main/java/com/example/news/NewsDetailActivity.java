package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Objects;


public class NewsDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private boolean isHideTollbarView = false;
    private FrameLayout date_behavior;
    private LinearLayout titleAppbar;
    private String mUrl, mTitle, mSource;


    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_derails);

        //init AppBarLayout
        AppBarLayout appBarLayout = findViewById(R.id.appbarlayout);
        //appBarLayout OnOffsetChangedListener
        appBarLayout.addOnOffsetChangedListener(this);
        /*final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle("");*/

        //init toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //Back arrow ,the default is false


        //init views
        ImageView imageView = findViewById(R.id.newsBackground);
        titleAppbar = findViewById(R.id.title_appbar);
        TextView appbar_title = findViewById(R.id.title_on_appbar);
        TextView appbar_subtitle = findViewById(R.id.subtitle_on_appbar);
        date_behavior = findViewById(R.id.date_behavior);
        TextView date = findViewById(R.id.date);
        TextView title = findViewById(R.id.title);
        TextView time = findViewById(R.id.time);

        //get data from intent from initListener from MainActivity
        Intent intent = getIntent();
        String mImg = intent.getStringExtra("img");
        // mAuthor = intent.getStringExtra("author");
        mUrl = intent.getStringExtra("url");
        String mData = intent.getStringExtra("date");
        mTitle = intent.getStringExtra("title");
        mSource = intent.getStringExtra("source");

        //init Glide for image loading
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.error(Utils.getRandomDrawableColor());
        Glide.with(this)
                .load(mImg)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(requestOptions)
                .into(imageView);
        appbar_title.setText(mSource);
        appbar_subtitle.setText(mUrl);
        date.setText(Utils.DateFormat(mData));
        title.setText(mTitle);
        time.setText(mSource + " \u2022 " + Utils.DateToTimeFormat(mData));
        initWebView(mUrl);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        WebView webView = findViewById(R.id.webView);
        // Configure related browser settings

        //Sets whether the WebView should load image resources.
        webView.getSettings().setLoadsImagesAutomatically(true);
        //Tells the WebView to enable JavaScript execution.
        webView.getSettings().setJavaScriptEnabled(true);
        //Specify the style of the scrollbars
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        webView.setWebViewClient(new WebViewClient());


        //Sets whether the DOM storage API is enabled. make app faster, disabled by default for space savings and security
        webView.getSettings().setDomStorageEnabled(true);
        //Sets whether the WebView should support zooming using its on-screen zoom controls and gestures.
        webView.getSettings().setSupportZoom(true);
        //Sets whether the WebView should use its built-in zoom mechanisms.
        webView.getSettings().setBuiltInZoomControls(true);
        //Sets whether the WebView should display on-screen zoom controls when using the built-in zoom mechanisms.
        webView.getSettings().setDisplayZoomControls(true);

        // Load the initial URL
        webView.loadUrl(url);
    }


    //To support reverse transitions when user clicks the device back button
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    //To support reverse transition when user clicks the action bar's Up/Home button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        /*final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        int min_height = ViewCompat.getMinimumHeight(collapsingToolbarLayout) * 2;
        float scale = (float) (min_height + verticalOffset) / min_height;
        imageView.setScaleX(scale >= 0 ? scale : 0);
        imageView.setScaleY(scale >= 0 ? scale : 0);*/

        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        if (percentage == 1f && isHideTollbarView) {
            date_behavior.setVisibility(View.GONE);
            titleAppbar.setVisibility(View.VISIBLE);
            isHideTollbarView = !isHideTollbarView;
        } else if (percentage < 1f && isHideTollbarView) {
            date_behavior.setVisibility(View.VISIBLE);
            titleAppbar.setVisibility(View.GONE);
            isHideTollbarView = !isHideTollbarView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {

            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plan");
                intent.putExtra(Intent.EXTRA_SUBJECT, mSource);
                String body = mTitle + "\n\n" + mUrl + "\n\n" + "Shared from the News App";
                intent.putExtra(Intent.EXTRA_TEXT, body);
                startActivity(Intent.createChooser(intent, "Share via"));

            } catch (Exception e) {
                Toast.makeText(this, "Hmm.. Sorry, \n Can't be shared", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.web_view) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mUrl));
            startActivity(intent);
            return true;

        }
        return super.onOptionsItemSelected(item);
    }
}