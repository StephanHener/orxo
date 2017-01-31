package com.tractive.android.orxo

import com.jakewharton.rxrelay.Relay
import rx.Scheduler
import rx.Subscription
import rx.functions.Action1
import rx.subscriptions.CompositeSubscription
import java.util.*


abstract class OrxoBus {

    val relay: Relay<Any, Any> by lazy {
        provideRelay()
    }

    private val subscriptions: HashMap<Any, CompositeSubscription> by lazy {
        HashMap<Any, CompositeSubscription>()
    }

    protected abstract fun provideRelay(): Relay<Any, Any>

    fun unregister(_subscriber: Any) {
        subscriptions[_subscriber]?.apply {
            clear()
            subscriptions.remove(_subscriber)
        }
    }

    fun <T> getEvent(_subscriber: Any, _event: Class<T>, _scheduler: Scheduler, _action: Action1<T>) = relay
            .ofType(_event)
            .observeOn(_scheduler)
            .subscribe(_action)
            .register(_subscriber)

    fun post(_event: Any) = relay.call(_event)

    private fun register(_object: Any, subscription: Subscription) {
        subscriptions[_object] = (subscriptions[_object] ?: CompositeSubscription())
                .apply {
                    add(subscription)
                }
    }

    private fun Subscription.register(_subscriber: Any) = register(_subscriber, this)
}

class OrxoHandler(val bus: OrxoBus, private val subscriber: Any, private val scheduler: Scheduler) {

    @JvmOverloads
    fun <T> subscribe(_event: Class<T>, _schedule: Scheduler = scheduler, _action1: Action1<T>) {
        bus.getEvent(subscriber, _event, _schedule, _action1)
    }
}






