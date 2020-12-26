package com.example.mynasa_java.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mynasa_java.App;
import com.example.mynasa_java.MainActivity;
import com.example.mynasa_java.api.model.DateDTO;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyWorker extends Worker {
    public final String TAG = "mylog";
    private DateRecycler dateRecyclerList;
    private JSONHelper jsonHelper;
    private List<DateDTO> listDTO;
    private DateDTO dates;
    private CompositeDisposable disposable;
    private App app;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        Log.i(MainActivity.TAG, "MyWorker_doWork: START. " + Thread.currentThread());

        app = (App) getApplicationContext();
        //disposable = new CompositeDisposable();
        dateRecyclerList = new DateRecycler();
        jsonHelper = new JSONHelper();
        listDTO = new ArrayList<>();
        dates = app.getNasaService().getApi().getDatesWithPhotoList();

        if (dates.getMedia_type().equals("image")) {

            jsonHelper.importFromJSON(app, listDTO);
            int size = listDTO.size();
            Log.i(TAG, "MyWorker_doWork: listDTO.size=" + size);

            if (!(dates.getDate().equals(listDTO.get(size - 1).getDate()))) {
                Log.i(TAG, "MyWorker_doWork: new data is available. " + Thread.currentThread());
                listDTO.add(dates);
                jsonHelper.exportToJSON(app, listDTO);
                Log.i(TAG, "MyWorker_doWork: data loaded into memory. " + Thread.currentThread());
            } else Log.i(TAG, "MyWorker_doWork: No new data. " + Thread.currentThread());
        } else Log.i(TAG, "MyWorker_doWork: No image data. " + Thread.currentThread());


        /*Observable.just(listDTO)
                .map(list -> jsonHelper.importFromJSON(app, list))
                .map(list -> {
                    if (list.size() > 0) {
                        listDTO.addAll(list);
                        Log.i(TAG, "MyWorker_doWork: listDTO.size="+listDTO.size());
                    }
                    return listDTO;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();*/


        /*disposable.add(app.getNasaService().getApi().getDatesWithPhoto()
                .subscribeOn(Schedulers.io())
                .subscribe((dates, throwable) -> {
                    int size = listDTO.size();//данные из памяти
                    if (throwable != null) {
                        Log.i(TAG, "MyWorker_doWork: ERROR: data are not loaded. " + Thread.currentThread());
                    } else {
                        Log.i(TAG, "MyWorker_doWork: data are loaded. " + Thread.currentThread());
                        if (size > 0) {
                            Log.i(TAG, "MyWorker_doWork: listDTO.size= " + listDTO.size());
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
                }));*/

        DateRecycler.listDates = listDTO;
        //disposable.dispose();
        return Result.success();
    }

}

