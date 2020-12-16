package com.example.mynasa_java.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mynasa_java.App;
import com.example.mynasa_java.api.model.DateDTO;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyWorker extends Worker {
    public static final String TAG = "mylog";
    private DateRecycler dateRecyclerList;
    private JSONHelper jsonHelper;
    private List<DateDTO> listDTO;


    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        CompositeDisposable disposable = new CompositeDisposable();

        Log.i(TAG, "MyWorker_doWork: START. " + Thread.currentThread());

        dateRecyclerList = new DateRecycler();
        jsonHelper = new JSONHelper();
        App app = (App) getApplicationContext();
        listDTO = new ArrayList<>();

        Observable.just(jsonHelper.importFromJSON(app))
                .subscribeOn(Schedulers.io())
                .subscribe(list -> {
                    Log.i(TAG, "MyWorker_doWork: list from the memory is loaded. " + Thread.currentThread());
                    if (list.size() > 0) {
                        listDTO.addAll(list);
                        disposable.add(app.getNasaService().getApi().getDatesWithPhoto()
                                .subscribeOn(Schedulers.io())
                                .subscribe((dates, throwable) -> {
                                    int size = listDTO.size();//данные из памяти
                                    if (throwable != null) {
                                        Log.i(TAG, "MyWorker_doWork: ERROR: data are not loaded. " + Thread.currentThread());
                                    } else {
                                        Log.i(TAG, "MyWorker_doWork: data are loaded. " + Thread.currentThread());
                                        if (size > 0) {
                                            if (!(dates.getDate().equals(listDTO.get(size - 1).getDate()))) {
                                                Log.i(TAG, "MyWorker_doWork: new data is available. " + Thread.currentThread());
                                                listDTO.add(dates);
                                                Observable.just(jsonHelper.exportToJSON(app, listDTO))
                                                        .subscribeOn(Schedulers.io())
                                                        .subscribe();
                                                Log.i(TAG, "MyWorker_doWork: data loaded into memory. " + Thread.currentThread());
                                            }
                                        }
                                    }
                                }));

                    }
                });
        DateRecycler.listDates = listDTO;
        return Result.success();
    }
}
