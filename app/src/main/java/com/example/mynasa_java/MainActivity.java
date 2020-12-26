package com.example.mynasa_java;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mynasa_java.WorkManager.MyWorker;
import com.example.mynasa_java.api.model.DateDTO;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;
import com.example.mynasa_java.databinding.ActivityMainBinding;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static androidx.work.NetworkType.CONNECTED;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "mylog";

    private ActivityMainBinding bindingActMain;
    private CompositeDisposable disposable;
    private List<DateDTO> listDTO;//лист с данными JSON, заполняется в init()
    private JSONHelper jsonHelper;
    private App app;

    private static final String EXTRA_URL = "PhotoActivity.EXTRA_URL";

    @SuppressLint("CheckResult")
    private void init() {
        app = (App) getApplication();
        disposable = new CompositeDisposable();
        jsonHelper = new JSONHelper();
        listDTO = new ArrayList<>();

        Observable.just(listDTO)
                .map(list->jsonHelper.importFromJSON(this,list))
                .map(list -> {
                    if (list.size() > 0) {
                        listDTO.addAll(list);
                        Log.i(TAG, "MainActivity_init: listDTO.size="+listDTO.size());
                    }
                    return listDTO;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

       /* Observable.just(jsonHelper.importFromJSON(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() > 0) {
                        listDTO.addAll(list);
                    }
                });*/
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "MainActivity_onCreate: START");
        super.onCreate(savedInstanceState);
        bindingActMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingActMain.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            backgroundWorkPeriodic(this);
        }
        init();
        //getSupportActionBar().setTitle(getString(R.string.choose_day));

        disposable.add(app.getNasaService().getApi().getDatesWithPhoto()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((dates, throwable) -> {
                    int size = listDTO.size();//данные из памяти
                    if (throwable != null) {
                        if (size > 0) {
                            Toast toast = Toast.makeText(MainActivity.this, "Не удалось загрузить данные с сети! \nПроверьте соединение с интернетом.\nЗагрузка данных из памяти...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //устанавливаем данные из памяти
                            bindingActMain.textView1.setText(listDTO.get(size - 1).getDate());
                            loadImageWithGlide(listDTO.get(size - 1).getUrl());
                            bindingActMain.textView2.setText(listDTO.get(size - 1).getExplanation());
                        } else {
                            Toast toast = Toast.makeText(MainActivity.this, "Нет данных\nПроверьте соединение с интернетом.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        if (!(dates.getMedia_type().equals("image"))) {
                            if (size > 0) {
                                Toast toast = Toast.makeText(MainActivity.this, "Media type not image.\nSetting data from the memory", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                bindingActMain.textView1.setText(listDTO.get(size - 1).getDate());
                                loadImageWithGlide(listDTO.get(size - 1).getUrl());
                                bindingActMain.textView2.setText(listDTO.get(size - 1).getExplanation());
                            } else {
                                Toast toast = Toast.makeText(MainActivity.this, "Media type not image.\nNo data in memory", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        } else if (dates.getMedia_type().equals("image")) {
                            if (size > 0) {
                                if (dates.getDate().equals(listDTO.get(size - 1).getDate())) {
                                    //если данные с сети такие же как в памяти
                                    Toast toast = Toast.makeText(MainActivity.this, "No new data.\nSetting data from the memory", Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                    //устанавливаем данные из памяти
                                    bindingActMain.textView1.setText(listDTO.get(size - 1).getDate());
                                    loadImageWithGlide(listDTO.get(size - 1).getUrl());
                                    bindingActMain.textView2.setText(listDTO.get(size - 1).getExplanation());
                                } else {
                                    //если данные с сети новые, устанавливаем их и запоминаем в файл через exportToJSON
                                    bindingActMain.textView1.setText(dates.getDate());
                                    loadImageWithGlide(dates.getUrl());
                                    bindingActMain.textView2.setText(dates.getExplanation());
                                    listDTO.add(dates);
                                    Observable.just(jsonHelper.exportToJSON(MainActivity.this, listDTO))
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe();
                                }
                            } else {
                                //если в памяти нет данных, устанавливаем новые данные и запоминаем их через exportToJSON
                                bindingActMain.textView1.setText(dates.getDate());
                                loadImageWithGlide(dates.getUrl());
                                bindingActMain.textView2.setText(dates.getExplanation());
                                listDTO.add(dates);
                                Observable.just(jsonHelper.exportToJSON(MainActivity.this, listDTO))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe();
                            }
                        }
                    }
                }));
        DateRecycler.listDates = listDTO;

        clickButton();

        clickImage();
    }


//    @RequiresApi(api = Build.VERSION_CODES.N)
//    public static void backgroundWork(Context context) {
//        /*Constraints constraints = new Constraints.Builder()
//                .addContentUriTrigger(Uri.parse("https://api.nasa.gov/planetary/apod?api_key=bUPDj3NcY7TPvoShGVEilLJJmiYHzdqyirJx04n4"), true)
//                .build();*/
//       /* Calendar currentDate = Calendar.getInstance();
//        Calendar dueDate = Calendar.getInstance();
//        dueDate.set(Calendar.HOUR_OF_DAY, 10);
//        dueDate.set(Calendar.MINUTE, 3);
//        dueDate.set(Calendar.SECOND, 0);
//        if (dueDate.before(currentDate)) {
//            dueDate.add(Calendar.HOUR_OF_DAY, 24);
//        }
//        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();*/
//
//        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
//                //.setInitialDelay(timeDiff, TimeUnit.MINUTES)
//                //.addTag("TAG")
//                .build();
//        WorkManager.getInstance(context).enqueueUniqueWork("download", ExistingWorkPolicy.REPLACE, myWorkRequest);
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void backgroundWorkPeriodic(Context context) {
        PeriodicWorkRequest myWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 4, TimeUnit.HOURS)
                .addTag("UNIQUE")
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(CONNECTED).setRequiresCharging(true).build())
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("UNIQUE", ExistingPeriodicWorkPolicy.KEEP, myWorkRequest);
    }

    private void loadImageWithGlide(String url) {
        Glide
                .with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        @SuppressLint("ShowToast")
                        Toast toast = Toast.makeText(MainActivity.this, "Не удалось загрузить изображение", Toast.LENGTH_LONG);
                        findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        findViewById(R.id.progress).setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(bindingActMain.image);
    }

    private void clickButton() {
        bindingActMain.button.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RecyclerActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        disposable.dispose();
        super.onDestroy();
    }

    private void clickImage() {
        bindingActMain.image.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PhotoActivity.class);
            if (DateRecycler.listDates.get(DateRecycler.listDates.size() - 1).getHdurl() != null) {
                intent.putExtra(EXTRA_URL, DateRecycler.listDates.get(DateRecycler.listDates.size() - 1).getHdurl());
            } else
                intent.putExtra(EXTRA_URL, DateRecycler.listDates.get(DateRecycler.listDates.size() - 1).getUrl());
            startActivity(intent);
        });
    }
}

