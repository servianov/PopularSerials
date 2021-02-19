package nick.servianov.serialskotlin

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import nick.servianov.serialskotlin.ApiCalls.SerialsApi
import nick.servianov.serialskotlin.CustomViews.SerialItemView
import nick.servianov.serialskotlin.Interfaces.SerialsListLoadedCallback
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.databinding.SimilarSerialsViewBinding
import kotlin.math.abs

class SimilarSerials(private val serialId: Int) : BottomSheetDialogFragment() {
    private var _binding: SimilarSerialsViewBinding? = null
    private val binding get() = _binding!!
    var page = 1
    var serialsList: ArrayList<SerialItemModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SimilarSerialsViewBinding.inflate(inflater, container, false)
        val api = SerialsApi()
        setupViewPager(binding.pager)
        val loadedCallback: SerialsListLoadedCallback = object : SerialsListLoadedCallback {
            override fun onSerialsListLoaded(list: ArrayList<SerialItemModel>, total_pages: Int) {
                for (listitem in list) {
                    serialsList.add(listitem)
                }
                if (serialsList.isEmpty()) {
                    binding.nodata.visibility = View.VISIBLE
                }
                binding.pager.adapter?.notifyDataSetChanged()
                binding.loader.visibility = View.GONE
            }

            override fun onError(error: String) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Error")
                builder.setMessage(error)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.cancel()
                    //ამ შემთხვევაში შეცდომის დროს ვხურავთ მთლიან დიალოგს
                    dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        api.getSimilar(serialId, page, loadedCallback)
        //3-ზე მეტი view-ს რენდერი არ შევინახოთ მეხსიერებაში
        binding.pager.offscreenPageLimit=2
        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                //ვამოწმებთ ახლად არჩეული ტაბი რამდენი პოზიციით განსხვავდება წინისგან
                val difference = tab?.position?.minus(binding.pager.currentItem)?.let { abs(it) }
                //თუ 1-ზე მეტია განსხვავება, tabLayout-ს ვუთისავთ smoothScroll-ს, რადგან ანიმაციის შემთხვევაში tabLayout ჩაისქროლება ძველ პოზიციაზე და ისევ დაბრუნდება არჩეულზე,
                // ხოლო 1-ით გადასვლის შემთხვევაში ყველა ვარიანტში მიმდინარე წერტილიდან დაიწყება ანიმაცია
                tab?.position?.let { binding.pager.setCurrentItem(it, difference!! <= 1) }
            }
        })
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val sheetInternal: View =
                dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
            val behavior = BottomSheetBehavior.from(sheetInternal)
            //ვხსნით დიალოგს ბოლომდე
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = AppSectionsPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            val view = SerialItemView(
                context,
                null
            ) //ვიყენებთ იგივე view მოდელს, რასაც ვიყენებდით მთავარი გვერდის სიაში
            view.fillView(serialsList[position]) //ვავსებთ არსებული მონაცემებით
            tab.customView = view //ვანიჭებთ custom view-ს ჩვენს ტაბს
        }.attach() //ვაბამთ viewpager-ს და tablayout-ს
    }

    //view pager ადაპტერი
    private inner class AppSectionsPagerAdapter(fa: SimilarSerials) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = serialsList.size
        override fun createFragment(position: Int): Fragment =
            SimilarSerialFragment.newInstance(serialsList[position]) //ვქმნით ფრაგმენტს viewPager გვერდზე საჩვენებლად
    }
}