package nick.servianov.serialskotlin

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.WindowManager
import com.bumptech.glide.Glide
import com.skydoves.transformationlayout.TransformationAppCompatActivity
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.databinding.ActivityDetailsBinding


class DetailsActivity : TransformationAppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val intent = intent
        //ვიღებთ სიიდან გადაცემულ Serializable სერიალის მოდელს
        val serial = intent.getSerializableExtra("serial") as SerialItemModel
        Glide.with(this)
            .load(serial.poster)
            .placeholder(R.drawable.default_image)
            .centerCrop()
            .into(binding.poster)
        binding.collapsingToolbar.title = serial.name
        binding.descrption.text = serial.description
        val ssb = SpannableStringBuilder(serial.average_vote.toString())
        ssb.setSpan(
            StyleSpan(Typeface.BOLD), 0, 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ssb.setSpan(
            RelativeSizeSpan(0.8f), 2, serial.average_vote.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.rating.text = ssb

        binding.similar.setOnClickListener {
            //მსგავსი სერიალების bottomSheet-ის გახსნა
            val modal = SimilarSerials(serial.id)
            modal.show(supportFragmentManager, "similar" + serial.id)
        }


    }
}