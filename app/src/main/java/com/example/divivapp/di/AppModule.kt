package com.example.divivapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.divivapp.data.DivivAppDatabase
import com.example.divivapp.data.MesaDao
import com.example.divivapp.data.MenuSeedData
import com.example.divivapp.network.EdamamApiService
import com.example.divivapp.repository.MesasRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDivivAppDatabase(
        @ApplicationContext context: Context
    ): DivivAppDatabase {

        lateinit var database: DivivAppDatabase

        val callback = object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = database.mesaDao()
                        if (dao.countMenuItems() == 0) {
                            dao.insertMenuItems(MenuSeedData.items)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        database = Room.databaseBuilder(
            context,
            DivivAppDatabase::class.java,
            "divivapp_db"
        )
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

        return database
    }

    @Provides
    @Singleton
    fun provideMesaDao(database: DivivAppDatabase): MesaDao {
        return database.mesaDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): com.google.firebase.firestore.FirebaseFirestore {
        return com.google.firebase.firestore.FirebaseFirestore.getInstance()
    }
}
