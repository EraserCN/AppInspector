package com.appinspector.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.AppRepository
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup system bars appearance for edge-to-edge/theming
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        setContentView(R.layout.activity_main)

        val repo = AppRepository(applicationContext)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo) as T
            }
        })[MainViewModel::class.java]

        setupRecyclerView()
        setupSearch()
        setupFilterChips()
        setupAboutButton()
        observeState()
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter { app ->
            AppDetailSheet.pendingApp = app
            AppDetailSheet().show(supportFragmentManager, "detail")
        }
        findViewById<RecyclerView>(R.id.rv_apps).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupSearch() {
        findViewById<SearchView>(R.id.search_view).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { viewModel.setSearch(query.orEmpty()) }
            override fun onQueryTextChange(newText: String?) = true.also { viewModel.setSearch(newText.orEmpty()) }
        })
    }

    private fun setupFilterChips() {
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_filter)

        // "All" chip
        val allChip = Chip(this).apply {
            text = "全部"
            isCheckable = true
            id = View.generateViewId()
            chipStrokeWidth = 0f
            
            val states = arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            )
            val colors = intArrayOf(
                ContextCompat.getColor(context, R.color.brand_color),
                ContextCompat.getColor(context, R.color.surface_variant)
            )
            chipBackgroundColor = ColorStateList(states, colors)
            
            val textColors = intArrayOf(
                ContextCompat.getColor(context, android.R.color.white),
                ContextCompat.getColor(context, R.color.text_primary)
            )
            setTextColor(ColorStateList(states, textColors))
        }
        chipGroup.addView(allChip)
        allChip.setOnClickListener { viewModel.clearFilters() }

        // One chip per category
        QueryMethod.Category.entries.forEach { cat ->
            val firstMethod = QueryMethod.entries.first { it.category == cat }
            val chip = Chip(this).apply {
                tag = cat
                text = cat.displayName
                isCheckable = true
                id = View.generateViewId()
                chipStrokeWidth = 0f
                
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(
                    MethodColors.chipTextColorFor(firstMethod),
                    MethodColors.chipBgColorFor(firstMethod)
                )
                chipBackgroundColor = ColorStateList(states, colors)
                
                val textColors = intArrayOf(
                    ContextCompat.getColor(context, android.R.color.white),
                    MethodColors.chipTextColorFor(firstMethod)
                )
                setTextColor(ColorStateList(states, textColors))
            }
            chip.setOnClickListener {
                viewModel.toggleFilter(cat)
            }
            chipGroup.addView(chip)
        }

        lifecycleScope.launch {
            viewModel.activeCategories.collectLatest { active ->
                allChip.isChecked = active.isEmpty()
                for (i in 1 until chipGroup.childCount) {
                    val chip = chipGroup.getChildAt(i) as Chip
                    val cat = chip.tag as? QueryMethod.Category
                    chip.isChecked = cat != null && cat in active
                }
            }
        }
    }

    private fun setupAboutButton() {
        findViewById<TextView>(R.id.btn_about).setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        val versionName = try {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "Unknown"
        }

        val aboutMessage = buildString {
            append(getString(R.string.app_description))
            append("\n\n")
            append(getString(R.string.version_format, versionName))
            append("\n")
            append(getString(R.string.author_info))
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage(aboutMessage)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                val progress = findViewById<CircularProgressIndicator>(R.id.progress)
                val rvApps = findViewById<RecyclerView>(R.id.rv_apps)
                val tvStatus = findViewById<TextView>(R.id.tv_status)
                val tvRoot = findViewById<TextView>(R.id.tv_root_status)

                when (state) {
                    is UiState.Loading -> {
                        progress.isVisible = true
                        rvApps.isVisible = false
                        tvStatus.text = "正在查询，请稍候..."
                    }
                    is UiState.Ready -> {
                        progress.isVisible = false
                        rvApps.isVisible = true
                        tvStatus.text = "共发现 ${state.apps.size} / ${state.totalCount} 个匹配应用"
                        tvRoot.text = if (state.rootAvailable) "✓ 已获取 Root 权限" else "✗ 未获取 Root 权限"
                        tvRoot.setTextColor(ContextCompat.getColor(this@MainActivity, 
                            if (state.rootAvailable) R.color.status_success else R.color.text_secondary))
                        adapter.submitList(state.apps)
                    }
                    is UiState.Error -> {
                        progress.isVisible = false
                        tvStatus.text = "错误: ${state.message}"
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
