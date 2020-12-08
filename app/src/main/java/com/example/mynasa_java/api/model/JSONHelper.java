package com.example.mynasa_java.api.model;


import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.mynasa_java.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class JSONHelper extends Application {

    private static final String FILE_NAME = "data.txt";
    private static final String TAG ="mylog" ;

    public List<DateDTO> exportToJSON(Context context,List<DateDTO> dataList) {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        String jsonString = gson.toJson(dataList);
        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos))) {
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Не удалось открыть (записать) файл", Toast.LENGTH_SHORT).show();
            Log.d(MainActivity.TAG, "ERROR: не удалось открыть (записать) файл");
        }
        return dataList;
    }

    public List<DateDTO> importFromJSON(Context context) {
        List<DateDTO> dataList = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(FILE_NAME); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis))) {
            Gson gson = new GsonBuilder().create();
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = bufferedReader.readLine();
            }
            if (stringBuilder.length()>0){
                dataList = gson.fromJson(String.valueOf(stringBuilder), new TypeToken<List<DateDTO>>() {}.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Не удалось открыть файл", Toast.LENGTH_SHORT).show();
            Log.d(MainActivity.TAG, "ERROR: не удалось открыть файл");
        }
        return dataList;
    }
}


