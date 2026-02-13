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

// ROTA 5: SALVAR SINAIS VITAIS (POST)
app.post('/sinais', async (req, res) => {
  console.log("==> SINAIS: Recebendo medição...", req.body);

  // CORREÇÃO: Ler 'usuario_id' (com underline) que vem do Android
  const { usuario_id, sistolica, diastolica, glicose } = req.body;

  try {
    const novoRegistro = await prisma.sinais_vitais.create({
      data: {
        // CORREÇÃO: Usar a variável usuario_id que lemos acima
        usuario_id: Number(usuario_id),
        sistolica: Number(sistolica),
        diastolica: Number(diastolica),
        glicose: glicose ? Number(glicose) : null
      }
    });

    console.log("✅ Sinais salvos com sucesso!");
    res.status(201).json(novoRegistro);



  } catch (error) {
    console.error("❌ Erro ao salvar sinais:", error);
    res.status(500).json({ error: "Erro ao salvar dados." });
  }
});

// ==========================================
// ROTA 6: BUSCAR HISTÓRICO (GET)
// O Android chama para montar o gráfico
// ==========================================
// ROTA 6: BUSCAR HISTÓRICO (GET)
app.get('/sinais/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;
  console.log(`📈 GRÁFICO: Buscando dados do usuário ${usuarioId}`);

  try {
    // 1. Tabela corrigida: sinais_vitais
    const historico = await prisma.sinais_vitais.findMany({
      where: {
          // 2. Coluna corrigida: usuario_id (também precisa mudar aqui!)
          usuario_id: Number(usuarioId)
      },
      orderBy: { data_hora: 'asc' },
      take: 20
    });

    res.status(200).json(historico);


  } catch (error) {
    console.error("❌ Erro ao buscar histórico:", error);
    res.status(500).json({ error: "Erro ao buscar dados." });
  }
});

// === ROTAS DE AGENDA ===

// 1. Criar Consulta
app.post('/consultas', async (req, res) => {
  const { usuario_id, medico, especialidade, data_hora, local } = req.body;
  try {
    const nova = await prisma.consultas.create({
      data: {
        usuario_id: Number(usuario_id),
        medico,
        especialidade,
        data_hora: new Date(data_hora), // Converte string pro formato de data do banco
        local
      }
    });
    res.json(nova);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Erro ao criar consulta" });
  }
});

// 2. Listar Consultas (Ordenadas pela data mais próxima)
app.get('/consultas/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;
  try {
    const lista = await prisma.consultas.findMany({
      where: { usuario_id: Number(usuarioId) },
      orderBy: { data_hora: 'asc' } // Da mais antiga para a mais nova
    });
    res.json(lista);
  } catch (error) {
    res.status(500).json({ error: "Erro ao buscar consultas" });
  }
});

// 3. Deletar Consulta (Caso cancelem)
app.delete('/consultas/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await prisma.consultas.delete({ where: { id: Number(id) } });
    res.json({ message: "Deletado com sucesso" });
  } catch (error) {
    res.status(500).json({ error: "Erro ao deletar" });
  }
});

// ... (seu código anterior continua igual) ...

// ==========================================
// ROTA 7: HIDRATAÇÃO (Salvar Copo d'água) 💧
// ==========================================
app.post('/hidratacao', async (req, res) => {
  const { usuario_id, quantidade_ml } = req.body;
  console.log(`💧 HIDRATAÇÃO: Usuário ${usuario_id} bebeu ${quantidade_ml}ml`);

  try {
    const registro = await prisma.hidratacao.create({
      data: {
        usuario_id: Number(usuario_id),
        quantidade_ml: Number(quantidade_ml),
        data_hora: new Date()
      }
    });
    res.status(201).json(registro);
  } catch (error) {
    console.error("❌ Erro ao salvar hidratação:", error);
    res.status(500).json({ error: "Erro ao registrar hidratação." });
  }
});

