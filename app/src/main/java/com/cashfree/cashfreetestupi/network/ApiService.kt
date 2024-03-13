package com.cashfree.cashfreetestupi.network

import com.cashfree.cashfreetestupi.model.SimulateRequest
import com.cashfree.cashfreetestupi.model.SimulateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import java.util.HashMap

interface ApiService {
    @POST("simulate")
    suspend fun simulateStatus(
        @HeaderMap headers: HashMap<String, String>,
        @Body reqData: SimulateRequest
    ): Response<SimulateResponse>
}