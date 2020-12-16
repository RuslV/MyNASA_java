package com.example.mynasa_java.api.model;

import java.util.ArrayList;
import java.util.List;

public class DateRecycler {
    public static List <DateDTO> listDates=new ArrayList<>();
    private DateDTO dates= new DateDTO();


    public DateRecycler(DateDTO dates) {
        this.dates=dates;
        addDates(dates);
    }

    public DateRecycler() {
    }

    public void addDates(DateDTO dates){
        listDates.add(dates);
    }
}
