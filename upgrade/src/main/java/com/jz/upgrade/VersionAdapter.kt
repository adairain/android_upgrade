package com.jz.upgrade

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jz.upgrade.databinding.ItemVersionBinding


/**
 * @author zhouyu
 * @date   2023/3/1 15:02
 */
class VersionAdapter(var context: Context, var list: List<AppVersion>) :
    RecyclerView.Adapter<VersionAdapter.ViewHolder>() {

    var showDownloadHistory = false
    var doOnUpgradeClick: ((position: Int, version: AppVersion) -> Unit)? = null
    var allowDownload = true
    var showHistoryVersion = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val viewBinding = ItemVersionBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.viewBinding.ivTvVersion.text = "V${data.versionName}"
        holder.viewBinding.ivTvContent.text = data.description
        holder.viewBinding.ivTvUpgrade.setOnClickListener {
            if (allowDownload) {
                doOnUpgradeClick?.invoke(holder.adapterPosition, data)
            }
        }
        if (showDownloadHistory && showHistoryVersion) holder.viewBinding.ivTvUpgrade.visibility =
            View.VISIBLE else
            holder.viewBinding.ivTvUpgrade.visibility = View.GONE
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var viewBinding: ItemVersionBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

}