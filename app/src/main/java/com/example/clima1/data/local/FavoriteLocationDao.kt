package com.example.clima1.data.local

import androidx.room.*
import com.example.clima1.data.model.FavoriteLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteLocation>>

    @Query("SELECT COUNT(*) FROM favorite_locations")
    suspend fun getFavoritesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocation)

    @Delete
    suspend fun deleteFavorite(location: FavoriteLocation)

    @Query("DELETE FROM favorite_locations WHERE id = :locationId")
    suspend fun deleteFavoriteById(locationId: Int)

    @Query("SELECT * FROM favorite_locations WHERE cityName = :cityName LIMIT 1")
    suspend fun getFavoriteByCity(cityName: String): FavoriteLocation?
}

