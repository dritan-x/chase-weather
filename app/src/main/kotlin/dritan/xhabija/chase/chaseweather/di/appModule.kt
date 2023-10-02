package dritan.xhabija.chase.chaseweather.di

import coil.ImageLoader
import dritan.xhabija.chase.chaseweather.BuildConfig
import dritan.xhabija.chase.chaseweather.network.weather.WeatherApiService
import dritan.xhabija.chase.chaseweather.network.weather.WeatherRepository
import dritan.xhabija.chase.chaseweather.ui.home.WeatherViewModel
import dritan.xhabija.chase.chaseweather.location.LocationViewModel
import dritan.xhabija.chase.chaseweather.ui.search.SearchRepository
import dritan.xhabija.chase.chaseweather.ui.search.SearchViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    // LocationViewModel declared as single to retain it for the duration of app's lifecycle.
    single {
        LocationViewModel(get())
    }

    single {
        WeatherViewModel(get())
    }

    single {
        SearchViewModel(get())
    }

    // SearchRepository used for retaining and retrieving last searches
    single {
        SearchRepository(get())
    }

    single {
        WeatherRepository(get())
    }

    // okhttpclient factory
    factory<OkHttpClient> {
        OkHttpClient().newBuilder().addInterceptor(object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                val url = request.url().newBuilder()
                    .addQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                    .addQueryParameter("units", "imperial")
                    .build()
                val newReq = request.newBuilder().url(url).build()
                return chain.proceed(newReq)
            }

        }).build()
    }

    // one Retrofit to rule them all
    single<Retrofit> {
        Retrofit.Builder().baseUrl(BuildConfig.WEATHER_API_URL).client(get())
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    // WeatherApi service
    factory<WeatherApiService> {
        val retrofit: Retrofit = get()
        retrofit.create(WeatherApiService::class.java)
    }
}