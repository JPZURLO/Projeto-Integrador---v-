package com.example.gerokernel.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gerokernel.model.entity.ConsultaEntity

@Dao
interface ConsultaDao {
    @Query("SELECT * FROM consultas WHERE usuarioId = :userId")
    fun listarPorUsuario(userId: Int): List<ConsultaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvar(consulta: ConsultaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarVarios(consultas: List<ConsultaEntity>)

    @Query("DELETE FROM consultas WHERE idRemoto = :id OR idLocal = :id")
    fun deletar(id: Int)

    @Query("DELETE FROM consultas")
    fun limparTudo()

    // 🔥 O MOTOR QUE MARCA A PRESENÇA NO MÉDICO
    @Query("UPDATE consultas SET realizada = :status WHERE idRemoto = :id OR idLocal = :id")
    fun atualizarStatusRealizada(id: Int, status: Boolean)
}