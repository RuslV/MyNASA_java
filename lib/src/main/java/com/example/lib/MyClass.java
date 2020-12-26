package com.example.lib;

import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.schedulers.Schedulers;

public class MyClass {
    @SuppressWarnings("CheckResult")
    public static void main(String[] args) {
   /*    long start = System.currentTimeMillis();
        Observable.range(1, 10)
                .concatMapEager(i -> Observable.just(i)
                        .map(MyClass::doubleInt)
                        .subscribeOn(Schedulers.computation()))
                .blockingSubscribe(MyClass::print);
        long stop = System.currentTimeMillis();
        System.out.println((stop-start));

        System.out.println();*/

      /*  Observable.just(12)
                .flatMap(i -> Observable.just(i)
                        .map(MyClass::doubleInt)
                        .subscribeOn(Schedulers.computation())
                        .subscribeOn(Schedulers.io()))
                .blockingSubscribe(System.out::println);*/

   /*     Observable.just(12)
                .map(MyClass::doubleInt)
                .subscribeOn(Schedulers.computation())
                .blockingSubscribe(System.out::println);*/

        List <Integer> list = new ArrayList<>();


        Observable.just(list)
                .map(MyClass::addList)
                .subscribeOn(Schedulers.computation())
                .blockingSubscribe(System.out::println);

    /*    Flowable.range(1, 10)
                .parallel()
                .runOn(Schedulers.computation())
                .map(MyClass::doubleInt)
                .sequential()
                .blockingSubscribe(MyClass::print);*/
    }

    @SuppressWarnings("CheckResult")
    private static int doubleInt(int i) {
        try {
            Thread.sleep(1000);
            System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getName() + ": doubleInt");
            return i * 2;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void print(int i) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getName() + ": " + i);
    }

    private  static List<Integer> addList(List<Integer> list){
        System.out.println(Thread.currentThread().getName() + ": addList");
        list.add(1);
        list.add(2);
        list.add(3);
        return list;
    }
}
