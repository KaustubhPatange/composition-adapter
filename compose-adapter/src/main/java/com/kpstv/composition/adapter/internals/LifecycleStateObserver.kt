package com.kpstv.composition.adapter.internals

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal fun interface LifecycleStateObserver : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onChange(source, event)
    }
    fun onChange(source: LifecycleOwner, event: Lifecycle.Event)
}