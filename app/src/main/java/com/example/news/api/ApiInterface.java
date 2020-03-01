package com.example.news.api;

import com.example.news.models.News;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines")
    Call<News> getNews(
            @Query("country") String country,
            @Query("apiKey") String api_kry
    );

    @GET("everything")
    Call<News> getNewsSearch(
            @Query("q") String Keyword,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String api_kry
    );
}
