# Testes E2E reais do chat

Os testes usam a aplicação, o serviço de chat, MongoDB, RabbitMQ, Firebase Auth e o transporte SockJS/STOMP reais. Não há interceptação de rede, mocks ou estado de usuário simulado. Inicie os serviços e o Spring Boot antes de executar `npm run test:e2e`.

Configure no `.env` na raiz do projeto (ao lado de `package.json`):

- `CHAT_USER_A_PASSWORD` e `CHAT_USER_B_PASSWORD`: senhas das contas reais de teste.
- `CHAT_USER_A_NAME` e `CHAT_USER_B_NAME`: nomes exibidos nos perfis, se diferentes de `usuario` e `usuariob`.
- Opcionalmente, `CHAT_USER_A_EMAIL` e `CHAT_USER_B_EMAIL` para substituir os e-mails padrão.

Exemplo de nomes de variáveis, sem valores reais:

```env
CHAT_USER_A_PASSWORD=
CHAT_USER_B_PASSWORD=
```

O Playwright carrega esse arquivo automaticamente do diretório de execução. Rode `npm run test:e2e` na raiz do projeto. O `.env` não deve ser commitado.

As duas contas precisam ter uma conversa existente sobre o mesmo anúncio. O teste abre essa conversa nos dois contextos, persiste uma mensagem única, recarrega para verificar o histórico e testa entrega em tempo real e concorrência. Para validar reconexão sem mocks, o teste observa o cliente SockJS real e fecha a conexão ativa pela API do cliente, aguardando o fechamento do WebSocket, a reconexão automática e uma nova inscrição no tópico.

O fluxo de login é o da aplicação e depende do Firebase Auth e do reCAPTCHA estarem disponíveis no ambiente. Não grave senhas nem arquivos de sessão no repositório.
