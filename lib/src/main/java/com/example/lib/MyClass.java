package com.example.lib;

import io.reactivex.Observable;

public class MyClass {
    @SuppressWarnings("CheckResult")
    public static void main(String[] args) {
        int a=0;
        int d=1;
        Observable
                .range(0,10)
                .map(s->s+5)
                .subscribe(s-> System.out.print(s+" "));
    }


}
