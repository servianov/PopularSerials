package nick.servianov.serialskotlin.Models

import java.io.Serializable
data class SerialItemModel(var id:Int, var name:String,var poster:String,var description:String,var average_vote:Double,var total_votes:Int) : Serializable