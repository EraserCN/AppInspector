package com.appinspector.ui

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.QueryMethod
import com.appinspector.util.MethodColors

class StatsAdapter(
    private val onItemClick: (MethodStat) -> Unit
) : ListAdapter<MethodStat, StatsAdapter.ViewHolder>(DIFF) {

    private var maxCount = 1

    override fun submitList(list: List<MethodStat>?) {
        maxCount = list?.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tv_stat_name)
        private val count: TextView = view.findViewById(R.id.tv_stat_count)
        private val progress: ProgressBar = view.findViewById(R.id.pb_stat)

        fun bind(stat: MethodStat) {
            // Requirement: If it's the aggregated PackageManager stat, show category name
            name.text = if (stat.isCategoryStat) stat.method.category.displayName else stat.method.displayName
            
            count.text = stat.count.toString()
            progress.max = maxCount
            progress.progress = stat.count
            
            val color = MethodColors.chipTextColorFor(stat.method)
            progress.progressTintList = ColorStateList.valueOf(color)
            
            itemView.setOnClickListener { onItemClick(stat) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MethodStat>() {
            override fun areItemsTheSame(old: MethodStat, new: MethodStat) = 
                old.method == new.method && old.isCategoryStat == new.isCategoryStat
            override fun areContentsTheSame(old: MethodStat, new: MethodStat) = old == new
        }
    }
}
