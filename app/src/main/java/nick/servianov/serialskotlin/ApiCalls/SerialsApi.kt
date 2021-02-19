package nick.servianov.serialskotlin.ApiCalls

import android.os.AsyncTask
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import nick.servianov.serialskotlin.Interfaces.SerialsListLoadedCallback
import nick.servianov.serialskotlin.Models.SerialItemModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class SerialsApi {
    class SerialToBuild {
        var returned_object: JSONObject? = null
        var callback: SerialsListLoadedCallback? = null
    }

    class SerialResult {
        var callback: SerialsListLoadedCallback? = null
        var list: java.util.ArrayList<SerialItemModel>? = null
        var total_pages = 0
    }

    val apiKey =
        "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI4MDJlZTgyMGRlYzk0MmQwMTBkOTY2Mzg4N2U4NjEwMiIsInN1YiI6IjYwMmFkYjU0ZTcyZmU4MDAzYjBmN2U3NSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.w2ZJW7yBy7CH61jIh-knJ5_-2C5cYvAvkgPoaXUKNEM"

    //პოპულარული სერიალების სიის წამოღება
    fun getSerialsList(page: Int, callback: SerialsListLoadedCallback) {
        if (!AndroidNetworking.isRequestRunning("SerialsApi.getSerialsList")) {
            AndroidNetworking.get("https://api.themoviedb.org/3/tv/popular?page=$page")
                .setTag("SerialsApi.getSerialsList")
                .setPriority(Priority.LOW)
                .addHeaders("Authorization", "Bearer $apiKey")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val toBuild = SerialToBuild()
                        toBuild.callback = callback
                        toBuild.returned_object = response
                        BackgroundProcessing().execute(toBuild)
                    }

                    override fun onError(error: ANError) {
                        show_error(error, callback)
                    }
                })
        }
    }

    //სერიალის ძებნა
    fun searchSerial(query: String, page: Int, callback: SerialsListLoadedCallback) {
        if (!AndroidNetworking.isRequestRunning("SerialsApi.searchSerial")) {
            AndroidNetworking.get("https://api.themoviedb.org/3/search/tv?page=$page&query=$query")
                .setTag("SerialsApi.searchSerial")
                .setPriority(Priority.LOW)
                .addHeaders("Authorization", "Bearer $apiKey")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val toBuild = SerialToBuild()
                        toBuild.callback = callback
                        toBuild.returned_object = response
                        BackgroundProcessing().execute(toBuild)
                    }

                    override fun onError(error: ANError) {
                        show_error(error, callback)
                    }
                })
        }
    }

    //მსგავსი სერიალების სია
    fun getSimilar(id: Int, page: Int, callback: SerialsListLoadedCallback) {
        if (!AndroidNetworking.isRequestRunning("SerialsApi.searchSerial")) {
            AndroidNetworking.get("https://api.themoviedb.org/3/tv/$id/similar?page=$page")
                .setTag("SerialsApi.searchSerial")
                .setPriority(Priority.LOW)
                .addHeaders("Authorization", "Bearer $apiKey")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val toBuild = SerialToBuild()
                        toBuild.callback = callback
                        toBuild.returned_object = response
                        BackgroundProcessing().execute(toBuild)
                    }

                    override fun onError(error: ANError) {
                        show_error(error, callback)
                    }
                })
        }
    }

    //გამომდინარე იქიდან, რომ შესაძლებელია დიდი და კომპლექსური სია დაბრუნდეს, ჯობია background thread-ზე დამუშავდეს, რათა UI არ გაჭედოს
    //სამივე აპის აქვს ერთნაირი პასუხის სტრუქტურა,ასე რომ შეგვიძლია გამოვიყენოთ ერთი ფუნქცია დასამუშავებლად
    private class BackgroundProcessing :
        AsyncTask<SerialToBuild?, Void?, SerialResult>() {
        override fun onPostExecute(response: SerialResult) {
            super.onPostExecute(response)
            response.list?.let { response.callback?.onSerialsListLoaded(it, response.total_pages) }
        }

        override fun doInBackground(vararg params: SerialToBuild?): SerialResult {
            val response = params[0]!!.returned_object
            val callback = params[0]?.callback
            val ObjectsArray = response!!.getJSONArray("results")
            val total_pages = response.getInt("total_pages")
            val list: ArrayList<SerialItemModel> = ArrayList()
            val image_base_url = "https://image.tmdb.org/t/p/w500/"
            for (i in 0 until ObjectsArray.length()) {
                val item = ObjectsArray.getJSONObject(i)
                val id = item.getInt("id")
                val original_name = item.getString("original_name")
                if (!original_name.isEmpty()) {
                    val poster_path = item.getString("poster_path")
                    val overview = if (item.getString("overview")
                            .isEmpty()
                    ) "No description" else item.getString("overview")
                    val vote_average = item.getDouble("vote_average")
                    val vote_count = item.getInt("vote_count")
                    val serialModel = SerialItemModel(
                        id,
                        original_name,
                        image_base_url + poster_path,
                        overview,
                        vote_average,
                        vote_count
                    )
                    list.add(serialModel)
                }
            }
            val result = SerialResult()
            result.callback = callback
            result.list = list
            result.total_pages = total_pages
            return result
        }
    }

    //კოდის დუბლირების ასაცილებლად შეცდომის დაბრუნება გამოტანილია ცალკე ფუნქციაში
    fun show_error(error: ANError, callback: SerialsListLoadedCallback) {
        when (error.errorCode) {
            0 -> callback.onError("No internet connection")
            401 -> callback.onError("No valid API key")
            404 -> callback.onError("Page not found")
            else -> {
                callback.onError("Unexpected error")
            }
        }
    }
}