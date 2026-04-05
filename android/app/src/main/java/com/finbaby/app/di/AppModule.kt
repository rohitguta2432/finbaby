package com.finbaby.app.di

import android.content.Context
import com.finbaby.app.data.db.FinBabyDatabase
import com.finbaby.app.data.db.dao.BudgetDao
import com.finbaby.app.data.db.dao.CategoryDao
import com.finbaby.app.data.db.dao.ProfileDao
import com.finbaby.app.data.db.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FinBabyDatabase {
        return FinBabyDatabase.buildDatabase(context)
    }

    @Provides
    fun provideTransactionDao(db: FinBabyDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: FinBabyDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideBudgetDao(db: FinBabyDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideProfileDao(db: FinBabyDatabase): ProfileDao = db.profileDao()
}
