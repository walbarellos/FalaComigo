package br.com.falacomigo.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

interface ArasaacApi {
    @GET("pictograms/{language}/search/{text}")
    suspend fun search(
        @Path("language") language: String = "pt",
        @Path("text") text: String
    ): List<ArasaacPictogram>

    @GET("pictograms/{language}/bestsearch/{text}")
    suspend fun bestSearch(
        @Path("language") language: String = "pt",
        @Path("text") text: String
    ): List<ArasaacPictogram>
}

data class ArasaacPictogram(
    @SerializedName("_id") val id: Int,
    @SerializedName("keywords") val keywords: List<Keyword>,
    @SerializedName("categories") val categories: List<String> = emptyList()
) {
    val imageUrl get() = "https://static.arasaac.org/pictograms/$id/${id}_500.png"
    val label get() = keywords.firstOrNull()?.keyword ?: id.toString()
}

data class Keyword(
    val keyword: String,
    val plural: String? = null,
    val meaning: String? = null
)