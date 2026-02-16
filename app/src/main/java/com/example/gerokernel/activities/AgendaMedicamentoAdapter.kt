package com.example.gerokernel.activities

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.models.Medicamento
import com.example.gerokernel.models.ConsultaModel
import java.text.SimpleDateFormat
import java.util.Locale

class AgendaMedicamentoAdapter(
    private val context: Context,
    private val onTomarClick: (Int) -> Unit,
    private val onExcluirClick: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listaGeral: List<Any> = emptyList()

    private val TYPE_MEDICAMENTO = 1
    private val TYPE_CONSULTA = 2

    fun atualizarLista(novaLista: List<Any>) {
        this.listaGeral = novaLista
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (listaGeral[position] is Medicamento) TYPE_MEDICAMENTO else TYPE_CONSULTA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_agenda_medicamento, parent, false)
        return if (viewType == TYPE_MEDICAMENTO) MedicamentoViewHolder(view) else ConsultaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listaGeral[position]
        if (holder is MedicamentoViewHolder && item is Medicamento) holder.bind(item)
        else if (holder is ConsultaViewHolder && item is ConsultaModel) holder.bind(item)
    }

    override fun getItemCount() = listaGeral.size

    // ViewHolder para Medicamentos
    inner class MedicamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val txtNome: TextView = view.findViewById(R.id.txtNomeRemedio)
        val txtDosagem: TextView = view.findViewById(R.id.txtDosagem)
        val btnTomar: ImageButton = view.findViewById(R.id.btnTomar)
        val btnExcluir: ImageButton = view.findViewById(R.id.btnExcluir)

        fun bind(item: Medicamento) {
            val formato = SimpleDateFormat("dd/MM - HH:mm", Locale.getDefault())
            txtHora.text = item.horario_agendado?.let { formato.format(it) } ?: "--:--"
            txtNome.text = item.nome_remedio
            val qtd = item.quantidade_total ?: 0
            txtDosagem.text = "${item.dosagem} • Restam: $qtd"
            txtDosagem.setTextColor(if (qtd <= 10) Color.RED else Color.GRAY)

            btnTomar.visibility = View.VISIBLE
            btnTomar.setOnClickListener { onTomarClick(item.id) }

            // Excluir Medicamento
            btnExcluir.setOnClickListener {
                confirmarExclusao(item.id, "este medicamento")
            }
        }
    }

    // ViewHolder para Consultas (CORRIGIDO COM EXCLUIR)
    inner class ConsultaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val txtNome: TextView = view.findViewById(R.id.txtNomeRemedio)
        val txtInfo: TextView = view.findViewById(R.id.txtDosagem)
        val btnTomar: ImageButton = view.findViewById(R.id.btnTomar)
        val btnExcluir: ImageButton = view.findViewById(R.id.btnExcluir) // Puxando o botão do XML

        fun bind(item: ConsultaModel) {
            // Pega a hora da string "16/02/2026 19:55"
            txtHora.text = item.dataHora.split(" ").lastOrNull() ?: "--:--"
            txtNome.text = "MÉDICO: ${item.medico}"
            txtInfo.text = "Especialidade: ${item.especialidade}"
            txtInfo.setTextColor(Color.parseColor("#2E7D32"))

            btnTomar.visibility = View.GONE // Médico não se toma kkk

            // CONFIGURAÇÃO DO CLIQUE DE EXCLUIR PARA MÉDICO
            btnExcluir.setOnClickListener {
                item.id?.let { id ->
                    confirmarExclusao(id, "esta consulta")
                }
            }
        }
    }

    // Função auxiliar para evitar repetição de código (DevOps style kkk)
    private fun confirmarExclusao(id: Int, tipo: String) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Excluir Agendamento")
            .setMessage("Deseja realmente remover $tipo da sua agenda?")
            .setPositiveButton("Sim") { _, _ -> onExcluirClick(id) }
            .setNegativeButton("Não", null)
            .show()
    }
}