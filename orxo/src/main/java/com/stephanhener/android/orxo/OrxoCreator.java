package com.stephanhener.android.orxo;

import android.support.annotation.NonNull;

import rx.Scheduler;
import rx.functions.Action;
import rx.functions.Action1;

/**
 * Created by hener on 22.08.2016.
 */

public class OrxoCreator<T> {


    private final OrxoBus<T> mBus;
    private Object mObject;
    private Scheduler mScheduler;

    private OrxoCreator(OrxoBus<T> _orxoBus) {
        if (_orxoBus == null) {
            throw new NullPointerException("Orxo bus can't be null");
        }
        mBus = _orxoBus;
    }

    public static <T> OrxoCreator create(@NonNull OrxoBus<T> _orxoBus) {
        return new OrxoCreator<T>(_orxoBus);
    }

    public OrxoCreator object(Object _object) {
        mObject = _object;
        return this;
    }

    public OrxoCreator schedule(Scheduler _schedule) {
        mScheduler = _schedule;
        return this;
    }

    public <R> OrxoCreator subscribe(Class<R> _eventType, Action1<R> _onNext) {
        mBus.register(mObject, _eventType, mScheduler, _onNext);
        return this;
    }
}
