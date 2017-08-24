package com.tractive.android.orxo2

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.jakewharton.rxrelay2.ReplayRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class OrxoBus(private val isSerialized: Boolean = false) {

    private val relay: Relay<Any> by lazy {
        if (isSerialized) provideRelay().toSerialized() else provideRelay()
    }

    private val disposables: MutableMap<Any, CompositeDisposable> = mutableMapOf()

    protected abstract fun provideRelay(): Relay<Any>

    fun unregister(eventReceiver: Any) {
        disposables[eventReceiver]?.apply {
            clear()
            disposables.remove(eventReceiver)
        }
    }

    @JvmOverloads
    fun <T> getEvent(subscriber: Any, event: Class<T>, scheduler: Scheduler = AndroidSchedulers.mainThread(), action: (T) -> Unit) = relay
            .toFlowable(BackpressureStrategy.BUFFER)
            .ofType(event)
            .observeOn(scheduler)
            .subscribe(action, { Log.i("Orxo", "Exception in OrxoBus: " + it.message) })
            .register(subscriber)

    fun post(event: Any) = relay.accept(event)

    private fun Disposable.register(eventReceiver: Any) {
        disposables[eventReceiver] = (disposables[eventReceiver] ?: CompositeDisposable()).apply {
                    add(this@register)
                }
    }

    class Behaviour(isSerialized: Boolean = false) : OrxoBus(isSerialized) {
        override fun provideRelay(): Relay<Any> = BehaviorRelay.create()
    }

    class Publish(isSerialized: Boolean = false) : OrxoBus(isSerialized) {
        override fun provideRelay(): Relay<Any> = PublishRelay.create()
    }

    class Replay(isSerialized: Boolean = false) : OrxoBus(isSerialized) {
        override fun provideRelay(): Relay<Any> = ReplayRelay.create()
    }
}

class Orxo(private val subscriber: Any, val bus: OrxoBus, private val scheduler: Scheduler) {

    @JvmOverloads
    fun <T> subscribe(event: Class<T>, schedule: Scheduler = scheduler, action: (T) -> Unit) {
        bus.getEvent(subscriber, event, schedule, action)
    }

    fun unregister() = bus.unregister(subscriber)
}