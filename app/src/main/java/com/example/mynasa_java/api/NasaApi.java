package com.example.mynasa_java.api;

import com.example.mynasa_java.api.model.DateDTO;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface NasaApi {

    @GET("apod")
    Single<DateDTO> getDatesWithPhoto();

    @GET("apod")
    DateDTO getDatesWithPhotoList();
}
