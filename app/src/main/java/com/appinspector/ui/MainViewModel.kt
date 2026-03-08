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
    private val _activeFilters = MutableStateFlow<Set<QueryMethod>>(emptySet())
    private val _loading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val activeFilters: StateFlow<Set<QueryMethod>> = _activeFilters.asStateFlow()
    val rootAvailable: StateFlow<Boolean> = _rootAvailable.asStateFlow()

    val uiState: StateFlow<UiState> = combine(
        _loading, _error, _allApps, _searchQuery, _activeFilters, _rootAvailable
    ) { values ->
        val loading = values[0] as Boolean
        val error = values[1] as? String
        val apps = values[2] as List<AppInfo>
        val query = values[3] as String
        val filters = values[4] as Set<QueryMethod>
        val root = values[5] as Boolean

        when {
            loading -> UiState.Loading
            error != null -> UiState.Error(error)
            else -> {
                val filtered = apps.filter { app ->
                    val matchesQuery = query.isBlank() ||
                        app.label.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                    val matchesFilters = filters.isEmpty() ||
                        app.discoveredBy.any { it in filters }
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
        val current = _activeFilters.value.toMutableSet()
        if (method in current) current.remove(method) else current.add(method)
        _activeFilters.value = current
    }

    fun clearFilters() {
        _activeFilters.value = emptySet()
    }
}
