package com.example.compose_adapter.ui

import android.os.Bundle
import android.view.View
import androidx.paging.PagingData
import com.example.compose_adapter.R
import com.example.compose_adapter.data.model.Multiple
import com.example.compose_adapter.data.model.Nested
import com.example.compose_adapter.data.model.SampleResponseJsonAdapter
import com.example.compose_adapter.data.model.Single
import com.example.compose_adapter.databinding.FragmentMainBinding
import com.example.compose_adapter.databinding.FragmentPagingBinding
import com.example.compose_adapter.ui.extensions.AbstractFragment
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

class NestedRecyclerViewExample : AbstractFragment<FragmentPagingBinding>(R.layout.fragment_paging) {

    private val factory = PolymorphicJsonAdapterFactory.of(Nested::class.java, "type")
        .withSubtype(Single::class.java, "single")
        .withSubtype(Multiple::class.java, "multiple")

    private val moshi = Moshi.Builder().add(factory).build()

    override fun attachBinding(view: View) = FragmentPagingBinding.bind(view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val json = resources.openRawResource(R.raw.sample).bufferedReader().readText()
        val data = SampleResponseJsonAdapter(moshi).fromJson(json)!!

        val nestedAdapter = NestedAdapter()
        nestedAdapter.attachToRecyclerView(binding.root)
        nestedAdapter.adapter.submitData(viewLifecycle, PagingData.from(data.items))
    }
}