package com.example.compose_adapter.ui.extensions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding

abstract class AbstractFragment<VB: ViewBinding>(@LayoutRes id: Int) : Fragment(id) {
    private var _binding: VB? = null

    val binding get() = _binding!!
    val viewLifecycleScope get() = viewLifecycleOwner.lifecycleScope
    val viewLifecycle get() = viewLifecycleOwner.lifecycle

    abstract fun attachBinding(view: View): VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        _binding = attachBinding(view!!)
        return view
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}