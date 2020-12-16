package com.example.mynasa_java.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import androidx.work.rxjava3.RxWorker;

import com.example.mynasa_java.App;
import com.example.mynasa_java.api.model.DateDTO;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.schedulers.Schedulers;


public class MyRxWorker extends RxWorker {
    public static final String TAG = "mylog";
    private JSONHelper jsonHelper;
    private List<DateDTO> listDTO;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public MyRxWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @SuppressLint("CheckResult")
    @NonNull
    @Override
    public Single<Result> createWork() {
        CompositeDisposable disposable = new CompositeDisposable();

        jsonHelper = new JSONHelper();
        App app = (App) getApplicationContext();
        listDTO = new ArrayList<>();

        Log.i(TAG, "MyWorker_doWork: START. " + Thread.currentThread());

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
        return Single.just(Result.failure());
    }
}

