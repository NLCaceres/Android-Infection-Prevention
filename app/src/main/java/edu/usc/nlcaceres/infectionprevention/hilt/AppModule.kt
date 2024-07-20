package edu.usc.nlcaceres.infectionprevention.hilt

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.usc.nlcaceres.infectionprevention.util.BaseURL
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import javax.inject.Singleton

/** Module for installing long-living dependencies, in particular Retrofit and Gson */

//?: Installing in the SingletonComponent creates dependencies as soon as they're needed
//?: AND keeps them alive as long as they are needed, de-initing them when the entire App is de-init
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
  companion object {
    // MARK: Retrofit Dependencies
    @Singleton //?: Singleton scoping ensures only 1 instance of each dependency is made for the entire App
    @Provides // - The Repositories use Dispatcher.IO as a default param value in their constructors, BUT this lets them swap
    fun provideIoDispatcher() = Dispatchers.IO // - Useful for a ton of coroutine off-main-thread jobs

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
      .registerTypeAdapter(Instant::class.java, JsonDeserializer { json, _, _ -> Instant.parse(json.asString) })
      .create()

    @Singleton
    @Provides // - Could add qualifier BUT only will use one type of converter (Gson) so unlikely to matter
    fun provideGsonConverterFactory(gson: Gson): retrofit2.Converter.Factory = GsonConverterFactory.create(gson)

    @Singleton // - Excellent example of Dagger's benefits, avoiding a semi-complex companion obj lazily to create a Singleton
    @Provides
    fun provideBaseRetrofitInstance(gsonConverterFactory: retrofit2.Converter.Factory): Retrofit {
      return Retrofit.Builder().baseUrl(BaseURL) // - BaseUrl must end in '/'
        .addConverterFactory(gsonConverterFactory) // - Custom Gson factory based on a GsonBuilder instance
        .build()
    }
  }
}