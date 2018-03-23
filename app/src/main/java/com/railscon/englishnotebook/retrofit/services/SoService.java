package com.railscon.englishnotebook.retrofit.services;

import com.railscon.englishnotebook.retrofit.model.Lesson;
import com.railscon.englishnotebook.retrofit.model.Version;

import retrofit2.Call;
import retrofit2.http.GET;

public interface SoService {
    @GET("get-version")
    Call<Version> getVersion();

    @GET("get-lessons")
    Call<Lesson[]> getLessons();

}
