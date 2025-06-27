package com.example.weatherpremium

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherpremium.R
import com.example.weatherpremium.model.WeatherResponse
import com.example.weatherpremium.network.WeatherService
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var cityInput: EditText
    private lateinit var searchButton: Button
    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var weatherStatus: TextView
    private lateinit var mainLayout: RelativeLayout

    private val apiKey = "20af16f7014c8ab1e5576e4cb220fa52" // Your real API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cityInput = findViewById(R.id.city_input)
        searchButton = findViewById(R.id.search_button)
        cityName = findViewById(R.id.city_name)
        temperature = findViewById(R.id.temperature)
        weatherStatus = findViewById(R.id.weather_status)
        mainLayout = findViewById(R.id.main_layout)

        searchButton.setOnClickListener {
            val city = cityInput.text.toString().trim()
            if (city.isNotEmpty()) {
                fetchWeather(city)
            } else {
                Toast.makeText(this, "Please enter a city", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(city: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getWeatherByCity(city, apiKey)

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.let {
                        cityName.text = it.name
                        temperature.text = "${it.main.temp}°C"
                        weatherStatus.text = it.weather[0].description

                        changeBackground(it.weather[0].main)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun changeBackground(condition: String) {
        val backgroundRes = when (condition.lowercase()) {
            "clear" -> R.drawable.sunny_bg
            "clouds" -> R.drawable.cloud_bg
            "rain" -> R.drawable.rain_bg
            "snow" -> R.drawable.snow_bg
            "thunderstorm" -> R.drawable.storm_bg
            else -> R.drawable.default_bg
        }
        mainLayout.setBackgroundResource(backgroundRes)
    }
}
