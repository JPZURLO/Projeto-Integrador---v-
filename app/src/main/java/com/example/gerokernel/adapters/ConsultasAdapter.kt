package com.example.gerokernel.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.models.ConsultaModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConsultasAdapter(
    private val lista: List<ConsultaModel>,
    private val onEditarClick: (ConsultaModel) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ConsultasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMedico: TextView = view.findViewById(R.id.txtMedico)
        val txtEsp: TextView = view.findViewById(R.id.txtEspecialidade)
        val txtData: TextView = view.findViewById(R.id.txtData)
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnDeletar: ImageButton = view.findViewById(R.id.btnDeletar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consulta, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtMedico.text = item.medico
        holder.txtEsp.text = item.especialidade

        try {
            val dataLimpa = item.dataHora.replace("Z", "")
            val dataObj = LocalDateTime.parse(dataLimpa)

            holder.txtData.text =
                "📅 " + dataObj.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            holder.txtHora.text =
                "⏰ " + dataObj.format(DateTimeFormatter.ofPattern("HH:mm"))

        } catch (e: Exception) {
            holder.txtData.text = item.dataHora
            holder.txtHora.text = ""
        }

        // ✏️ EDITAR
        holder.btnEditar.setOnClickListener {
            onEditarClick(item)
        }

        // 🗑 EXCLUIR COM CONFIRMAÇÃO
        holder.btnDeletar.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Excluir consulta")
                .setMessage("Deseja realmente excluir esta consulta?")
                .setPositiveButton("Excluir") { _, _ ->
                    item.id?.let { id -> onDeleteClick(id) }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun getItemCount() = lista.size
}