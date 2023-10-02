package dritan.xhabija.chase.chaseweather

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import dritan.xhabija.chase.chaseweather.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WeatherApplication : Application(), ImageLoaderFactory {

    /**
     * Init Coil's ImageLoader to use memory and disk cache
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    /**
     * Init Koin DI framework
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WeatherApplication)
            modules(
                listOf(
                    appModule
                )
            )
        }
    }
}