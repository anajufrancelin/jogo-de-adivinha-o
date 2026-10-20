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

