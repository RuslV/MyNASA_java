package com.example.mynasa_java.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mynasa_java.App;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MyWorker extends Worker {
    public static final String TAG = "mylog";
    private DateRecycler dateRecyclerList;
    private JSONHelper jsonHelper;


    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("CheckResult")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public Result doWork() {
        CompositeDisposable disposable = new CompositeDisposable();
        dateRecyclerList = new DateRecycler();
        jsonHelper = new JSONHelper();
        App app = (App) getApplicationContext();

        Observable.just(jsonHelper.importFromJSON(app))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() > 0) {
                        dateRecyclerList.getListDates().clear();
                        dateRecyclerList.getListDates().addAll(list);
                    }
                });


        disposable.add(app.getNasaService().getApi().getDatesWithPhoto()
                .subscribeOn(Schedulers.io())
                .subscribe((dates, throwable) -> {
                    if (!(dates.getDate().equals(dateRecyclerList.getListDates().get(dateRecyclerList.getListDates().size() - 1).getDate()))) {
                        dateRecyclerList.addDates(dates);
                        Observable.just(jsonHelper.exportToJSON(app, dateRecyclerList.getListDates()))
                                .subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                }));

        return Result.success();
    }
}
