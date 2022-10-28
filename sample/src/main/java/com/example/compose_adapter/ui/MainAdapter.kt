package com.example.compose_adapter.ui

import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.compose_adapter.data.model.Article
import com.example.compose_adapter.data.model.Loader
import com.example.compose_adapter.data.model.Widget
import com.example.compose_adapter.data.model.loaders
import com.example.compose_adapter.databinding.ItemLayoutBinding
import com.example.compose_adapter.databinding.ItemLayoutLoaderBinding
import com.kpstv.composition.adapter.ComposeAdapter
import com.kpstv.composition.adapter.composeAdapter
import com.kpstv.composition.adapter.withLoadingStateAdapters

class MainAdapter {
    private var _adapter: ComposeAdapter<Widget>? = null
    val adapter: ComposeAdapter<Widget> get() = _adapter!!

    fun attachToRecyclerView(parent: RecyclerView) {
        _adapter = composeAdapter {
            addHolder(
                Article::class,
                ItemLayoutBinding::inflate
            ) {
                onBind = { item, _ ->
                    imageView.load(item.imageUrl)
                    title.text = item.title
                    description.text = item.description
                }
            }
        }

        parent.adapter = adapter.withLoadingStateAdapters(
            initialLoadAdapter = composeAdapter {
                setInitialLoadItems(loaders)

                addHolder(
                    Loader::class,
                    ItemLayoutLoaderBinding::inflate
                ) {}
            }
        )
    }
}