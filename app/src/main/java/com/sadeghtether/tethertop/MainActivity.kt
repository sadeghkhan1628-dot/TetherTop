package com.sadeghtether.tethertop

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONObject
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var priceText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(24, 24, 24, 24)
        layout.setBackgroundColor(Color.rgb(18, 25, 40))

        val title = TextView(this)
        title.text = "TetherTop"
        title.textSize = 30f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER

        priceText = TextView(this)
        priceText.text = "در حال دریافت قیمت..."
        priceText.textSize = 22f
        priceText.setTextColor(Color.GREEN)
        priceText.gravity = Gravity.CENTER
        priceText.setPadding(0, 30, 0, 30)

        val refresh = TextView(this)
        refresh.text = "به‌روزرسانی قیمت"
        refresh.textSize = 18f
        refresh.setTextColor(Color.WHITE)
        refresh.gravity = Gravity.CENTER
        refresh.setPadding(20, 20, 20, 20)
        refresh.setOnClickListener { loadPrice() }

        layout.addView(title)
        layout.addView(priceText)
        layout.addView(refresh)

        setContentView(layout)
        loadPrice()
    }

    private fun loadPrice() {
        priceText.text = "در حال دریافت قیمت..."

        executor.execute {
            try {
                val address =
                    "https://api.coingecko.com/api/v3/simple/price?ids=tether&vs_currencies=usd"

                val connection = URL(address).openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.getInputStream()
                    .bufferedReader()
                    .use { it.readText() }

                val price = JSONObject(response)
                    .getJSONObject("tether")
                    .getDouble("usd")

                runOnUiThread {
                    priceText.text = "قیمت تتر: $
