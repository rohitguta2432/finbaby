package com.finbaby.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.finbaby.app.data.db.dao.BudgetDao
import com.finbaby.app.data.db.dao.CategoryDao
import com.finbaby.app.data.db.dao.ProfileDao
import com.finbaby.app.data.db.dao.TransactionDao
import com.finbaby.app.data.db.entity.BudgetEntity
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.ProfileEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        ProfileEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class FinBabyDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun profileDao(): ProfileDao

    companion object {
        fun buildDatabase(context: Context): FinBabyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                FinBabyDatabase::class.java,
                "finbaby_db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = Room.databaseBuilder(
                                context.applicationContext,
                                FinBabyDatabase::class.java,
                                "finbaby_db"
                            ).build()
                            database.categoryDao().insertAll(DefaultCategories.list)
                        }
                    }
                })
                .build()
        }
    }
}

object DefaultCategories {
    val list = listOf(
        CategoryEntity(id = 1, name = "Groceries", icon = "shopping_bag", color = 0xFF2E7D32, sortOrder = 0, budgetType = "needs"),
        CategoryEntity(id = 2, name = "Food/Ordering", icon = "restaurant", color = 0xFFE65100, sortOrder = 1, budgetType = "wants"),
        CategoryEntity(id = 3, name = "Petrol/CNG", icon = "local_gas_station", color = 0xFF1565C0, sortOrder = 2, budgetType = "needs"),
        CategoryEntity(id = 4, name = "Rent", icon = "home_work", color = 0xFF6A1B9A, sortOrder = 3, budgetType = "needs"),
        CategoryEntity(id = 5, name = "Bills", icon = "receipt_long", color = 0xFF00838F, sortOrder = 4, budgetType = "needs"),
        CategoryEntity(id = 6, name = "Medical", icon = "medical_services", color = 0xFFC62828, sortOrder = 5, budgetType = "needs"),
        CategoryEntity(id = 7, name = "Shopping", icon = "shopping_cart", color = 0xFFAD1457, sortOrder = 6, budgetType = "wants"),
        CategoryEntity(id = 8, name = "Education", icon = "school", color = 0xFF283593, sortOrder = 7, budgetType = "needs"),
        CategoryEntity(id = 9, name = "Maid/Cook", icon = "cleaning_services", color = 0xFF4E342E, sortOrder = 8, budgetType = "needs"),
        CategoryEntity(id = 10, name = "EMI", icon = "account_balance", color = 0xFFBF360C, sortOrder = 9, budgetType = "needs"),
        CategoryEntity(id = 11, name = "Entertainment", icon = "movie", color = 0xFF7B1FA2, sortOrder = 10, budgetType = "wants"),
        CategoryEntity(id = 12, name = "Transport", icon = "directions_car", color = 0xFF0277BD, sortOrder = 11, budgetType = "needs"),
        CategoryEntity(id = 13, name = "Personal Care", icon = "spa", color = 0xFF00695C, sortOrder = 12, budgetType = "wants"),
        CategoryEntity(id = 14, name = "Recharge", icon = "phone_android", color = 0xFF558B2F, sortOrder = 13, budgetType = "needs"),
        CategoryEntity(id = 15, name = "Salary", icon = "payments", color = 0xFF2E7D32, sortOrder = 14, budgetType = "needs"),
    )
}
