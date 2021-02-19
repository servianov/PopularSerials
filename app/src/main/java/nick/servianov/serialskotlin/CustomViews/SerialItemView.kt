package nick.servianov.serialskotlin.CustomViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.R
import nick.servianov.serialskotlin.databinding.SerialItemViewBinding

class SerialItemView(context: Context?, attrs: AttributeSet?): RelativeLayout(context,attrs) {
    //სერიალის სიის ელემენტის view მოდელი, მონაცემების შესავსებად გამოიყენება fillView ფუნქცია
    private lateinit var binding :SerialItemViewBinding
    init {
        initialize()
    }
    private fun initialize() {
        setupView()
    }
    private fun setupView() {
        binding = SerialItemViewBinding.inflate(LayoutInflater.from(context), this, true)
    }
    fun fillView(item: SerialItemModel)
    {
        set_poster(item.poster)
        set_title(item.name)
        set_total_votes(item.total_votes)
        set_average_vote(item.average_vote)
    }
    fun set_poster(url:String) {
        Glide.with(context)
            .load(url)
            .centerCrop()
            .placeholder(R.drawable.default_image)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(binding.poster)
    }
    fun set_title(title:String)
    {
        binding.title.text = title
    }
    @SuppressLint("SetTextI18n")
    fun set_total_votes(total_votes:Int)
    {
        binding.totalVotes.text = "$total_votes votes"
    }
    fun set_average_vote(average_vote:Double)
    {
        val ssb = SpannableStringBuilder(average_vote.toString())
        ssb.setSpan(
            StyleSpan(Typeface.BOLD),0,2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssb.setSpan(
            RelativeSizeSpan(0.8f),2,average_vote.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.averageVote.text = ssb
        if(average_vote >6)
        {
            binding.ratingIcon.setAnimation(R.raw.good)
        }
        else{
            binding.ratingIcon.setAnimation(R.raw.bad)
        }
    }
}