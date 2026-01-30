import express from 'express';
import { PrismaClient } from '@prisma/client';
import cors from 'cors';
import nodemailer from 'nodemailer';

const prisma = new PrismaClient();
const app = express();

app.use(cors());
app.use(express.json());

// 1. Configure o transportador (Mantenha sua Senha de App aqui)
const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'jpzurlo.jz@gmail.com', // Seu e-mail
    pass: 'jrdz yvht mltt jofr'   // Sua Senha de App
  }
});

// 2. Rota Única de Cadastro
app.post('/cadastro', async (req, res) => {
  console.log("==> REQUISIÇÃO RECEBIDA! Dados:", req.body);

  // Extraímos todos os campos do corpo da requisição
  const { nome, email, senha, cpf, data_nascimento, tipo_usuario } = req.body;

  try {
    // Salvando no MySQL via Prisma
    const novoUsuario = await prisma.usuarios.create({
      data: {
        nome,
        email,
        senha,
        cpf,
        data_nascimento,
        // Garante que o tipo combine com o seu Enum
        tipo_usuario: tipo_usuario ? tipo_usuario.toLowerCase() : 'idoso'
      }
    });

    console.log("✅ Usuário salvo no banco! Tentando enviar e-mail...");

    // Envio do e-mail com AWAIT para não travar
    await transporter.sendMail({
      from: '"GeroKernel" <jpzurlo.jz@gmail.com>',
      to: email,
      subject: "Bem-vindo ao GeroKernel!",
      text: `Olá ${nome}, seu cadastro foi realizado com sucesso no GeroKernel.`
    });

    console.log("📧 E-mail disparado com sucesso!");
    res.status(201).json(novoUsuario);

  } catch (error) {
    console.error("❌ Erro no processo:", error);
    res.status(500).json({ error: "Erro interno no servidor." });
  }
}); // Fechamento correto da rota

// 3. Inicialização do Servidor
const PORT = 3000;
app.listen(3000, '0.0.0.0', () => {
  console.log("🚀 SERVIDOR ATIVO EM TODAS AS INTERFACES");
});