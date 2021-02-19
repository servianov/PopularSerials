package nick.servianov.serialskotlin.Interfaces

import nick.servianov.serialskotlin.Models.SerialItemModel

interface SerialsListLoadedCallback {
    fun onSerialsListLoaded(list: ArrayList<SerialItemModel>, total_pages: Int)
    fun onError(error: String)
}