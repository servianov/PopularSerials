package nick.servianov.serialskotlin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.databinding.SimilarSerialItemBinding
import nick.servianov.serialskotlin.databinding.SimilarSerialsViewBinding

class SimilarSerialFragment : Fragment() {
    companion object {
        private const val ITEM = "item"
        private lateinit var item: SerialItemModel
        private var _binding: SimilarSerialItemBinding? = null
        private val binding get() = _binding!!

        //viewPager ადაპტერიდან ვქმნით ახალ instance-ს და გადავცემთ სერიალის ობიექტს
        fun newInstance(item: SerialItemModel) = SimilarSerialFragment().apply {
            arguments = Bundle().apply {
                putSerializable(ITEM, item)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getSerializable(ITEM)?.let {
            item = it as SerialItemModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SimilarSerialItemBinding.inflate(inflater, container, false)
        binding.title.text = item.name
        binding.descrption.text = item.description
        context?.let {
            Glide.with(it)
                .load(item.poster)
                .centerCrop()
                .placeholder(R.drawable.default_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.poster)
        }
        return binding.root
    }
}