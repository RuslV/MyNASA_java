package com.example.mynasa_java;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mynasa_java.WorkManager.MyWorker;
import com.example.mynasa_java.api.model.DateRecycler;
import com.example.mynasa_java.api.model.JSONHelper;
import com.example.mynasa_java.databinding.ActivityMainBinding;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "mylog";

    private ActivityMainBinding bindingActMain;
    private CompositeDisposable disposable;
    private DateRecycler dateRecyclerList;
    private JSONHelper jsonHelper;

    private static final String EXTRA_URL = "PhotoActivity.EXTRA_URL";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindingActMain = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(bindingActMain.getRoot());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            backgroundWorkPeriodic(this);
        }

        init();

        //getSupportActionBar().setTitle(getString(R.string.choose_day));

        App app = (App) getApplication();

        disposable.add(app.getNasaService().getApi().getDatesWithPhoto()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((dates, throwable) -> {
                    int size = dateRecyclerList.getListDates().size();//данные из памяти
                    if (throwable != null) {
                        if (size > 0) {
                            Toast toast = Toast.makeText(MainActivity.this, "Не удалось загрузить данные с сети! \nПроверьте соединение с интернетом.\nЗагрузка данных из памяти...", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //устанавливаем данные из памяти
                            bindingActMain.textView1.setText(dateRecyclerList.getListDates().get(size - 1).getDate());
                            loadImageWithGlide(dateRecyclerList.getListDates().get(size - 1).getUrl());
                            bindingActMain.textView2.setText(dateRecyclerList.getListDates().get(size - 1).getExplanation());
                        } else {
                            Toast toast = Toast.makeText(MainActivity.this, "Нет данных\nПроверьте соединение с интернетом.", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    } else {
                        if (size > 0) {
                            if (dates.getDate().equals(dateRecyclerList.getListDates().get(size - 1).getDate())) {
                                //если данные с сети такие же как в памяти
                                Toast toast = Toast.makeText(MainActivity.this, "Нет новых данных", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                //устанавливаем данные из памяти
                                bindingActMain.textView1.setText(dateRecyclerList.getListDates().get(size - 1).getDate());
                                loadImageWithGlide(dateRecyclerList.getListDates().get(size - 1).getUrl());
                                bindingActMain.textView2.setText(dateRecyclerList.getListDates().get(size - 1).getExplanation());

                            } else {
                                //если данные с сети новые, устанавливаем их и запоминаем в файл через exportToJSON
                                bindingActMain.textView1.setText(dates.getDate());
                                loadImageWithGlide(dates.getUrl());
                                bindingActMain.textView2.setText(dates.getExplanation());
                                dateRecyclerList.addDates(dates);
                                Observable.just(jsonHelper.exportToJSON(MainActivity.this, dateRecyclerList.getListDates()))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe();
                                DateRecycler.listDates = dateRecyclerList.getListDates();
                            }
                        } else {
                            //если в памяти нет данных, устанавливаем новые данные и запоминаем их через exportToJSON
                            bindingActMain.textView1.setText(dates.getDate());
                            loadImageWithGlide(dates.getUrl());
                            bindingActMain.textView2.setText(dates.getExplanation());
                            dateRecyclerList.addDates(dates);
                            Observable.just(jsonHelper.exportToJSON(MainActivity.this, dateRecyclerList.getListDates()))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();
                            DateRecycler.listDates = dateRecyclerList.getListDates();
                        }
                    }
                }));
        clickButton();
        clickImage();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void backgroundWork(Context context) {
        Constraints constraints = new Constraints.Builder()
                .addContentUriTrigger(Uri.parse("https://api.nasa.gov/planetary/apod?api_key=bUPDj3NcY7TPvoShGVEilLJJmiYHzdqyirJx04n4"), true)
                .build();
        OneTimeWorkRequest myWorkRequest = new OneTimeWorkRequest.Builder(MyWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork("download", ExistingWorkPolicy.REPLACE, myWorkRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void backgroundWorkPeriodic(Context context) {
        PeriodicWorkRequest myWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class,1440,TimeUnit.MINUTES, 1400,TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context).enqueue(myWorkRequest);
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
            intent.putExtra(EXTRA_URL, DateRecycler.listDates.get(DateRecycler.listDates.size() - 1).getUrl());
            startActivity(intent);
        });
    }

    @SuppressLint("CheckResult")
    private void init() {
        disposable = new CompositeDisposable();
        dateRecyclerList = new DateRecycler();
        jsonHelper = new JSONHelper();

        Observable.just(jsonHelper.importFromJSON(this))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.size() > 0) {
                        dateRecyclerList.getListDates().clear();
                        dateRecyclerList.getListDates().addAll(list);
                    }
                });

    }
}

