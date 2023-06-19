package com.example.compose_adapter.ui

import android.view.View
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import com.example.compose_adapter.data.model.Multiple
import com.example.compose_adapter.data.model.Nested
import com.example.compose_adapter.data.model.Single
import com.example.compose_adapter.databinding.ItemLayoutBinding
import com.example.compose_adapter.databinding.ItemMultipleBinding
import com.example.compose_adapter.databinding.ItemMultipleLayoutBinding
import com.kpstv.composition.adapter.AdapterDefinition
import com.kpstv.composition.adapter.ComposeAdapter
import com.kpstv.composition.adapter.composeAdapter
import kotlin.random.Random

class NestedAdapter {
    private var _adapter: ComposeAdapter<Nested>? = null
    val adapter: ComposeAdapter<Nested> get() = _adapter!!

    fun attachToRecyclerView(parent: RecyclerView) {
        _adapter = composeAdapter {
            /* ViewHolder for single item */
            singleItemHolder()

            /* ViewHolder for multiple item */
            multipleItemHolder()
        }
        parent.adapter = _adapter
    }
}

val r = Random(System.currentTimeMillis())
private fun singleCacheImageUrl(image: String): String {
    return image + "t?=" + r.nextLong()
}

private fun AdapterDefinition<Nested>.singleItemHolder() {
    addHolder(
        Single::class,
        ItemLayoutBinding::inflate
    ) {
        onBind = { item, _ ->
            imageView.load(singleCacheImageUrl(item.imageUrl))
            title.visibility = View.GONE
            description.visibility = View.GONE
        }
    }
}

private fun AdapterDefinition<Nested>.multipleItemHolder() {
    addHolder(
        Multiple::class,
        ItemMultipleLayoutBinding::inflate
    ) {
        onBind = { item, _ ->
            root.adapter = composeAdapter<Nested> {
                addHolder(
                    Single::class,
                    ItemMultipleBinding::inflate
                ) {
                    onBind = { item, _ ->
                        imageView.load(singleCacheImageUrl(item.imageUrl))
                    }
                }
            }.apply {
                submitData(lifecycleOwner.lifecycle, PagingData.from(item.items))
            }
        }
    }
}

