package com.appinspector.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appinspector.R
import com.appinspector.data.AppInfo

class AppExportAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : ListAdapter<AppInfo, AppExportAdapter.ViewHolder>(DiffCallback()) {

    val selectedPackages = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_export, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = getItem(position)
        holder.bind(app)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivIcon: ImageView = view.findViewById(R.id.iv_app_icon)
        private val tvLabel: TextView = view.findViewById(R.id.tv_app_label)
        private val tvPkg: TextView = view.findViewById(R.id.tv_app_pkg)
        private val cbExport: CheckBox = view.findViewById(R.id.cb_export)

        fun bind(app: AppInfo) {
            ivIcon.setImageDrawable(app.icon)
            tvLabel.text = app.label
            tvPkg.text = app.packageName
            
            cbExport.setOnCheckedChangeListener(null)
            cbExport.isChecked = app.packageName in selectedPackages
            
            val toggle = {
                if (app.packageName in selectedPackages) {
                    selectedPackages.remove(app.packageName)
                } else {
                    selectedPackages.add(app.packageName)
                }
                cbExport.isChecked = app.packageName in selectedPackages
                onSelectionChanged(selectedPackages.size)
            }

            itemView.setOnClickListener { toggle() }
            cbExport.setOnClickListener { toggle() }
        }
    }

    fun selectAll() {
        currentList.forEach { selectedPackages.add(it.packageName) }
        notifyDataSetChanged()
        onSelectionChanged(selectedPackages.size)
    }

    fun selectNone() {
        selectedPackages.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedPackages.size)
    }

    fun selectSystem(system: Boolean) {
        currentList.forEach { 
            if (it.isSystemApp == system) selectedPackages.add(it.packageName)
            else selectedPackages.remove(it.packageName)
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedPackages.size)
    }

    private class DiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo) = oldItem.packageName == newItem.packageName
        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo) = oldItem == newItem
    }
}
