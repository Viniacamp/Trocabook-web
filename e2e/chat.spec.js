// @ts-check
import { randomUUID } from 'node:crypto';
import { test, expect } from '@playwright/test';

const urlBaseAplicacao = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:8080';

const usuarioA = {
  email: process.env.CHAT_USER_A_EMAIL || 'usuarioa@gmail.com',
  senha: process.env.CHAT_USER_A_PASSWORD,
  nomeContato: process.env.CHAT_USER_B_NAME || 'usuariob',
};

const usuarioB = {
  email: process.env.CHAT_USER_B_EMAIL || 'usuariob@gmail.com',
  senha: process.env.CHAT_USER_B_PASSWORD,
  nomeContato: process.env.CHAT_USER_A_NAME || 'usuario',
};

async function autenticar(pagina, usuario, variavelSenha) {
  expect(usuario.senha, variavelSenha).toBeTruthy();

  await pagina.goto('/login');
  await pagina.locator('#email').fill(usuario.email);
  await pagina.locator('#senha').fill(usuario.senha);
  await pagina.locator('#loginButton').click();

  await pagina.waitForURL(endereco => endereco.pathname !== '/login', { timeout: 60_000 });
  await pagina.goto('/chat');
  await expect(pagina.locator('#chatList')).toBeVisible();
}

async function abrirConversa(pagina, nomeContato, uidAnuncio) {
  const seletor = uidAnuncio
    ? `.chat-item[data-anuncio="${uidAnuncio}"]`
    : '.chat-item';
  const item = pagina.locator(seletor).filter({ hasText: nomeContato }).first();

  await expect(item, `Conversa com "${nomeContato}"${uidAnuncio ? ` para o anúncio ${uidAnuncio}` : ''} não encontrada.`).toBeVisible();

  const uid = uidAnuncio || await item.getAttribute('data-anuncio');
  const uidDestinatario = await item.getAttribute('data-destinatario');
  expect(uid, 'A conversa não contém o identificador do anúncio.').toBeTruthy();
  expect(uidDestinatario, 'A conversa não contém o identificador do destinatário.').toBeTruthy();
  const promessaResposta = pagina.waitForResponse(resposta => {
    const endereco = new URL(resposta.url());
    return endereco.pathname === '/chat/conversar/dados'
      && endereco.searchParams.get('anuncio') === uid
      && endereco.searchParams.get('destinatario') === uidDestinatario;
  });

  await item.click();

  const resposta = await promessaResposta;
  expect(resposta.ok()).toBeTruthy();
  const dadosResposta = await resposta.json();
  expect(dadosResposta.status).toBe('success');
  expect(Array.isArray(dadosResposta.data.mensagens)).toBeTruthy();
  await expect(pagina.locator('#chatHeader')).toBeVisible();
  await expect(pagina.locator('#messagesContainer')).toBeVisible();
  await expect(pagina.locator('#messagesContainer .message')).toHaveCount(dadosResposta.data.mensagens.length);
  await expect(pagina.locator('#conteudo')).toBeVisible();

  return { uidAnuncio: uid, dadosResposta };
}

function localizarTextoMensagem(pagina, conteudo) {
  return pagina.locator('#messagesContainer .message p').filter({ hasText: conteudo });
}

function localizarBalaoMensagem(pagina, conteudo) {
  return pagina.locator('#messagesContainer .message').filter({ hasText: conteudo });
}

async function abrirOpcoesMensagem(pagina, conteudo) {
  const balaoMensagem = localizarBalaoMensagem(pagina, conteudo);
  await expect(balaoMensagem).toHaveCount(1);
  await balaoMensagem.locator('.menu-opcoes i').click();
  return balaoMensagem.locator('.menu-dropdown');
}

async function enviarMensagem(pagina, conteudo) {
  const campoMensagem = pagina.locator('#conteudo');
  await campoMensagem.fill(conteudo);
  await campoMensagem.press('Enter');
  await expect(campoMensagem).toHaveValue('');
}

async function clicarRapidoEVerificarBloqueio(pagina, seletorBotao) {
  return pagina.locator(seletorBotao).evaluate(botao => {
    botao.click();
    const foiDesabilitado = botao.disabled;
    botao.click();
    return foiDesabilitado;
  });
}

async function monitorarConexoesSockJs(pagina) {
  await pagina.addInitScript(() => {
    const conexoes = [];
    Object.defineProperty(window, '__conexoesSockJsE2E', { value: conexoes });

    let construtorSockJs;
    Object.defineProperty(window, 'SockJS', {
      configurable: true,
      get: () => construtorSockJs,
      set: construtor => {
        // Mantém a implementação SockJS real e apenas registra as instâncias criadas.
        construtorSockJs = new Proxy(construtor, {
          construct(alvo, argumentos, novoConstrutor) {
            const conexao = Reflect.construct(alvo, argumentos, novoConstrutor);
            conexoes.push(conexao);
            return conexao;
          },
        });
      },
    });
  });
}

