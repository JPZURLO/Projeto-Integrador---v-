package com.example.gerokernel.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gerokernel.R

class LembreteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // 🔥 A MÁGICA: Agora ele lê exatamente o que a AgendaActivity mandou!
        val titulo = intent.getStringExtra("TITULO") ?: "Lembrete GeroKernel"
        val mensagem = intent.getStringExtra("MENSAGEM") ?: "Você tem um aviso na sua agenda!"
        val idNotificacao = intent.getIntExtra("ID", (System.currentTimeMillis() % 10000).toInt())

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Cria o Canal de Notificação (Obrigatório nas versões novas do Android)
        val channelId = "gerokernel_alertas"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes da Agenda",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de Remédios e Consultas Médicas"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Monta a notificação na tela do Idoso
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ⚠️ Troque pelo ícone do seu App se quiser
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensagem)) // Pra mensagens longas não cortarem
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Faz a notificação pular no topo da tela
            .setAutoCancel(true) // Some quando o idoso clica

        // Dispara o alerta!
        notificationManager.notify(idNotificacao, builder.build())
    }
}