// ==========================================
// ROTA 8: HIDRATAÇÃO (Pegar Histórico) 📊
// ==========================================
app.get('/hidratacao/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;
  try {
    const registros = await prisma.hidratacao.findMany({
      where: { usuario_id: Number(usuarioId) },
      orderBy: { data_hora: 'desc' }
    });
    res.status(200).json(registros);
  } catch (error) {
    console.error("❌ Erro ao buscar hidratação:", error);
    res.status(500).json({ error: "Erro ao buscar histórico." });
  }
});

// ==========================================
// ROTA 9: FICHA MÉDICA COMPLETA 🏥
// Busca dados de emergência + remédios
// ==========================================
app.get('/ficha/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;

  try {
    // 1. Busca a ficha básica
    const ficha = await prisma.ficha_medica.findUnique({
      where: { usuario_id: Number(usuarioId) }
    });

    // 2. Busca os remédios
    const remedios = await prisma.medicamentos.findMany({
      where: { usuario_id: Number(usuarioId) }
    });

    // 3. Manda tudo junto
    res.json({
      ...ficha, // Espalha os dados da ficha (tipo, alergias...)
      lista_medicamentos: remedios // Adiciona a lista de remédios
    });

  } catch (error) {
    console.error("Erro na ficha:", error);
    res.status(500).json({ error: "Erro ao buscar ficha" });
  }
});

// Rota para SALVAR/ATUALIZAR a ficha (Postman)
app.post('/ficha', async (req, res) => {
    const { usuario_id, tipo_sanguineo, alergias, contato_telefone, contato_nome } = req.body;
    try {
        const ficha = await prisma.ficha_medica.upsert({
            where: { usuario_id: Number(usuario_id) },
            update: { tipo_sanguineo, alergias, contato_telefone, contato_nome },
            create: { usuario_id: Number(usuario_id), tipo_sanguineo, alergias, contato_telefone, contato_nome }
        });
        res.json(ficha);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Erro ao salvar ficha" });
    }
});

// ==========================================
// ROTA 10: ATUALIZAR PERFIL COMPLETO ⚙️
// Atualiza dados do usuário E da ficha médica
// ==========================================
app.put('/perfil/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;
  const { nome, email, telefone, tipo_sanguineo, alergias } = req.body;

  try {
    // 1. Atualiza dados básicos (Tabela Usuarios)
    const usuarioAtualizado = await prisma.usuarios.update({
      where: { id: Number(usuarioId) },
      data: { nome, email }
    });

    // 2. Atualiza ou Cria a Ficha Médica (Tabela Ficha)
    // Usamos 'upsert': se existe, atualiza; se não, cria.
    const fichaAtualizada = await prisma.ficha_medica.upsert({
      where: { usuario_id: Number(usuarioId) },
      update: {
        contato_telefone: telefone,
        tipo_sanguineo: tipo_sanguineo,
        alergias: alergias
      },
      create: {
        usuario_id: Number(usuarioId),
        contato_telefone: telefone,
        tipo_sanguineo: tipo_sanguineo,
        alergias: alergias
      }
    });

    res.json({ usuario: usuarioAtualizado, ficha: fichaAtualizada });

  } catch (error) {
    console.error("Erro ao atualizar perfil:", error);
    res.status(500).json({ error: "Erro ao atualizar dados." });
  }
});

