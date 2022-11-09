package com.example.mycoffee.views.adapters

import android.view.*
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mycoffee.R
import com.example.mycoffee.databinding.RecyclerPathItemBinding
import com.example.mycoffee.models.FilePath

class PathRecyclerViewAdapter(private val interaction: Interaction? = null):
    RecyclerView.Adapter<PathRecyclerViewAdapter.ViewHolder>() {
    private lateinit var recyclerPathItemBinding: RecyclerPathItemBinding
    private lateinit var viewHolder: ViewHolder

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FilePath>() {
        override fun areItemsTheSame(oldItem: FilePath, newItem: FilePath): Boolean {
            return oldItem.filePath == newItem.filePath
        }

        override fun areContentsTheSame(oldItem: FilePath, newItem: FilePath): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PathRecyclerViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        recyclerPathItemBinding = RecyclerPathItemBinding.inflate(inflater)
        viewHolder = ViewHolder(recyclerPathItemBinding, interaction)
        return viewHolder
    }

    override fun onBindViewHolder(holder: PathRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class ViewHolder
        (
        _itemViewBinding: RecyclerPathItemBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(_itemViewBinding.root) {
        private val itemBinding = _itemViewBinding
        fun bind(item: FilePath) = with(itemView.rootView) {

            itemBinding.root.setOnClickListener {
                interaction?.onItemClicked(adapterPosition, differ.currentList[adapterPosition])
            }
            itemBinding.text.text = item.filePath
            itemBinding.imageView.setImageResource(R.drawable.ic_path_seperator)

            return@with itemView
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, filePath: FilePath)
    }
}
