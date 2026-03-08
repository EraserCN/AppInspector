package com.appinspector.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.AppInfo
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AppDetailSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(app: AppInfo) = AppDetailSheet().apply {
            pendingApp = app
        }
        var pendingApp: AppInfo? = null
    }

    override fun getTheme(): Int = R.style.Theme_AppInspector_BottomSheet

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
        view.findViewById<TextView>(R.id.tv_detail_system).apply {
            text = if (app.isSystemApp) "系统应用" else "用户应用"
            val color = if (app.isSystemApp) 0xFF86868B.toInt() else 0xFF007AFF.toInt()
            setTextColor(color)
            setBackgroundColor(if (app.isSystemApp) 0xFFF1F3F5.toInt() else 0xFFE5F1FF.toInt())
        }
        view.findViewById<TextView>(R.id.tv_detail_method_count).text =
            "共由 ${app.discoveredBy.size} 种方法检测到"

        val rv = view.findViewById<RecyclerView>(R.id.rv_methods)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = MethodDetailAdapter(app.discoveredBy)
    }
}

class MethodDetailAdapter(
    private val methods: Set<QueryMethod>
) : RecyclerView.Adapter<MethodDetailAdapter.VH>() {

    private val allMethods = QueryMethod.values().toList()
        .sortedByDescending { it in methods }
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
                tvFound.setTextColor(ContextCompat.getColor(itemView.context, R.color.status_success))
                tvFound.setBackgroundResource(R.drawable.bg_circle_indicator)
                itemView.alpha = 1f
                
                val catColor = MethodColors.chipTextColorFor(method)
                tvCategory.setTextColor(catColor)
                tvCategory.setBackgroundColor(MethodColors.chipBgColorFor(method))
            } else {
                tvFound.text = ""
                tvFound.setBackgroundResource(R.drawable.bg_circle_indicator_inactive)
                itemView.alpha = 0.5f
                tvCategory.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.surface_variant))
            }
        }
    }
}
