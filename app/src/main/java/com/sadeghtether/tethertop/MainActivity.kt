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
    private lateinit var updateText: TextView

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

        updateText = TextView(this)
        updateText.text = "قیمت بازار ایران"
        updateText.textSize = 16f
        updateText.setTextColor(Color.LTGRAY)
        updateText.gravity = Gravity.CENTER

        val refresh = TextView(this)
        refresh.text = "به‌روزرسانی قیمت"
        refresh.textSize = 18f
        refresh.setTextColor(Color.WHITE)
        refresh.gravity = Gravity.CENTER
        refresh.setPadding(20, 30, 20, 20)
        refresh.setOnClickListener { loadPrice() }

        layout.addView(title)
        layout.addView(priceText)
        layout.addView(updateText)
        layout.addView(refresh)

        setContentView(layout)
        loadPrice()
    }

    private fun loadPrice() {
        priceText.text = "در حال دریافت قیمت..."
        updateText.text = "اتصال به بازار نوبیتکس"

        executor.execute {
            var connection: HttpURLConnection? = null

            try {
                val address = "https://api.nobitex.ir/market/stats"

                connection = URL(address).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json"
                )

                val request = JSONObject()
                    .put("srcCurrency", "usdt")
                    .put("dstCurrency", "rls")

                connection.outputStream.use { output ->
                    output.write(request.toString().toByteArray(Charsets.UTF_8))
                }

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val result = JSONObject(response)

                if (result.optString("status") != "ok") {
                    throw Exception("Market API error")
                }

                val stats = result.getJSONObject("stats")
                    .getJSONObject("usdt-rls")

                val rialPrice = stats.getString("latest").toDouble()
                val tomanPrice = rialPrice / 10.0

                val formatter = DecimalFormat("#,##0.##")
                val formattedPrice = formatter.format(tomanPrice)

                runOnUiThread {
                    priceText.text = "$formattedPrice تومان"
                    updateText.text = "قیمت آخرین معامله در نوبیتکس"
                }

            } catch (e: Exception) {
                runOnUiThread {
                    priceText.text = "دریافت قیمت ناموفق بود"
                    updateText.text = "اینترنت را بررسی کن و دوباره تلاش کن"
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
