package com.example.gerokernel.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.gerokernel.R
import com.example.gerokernel.activities.AgendaActivity

class LembreteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val medico = intent.getStringExtra("MEDICO") ?: "Consulta"
        val especialidade = intent.getStringExtra("ESPECIALIDADE") ?: "Saúde"
        val id = intent.getIntExtra("ID_CONSULTA", 0)

        val channelId = "canal_agenda_gerokernel"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 🔔 Canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Consulta",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de consultas agendadas"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intentApp = Intent(context, AgendaActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            id, // ⚠️ usa ID único
            intentApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // ✔ ícone válido
            .setContentTitle("⏰ Hora da Consulta")
            .setContentText("Consulta com $medico ($especialidade)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }
}