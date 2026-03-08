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

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _rootAvailable = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _activeCategories = MutableStateFlow<Set<QueryMethod.Category>>(emptySet())
    private val _loading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val activeCategories: StateFlow<Set<QueryMethod.Category>> = _activeCategories.asStateFlow()
    val rootAvailable: StateFlow<Boolean> = _rootAvailable.asStateFlow()

    // 使用分段合并 (combine) 以避免 6 个以上参数时的类型推断问题
    private val filterState = combine(_searchQuery, _activeCategories, _rootAvailable) { query, categories, root ->
        Triple(query, categories, root)
    }

    val uiState: StateFlow<UiState> = combine(
        _loading, _error, _allApps, filterState
    ) { loading, error, apps, filters ->
        val (query, categories, root) = filters

        when {
            loading -> UiState.Loading
            error != null -> UiState.Error(error)
            else -> {
                val filtered = apps.filter { app ->
                    val matchesQuery = query.isBlank() ||
                        app.label.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                    val matchesFilters = categories.isEmpty() ||
                        app.discoveredBy.any { method -> method.category in categories }
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

    fun toggleFilter(category: QueryMethod.Category) {
        val current = _activeCategories.value.toMutableSet()
        if (category in current) current.remove(category) else current.add(category)
        _activeCategories.value = current
    }

    fun clearFilters() {
        _activeCategories.value = emptySet()
    }
}
