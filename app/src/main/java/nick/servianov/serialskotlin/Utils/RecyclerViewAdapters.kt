package nick.servianov.serialskotlin.Utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.transformationlayout.TransformationCompat
import nick.servianov.serialskotlin.DetailsActivity
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.databinding.ListItemBinding
import kotlin.collections.ArrayList

class RecyclerViewAdapters {

    class ListAdapter(private val serialsList: ArrayList<SerialItemModel>) :
        RecyclerView.Adapter<ListAdapter.ListHolder>() {
        class ListHolder(v: View, binding: ListItemBinding, private val mContext: Context) :
            RecyclerView.ViewHolder(v) {

            var serial: SerialItemModel? = null
            private val serialItem = binding.serialItem
            private val transformationLayout = binding.transformationLayout
            fun bindSerial(serial: SerialItemModel?) {
                this.serial = serial
                serial?.let { serialItem.fillView(it) }
            }

            init {
                itemView.setOnClickListener {
                    val intent = Intent(mContext, DetailsActivity::class.java)
                    intent.putExtra("serial", serial)
                    TransformationCompat.startActivity(transformationLayout, intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListHolder {
            val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context))
            return ListHolder(binding.root, binding, parent.context)
        }

        override fun onBindViewHolder(holder: ListHolder, position: Int) {
            holder.bindSerial(serialsList[position])
        }

        override fun getItemCount(): Int = serialsList.size
    }
}