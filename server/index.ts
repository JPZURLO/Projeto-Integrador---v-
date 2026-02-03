import express from 'express';
import { PrismaClient } from '@prisma/client';
import cors from 'cors';
import nodemailer from 'nodemailer';

const prisma = new PrismaClient();
const app = express();

app.use(cors());
app.use(express.json());

// 1. Configure o transportador
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
// ROTA NOVA: O "TRAMPOLIM" (Bypass do Gmail) 🚀
// O e-mail clica aqui (HTTP) -> Node.js -> Redireciona pro App (Deep Link)
// ==========================================
app.get('/abrir-app', (req, res) => {
    const email = req.query.email;

    // O servidor manda o celular abrir o App
    console.log(`🦘 TRAMPOLIM: Redirecionando ${email} para o App...`);
    res.redirect(`gerokernel://redefinir?email=${email}`);
});

// ==========================================
// ROTA 1: CADASTRO
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
// ROTA 2: LOGIN
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
// ROTA 3: RECUPERAR SENHA (ATUALIZADA)
// Envia link HTTP que redireciona para o App
// ==========================================
app.post('/recuperar-senha', async (req, res) => {
    const { email } = req.body;
    console.log(`📧 RECUPERAÇÃO: Solicitada para ${email}`);

    try {
        const usuario = await prisma.usuarios.findUnique({
            where: { email: email }
        });

        if (!usuario) {
            return res.status(404).json({ error: "Usuário não encontrado." });
        }

        // ⚠️ CONFIGURAÇÃO DE IP (MUITO IMPORTANTE)
        // Abra o terminal, digite ipconfig e pegue o endereço IPv4.
        const MEU_IP = "192.168.1.8"; // <--- TROQUE PELO SEU IP AQUI!!!
        const PORTA = 3000;

        // Link seguro que o Gmail aceita (HTTP)
        const linkSeguro = `http://${MEU_IP}:${PORTA}/abrir-app?email=${email}`;

        await transporter.sendMail({
            from: '"Suporte GeroKernel" <jpzurlo.jz@gmail.com>',
            to: email,
            subject: "Redefinição de Senha - GeroKernel",
            html: `
                <div style="font-family: Arial, sans-serif; color: #333; padding: 20px; text-align: center;">
                    <h2 style="color: #2E7D32;">Olá, ${usuario.nome}!</h2>
                    <p>Recebemos um pedido para redefinir sua senha.</p>
                    <p>Toque no botão abaixo para criar uma nova senha:</p>

                    <a href="${linkSeguro}" style="
                        background-color: #2E7D32;
                        color: white;
                        padding: 15px 25px;
                        text-decoration: none;
                        border-radius: 8px;
                        font-weight: bold;
                        display: inline-block;
                        margin-top: 10px;
                        font-size: 16px;">
                        CRIAR NOVA SENHA
                    </a>

                    <p style="margin-top: 20px; color: #999; font-size: 12px;">
                        Se não funcionar, copie este link no navegador do celular:<br>
                        ${linkSeguro}
                    </p>
                </div>
            `
        });

        console.log("✅ E-mail enviado com link HTTP de redirecionamento!");
        res.status(200).json({ message: "E-mail enviado." });

    } catch (error) {
        console.error("❌ Erro ao enviar recuperação:", error);
        res.status(500).json({ error: "Erro ao processar solicitação." });
    }
});

// ==========================================
// ROTA 4: REDEFINIR SENHA (SALVAR NO BANCO)
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

const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 SERVIDOR GeroKernel RODANDO NA PORTA ${PORT}`);
});