package com.appinspector.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appinspector.data.AppInfo
import com.appinspector.data.AppRepository
import com.appinspector.data.QueryMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Ready(
        val apps: List<AppInfo>,
        val totalCount: Int,
        val rootAvailable: Boolean
    ) : UiState()
    data class Error(val message: String) : UiState()
}

data class MethodStat(val method: QueryMethod, val count: Int, val isCategoryStat: Boolean = false)

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _rootAvailable = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _activeMethods = MutableStateFlow<Set<QueryMethod>>(emptySet())
    private val _loading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val activeMethods: StateFlow<Set<QueryMethod>> = _activeMethods.asStateFlow()
    val rootAvailable: StateFlow<Boolean> = _rootAvailable.asStateFlow()

    val methodStats: StateFlow<List<MethodStat>> = _allApps.map { apps ->
        val stats = mutableListOf<MethodStat>()
        
        // PackageManager Category: aggregate all its methods into one
        val pmApps = apps.count { app -> app.discoveredBy.any { it.category == QueryMethod.Category.PACKAGE_MANAGER } }
        val firstPmMethod = QueryMethod.entries.first { it.category == QueryMethod.Category.PACKAGE_MANAGER }
        stats.add(MethodStat(firstPmMethod, pmApps, isCategoryStat = true))
        
        // Other methods: keep individual
        QueryMethod.entries.filter { it.category != QueryMethod.Category.PACKAGE_MANAGER }.forEach { method ->
            stats.add(MethodStat(method, apps.count { app -> method in app.discoveredBy }))
        }
        
        stats.sortedByDescending { it.count }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val filterState = combine(_searchQuery, _activeMethods, _rootAvailable) { query, methods, root ->
        Triple(query, methods, root)
    }

    val uiState: StateFlow<UiState> = combine(
        _loading, _error, _allApps, filterState
    ) { loading, error, apps, filters ->
        val (query, methods, root) = filters

        when {
            loading -> UiState.Loading
            error != null -> UiState.Error(error)
            else -> {
                val filtered = apps.filter { app ->
                    val matchesQuery = query.isBlank() ||
                        app.label.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                    val matchesFilters = methods.isEmpty() ||
                        app.discoveredBy.any { it in methods }
                    matchesQuery && matchesFilters
                }
                UiState.Ready(filtered, apps.size, root)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading)

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val (map, rootAvail) = repository.queryAll()
                _allApps.value = map.values
                    .sortedWith(compareBy({ it.label.lowercase() }, { it.packageName }))
                _rootAvailable.value = rootAvail
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }

    fun setSearch(query: String) {
        _searchQuery.value = query
    }

    fun toggleFilter(method: QueryMethod) {
        val current = _activeMethods.value.toMutableSet()
        if (method in current) current.remove(method) else current.add(method)
        _activeMethods.value = current
    }

    fun clearFilters() {
        _activeMethods.value = emptySet()
    }
}