test('chat real: histórico, entrega WebSocket, mensagens concorrentes e reconexão', async ({ browser: navegador }) => {
  test.setTimeout(180_000);

  const contextoA = await navegador.newContext({ baseURL: urlBaseAplicacao });
  const contextoB = await navegador.newContext({ baseURL: urlBaseAplicacao });
  const paginaA = await contextoA.newPage();
  const paginaB = await contextoB.newPage();

  try {
    let uidAnuncio;
    let quantidadeWebSockets = 0;
    let quantidadeFechamentosWebSocket = 0;
    const topicosInscritos = [];

    paginaB.on('websocket', conexao => {
      if (!new URL(conexao.url()).pathname.includes('/ws-chat/')) return;
      quantidadeWebSockets++;
      conexao.on('close', () => {
        quantidadeFechamentosWebSocket++;
      });
    });
    paginaB.on('console', mensagemConsole => {
      const prefixo = 'Subscrito no tópico: ';
      if (mensagemConsole.text().startsWith(prefixo)) {
        topicosInscritos.push(mensagemConsole.text().slice(prefixo.length));
      }
    });
    await monitorarConexoesSockJs(paginaB);

    await test.step('autenticar as duas contas reais em contextos independentes', async () => {
      await autenticar(paginaA, usuarioA, 'CHAT_USER_A_PASSWORD');
      await autenticar(paginaB, usuarioB, 'CHAT_USER_B_PASSWORD');
    });

    await test.step('abrir a mesma conversa e confirmar o canal WebSocket real', async () => {
      const conversaA = await abrirConversa(paginaA, usuarioA.nomeContato);
      uidAnuncio = conversaA.uidAnuncio;
      await abrirConversa(paginaB, usuarioB.nomeContato, uidAnuncio);

      await expect.poll(() => quantidadeWebSockets, { timeout: 30_000 }).toBeGreaterThan(0);
      await expect.poll(() => topicosInscritos.some(topico => topico.includes(uidAnuncio)), { timeout: 30_000 }).toBeTruthy();
    });

    const navegacoesFramePrincipal = [];
    paginaB.on('framenavigated', quadro => {
      if (quadro === paginaB.mainFrame()) navegacoesFramePrincipal.push(quadro.url());
    });

    const identificadorExecucao = randomUUID();
    const mensagemHistorica = `E2E histórico ${identificadorExecucao}`;

    await test.step('entregar mensagem em tempo real sem recarregar a página receptora', async () => {
      await enviarMensagem(paginaA, mensagemHistorica);

      await expect(localizarTextoMensagem(paginaA, mensagemHistorica)).toHaveCount(1);
      await expect(localizarBalaoMensagem(paginaA, mensagemHistorica)).toHaveClass(/user/);
      await expect(localizarTextoMensagem(paginaB, mensagemHistorica)).toHaveCount(1, { timeout: 15_000 });
      await expect(localizarBalaoMensagem(paginaB, mensagemHistorica)).toHaveClass(/negociador/);
      expect(navegacoesFramePrincipal).toHaveLength(0);
    });

    await test.step('recarregar e validar histórico persistido via resposta REST', async () => {
      await paginaB.reload();
      await expect(paginaB.locator('#chatList')).toBeVisible();
      const conversa = await abrirConversa(paginaB, usuarioB.nomeContato, uidAnuncio);

      expect(conversa.dadosResposta.data.mensagens.some(mensagem => mensagem.conteudo === mensagemHistorica)).toBeTruthy();
      await expect(localizarTextoMensagem(paginaB, mensagemHistorica)).toHaveCount(1);
    });

    await test.step('enviar mensagens especiais e uma sequência rápida sem perda ou duplicação', async () => {
      const mensagemEspecial = `<img src=x onerror=alert(1)> & "aspas" ${identificadorExecucao}`;
      await enviarMensagem(paginaA, mensagemEspecial);

      await expect(localizarTextoMensagem(paginaA, mensagemEspecial)).toHaveCount(1);
      await expect(localizarTextoMensagem(paginaB, mensagemEspecial)).toHaveCount(1, { timeout: 15_000 });
      await expect(localizarTextoMensagem(paginaB, mensagemEspecial)).toHaveText(mensagemEspecial);
      await expect(paginaB.locator('#messagesContainer [onerror]')).toHaveCount(0);

      const mensagensConcorrentes = Array.from(
        { length: 6 },
        (_, indice) => `E2E concorrente ${indice + 1} ${identificadorExecucao}`,
      );

      for (const conteudo of mensagensConcorrentes) {
        await enviarMensagem(paginaA, conteudo);
      }

      for (const conteudo of mensagensConcorrentes) {
        await expect(localizarTextoMensagem(paginaA, conteudo)).toHaveCount(1);
        await expect(localizarTextoMensagem(paginaB, conteudo)).toHaveCount(1, { timeout: 20_000 });
      }
    });

    await test.step('editar mensagem própria e confirmar atualização no outro usuário e no histórico', async () => {
      const conteudoOriginal = `E2E antes da edição ${identificadorExecucao}`;
      const conteudoEditado = `E2E depois da edição ${identificadorExecucao}`;
      await enviarMensagem(paginaA, conteudoOriginal);

      const balaoOriginal = localizarBalaoMensagem(paginaA, conteudoOriginal);
      await expect(balaoOriginal).toHaveAttribute('data-id', /.+/);
      const idMensagem = await balaoOriginal.getAttribute('data-id');
      expect(idMensagem).toBeTruthy();
      await expect(localizarTextoMensagem(paginaB, conteudoOriginal)).toHaveCount(1, { timeout: 15_000 });

      const menuAntesDoCliqueFora = await abrirOpcoesMensagem(paginaA, conteudoOriginal);
      await expect(menuAntesDoCliqueFora).toBeVisible();
      await paginaA.locator('#chatHeader').click();
      await expect(menuAntesDoCliqueFora).toBeHidden();
      expect(await menuAntesDoCliqueFora.evaluate(menu => menu.contains(document.activeElement))).toBeFalsy();

      const menuOpcoes = await abrirOpcoesMensagem(paginaA, conteudoOriginal);
      await menuOpcoes.getByRole('button', { name: 'Editar' }).click();
      const modalEdicao = paginaA.locator('#modalEditarMensagem');
      const campoEdicao = paginaA.locator('#inputEditarMensagem');
      await expect(modalEdicao).toBeVisible();
      await expect(campoEdicao).toHaveValue(conteudoOriginal);
      await campoEdicao.fill(conteudoEditado);
      const requisicoesEdicao = [];
      paginaA.on('request', requisicao => {
        if (requisicao.method() === 'PUT'
          && new URL(requisicao.url()).pathname === `/chat/mensagens/${idMensagem}`) {
          requisicoesEdicao.push(requisicao);
        }
      });
      const requisicaoEdicao = paginaA.waitForResponse(resposta => {
        const requisicao = resposta.request();
        return requisicao.method() === 'PUT'
          && new URL(resposta.url()).pathname === `/chat/mensagens/${idMensagem}`;
      });
      const cliqueEdicaoBloqueado = await clicarRapidoEVerificarBloqueio(paginaA, '#btnSalvarEdicao');
      expect(cliqueEdicaoBloqueado).toBeTruthy();

      const respostaEdicao = await requisicaoEdicao;
      expect(respostaEdicao.ok()).toBeTruthy();
      const dadosEdicao = await respostaEdicao.json();
      expect(dadosEdicao.status).toBe('success');
      expect(dadosEdicao.data.conteudo).toBe(conteudoEditado);
      expect(requisicoesEdicao).toHaveLength(1);
      await expect(modalEdicao).toBeHidden();
      await expect(paginaA.locator('#btnSalvarEdicao')).toBeEnabled();
      await expect(localizarTextoMensagem(paginaA, conteudoEditado)).toHaveCount(1);
      await expect(localizarTextoMensagem(paginaB, conteudoEditado)).toHaveCount(1, { timeout: 15_000 });
      await expect(localizarTextoMensagem(paginaB, conteudoOriginal)).toHaveCount(0);

      const conversaAtualizada = await abrirConversa(paginaB, usuarioB.nomeContato, uidAnuncio);
      expect(conversaAtualizada.dadosResposta.data.mensagens.some(
        mensagem => mensagem.conteudo === conteudoEditado,
      )).toBeTruthy();
      expect(conversaAtualizada.dadosResposta.data.mensagens.some(
        mensagem => mensagem.conteudo === conteudoOriginal,
      )).toBeFalsy();
    });

    await test.step('cancelar exclusão e depois excluir mensagem com confirmação', async () => {
      const conteudoExclusao = `E2E mensagem para excluir ${identificadorExecucao}`;
      await enviarMensagem(paginaA, conteudoExclusao);

      const balaoExclusao = localizarBalaoMensagem(paginaA, conteudoExclusao);
      const idMensagem = await balaoExclusao.getAttribute('data-id');
      expect(idMensagem).toBeTruthy();
      await expect(localizarTextoMensagem(paginaB, conteudoExclusao)).toHaveCount(1, { timeout: 15_000 });

      let requisicoesExclusao = 0;
      paginaA.on('request', requisicao => {
        if (requisicao.method() === 'DELETE'
          && new URL(requisicao.url()).pathname === `/chat/mensagens/${idMensagem}`) {
          requisicoesExclusao++;
        }
      });

      const menuOpcoes = await abrirOpcoesMensagem(paginaA, conteudoExclusao);
      await menuOpcoes.getByRole('button', { name: 'Excluir' }).click();
      const modalExclusao = paginaA.locator('#modalExcluirMensagem');
      await expect(modalExclusao).toBeVisible();
      await expect(modalExclusao).toContainText('Deseja realmente excluir esta mensagem?');
      await paginaA.locator('#btnCancelarExclusao').click();
      await expect(modalExclusao).toBeHidden();
      await expect(localizarTextoMensagem(paginaA, conteudoExclusao)).toHaveCount(1);
      expect(requisicoesExclusao).toBe(0);

      const menuConfirmacao = await abrirOpcoesMensagem(paginaA, conteudoExclusao);
      await menuConfirmacao.getByRole('button', { name: 'Excluir' }).click();
      await expect(modalExclusao).toBeVisible();
      await expect(modalExclusao).toContainText('Deseja realmente excluir esta mensagem?');
      const respostaExclusao = paginaA.waitForResponse(resposta => {
        return resposta.request().method() === 'DELETE'
          && new URL(resposta.url()).pathname === `/chat/mensagens/${idMensagem}`;
      });
      const cliqueExclusaoBloqueado = await clicarRapidoEVerificarBloqueio(paginaA, '#btnConfirmarExclusao');
      expect(cliqueExclusaoBloqueado).toBeTruthy();

      const resposta = await respostaExclusao;
      expect(resposta.ok()).toBeTruthy();
      const dadosExclusao = await resposta.json();
      expect(dadosExclusao.status).toBe('success');
      expect(requisicoesExclusao).toBe(1);
      await expect(modalExclusao).toBeHidden();
      await expect(paginaA.locator('#btnConfirmarExclusao')).toBeEnabled();
      await expect(localizarTextoMensagem(paginaA, conteudoExclusao)).toHaveCount(0);
      await expect(localizarTextoMensagem(paginaB, conteudoExclusao)).toHaveCount(0, { timeout: 15_000 });

      const conversaAtualizada = await abrirConversa(paginaB, usuarioB.nomeContato, uidAnuncio);
      expect(conversaAtualizada.dadosResposta.data.mensagens.some(
        mensagem => mensagem.conteudo === conteudoExclusao,
      )).toBeFalsy();
    });

    await test.step('não enviar conteúdo vazio ou apenas espaços', async () => {
      const requisicoesPost = [];
      paginaA.on('request', requisicao => {
        if (requisicao.method() === 'POST' && new URL(requisicao.url()).pathname === '/chat/mensagens') {
          requisicoesPost.push(requisicao);
        }
      });

      await paginaA.locator('#conteudo').fill('   ');
      await paginaA.locator('#chatForm').locator('button[type="submit"]').click();
      await expect(paginaA.locator('#conteudo')).toHaveValue('   ');
      expect(requisicoesPost).toHaveLength(0);
    });

    await test.step('restabelecer a conexão WebSocket após uma queda real de rede', async () => {
      const quantidadeWebSocketsAnterior = quantidadeWebSockets;
      const quantidadeFechamentosAnterior = quantidadeFechamentosWebSocket;
      const quantidadeTopicosAnterior = topicosInscritos.length;

      await paginaB.evaluate(() => {
        const conexoes = window.__conexoesSockJsE2E;
        const conexaoAtiva = conexoes[conexoes.length - 1];
        if (!conexaoAtiva) throw new Error('Nenhuma conexão SockJS ativa foi registrada.');
        conexaoAtiva.close();
      });
      await expect.poll(() => quantidadeFechamentosWebSocket, { timeout: 15_000 }).toBeGreaterThan(quantidadeFechamentosAnterior);
      await expect.poll(() => quantidadeWebSockets, { timeout: 30_000 }).toBeGreaterThan(quantidadeWebSocketsAnterior);
      await expect.poll(() => topicosInscritos.length, { timeout: 30_000 }).toBeGreaterThan(quantidadeTopicosAnterior);

      const mensagemAposReconexao = `E2E após reconexão ${identificadorExecucao}`;
      await enviarMensagem(paginaA, mensagemAposReconexao);
      await expect(localizarTextoMensagem(paginaB, mensagemAposReconexao)).toHaveCount(1, { timeout: 20_000 });
    });
  } finally {
    await contextoA.close();
    await contextoB.close();
  }
});
