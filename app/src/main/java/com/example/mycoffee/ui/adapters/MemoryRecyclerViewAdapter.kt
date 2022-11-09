package com.example.mycoffee.ui.adapters

import android.graphics.Color.GRAY
import android.graphics.Color.WHITE
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.PopupMenu
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mycoffee.databinding.MemoryListItemBinding
import com.example.mycoffee.models.File
import com.example.mycoffee.viewModels.MemoryViewModel
import com.example.mycoffee.R

class MemoryRecyclerViewAdapter(private val interaction: Interaction? = null):
    RecyclerView.Adapter<MemoryRecyclerViewAdapter.ViewHolder>() {

    private var selectionItemsTracker: SelectionTracker<Long>? = null
    private lateinit var listViewBinding: MemoryListItemBinding
    lateinit var viewHolder: ViewHolder

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.file.name == newItem.file.name
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
        //set ID for each list item to get when start multi selection
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        listViewBinding = MemoryListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(listViewBinding, interaction)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    //get ID of the selected item, this function should be overridden when using setHasStableIds(true)
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    //set memory fragment tracker to the adapter
    fun setTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionItemsTracker = selectionTracker
    }

    inner class ViewHolder(_itemViewBinding: MemoryListItemBinding,
                           private val interaction: Interaction?): RecyclerView.ViewHolder(_itemViewBinding.root),
        PopupMenu.OnMenuItemClickListener {
        private val itemBinding = _itemViewBinding

        fun bind(item: File) = with(itemView.rootView) {
            itemBinding.root.setOnClickListener {
                interaction?.onItemClicked(adapterPosition, differ.currentList[adapterPosition])
            }
            itemBinding.file = item

//            if(selectionItemsTracker!!.isSelected(position.toLong())) {
//                itemView.background = ColorDrawable(GRAY)
//                itemBinding.checkBox.isChecked = true
//            }
//            else {
//                itemView.background = ColorDrawable(WHITE)
//                itemBinding.checkBox.isChecked = false
//            }
            itemBinding.root.isActivated = true
        }

        //get details object of selected item in recyclerView
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object: ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = itemId
            }

//        private fun showPopUpMenu(v: View?, position: Int) {
//            val popup = PopupMenu(v?.context, v)
//            popup.inflate(R.menu.list_item_menu)
//            val menu: Menu = popup.menu
//            menu.removeItem(R.id.select_all)
//            menu.removeItem(R.id.dismiss)
//            menu.removeItem(R.id.create_folder)
//            if (differ.currentList[position].file.isDirectory) menu.removeItem(R.id.share)
//            popup.setOnMenuItemClickListener(this)
//            popup.show()
//        }

        override fun onMenuItemClick(p0: MenuItem?): Boolean {
            when(p0?.itemId){
//                R.id.copy -> {
//                    Toast.makeText(viewHolder.itemBinding.arrowDown.context, "item selected", Toast.LENGTH_LONG).show()
//                    return true
//                }
            }
            return false
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, file: File)
    }
}
