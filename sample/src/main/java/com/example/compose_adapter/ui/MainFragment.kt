package com.example.compose_adapter.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import com.example.compose_adapter.R
import com.example.compose_adapter.databinding.FragmentMainBinding
import com.example.compose_adapter.ui.extensions.AbstractFragment
import kotlinx.coroutines.launch

class MainFragment : AbstractFragment<FragmentMainBinding>(R.layout.fragment_main) {

    private val viewModel by viewModels<MainViewModel>()

    override fun attachBinding(view: View) = FragmentMainBinding.bind(view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainAdapter = MainAdapter()
        mainAdapter.attachToRecyclerView(binding.root)

        viewLifecycleScope.launch {
            viewModel.getTopNews().pagedData
                .flowWithLifecycle(viewLifecycle)
                .collect { mainAdapter.adapter.submitData(viewLifecycle, it) }
        }
    }
}