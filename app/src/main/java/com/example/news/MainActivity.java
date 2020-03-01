package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.news.api.ApiClient;
import com.example.news.api.ApiInterface;
import com.example.news.models.Articles;
import com.example.news.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    //the API_KEY i get from the News API Website with my gmail account
    public static final String API_KEY = "563eef5e6bf64c56b1579a586adf6692";

    //views
    private RecyclerView recyclerView;
    private TextView topHeadLines, errorTitle, errorMessage;
    private ImageView errorImage;
    private RelativeLayout errorLayout;
    private Button btnRetry;
    private SwipeRefreshLayout swipeRefreshLayout;

    //init articles list
    private List<Articles> articles = new ArrayList<>();
    //newsAdapter
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        recyclerView = findViewById(R.id.recyclerView);
        topHeadLines = findViewById(R.id.top_headlines);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        errorImage = findViewById(R.id.errorImage);
        errorLayout = findViewById(R.id.errorLayout);
        btnRetry = findViewById(R.id.btnRetry);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        //swipeRefreshLayout setOnRefreshListener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadJSON("");
            }
        });


        /*int c1 = getResources().getColor(R.color.refresh_progress_1);
        int c2 = getResources().getColor(R.color.refresh_progress_2);
        int c3 = getResources().getColor(R.color.refresh_progress_3);
        mySwipeRefreshLayout.setColorSchemeColors(c1, c2, c3);*/
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark,
                R.color.colorAccent, R.color.colorTextSubtitle, R.color.colorBackground);

        //init RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //recyclerView.setNestedScrollingEnabled(false);
        onLoadingSwipeRefresh("");
    }

    public void loadJSON(final String Keyword) {
        topHeadLines.setVisibility(View.INVISIBLE);
        errorLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);  //To make the swiping circle loading

        //get ApiClient
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        //get country
        String country = Utils.getCountry();
        //init the call based on the Keyword
        Call<News> call;
        if (Keyword.length() > 0) {
            call = apiInterface.getNewsSearch(Keyword, "publishedAt", API_KEY);

        } else {
            call = apiInterface.getNews(country, API_KEY);
        }

        //get the news from the server and store them into the articles list
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (!articles.isEmpty()) {
                        articles.clear();
                    }

                    //get articles
                    articles = response.body().getArticles();
                    //newsAdapter
                    newsAdapter = new NewsAdapter(articles, MainActivity.this);
                    //set adapter to the recyclerView
                    recyclerView.setAdapter(newsAdapter);
                    //refresh the adapter if there is any changing
                    newsAdapter.notifyDataSetChanged();
                    initListener();
                    topHeadLines.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    topHeadLines.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 Not Found";
                            break;
                        case 500:
                            errorCode = "500 Server Broken";
                            break;
                        default:
                            errorCode = "Unknown Error";
                            break;
                    }
                    showErrorMessage(R.drawable.no_result, "No Result", "Please Try Again \n" + errorCode);
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadLines.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(R.drawable.oops, "Oops..", "Network failure, Please Try Again \n" + t.toString());
            }
        });

    }

    private void initListener() {
        newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                ImageView imageView = findViewById(R.id.img);
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);
                Articles article = articles.get(position);
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("author", article.getAuthor());
                intent.putExtra("url", article.getUrl());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("source", article.getSource().getName());
                Pair<View, String> pair = Pair.create((View) imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, pair);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, activityOptionsCompat.toBundle());
                } else
                    startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        assert searchManager != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));    //Where to search
        searchView.setQueryHint("Search Latest News...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                if (s.length() > 2) {
                    onLoadingSwipeRefresh(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                return false;
            }
        });

        /*MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.getIcon().setVisible(false, false);*/
        return true;
    }

    //Run when you open the app and when you search for some thing and click search icon bun on single thread
    public void onLoadingSwipeRefresh(final String Keyword) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadJSON(Keyword);
            }
        });
    }

    private void showErrorMessage(int Imageview, String title, String message) {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }
        errorImage.setImageResource(Imageview);
        errorTitle.setText(title);
        errorMessage.setText(message);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoadingSwipeRefresh("");
            }
        });
    }
}
