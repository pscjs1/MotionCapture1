package uk.ac.bristol.motioncapture.presentation.watchInputMethod

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uk.ac.bristol.motioncapture.databinding.ItemRowWithimageBinding


class ActivityAdaptor (
    private val items: MutableList<ActivityModel>,
    private val onItemClick : (String) -> Unit // pass the selected activity to the activity selection screen
    ) :
    RecyclerView.Adapter<ActivityAdaptor.ViewHolder>() {
        private lateinit var binding : ItemRowWithimageBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemRowWithimageBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(itemView: ItemRowWithimageBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(item : ActivityModel) {
            binding.apply {
                textView.text = item.name
                imageView.setImageResource(item.image)
            }
            // Set On Click Listener for item
            itemView.setOnClickListener {
                onItemClick(item.name)
            }
        }
    }
}