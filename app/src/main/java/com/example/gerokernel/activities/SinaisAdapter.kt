package com.example.gerokernel.adapters

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gerokernel.R
import com.example.gerokernel.models.SinaisModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SinaisAdapter(
    private val lista: List<SinaisModel>
) : RecyclerView.Adapter<SinaisAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtData: TextView = view.findViewById(R.id.txtData)
        val txtHora: TextView = view.findViewById(R.id.txtHora)
        val txtPressao: TextView = view.findViewById(R.id.txtPressao)
        val txtGlicose: TextView = view.findViewById(R.id.txtGlicose)
        val imgStatus: ImageView = view.findViewById(R.id.imgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Certifique-se de ter criado o layout 'item_sinal.xml'
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sinal, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // 1. DATA E HORA
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && item.data_hora != null) {
            try {
                val dataLimpa = item.data_hora.replace("Z", "")
                val dataObj = LocalDateTime.parse(dataLimpa)
                holder.txtData.text = dataObj.format(DateTimeFormatter.ofPattern("dd/MM"))
                holder.txtHora.text = dataObj.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                holder.txtData.text = "--/--"
                holder.txtHora.text = "--:--"
            }
        } else {
            holder.txtData.text = item.data_hora ?: "-"
        }

        // 2. FORMATAÇÃO POPULAR (O TRUQUE DO 12 POR 8)
        fun formatarInteligente(valor: Int): String {
            // Se for menor que 50 (tipo 12 ou 16), o idoso já digitou simplificado. Não divide!
            if (valor < 50) return valor.toString()

            // Se for maior (tipo 120 ou 160), aí sim divide por 10.
            return if (valor % 10 == 0) (valor / 10).toString() else (valor / 10.0).toString()
        }

        val sysPop = formatarInteligente(item.sistolica)
        val diaPop = formatarInteligente(item.diastolica)

        holder.txtPressao.text = "$sysPop/$diaPop"


        // 3. GLICOSE (Mantém o número original)
        if (item.glicose != null && item.glicose > 0) {
            holder.txtGlicose.text = "${item.glicose}"
        } else {
            holder.txtGlicose.text = "-"
        }

        // 4. CORES DE ALERTA (SEMÁFORO)
        if (item.sistolica >= 140 || item.diastolica >= 90) {
            // Vermelho (Pressão Alta)
            holder.txtPressao.setTextColor(Color.parseColor("#D32F2F"))
            holder.imgStatus.setColorFilter(Color.parseColor("#D32F2F"))
        } else if (item.sistolica >= 130 || item.diastolica >= 85) {
            // Laranja (Atenção)
            holder.txtPressao.setTextColor(Color.parseColor("#F57F17"))
            holder.imgStatus.setColorFilter(Color.parseColor("#F57F17"))
        } else {
            // Verde (Normal)
            holder.txtPressao.setTextColor(Color.parseColor("#2E7D32"))
            holder.imgStatus.setColorFilter(Color.parseColor("#2E7D32"))
        }
    }

    override fun getItemCount() = lista.size
}