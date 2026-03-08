package com.appinspector.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StatsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: MainViewModel
    private val adapter = StatsAdapter { stat ->
        // When clicking a stat in the sheet, apply filter and navigate to app list
        viewModel.clearFilters()
        viewModel.toggleFilter(stat.method)
        
        (activity as? MainActivity)?.let { activity ->
            activity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_apps
            val tabLayout = activity.findViewById<TabLayout>(R.id.tab_layout_categories)
            for (i in 0 until tabLayout.tabCount) {
                if (tabLayout.getTabAt(i)?.tag == stat.method.category) {
                    tabLayout.getTabAt(i)?.select()
                    break
                }
            }
        }
        dismiss()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_stats_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_stats)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.methodStats.collectLatest { stats ->
                // Filter out methods with 0 apps for cleaner stats
                adapter.submitList(stats.filter { it.count > 0 })
            }
        }
    }

    override fun getTheme(): Int = R.style.Theme_AppInspector_BottomSheet
}
