package me.easynap

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import me.easynap.data.TimerPreferenceStore
import me.easynap.data.TimerStore
import me.easynap.data.timerDataStore

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTimerStore(impl: TimerPreferenceStore): TimerStore

    companion object {
        @Provides
        @Singleton
        fun provideTimerDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return context.timerDataStore
        }
    }
}
