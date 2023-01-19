package com.kpstv.composition.adapter.internals

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Since in RecyclerView, when an item is detached it does not mean that the view is destroyed.
 * It can also be reused for other items or in case re-attached again without modification or
 * with modification (onBind will be called).
 *
 * This means any coroutine/flow launch or being collected within this lifecycle should respect
 * this trait of RecyclerView. In such case, it is feasible to cancel the coroutine scope in
 * onDetach & recreate it again to ensure that when the view is attached again
 * (onAttach will be called) the last cancelled coroutine/flow will be launched/collected again.
 */
internal abstract class RecyclerItemLifecycleOwner : LifecycleOwner {

    private var _scope: CoroutineScope? = null
    val lifecycleScope: CoroutineScope get() {
        var scope = _scope
        if (scope != null) return scope
        scope = RecyclerLifecycleCoroutineScopeImpl(lifecycle).apply {
            register()
        }
        _scope = scope
        return scope
    }

    private class RecyclerLifecycleCoroutineScopeImpl(
        private val lifecycle: Lifecycle,
    ) : CoroutineScope, LifecycleEventObserver {
        private var job = SupervisorJob()
        override val coroutineContext: CoroutineContext
            get() = Dispatchers.Main.immediate + job

        init {
            if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
                job.cancel()
            }
        }

        fun register() {
            launch(Dispatchers.Main.immediate) {
                if (lifecycle.currentState >= Lifecycle.State.INITIALIZED) {
                    lifecycle.addObserver(this@RecyclerLifecycleCoroutineScopeImpl)
                } else {
                    job.cancel()
                }
            }
        }
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                lifecycle.removeObserver(this)
                job.cancel()
                return
            }

            if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) && event.targetState.isAtLeast(Lifecycle.State.CREATED)) {
                job.cancel()
                job = SupervisorJob()
            }
        }
    }
}