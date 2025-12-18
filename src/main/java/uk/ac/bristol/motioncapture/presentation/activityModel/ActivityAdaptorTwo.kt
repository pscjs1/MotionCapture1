package uk.ac.bristol.motioncapture.presentation.activityModel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.ac.bristol.motioncapture.databinding.ItemRowNoimageBinding


class ActivityAdaptorTwo (
    private val items: MutableList<ActivityModelTwo>,
    private val onItemClick : (String) -> Unit // pass the selected activity to the activity selection screen
    ) :
    RecyclerView.Adapter<ActivityAdaptorTwo.ViewHolderTwo>() {
        private lateinit var binding : ItemRowNoimageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderTwo {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowNoimageBinding.inflate(inflater, parent, false)
        return ViewHolderTwo(binding)
    }

    override fun onBindViewHolder(holder: ViewHolderTwo, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolderTwo(itemView: ItemRowNoimageBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(item : ActivityModelTwo) {
            binding.apply {
                tVActivityName.text = item.name
            }
            // Set On Click Listener for item
            itemView.setOnClickListener {
                onItemClick(item.name)
            }
        }
    }
}