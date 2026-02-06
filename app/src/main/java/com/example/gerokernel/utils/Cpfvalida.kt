package com.example.gerokernel.utils

object CpfUtils {
    fun isValidCPF(input: String): Boolean {
        val cpf = input.filter { it.isDigit() }
        if (cpf.length != 11) return false
        if (cpf.all { it == cpf[0] }) return false

        val dv1 = run {
            var sum = 0
            for (i in 0..8) sum += (cpf[i].code - '0'.code) * (10 - i)
            val mod = sum % 11
            if (mod < 2) 0 else 11 - mod
        }
        if (dv1 != (cpf[9].code - '0'.code)) return false

        val dv2 = run {
            var sum = 0
            for (i in 0..9) sum += (cpf[i].code - '0'.code) * (11 - i)
            val mod = sum % 11
            if (mod < 2) 0 else 11 - mod
        }
        return dv2 == (cpf[10].code - '0'.code)
    }
}