app.post('/medicamentos', async (req, res) => {
  const { usuario_id, nome_remedio, dosagem, frequencia_horas, quantidade_total, horario_inicio } = req.body;

  try {
    // 1. Prepara a data base (Hoje na hora escolhida)
    const [horas, minutos] = horario_inicio.split(':');
    let dataReferencia = new Date();
    dataReferencia.setHours(parseInt(horas), parseInt(minutos), 0, 0);

    const frequenciaMs = Number(frequencia_horas) * 60 * 60 * 1000;
    const cronograma = [];

    // 2. Loop que gera as doses e diminui o estoque virtualmente
    for (let i = 0; i < Number(quantidade_total); i++) {
      // Cálculo da quantidade que restará APÓS tomar essa dose
      const estoqueRestante = Number(quantidade_total) - i;

      cronograma.push({
        usuario_id: Number(usuario_id),
        nome_remedio: nome_remedio,
        dosagem: dosagem,
        frequencia_horas: Number(frequencia_horas),
        quantidade_total: estoqueRestante, // Salva o estoque decrescente
        horario_agendado: new Date(dataReferencia.getTime() + (i * frequenciaMs))
      });
    }

    // 3. Salva o cronograma completo no MySQL
    const resultado = await prisma.medicamentos.createMany({
      data: cronograma
    });

    console.log(`✅ Cronograma gerado: ${resultado.count} doses.`);
    res.status(201).json(resultado);

  } catch (error) {
    console.error("❌ Erro:", error);
    res.status(500).json({ error: "Erro ao gerar cronograma." });
  }
});

app.get('/medicamentos/:usuarioId', async (req, res) => {
  const { usuarioId } = req.params;
  console.log(`🔍 BUSCA: Remédios do usuário ${usuarioId}`);
  try {
    const lista = await prisma.medicamentos.findMany({
      where: { usuario_id: Number(usuarioId) }
    });
    res.json(lista);
  } catch (error) {
    res.status(500).json({ error: "Erro ao buscar" });
  }
});

// ==========================================
// ROTA 12: REGISTRAR TOMADA DE REMÉDIO (COM AVISO DE ESTOQUE BAIXO)
// ==========================================
app.post('/medicamentos/tomar/:id', async (req, res) => {
  const { id } = req.params;

  try {
    const med = await prisma.medicamentos.findUnique({
        where: { id: Number(id) },
        include: { usuarios: true } // Incluímos o usuário para saber o nome/email dele no aviso
    });

    if (med && med.quantidade_total !== null && med.quantidade_total > 0) {
      const novaQuantidade = med.quantidade_total - 1;

      const atualizado = await prisma.medicamentos.update({
        where: { id: Number(id) },
        data: { quantidade_total: novaQuantidade }
      });

      // 🚨 LÓGICA DE EVENTO: ESTOQUE BAIXO (Avisa quando chegar em 10 ou menos)
      // Dentro da Rota 12, após atualizar a quantidade no Prisma
      if (novaQuantidade <= 10) {
          // 📧 CANAL 1: E-MAIL (Redundância/Histórico)
          if (med.usuarios && med.usuarios.email) {
              await transporter.sendMail({
                  from: '"GeroKernel" <jpzurlo.jz@gmail.com>',
                  to: med.usuarios.email,
                  subject: `⚠️ Estoque Baixo: ${med.nome_remedio}`,
                  text: `Atenção! Constam apenas ${novaQuantidade} unidades de ${med.nome_remedio}.`
              });
          }

          // 📱 CANAL 2: RESPOSTA DO APP (Alerta Imediato)
          return res.status(200).json({
              ...atualizado,
              alerta_estoque: true,
              mensagem: `Atenção: Apenas ${novaQuantidade} unidades restantes!`
          });
      }

      res.status(200).json({
          ...atualizado,
          alerta: novaQuantidade <= 10 ? "Estoque baixo!" : null
      });

    } else {
      res.status(400).json({ error: "Estoque insuficiente ou nulo." });
    }
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "Erro ao dar baixa no remédio" });
  }
});

// No seu arquivo index.ts
app.delete('/medicamentos/:id', async (req, res) => {
  const { id } = req.params;
  try {
    await prisma.medicamentos.delete({
      where: { id: Number(id) }
    });
    console.log(`🗑️ Medicamento ID ${id} excluído.`);
    res.json({ message: "Excluído com sucesso" });
  } catch (error) {
    console.error("Erro ao deletar:", error);
    res.status(500).json({ error: "Erro ao excluir o registro." });
  }
});

// ==========================================
// INICIALIZAÇÃO DO SERVIDOR 🚀
// ==========================================
const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`🚀 SERVIDOR GeroKernel RODANDO NA PORTA ${PORT}`);
});