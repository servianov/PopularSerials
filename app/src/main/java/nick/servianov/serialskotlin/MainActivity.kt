package nick.servianov.serialskotlin

import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.skydoves.transformationlayout.onTransformationStartContainer
import nick.servianov.serialskotlin.ApiCalls.SerialsApi
import nick.servianov.serialskotlin.Interfaces.SerialsListLoadedCallback
import nick.servianov.serialskotlin.Models.SerialItemModel
import nick.servianov.serialskotlin.Utils.RecyclerViewAdapters
import nick.servianov.serialskotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var serialsList: ArrayList<SerialItemModel> = ArrayList()
    var visibleItemCount: Int = 0
    var totalItemCount: Int = 0
    var pastVisibleItems: Int = 0
    var loading: Boolean = false
    var firstVisibleItems = IntArray(N)
    var totalPages: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        onTransformationStartContainer()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        var page = 1
        val view = binding.root
        setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
        binding.list.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        //ვანიჭებთ სიას ადაპტერს ცარიელი სიით, და შემდგომ ვანახლებთ notifyDataSetChanged() საშუალებით
        binding.list.adapter = RecyclerViewAdapters.ListAdapter(serialsList)
        val api = SerialsApi()
        //ცალკე გამოგვაქვს API-ს პასუხის ინტერფეისი, რადგან არ გვჭირდება ყოველი API-ს გამოძახებისას ახალი ობიექტის შექმნა
        val loadedCallback = object : SerialsListLoadedCallback {
            override fun onSerialsListLoaded(list: ArrayList<SerialItemModel>, total_pages: Int) {
                binding.tryAgain.visibility = View.GONE
                totalPages = total_pages
                for (listitem in list) {
                    serialsList.add(listitem)
                }
                if (serialsList.size > 0) {
                    binding.list.visibility = View.VISIBLE
                } else {
                    binding.list.visibility = View.GONE
                }
                binding.loader.visibility = View.GONE
                binding.list.adapter?.notifyDataSetChanged()
                loading = false
            }

            override fun onError(error: String) {
                //შეცდომის alert-ის ჩვენება
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Error")
                builder.setMessage(error)
                builder.setPositiveButton("Ok") { dialog, _ ->
                    dialog.cancel()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()

                binding.loader.visibility = View.GONE
                //თუ სია ცარიელია,იმას ნიშნავს, რომ პირველივე request-ის დროს მოხდა შეცდომა(მაგ. არასტაბილური ინტერნეტი), ამიტომ ამ შემთხვევაში ვაჩენთ try again ღილაკს, რომლის დაჭერით თავიდან გამოვა პოპულარული სერიალების სიაზე,
                //თუ კი სია არ არის ცარიელი, ეს იმას ნიშნავს რომ იყო scroll, ამიტომ ღილაკის დამატებას აზრი არ აქვს
                if (serialsList.isEmpty()) {
                    binding.list.visibility = View.GONE
                    binding.tryAgain.visibility = View.VISIBLE
                }
            }
        }
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                page = 1
                //ძებნის დროს ვასუფთავებთ სიას და გვერდს ვცვლით 1-ზე
                serialsList.clear()
                binding.loader.visibility = View.VISIBLE
                if (newText != null) {
                    if (newText.isNotEmpty()) {
                        binding.list.adapter?.notifyDataSetChanged()
                        //ძებნის API-ზე გასვლა searchview-ს ტექსტის ცვლილებაზე და თუ ტექსტი არ არის ცარიელი
                        api.searchSerial(binding.search.query.toString(), page, loadedCallback)
                    } else {
                        //ცარიელ ტექსტზე პოპულარულების სიის წამოღება
                        api.getSerialsList(page, loadedCallback)
                    }
                }
                return false
            }
        })
        val load_more_listener: RecyclerView.OnScrollListener =
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) //ქვემოთ სქროლის შემოწმება
                    {
                        visibleItemCount =
                            (binding.list.layoutManager as StaggeredGridLayoutManager).childCount //ელემენტების რაოდენობა, რომელიც ჩანს ეკრანზე
                        totalItemCount =
                            (binding.list.layoutManager as StaggeredGridLayoutManager).itemCount //სულ ელემენტების რაოდენობა
                        firstVisibleItems =
                            (binding.list.layoutManager as StaggeredGridLayoutManager).findFirstVisibleItemPositions(
                                null
                            )
                        if (firstVisibleItems.isNotEmpty()) {
                            pastVisibleItems = firstVisibleItems[0]
                        }
                        if (!loading && page < totalPages) { //ვამოწმებთ,რომ არ მიმდინარეობს ჩატვირთვის პროცესი და მიმდინარე გვერდი დაბრუნებული ჯამური გვერდების რაოდენობაზე ნაკლებია
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) { // ვამოწმებთ, რომ სიის ბოლოში ჩავედით
                                loading = true
                                page++
                                api.getSerialsList(page, loadedCallback)
                            }
                        }
                    }
                    super.onScrolled(recyclerView, dx, dy)
                }
            }
        binding.list.addOnScrollListener(load_more_listener)

        binding.tryAgain.setOnClickListener { api.getSerialsList(page, loadedCallback) }
        api.getSerialsList(page, loadedCallback)
    }
}
