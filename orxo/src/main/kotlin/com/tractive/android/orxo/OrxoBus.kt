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


    fun unregister(_object: Any) {
        subscriptions[_object]?.apply {
            clear()
            subscriptions.remove(_object)
        }
    }

    fun <T> getEvent(_subscriber: Any, _event: Class<T>, _scheduler: Scheduler, _action: Action1<T>) = this@OrxoBus.relay
            .ofType(_event)
            .observeOn(_scheduler)
            .subscribe(_action)
            .register(_subscriber)

    fun send(_subscriber: Any) = relay.call(_subscriber)

    private fun register(_object: Any, subscription: Subscription) {
        subscriptions[_object] = (subscriptions[_object] ?: CompositeSubscription())
                .apply {
                    add(subscription)
                }
    }

    private fun Subscription.register(_subscriber: Any) = register(_subscriber, this)


}

class OrxoHandler(val bus: OrxoBus, private val subscriber: Any, private val scheduler: Scheduler) {

    fun <T> subscribe(_event: Class<T>, _action1: Action1<T>) {
        subscribe(_event, scheduler, _action1)
    }

    fun <T> subscribe(_event: Class<T>, _schedule: Scheduler, _action1: Action1<T>) {
        bus.getEvent(subscriber, _event, _schedule, _action1);
    }
}






