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
  debug: true,
  logger: true,
  auth: {
    user: 'jpzurlo.jz@gmail.com',
    pass: 'jrdz yvht mltt jofr'
  }
});

// ==========================================
// ROTA 1: CADASTRO (Já existia)
// ==========================================
app.post('/cadastro', async (req, res) => {
  console.log("==> CADASTRO: Dados recebidos:", req.body);
  const { nome, email, senha, cpf, data_nascimento, tipo_usuario } = req.body;

  try {
    const novoUsuario = await prisma.usuarios.create({
      data: {
        nome, email, senha, cpf, data_nascimento,
        tipo_usuario: tipo_usuario ? tipo_usuario.toLowerCase() : 'idoso'
      }
    });

    // Envia e-mail de boas-vindas
    await transporter.sendMail({
      from: '"GeroKernel" <jpzurlo.jz@gmail.com>',
      to: email,
      subject: "Bem-vindo ao GeroKernel!",
      text: `Olá ${nome}, seu cadastro foi realizado com sucesso.`
    });

    res.status(201).json(novoUsuario);
  } catch (error) {
    console.error("❌ Erro no cadastro:", error);
    res.status(500).json({ error: "Erro interno ou usuário já existe." });
  }
});

// ==========================================
// ROTA 2: LOGIN (Já existia)
// ==========================================
app.post('/login', async (req, res) => {
  const { email, senha } = req.body;
  console.log(`🔑 LOGIN: Tentativa para ${email}`);

  try {
    const usuario = await prisma.usuarios.findUnique({
      where: { email: email }
    });

    if (usuario && usuario.senha === senha) {
      res.status(200).json(usuario);
    } else {
      res.status(401).json({ error: "E-mail ou senha incorretos." });
    }
  } catch (error) {
    res.status(500).json({ error: "Erro interno no servidor." });
  }
});

// ==========================================
// ROTA 3: RECUPERAR SENHA (NOVA! 🚀)
// Envia o e-mail com o Deep Link
// ==========================================
app.post('/recuperar-senha', async (req, res) => {
    const { email } = req.body;
    console.log(`📧 RECUPERAÇÃO: Solicitada para ${email}`);

    try {
        // 1. Verifica se o usuário existe
        const usuario = await prisma.usuarios.findUnique({
            where: { email: email }
        });

        if (!usuario) {
            // Por segurança, não dizemos que o e-mail não existe, mas logamos o erro
            return res.status(404).json({ error: "Usuário não encontrado." });
        }

        // 2. O LINK MÁGICO (Deep Link)
        // Isso fará o Android abrir direto na tela de Redefinir
        const linkRecuperacao = `gerokernel://redefinir?email=${email}`;

        // 3. Enviar E-mail
        await transporter.sendMail({
            from: '"Suporte GeroKernel" <jpzurlo.jz@gmail.com>',
            to: email,
            subject: "Redefinição de Senha - GeroKernel",
            html: `
                <div style="font-family: Arial, sans-serif; color: #333; padding: 20px;">
                    <h2 style="color: #2E7D32;">Olá, ${usuario.nome}!</h2>
                    <p>Recebemos um pedido para redefinir sua senha.</p>
                    <p>Toque no botão abaixo para criar uma nova senha no aplicativo:</p>

                    <a href="${linkRecuperacao}" style="
                        background-color: #2E7D32;
                        color: white;
                        padding: 15px 25px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: bold;
                        display: inline-block;
                        margin-top: 10px;">
                        ABRIR APP E CRIAR SENHA
                    </a>

                    <p style="margin-top: 30px; color: #999; font-size: 12px;">
                        Se o botão não funcionar, tente este link:<br>
                        ${linkRecuperacao}
                    </p>
                </div>
            `
        });

        console.log("✅ E-mail de recuperação enviado!");
        res.status(200).json({ message: "E-mail enviado." });

    } catch (error) {
        console.error("❌ Erro ao enviar recuperação:", error);
        res.status(500).json({ error: "Erro ao processar solicitação." });
    }
});

// ==========================================
// ROTA 4: REDEFINIR SENHA (SALVAR NO BANCO) (NOVA! 🚀)
// O Android chama essa rota depois que o idoso digita a senha nova
// ==========================================
app.post('/redefinir-senha', async (req, res) => {
    const { email, novaSenha } = req.body;
    console.log(`🔄 REDEFINIÇÃO: Trocando senha de ${email}`);

    try {
        const usuarioAtualizado = await prisma.usuarios.update({
            where: { email: email },
            data: { senha: novaSenha }
        });

        console.log("✅ Senha atualizada com sucesso!");
        res.status(200).json(usuarioAtualizado);

    } catch (error) {
        console.error("❌ Erro ao atualizar senha no banco:", error);
        res.status(500).json({ error: "Erro ao atualizar senha." });
    }
});

// ==========================================
// INICIALIZAÇÃO
// ==========================================
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 SERVIDOR GeroKernel RODANDO NA PORTA ${PORT}`);
});