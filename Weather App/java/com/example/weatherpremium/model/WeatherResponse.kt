package com.example.weatherpremium.model

data class WeatherResponse(
    val name: String,
    val weather: List<Weather>,
    val main: Main
)

data class Weather(
    val main: String,
    val description: String
)

data class Main(
    val temp: Double
)
