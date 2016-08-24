package com.stephanhener.android.orxo;


import com.jakewharton.rxrelay.Relay;


import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


public abstract class OrxoBus<T> {

    private static final String TAG = "Orxo";
    public static final int EXCEPTION = 0;
    public static final int IGNORE = 1;
    public static final int MULTIPLE = 2;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            EXCEPTION,
            IGNORE,
            MULTIPLE
    })
    public @interface MultiType {
    }

    private Relay<T, T> mRelay;

    private HashMap<Object, HashMap<String, Subscription>> mObjectMap = new HashMap<>();

    public OrxoBus() {
        mRelay = provideRelay();
    }

    protected @MultiType int getMultiType() {
        return IGNORE;
    }


    protected abstract Relay<T, T> provideRelay();


    public void unregister(Object _object) {

        if (_object == null) {
            throw new NullPointerException("Object to unregister can't be null");
        }

        HashMap<String, Subscription> subscriptions = mObjectMap.get(_object);
        if (subscriptions == null) {
            Log.w(TAG, "object has no active subscriptions");
        } else {

            for (Map.Entry<String, Subscription> subscriptionEntry : subscriptions.entrySet()) {
                subscriptionEntry.getValue().unsubscribe();
            }
        }
        mObjectMap.remove(_object);

    }

    public <R> void register(Object _object, Class<R> _eventType, Scheduler _scheduler, Action1<R> _onNext) {

        if (_object == null) {
            throw new NullPointerException("Object to register can't be null");
        }

        HashMap<String, Subscription> subscriptionMap = mObjectMap.get(_object);

        if (subscriptionMap == null) {
            subscriptionMap = new HashMap<>();
            mObjectMap.put(_object, subscriptionMap);
            addSubscription(subscriptionMap, _eventType, _scheduler, _onNext);
            return;
        }

        if (subscriptionMap.containsKey(_eventType.getCanonicalName())) {
            switch (getMultiType()) {

                case EXCEPTION:
                    throw new IllegalStateException("Subscribed twice for the event");
                case IGNORE:
                    //do nothing
                    break;
                case MULTIPLE:
                    addSubscription(subscriptionMap, _eventType, _scheduler, _onNext);
                    break;
            }
        } else {

            addSubscription(subscriptionMap, _eventType, _scheduler, _onNext);
        }
        ;


    }

    private <R> void addSubscription(HashMap<String, Subscription> _subscriptionMap, Class<R> _eventType, Scheduler _scheduler, Action1<R> _onNext) {
        Subscription subscription = mRelay.ofType(_eventType)
                .subscribeOn(Schedulers.io())
                .observeOn(_scheduler)
                .subscribe(_onNext, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable exception) {
                        Log.e("TAG", "error subscribing");
                    }
                });
        _subscriptionMap.put(_eventType.getCanonicalName(), subscription);
    }

    public <R> void register(Object _object, Class<R> _eventType, Action1<R> _onNext) {
        register(_object, _eventType, AndroidSchedulers.mainThread(), _onNext);
    }

    public void send(T _object) {
        mRelay.call(_object);
    }

}
