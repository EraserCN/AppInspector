package com.appinspector.ui

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
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
import com.appinspector.util.MethodColors
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
            methodCount.text = "${app.discoveredBy.size} Methods"

            chipGroup.removeAllViews()
            // Aggregate by category for the item preview to save space
            app.discoveredBy.groupBy { it.category }.forEach { (cat, methods) ->
                val firstMethod = methods.first()
                
                // Use TextView instead of Chip for extreme compaction and no vertical padding issues
                val tagView = TextView(itemView.context).apply {
                    text = if (methods.size == 1) firstMethod.displayName
                           else "${cat.displayName} (${methods.size})"
                    
                    val bg = MethodColors.chipBgColorFor(firstMethod)
                    val txt = MethodColors.chipTextColorFor(firstMethod)
                    
                    setTextColor(txt)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f)
                    includeFontPadding = false
                    gravity = Gravity.CENTER
                    
                    background = GradientDrawable().apply {
                        setColor(bg)
                        cornerRadius = 4f * itemView.resources.displayMetrics.density
                    }
                    
                    // Compact padding: 4dp horizontal, 1dp vertical
                    val hp = (4f * itemView.resources.displayMetrics.density).toInt()
                    val vp = (1f * itemView.resources.displayMetrics.density).toInt()
                    setPadding(hp, vp, hp, vp)
                }
                
                chipGroup.addView(tagView)
            }

            itemView.setOnClickListener { onItemClick(app) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(old: AppInfo, new: AppInfo) = old.packageName == new.packageName
            override fun areContentsTheSame(old: AppInfo, new: AppInfo) = 
                old.discoveredBy == new.discoveredBy && old.label == new.label
        }
    }
}
