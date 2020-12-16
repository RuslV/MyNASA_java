package com.example.mynasa_java;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mynasa_java.api.model.DateDTO;
import com.example.mynasa_java.api.model.DateRecycler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecyclerActivity extends AppCompatActivity {
    private RecyclerView recycler;
    private Adapter adapter;
    private List<DateDTO> listDTO;
    private TextView textView;
    private static final String EXTRA_URL = "PhotoActivity.EXTRA_URL";
    public static final String TAG = "mylog";


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler);
        init();

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(adapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        listDTO = DateRecycler.listDates.stream().sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate())).collect(Collectors.toList());
        recycler = findViewById(R.id.recycler);
        adapter = new Adapter(getApplicationContext(), listDTO);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
       private Context context;
        private final List<DateDTO> listDates;


        public Adapter(Context context, List<DateDTO> listDates) {
            this.context=context;
            this.listDates = listDates;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_date, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            textView.setText(listDates.get(position).getDate());
            loadImageWithGlide(listDates.get(position).getUrl(), holder.imageView);
            holder.bind(listDates.get(position).getUrl());
        }

        @Override
        public int getItemCount() {
            return listDates.size();
        }
    }

    private void loadImageWithGlide(String url, ImageView imageView) {
        Glide
                .with(this)
                .load(url)
                .into(imageView);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        String url;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItemDate);
            textView = itemView.findViewById(R.id.textViewItem);
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), PhotoActivity.class);
                intent.putExtra(EXTRA_URL, url);
                startActivity(intent);
            });
        }

        public void bind(String url) {
            this.url = url;
        }
    }
}
