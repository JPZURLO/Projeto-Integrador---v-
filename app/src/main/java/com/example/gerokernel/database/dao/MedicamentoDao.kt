package com.example.gerokernel.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gerokernel.model.entity.MedicamentoEntity

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE usuarioId = :userId")
    fun listarPorUsuario(userId: Int): List<MedicamentoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvar(medicamento: MedicamentoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun salvarVarios(medicamentos: List<MedicamentoEntity>)

    @Query("DELETE FROM medicamentos WHERE idRemoto = :id OR idLocal = :id")
    fun deletar(id: Int)

    @Query("DELETE FROM medicamentos")
    fun limparTudo()

    @Query("UPDATE medicamentos SET tomado = :status WHERE idRemoto = :id OR idLocal = :id")
    fun atualizarStatusTomado(id: Int, status: Boolean)
}