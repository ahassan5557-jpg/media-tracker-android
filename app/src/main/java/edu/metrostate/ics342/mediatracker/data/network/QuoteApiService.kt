package edu.metrostate.ics342.mediatracker.data.network


import edu.metrostate.ics342.mediatracker.data.model.Quote
import retrofit2.Response
import retrofit2.http.*

interface QuoteApiService {

    @GET("quotes")
    suspend fun getQuotes(
        @Query("public") public: Boolean? = null,
        @Query("limit") limit: Int = 20,
        @Query("after") after: String? = null
    ): Response<List<Quote>>

    @POST("quotes")
    suspend fun createQuote(@Body quote: Quote): Response<Quote>

    @PUT("quotes/{id}")
    suspend fun updateQuote(@Path("id") id: Int, @Body quote: Quote): Response<Quote>

    @DELETE("quotes/{id}")
    suspend fun deleteQuote(@Path("id") id: Int): Response<Unit>

    @POST("quotes/{id}/likes")
    suspend fun likeQuote(@Path("id") id: Int): Response<Unit>

    @DELETE("quotes/{id}/likes")
    suspend fun unlikeQuote(@Path("id") id: Int): Response<Unit>
}
