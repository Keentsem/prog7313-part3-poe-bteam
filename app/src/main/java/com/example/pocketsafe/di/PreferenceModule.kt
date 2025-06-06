package com.example.pocketsafe.di

/*
import android.content.Context
import com.example.pocketsafe.util.PreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferenceModule {
    
    @Provides
    @Singleton
    fun providePreferenceHelper(@ApplicationContext context: Context): PreferenceHelper {
        return PreferenceHelper(context)
    }
}
*/

/**
 * PreferenceModule has been temporarily disabled as part of the Hilt DI disabling process.
 * Will be re-enabled when Hilt is properly integrated.
 * 
 * For now, access PreferenceHelper directly through MainApplication.getPreferenceHelper()
 */
