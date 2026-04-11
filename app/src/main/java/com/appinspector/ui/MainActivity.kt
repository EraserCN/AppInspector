package com.appinspector.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.BuildConfig
import com.appinspector.R
import com.appinspector.data.AppRepository
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: AppListAdapter
    private lateinit var exportAdapter: AppExportAdapter
    private lateinit var statsAdapter: StatsAdapter
    private var currentCategory: QueryMethod.Category? = null

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { exportDataToUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Force true Edge-to-Edge and transparent bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_main)

        // 2. Handle System Bar Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            findViewById<View>(R.id.app_bar).updatePadding(top = systemBars.top)
            findViewById<View>(R.id.layout_stats_content).updatePadding(top = systemBars.top)
            findViewById<View>(R.id.layout_export_content).updatePadding(top = systemBars.top)
            findViewById<View>(R.id.layout_about_content).updatePadding(top = systemBars.top)
            insets
        }

        val repo = AppRepository(applicationContext)
        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(repo) as T
            }
        })[MainViewModel::class.java]

        setupBottomNavigation()
        setupRecyclerView()
        setupSearch()
        setupTabs()
        setupFilterChips()
        setupStatsPage()
        setupExportPage()
        setupAboutPage()
        observeState()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val layoutApps = findViewById<View>(R.id.layout_apps)
        val layoutStats = findViewById<View>(R.id.layout_stats)
        val layoutExport = findViewById<View>(R.id.layout_export)
        val layoutAbout = findViewById<View>(R.id.layout_about)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_apps -> {
                    layoutApps.isVisible = true
                    layoutStats.isVisible = false
                    layoutExport.isVisible = false
                    layoutAbout.isVisible = false
                    true
                }
                R.id.nav_stats -> {
                    layoutApps.isVisible = false
                    layoutStats.isVisible = true
                    layoutExport.isVisible = false
                    layoutAbout.isVisible = false
                    true
                }
                R.id.nav_export -> {
                    layoutApps.isVisible = false
                    layoutStats.isVisible = false
                    layoutExport.isVisible = true
                    layoutAbout.isVisible = false
                    true
                }
                R.id.nav_about -> {
                    layoutApps.isVisible = false
                    layoutStats.isVisible = false
                    layoutExport.isVisible = false
                    layoutAbout.isVisible = true
                    true
                }
                else -> false
            }
        }
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

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout_categories)
        tabLayout.addTab(tabLayout.newTab().setText("全部").setTag(null))
        QueryMethod.Category.entries.forEach { cat ->
            tabLayout.addTab(tabLayout.newTab().setText(cat.displayName).setTag(cat))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentCategory = tab?.tag as? QueryMethod.Category
                updateChips()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFilterChips() {
        lifecycleScope.launch {
            viewModel.methodStats.collectLatest { stats ->
                updateChips(stats)
            }
        }
    }

    private fun updateChips(stats: List<MethodStat> = viewModel.methodStats.value) {
        val chipGroup = findViewById<ChipGroup>(R.id.chip_group_filter)
        val scrollChips = findViewById<View>(R.id.scroll_chips)
        chipGroup.removeAllViews()

        if (currentCategory == null) {
            scrollChips.isVisible = false
            return
        }

        scrollChips.isVisible = true
        val categoryMethods = QueryMethod.entries.filter { it.category == currentCategory }

        categoryMethods.forEach { method ->
            val count = viewModel.uiState.value.let { state ->
                if (state is UiState.Ready) state.apps.count { method in it.discoveredBy } else 0
            }
            
            val chip = Chip(this).apply {
                tag = method
                text = "${method.displayName} ($count)"
                isCheckable = true
                id = View.generateViewId()
                chipStrokeWidth = 0f
                
                val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
                val colors = intArrayOf(MethodColors.chipTextColorFor(method), MethodColors.chipBgColorFor(method))
                chipBackgroundColor = ColorStateList(states, colors)
                setTextColor(ColorStateList(states, intArrayOf(Color.WHITE, MethodColors.chipTextColorFor(method))))
                
                isChecked = method in viewModel.activeMethods.value
            }
            chip.setOnClickListener { viewModel.toggleFilter(method) }
            chipGroup.addView(chip)
        }
    }

    private fun setupStatsPage() {
        val rvStats = findViewById<RecyclerView>(R.id.rv_stats_page)
        val pieChart = findViewById<PieChartView>(R.id.pie_chart)
        
        statsAdapter = StatsAdapter { stat ->
            viewModel.clearFilters()
            viewModel.toggleFilter(stat.method)
            findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_apps
            val tabLayout = findViewById<TabLayout>(R.id.tab_layout_categories)
            for (i in 0 until tabLayout.tabCount) {
                if (tabLayout.getTabAt(i)?.tag == stat.method.category) {
                    tabLayout.getTabAt(i)?.select()
                    break
                }
            }
        }
        
        rvStats.layoutManager = LinearLayoutManager(this)
        rvStats.adapter = statsAdapter

        lifecycleScope.launch {
            viewModel.methodStats.collectLatest { stats ->
                val activeStats = stats.filter { it.count > 0 }
                statsAdapter.submitList(activeStats)
                pieChart.setData(activeStats)
            }
        }
    }

    private fun setupExportPage() {
        val rvExport = findViewById<RecyclerView>(R.id.rv_export)
        val btnExport = findViewById<MaterialButton>(R.id.btn_export_csv)
        
        exportAdapter = AppExportAdapter { count ->
            btnExport.text = "导出已选 ($count)"
        }
        
        rvExport.layoutManager = LinearLayoutManager(this)
        rvExport.adapter = exportAdapter

        findViewById<View>(R.id.btn_select_all).setOnClickListener { exportAdapter.selectAll() }
        findViewById<View>(R.id.btn_select_none).setOnClickListener { exportAdapter.selectNone() }
        findViewById<View>(R.id.btn_select_user).setOnClickListener { exportAdapter.selectSystem(false) }
        findViewById<View>(R.id.btn_select_system).setOnClickListener { exportAdapter.selectSystem(true) }

        btnExport.setOnClickListener {
            if (exportAdapter.selectedPackages.isEmpty()) {
                Toast.makeText(this, "请先选择要导出的应用", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            createDocumentLauncher.launch("AppInspector_Export_${System.currentTimeMillis()}.csv")
        }
    }

    private fun exportDataToUri(uri: Uri) {
        val state = viewModel.uiState.value as? UiState.Ready ?: return
        val selectedPkgs = exportAdapter.selectedPackages
        val apps = state.apps.filter { it.packageName in selectedPkgs }
        val tvStatus = findViewById<TextView>(R.id.tv_export_status)
        
        lifecycleScope.launch {
            try {
                tvStatus.text = "正在导出..."
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream, Charset.forName("UTF-8")).use { writer ->
                            writer.write("\uFEFF")
                            writer.write("应用名称,包名,启动类\n")
                            apps.forEach { app ->
                                val label = app.label.replace(",", " ")
                                val pkg = app.packageName
                                val launch = app.launchActivity ?: "无"
                                writer.write("$label,$pkg,$launch\n")
                            }
                            writer.flush()
                        }
                    }
                }
                tvStatus.text = "导出成功！已保存 ${apps.size} 个应用。"
                Toast.makeText(this@MainActivity, "导出成功", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                tvStatus.text = "导出失败: ${e.message}"
                Toast.makeText(this@MainActivity, "导出失败", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupAboutPage() {
        val versionName = try { packageManager.getPackageInfo(packageName, 0).versionName } catch (e: Exception) { "Unknown" }
        findViewById<TextView>(R.id.tv_about_version).text = getString(R.string.version_format, versionName)
        findViewById<TextView>(R.id.tv_compiler_info).text = BuildConfig.COMPILER_INFO
        findViewById<TextView>(R.id.tv_compiled_platform).text = BuildConfig.COMPILED_PLATFORM
        findViewById<TextView>(R.id.tv_build_date).text = BuildConfig.BUILD_DATE
        findViewById<TextView>(R.id.tv_target_sdk).text = BuildConfig.TARGET_SDK.toString()
        
        findViewById<View>(R.id.btn_github).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url)))
            startActivity(intent)
        }
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
                        tvRoot.setTextColor(ContextCompat.getColor(this@MainActivity, if (state.rootAvailable) R.color.status_success else R.color.text_secondary))
                        adapter.submitList(state.apps)
                        exportAdapter.submitList(state.apps)
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
