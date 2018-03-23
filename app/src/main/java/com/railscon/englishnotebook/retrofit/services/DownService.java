package com.railscon.englishnotebook.retrofit.services;

import com.railscon.englishnotebook.retrofit.model.Lesson;
import com.railscon.englishnotebook.retrofit.model.Version;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface DownService {
    @GET
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);

}
