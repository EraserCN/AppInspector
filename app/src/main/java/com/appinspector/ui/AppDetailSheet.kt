package com.appinspector.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.AppInfo
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class AppDetailSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_PKG = "pkg"
        fun newInstance(app: AppInfo) = AppDetailSheet().apply {
            // Pass via static cache — avoids Parcelable complexity
            pendingApp = app
        }
        var pendingApp: AppInfo? = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val app = pendingApp ?: run { dismiss(); return }

        view.findViewById<ImageView>(R.id.iv_detail_icon).apply {
            if (app.icon != null) setImageDrawable(app.icon)
            else setImageResource(R.drawable.ic_app_default)
        }
        view.findViewById<TextView>(R.id.tv_detail_label).text = app.label
        view.findViewById<TextView>(R.id.tv_detail_pkg).text = app.packageName
        view.findViewById<TextView>(R.id.tv_detail_system).text =
            if (app.isSystemApp) "系统应用" else "用户应用"
        view.findViewById<TextView>(R.id.tv_detail_method_count).text =
            "被 ${app.discoveredBy.size} 种查询方法发现"

        val rv = view.findViewById<RecyclerView>(R.id.rv_methods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = MethodDetailAdapter(app.discoveredBy)
    }
}

class MethodDetailAdapter(
    private val methods: Set<QueryMethod>
) : RecyclerView.Adapter<MethodDetailAdapter.VH>() {

    // Show all methods, grouped: found ones first (colored), then not-found (gray)
    private val allMethods = QueryMethod.values().toList()
    private val foundSet = methods

    override fun getItemCount() = allMethods.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_method_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(allMethods[position])
    }

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_method_name)
        private val tvDesc: TextView = itemView.findViewById(R.id.tv_method_desc)
        private val tvFound: TextView = itemView.findViewById(R.id.tv_method_found)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_method_category)

        fun bind(method: QueryMethod) {
            val found = method in foundSet
            tvName.text = method.displayName
            tvDesc.text = method.description
            tvCategory.text = method.category.displayName
            if (found) {
                tvFound.text = "✓"
                tvFound.setTextColor(0xFF43A047.toInt())
                itemView.alpha = 1f
                tvCategory.setTextColor(MethodColors.chipBgColorFor(method))
            } else {
                tvFound.text = "✗"
                tvFound.setTextColor(0xFFB0BEC5.toInt())
                itemView.alpha = 0.45f
                tvCategory.setTextColor(0xFFB0BEC5.toInt())
            }
        }
    }
}
