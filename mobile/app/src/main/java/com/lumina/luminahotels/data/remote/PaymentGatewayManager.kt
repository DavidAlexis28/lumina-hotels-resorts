package com.lumina.luminahotels.data.remote

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object PaymentGatewayManager {

    private val client = OkHttpClient()
    private val gson = com.google.gson.Gson()

    data class PayPalOrderResponse(
        val orderId: String,
        val approveUrl: String
    )

    /**
     * Llama a la Supabase Edge Function 'create-paypal-order' para crear una orden en PayPal Sandbox.
     * Si no hay conexión o credenciales de Edge Function configuradas, provee un Sandbox fallback local simulado.
     */
    suspend fun createPayPalOrder(amountUsd: Double): Result<PayPalOrderResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val jsonBody = """{"amount": $amountUsd, "currency": "USD"}"""
            val requestBody = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("https://ugcyptfjwsrdcjtymasr.supabase.co/functions/v1/create-paypal-order")
                .header("Authorization", "Bearer sb_publishable_EZCAqI7-Z04wQbLpHrgZ6w_LnvMdUvm")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string()

            if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                val json = gson.fromJson(bodyString, com.google.gson.JsonObject::class.java)
                val orderId = json.get("orderId")?.asString ?: json.get("order_id")?.asString
                val approveUrl = json.get("approveUrl")?.asString ?: json.get("approve_url")?.asString
                if (!orderId.isNullOrBlank() && !approveUrl.isNullOrBlank()) {
                    return@runCatching PayPalOrderResponse(orderId, approveUrl)
                }
            }

            // Fallback local Sandbox para demostraciones universitarias en vivo
            val mockOrderId = "PAYPAL-SBX-${System.currentTimeMillis()}"
            val mockApproveUrl = "https://www.sandbox.paypal.com/checkoutnow?token=EC-$mockOrderId"
            PayPalOrderResponse(mockOrderId, mockApproveUrl)
        }
    }

    /**
     * Abre el enlace de aprobación en un Android Custom Tab cifrado bajo TLS/PCI-DSS fuera de la aplicación.
     */
    fun openPayPalCheckout(context: Context, approveUrl: String) {
        try {
            val intent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            intent.launchUrl(context, Uri.parse(approveUrl))
        } catch (e: Exception) {
            android.util.Log.e("PaymentGatewayManager", "Error abriendo Custom Tab de PayPal", e)
        }
    }
}
