# Jogo de adivinhação com sockets TCP — Redes de Computadores II

## Integrantes
- Ana Julia Francelin Blesio
- Anna Clara Diniz Caetano
- (até 5 integrantes)

## Compilar
Com o JDK instalado, no diretório dos arquivos:

    javac Servidor2.java Cliente2.java

Se aparecerem erros de caracteres (acentos), use:

    javac -encoding UTF-8 Servidor2.java Cliente2.java

## Executar
Terminal 1 (servidor, porta TCP 5000):

    java Servidor2

Outros terminais (pelo menos três clientes):

    java Cliente2 localhost

Para clientes em outras máquinas, use o IP do computador do servidor:

    java Cliente 192.168.1.10

### Extra: nova partida sem reiniciar
Com o servidor rodando, digite `NOVA` no terminal dele para sortear outro número e iniciar uma nova partida. Os clientes da partida anterior tiveram a conexão encerrada e precisam executar `java Cliente localhost` novamente. Sem esse comando, o comportamento é o do enunciado: conexões após o fim da partida recebem apenas o resultado.

## Protocolo (uma mensagem por linha, UTF-8)
| Origem | Mensagem | Significado |
|---|---|---|
| Cliente | `450` | Palpite |
| Cliente | `SAIR` | Desconectar |
| Servidor | `BEM_VINDO ...` | Início da participação |
| Servidor | `MAIOR` / `MENOR` | Dica sobre o número secreto |
| Servidor | `ERRO ...` | Entrada inválida |
| Servidor | `VENCEDOR IP=... HOST=... NUMERO=...` | Fim da partida, enviada a todos |

## Como funciona
- O servidor sorteia o número uma vez e cria uma thread por cliente.
- Palpites e definição do vencedor são feitos dentro de `synchronized (travaJogo)`, garantindo um único vencedor.
- O cliente usa uma thread exclusiva para receber mensagens, então o anúncio aparece mesmo durante a digitação.
- Ao final, o servidor fecha as conexões; conexões posteriores recebem apenas o resultado.

## 1. Por que o cliente precisa receber mensagens em outra thread?
Porque readLine() e o Scanner do teclado são bloqueantes: a thread que fica esperando o usuário digitar não consegue, ao mesmo tempo, ler o que chega do servidor. Com uma thread só, o anúncio do vencedor ficaria parado até a pessoa apertar ENTER. A thread receptora fica o tempo todo em entrada.readLine() e imprime a mensagem assim que ela chega, mesmo com o jogador parado sem digitar nada.

## 2. O que pode acontecer sem sincronização no servidor?
Haveria uma condição de corrida. Se dois clientes enviarem o palpite certo quase ao mesmo tempo, as duas threads podem checar encerrado == false antes de qualquer uma marcá-lo como true. Aí as duas se declaram vencedoras, e o servidor manda dois anúncios diferentes, com resultados inconsistentes entre os clientes. Também poderia entrar um cliente novo entre a definição do vencedor e o anúncio, e ele nunca receberia o resultado. O synchronized (travaJogo) faz a verificação, a definição do vencedor e o envio da mensagem final acontecerem de forma atômica, e só a primeira thread a entrar consegue vencer.

## 3. Qual é a diferença entre IP, hostname e porta?
- IP é o endereço numérico da máquina na rede (por exemplo, 192.168.1.20). Ele identifica o computador.
- Hostname é o nome textual da máquina (por exemplo, pc-lab-03), obtido por resolução de nomes (DNS). Se a rede não resolver, o Java devolve o próprio IP, e em testes locais aparece localhost.
- Porta identifica o processo ou a conexão dentro da máquina. O servidor usa a 5000, e cada cliente recebe uma porta de origem diferente, escolhida pelo sistema operacional. Por isso dois clientes na mesma máquina têm o mesmo IP, mas portas distintas.

## 4. O que acontece quando readLine() retorna null?
Significa que o outro lado fechou a conexão (fim do fluxo), e não que chegou uma linha vazia. No servidor, o while ((linha = entrada.readLine()) != null) termina, o finally remove o jogador da lista e o try-with-resources fecha o socket, sem derrubar o servidor nem os outros jogadores. No cliente, a thread receptora sai do laço e marca encerrado = true. Já uma linha vazia retorna "", e o servidor a trata como entrada inválida, respondendo ERRO.

