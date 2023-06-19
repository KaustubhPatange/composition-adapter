package com.example.compose_adapter.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import com.example.compose_adapter.R
import com.example.compose_adapter.databinding.FragmentPagingBinding
import com.example.compose_adapter.ui.extensions.AbstractFragment
import kotlinx.coroutines.launch

class PagingExample : AbstractFragment<FragmentPagingBinding>(R.layout.fragment_paging) {

    private val viewModel by viewModels<MainViewModel>()

    override fun attachBinding(view: View) = FragmentPagingBinding.bind(view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainAdapter = MainAdapter()
        mainAdapter.attachToRecyclerView(binding.root)

        viewLifecycleScope.launch {
            viewModel.getTopNews().pagedData
                .flowWithLifecycle(viewLifecycle)
                .collect { mainAdapter.adapter.submitData(viewLifecycle, it) }
        }

        // To refresh (reload all items again), call
        // mainAdapter.adapter.refresh()
    }
}