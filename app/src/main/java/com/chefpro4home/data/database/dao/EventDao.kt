package com.chefpro4home.data.database.dao

import androidx.room.*
import com.chefpro4home.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: String): Event?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEvents(events: List<Event>)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: String)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}


