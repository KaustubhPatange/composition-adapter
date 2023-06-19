package com.example.compose_adapter.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import coil.Coil
import coil.imageLoader
import com.example.compose_adapter.R
import com.example.compose_adapter.ui.extensions.AbstractNavigation
import kotlin.reflect.KClass

class MainActivity : AppCompatActivity(), AbstractNavigation {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FragmentContainerView(this, null).apply {
            id = R.id.container
        })

        navigate(MainFragment(), false)

        supportFragmentManager.addOnBackStackChangedListener {
            val frag = supportFragmentManager.findFragmentById(R.id.container)
            val title = when (frag) {
                is PagingExample -> "Paging Sample"
                is NestedRecyclerViewExample -> "Nested RecyclerView Sample"
                else -> "Compose Adapter"
            }
            supportActionBar?.title = title
        }
    }

    override fun navigate(fragment: Fragment, addToBackStack: Boolean) {
        supportFragmentManager.commit {
            replace(R.id.container, fragment)
            if (addToBackStack) {
                addToBackStack(null)
            }
        }
    }
}