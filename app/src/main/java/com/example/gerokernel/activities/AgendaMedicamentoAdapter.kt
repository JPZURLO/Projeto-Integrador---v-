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
    private val onMedicamentoCheck: (Medicamento) -> Unit,
    private val onConsultaCheck: (ConsultaModel) -> Unit,
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

    inner class MedicamentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardFundo: com.google.android.material.card.MaterialCardView = view as com.google.android.material.card.MaterialCardView
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

            val agora = System.currentTimeMillis()
            val horaDoRemedio = item.horario_agendado?.time ?: 0L
            val jaTomou = item.tomado

            // 🔥 SEMÁFORO CORRIGIDO (Apenas fundo e texto)
            if (jaTomou) {
                // TOMOU = VERDE (E esconde o botão para não clicar de novo)
                cardFundo.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                txtHora.setTextColor(Color.parseColor("#2E7D32"))
                btnTomar.visibility = View.GONE

            } else if (horaDoRemedio > 0L && horaDoRemedio < agora) {
                // ATRASADO = VERMELHO
                cardFundo.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                txtHora.setTextColor(Color.parseColor("#D32F2F"))
                btnTomar.visibility = View.VISIBLE

            } else {
                // FUTURO = BRANCO
                cardFundo.setCardBackgroundColor(Color.WHITE)
                txtHora.setTextColor(Color.parseColor("#0288D1"))
                btnTomar.visibility = View.VISIBLE
            }

            txtDosagem.setTextColor(if (qtd <= 10 && !jaTomou) Color.RED else Color.GRAY)

            btnTomar.setOnClickListener { onMedicamentoCheck(item) }

            btnExcluir.setOnClickListener {
                confirmarExclusao(item.id, "este medicamento")
            }
        }
    }

    inner class ConsultaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardFundo: com.google.android.material.card.MaterialCardView = view as com.google.android.material.card.MaterialCardView
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val txtNome: TextView = view.findViewById(R.id.txtNomeRemedio)
        val txtInfo: TextView = view.findViewById(R.id.txtDosagem)
        val btnTomar: ImageButton = view.findViewById(R.id.btnTomar)
        val btnExcluir: ImageButton = view.findViewById(R.id.btnExcluir)

        fun bind(item: ConsultaModel) {
            txtHora.text = item.dataHora.split(" ").lastOrNull() ?: "--:--"
            txtNome.text = "MÉDICO: ${item.medico}"
            txtInfo.text = "Especialidade: ${item.especialidade}"

            val formatoConsulta = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val horaDaConsulta = try { formatoConsulta.parse(item.dataHora)?.time ?: 0L } catch(e:Exception){ 0L }
            val agora = System.currentTimeMillis()
            val jaFoi = item.realizada

            // 🔥 SEMÁFORO DA CONSULTA CORRIGIDO
            if (jaFoi) {
                // FOI NA CONSULTA = VERDE
                cardFundo.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                txtHora.setTextColor(Color.parseColor("#2E7D32"))
                txtInfo.setTextColor(Color.parseColor("#2E7D32"))
                btnTomar.visibility = View.GONE // Esconde o botão pq já passou

            } else if (horaDaConsulta > 0L && horaDaConsulta < agora) {
                // FALTOU NA CONSULTA (ATRASADO) = VERMELHO
                cardFundo.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                txtHora.setTextColor(Color.parseColor("#D32F2F"))
                txtInfo.setTextColor(Color.parseColor("#D32F2F"))
                btnTomar.visibility = View.VISIBLE

            } else {
                // CONSULTA NO FUTURO = BRANCO
                cardFundo.setCardBackgroundColor(Color.WHITE)
                txtHora.setTextColor(Color.parseColor("#0288D1"))
                txtInfo.setTextColor(Color.parseColor("#2E7D32"))
                btnTomar.visibility = View.VISIBLE
            }

            btnTomar.setOnClickListener { onConsultaCheck(item) }

            btnExcluir.setOnClickListener {
                item.id?.let { id -> confirmarExclusao(id, "esta consulta") }
            }
        }
    }

    private fun confirmarExclusao(id: Int, tipo: String) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Excluir Agendamento")
            .setMessage("Deseja realmente remover $tipo da sua agenda?")
            .setPositiveButton("Sim") { _, _ -> onExcluirClick(id) }
            .setNegativeButton("Não", null)
            .show()
    }
}