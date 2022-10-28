@file:Suppress("unused")

package com.kpstv.composition.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.util.*
import kotlin.collections.set
import kotlin.reflect.KClass

/*
 * Author: Kaustubh Patange (KP)
 */

internal fun interface LifecycleStateObserver : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onChange(source, event)
    }
    fun onChange(source: LifecycleOwner, event: Lifecycle.Event)
}

typealias ViewBindingGenerator<VB> = (inflater: LayoutInflater, parent: ViewGroup?, root: Boolean) -> VB

/**
 * A default extensible diffutil callback.
 *
 * You can provide your own via,
 * ```
 * val adapter = composeAdapter(...) {
 *  diffCallback = MyCustomDiffCallback(...)
 * }
 * ```
 */
open class DiffCallback<ITEM: Any> : DiffUtil.ItemCallback<ITEM>() {
    override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean = oldItem === newItem
    override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean =
        Objects.hashCode(oldItem) == Objects.hashCode(newItem)
}

@CompositionViewHolderMarker
class ViewHolderConfig<ITEM, VB: ViewBinding>(
    /**
     * Called during `onCreateViewHolder`.
     */
    var onCreate: VB.() -> Unit = {},
    /**
     * Called during `onBindViewHolder`.
     */
    var onBind: VB.(ITEM, Int) -> Unit = { _, _ -> },
    /**
     * Called during `onBindViewHolder(..., payloads)`.
     */
    var onBindPayload: VB.(ITEM, Int, List<Any>) -> Unit = { _, _, _ -> },
    /**
     * Called during `onViewAttachedToWindow`.
     */
    var onAttach: VB.() -> Unit = {},
    /**
     * Called during `onViewDetachedToWindow`.
     */
    var onDetach: VB.() -> Unit = {},
) {
    private var owner = Owner()

    /**
     * A [LifecycleOwner] bound to this ViewHolder.
     */
    val lifecycleOwner get() = (owner as LifecycleOwner)

    /**
     * A [Lifecycle] bound to this ViewHolder.
     */
    val lifecycle get() = owner.lifecycle

    /**
     * A [LifecycleCoroutineScope] bound to this ViewHolder, will be cancelled
     * when the ViewHolder is detached.
     */
    val lifecycleScope get() = owner.lifecycleScope

    private class Owner : LifecycleOwner {
        val lifecycleRegistry = LifecycleRegistry(this)

        override fun getLifecycle(): Lifecycle = lifecycleRegistry
    }

    internal fun moveToState(state: Lifecycle.Event) {
        // sometimes adapter does detach view from window before
        // rebinding again even though view is already recycled.
        try {
            owner.lifecycleRegistry.handleLifecycleEvent(state)
        } catch (e: Exception) { }
        if (state == Lifecycle.Event.ON_DESTROY) {
            owner = Owner()
        }
    }
}

data class HolderDefinition<ITEM: Any, VB : ViewBinding>(
    val generateBinding: ViewBindingGenerator<VB>,
    val viewHolderConfig: ViewHolderConfig<ITEM, VB>,
)

@CompositionViewHolderMarker
class AdapterDefinition<ITEM : Any>(
    /**
     * Set a custom [DiffCallback].
     */
    var diffCallback: DiffCallback<ITEM> = DiffCallback()
) {
    val loadItems: ArrayList<ITEM> = arrayListOf()
    val holderDefinitions = HashMap<Int, HolderDefinition<ITEM, ViewBinding>>()

    /**
     * Add a holder definition for a [type] & supporting [ViewBinding] generator
     * through [generateBinding].
     */
    fun <T: ITEM, VB : ViewBinding> addHolder(
        type: KClass<T>,
        generateBinding: ViewBindingGenerator<VB>,
        viewHolderConfig: ViewHolderConfig<T, VB>.() -> Unit
    ) {
        val definition = HolderDefinition(generateBinding, ViewHolderConfig<T, VB>().apply(viewHolderConfig))
        holderDefinitions[type.hashCode()] = definition as HolderDefinition<ITEM, ViewBinding>
    }

    /**
     * Set initial items, useful for adding loading adapters.
     */
    fun setInitialLoadItems(items: List<ITEM>) {
        loadItems.clear()
        loadItems.addAll(items)
    }

    /**
     * Clear all the list of holder definitions added via `addHolder(...)` DSL.
     */
    fun clear() {
        holderDefinitions.clear()
    }
}

/**
 * `ComposeAdapter`, which will help you to create an adapter in a
 * declarative way using Kotlin's DSL while promoting composition
 * so you never have to create adapter & ViewHolder class again.
 *
 * One key feature of `ComposeAdapter` is, all of it's ViewHolders are
 * lifecycle aware meaning you get a lifecycle & lifecycleScope to
 * launch coroutines which will be cancelled when ViewHolder is detached.
 * You also get onCreate, onBind & such functions.
 */
