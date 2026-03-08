package com.appinspector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.AppInfo
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AppListAdapter(
    private val onItemClick: (AppInfo) -> Unit
) : ListAdapter<AppInfo, AppListAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.iv_app_icon)
        private val label: TextView = itemView.findViewById(R.id.tv_app_label)
        private val pkgName: TextView = itemView.findViewById(R.id.tv_pkg_name)
        private val methodCount: TextView = itemView.findViewById(R.id.tv_method_count)
        private val chipGroup: ChipGroup = itemView.findViewById(R.id.chip_group_methods)

        fun bind(app: AppInfo) {
            if (app.icon != null) icon.setImageDrawable(app.icon) else icon.setImageResource(R.drawable.ic_app_default)
            label.text = app.label
            pkgName.text = app.packageName
            methodCount.text = "${app.discoveredBy.size} 种方法"

            chipGroup.removeAllViews()
            // Group by category, one chip per category showing count
            app.discoveredBy.groupBy { it.category }.forEach { (cat, methods) ->
                val chip = Chip(itemView.context).apply {
                    text = if (methods.size == 1) methods.first().displayName
                           else "${cat.displayName}(${methods.size})"
                    setChipBackgroundColorResource(android.R.color.transparent)
                    chipStrokeWidth = 0f
                    val bg = MethodColors.chipBgColorFor(methods.first())
                    setChipBackgroundColor(android.content.res.ColorStateList.valueOf(bg))
                    setTextColor(MethodColors.chipTextColor())
                    textSize = 9f
                    chipMinHeight = 56f
                    isClickable = false
                    isFocusable = false
                    chipCornerRadius = 20f
                }
                chipGroup.addView(chip)
            }

            itemView.setOnClickListener { onItemClick(app) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(old: AppInfo, new: AppInfo) = old.packageName == new.packageName
            override fun areContentsTheSame(old: AppInfo, new: AppInfo) = old.discoveredBy == new.discoveredBy && old.label == new.label
        }
    }
}
