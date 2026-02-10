package com.example.gerokernel.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class HidratacaoReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Por enquanto, apenas mostra um Toast
        // Depois podemos colocar a notificação completa aqui
        Toast.makeText(context, "Hora de beber água! 💧", Toast.LENGTH_LONG).show()
    }
}