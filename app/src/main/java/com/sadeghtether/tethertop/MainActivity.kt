package com.sadeghtether.tethertop

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var priceText: TextView
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(24, 24, 24, 24)
        layout.setBackgroundColor(Color.rgb(18, 25, 40))

        val title = TextView(this)
        title.text = "TetherTop"
        title.textSize = 32f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        priceText = TextView(this)
        priceText.text = "در حال دریافت قیمت..."
        priceText.textSize = 24f
        priceText.setTextColor(Color.GREEN)
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(0, 30, 0, 20)

        statusText = TextView(this)
        statusText.text = "بازار تتر ایران"
        statusText.textSize = 16f
        statusText.setTextColor(Color.LTGRAY)
        statusText.gravity = Gravity.CENTER

        val refresh = TextView(this)
        refresh.text = "به‌روزرسانی قیمت"
        refresh.textSize = 18f
        refresh.setTextColor(Color.WHITE)
        refresh.gravity = Gravity.CENTER
        refresh.setPadding(20, 30, 20, 20)
        refresh.setOnClickListener { loadPrice() }

        layout.addView(title)
        layout.addView(priceText)
        layout.addView(statusText)
        layout.addView(refresh)

        setContentView(layout)
        loadPrice()
    }

    private fun loadPrice() {
        priceText.text = "در حال دریافت قیمت..."
        statusText.text = "در حال اتصال به بازار نوبیتکس"

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                val address =
                    "https://apiv2.nobitex.ir/v3/orderbook/USDTIRT"

                connection =
                    URL(address).openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty(
                    "User-Agent",
                    "TetherTop Android"
                )

                val responseCode = connection.responseCode

                val responseStream =
                    if (responseCode in 200..299) {
                        connection.inputStream
                    } else {
                        connection.errorStream
                    }

                val response = responseStream
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: throw Exception("پاسخ سرور خالی است")

                if (responseCode !in 200..299) {
                    throw Exception(
                        "خطای سرور HTTP $responseCode: " +
                            response.take(150)
                    )
                }

                val result = JSONObject(response)

                val rawPrice = result.optString("lastTradePrice", "")

                if (rawPrice.isBlank() || rawPrice == "null") {
                    throw Exception(
                        "قیمت در پاسخ سرور پیدا نشد: " +
                            response.take(180)
                    )
                }

                val rialPrice = rawPrice.toDoubleOrNull()
                    ?: throw Exception("قیمت دریافتی معتبر نیست: $rawPrice")

                val tomanPrice = rialPrice / 10.0
                val formatter = DecimalFormat("#,##0.##")
                val formattedPrice = formatter.format(tomanPrice)

                runOnUiThread {
                    priceText.text = "$formattedPrice تومان"
                    statusText.text = "آخرین معامله تتر در نوبیتکس"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    priceText.text =
                        "خطا: ${e.javaClass.simpleName}"

                    statusText.text =
                        e.message ?: "خطای نامشخص"
                }
            } finally {
                connection?.disconnect()
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