class ComposeAdapter<ITEM: Any>(
    private val definition: AdapterDefinition<ITEM>
) : PagingDataAdapter<ITEM, ComposeAdapter.ComposeViewHolder<ITEM>>(definition.diffCallback) {

    internal var startIndex = 0
    internal var enableLoadState: Boolean = false
        set(value) {
            if (field != value) {
                if (value) {
                    notifyItemRangeInserted(startIndex, definition.loadItems.size)
                } else {
                    notifyItemRangeRemoved(startIndex, definition.loadItems.size)
                }
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeViewHolder<ITEM> {
        val inflater = LayoutInflater.from(parent.context)
        val config = definition.holderDefinitions[viewType] ?: run {
            Log.w("ComposeAdapter", "No ViewHolder definition found for type: $viewType")
            return ComposeViewHolder(
                { View(parent.context) } // fallback
            )
        }
        val binding = config.generateBinding(inflater, parent, false)
        config.viewHolderConfig.moveToState(Lifecycle.Event.ON_CREATE)
        config.viewHolderConfig.onCreate(binding)
        return ComposeViewHolder(binding, config)
    }

    override fun onBindViewHolder(holder: ComposeViewHolder<ITEM>, position: Int) {
        val item = findItem(position) ?: return
        holder.bind(item, position)
    }

    override fun onBindViewHolder(holder: ComposeViewHolder<ITEM>, position: Int, payloads: MutableList<Any>) {
        val item = findItem(position)
        if (payloads.isNotEmpty() && item != null) {
            holder.config?.let { it.viewHolderConfig.onBindPayload(holder.binding, item, position, payloads) }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return if (definition.loadItems.isNotEmpty()) {
            if (enableLoadState) definition.loadItems.size else 0
        } else {
            super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = findItem(position) ?: return -1
        return item::class.hashCode()
    }

    private fun findItem(position: Int): ITEM? {
        return if (definition.loadItems.isNotEmpty()) {
            definition.loadItems[position]
        } else {
            getItem(position)
        }
    }

    override fun onViewAttachedToWindow(holder: ComposeViewHolder<ITEM>) {
        holder.config?.let { config ->
            config.viewHolderConfig.moveToState(Lifecycle.Event.ON_RESUME)
            config.viewHolderConfig.onAttach(holder.binding)
        }
    }

    override fun onViewDetachedFromWindow(holder: ComposeViewHolder<ITEM>) {
        holder.config?.let { config ->
            config.viewHolderConfig.moveToState(Lifecycle.Event.ON_DESTROY)
            config.viewHolderConfig.onDetach(holder.binding)
        }
    }

    // Can't use generics here as it crashes Kotlin's front end compiler :0
    class ComposeViewHolder<ITEM: Any>(
        val binding: ViewBinding,
        val config: HolderDefinition<ITEM, ViewBinding>? = null
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ITEM, position: Int) {
            config?.let { config ->
                config.viewHolderConfig.moveToState(Lifecycle.Event.ON_RESUME)
                config.viewHolderConfig.onBind(binding, item, position)
            }
        }
    }
}

/**
 * A DSL for [ComposeAdapter].
 *
 * - [lifecycleOwner] : Attach to a lifecycle owner to automatically clear ViewHolder's definition,
 *                      useful when Fragment's view is destroyed but Fragment is kept alive.
 */
fun <ITEM : Any> composeAdapter(
    lifecycleOwner: LifecycleOwner? = null,
    config: AdapterDefinition<ITEM>.() -> Unit
): ComposeAdapter<ITEM> {
    val definition = AdapterDefinition<ITEM>().apply(config)

    lifecycleOwner?.apply {
        lifecycle.addObserver(LifecycleStateObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                definition.holderDefinitions.clear() // to avoid memory leaks
            }
        })
    }

    return ComposeAdapter(definition)
}

/**
 * Add loading adapters for initial state (when data is not yet available)
 * & append load adapters to show loading widgets whenever on load more triggers.
 *
 * Both are [ComposeAdapter], you can customize them accordingly.
 */
fun <ITEM: Any> ComposeAdapter<ITEM>.withLoadingStateAdapters(
    initialLoadAdapter: ComposeAdapter<ITEM>? = null,
    appendLoadAdapter: ComposeAdapter<ITEM>? = null,
): ConcatAdapter {
    addLoadStateListener { states ->
        val snapshot = snapshot()

        // initial load
        initialLoadAdapter?.startIndex = 0
        if (snapshot.isEmpty() && states.refresh is LoadState.Loading) {
            initialLoadAdapter?.enableLoadState = true
        } else if (snapshot.isNotEmpty() && states.refresh is LoadState.NotLoading) {
            initialLoadAdapter?.enableLoadState = false
        }

        // append load (when new page is loading)
        appendLoadAdapter?.startIndex = snapshot.lastIndex
        appendLoadAdapter?.enableLoadState = states.append is LoadState.Loading
    }
    return ConcatAdapter(listOfNotNull(initialLoadAdapter, this, appendLoadAdapter))
}