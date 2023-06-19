package com.example.compose_adapter.ui

import android.os.Bundle
import android.view.View
import com.example.compose_adapter.R
import com.example.compose_adapter.databinding.FragmentMainBinding
import com.example.compose_adapter.ui.extensions.AbstractFragment

class MainFragment : AbstractFragment<FragmentMainBinding>(R.layout.fragment_main) {
    override fun attachBinding(view: View) = FragmentMainBinding.bind(view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSample1.setOnClickListener {
            navigator.navigate(PagingExample())
        }
        binding.btnSample2.setOnClickListener {
            navigator.navigate(NestedRecyclerViewExample())
        }
    }
}