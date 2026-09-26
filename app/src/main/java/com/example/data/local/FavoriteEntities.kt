package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorite_matches")
data class FavoriteMatchEntity(
    @PrimaryKey val matchId: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val league: String,
    val matchTime: String,
    val tvChannel: String,
    val notifyGoal: Boolean = true,
    val notifyStart: Boolean = true,
    val savedAtTimestamp: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteMatchDao {
    @Query("SELECT * FROM favorite_matches ORDER BY savedAtTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteMatchEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_matches WHERE matchId = :matchId)")
    fun isFavorite(matchId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(match: FavoriteMatchEntity)

    @Query("DELETE FROM favorite_matches WHERE matchId = :matchId")
    suspend fun deleteFavorite(matchId: String)
}

@Database(entities = [FavoriteMatchEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteMatchDao(): FavoriteMatchDao
}
