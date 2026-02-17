package com.example.jlg_czg_sicenet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface KardexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kardex: KardexEntity)

    @Update
    suspend fun update(kardex: KardexEntity)

    @Query("SELECT * FROM kardex WHERE matricula = :matricula")
    suspend fun getByMatricula(matricula: String): KardexEntity?

    @Query("DELETE FROM kardex WHERE matricula = :matricula")
    suspend fun deleteByMatricula(matricula: String)
}