package redes2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Servidor2 {

    private static final int PORTA = 5000;
    private static final CopyOnWriteArrayList<Jogador> jogadores =
            new CopyOnWriteArrayList<>();
    private static final Object travaJogo = new Object();
    private static int numeroSecreto;
    private static boolean encerrado = false;
    private static String mensagemFinal;

    public static void main(String[] args) throws IOException {
        // TODO 1: sorteio único, de 0 a 1000 inclusive.
        numeroSecreto = new Random().nextInt(1001);
        // Para testar acertos simultâneos, fixe temporariamente um valor:
        // numeroSecreto = 500;  (restaure o sorteio antes da entrega)

        // EXTRA: digitar NOVA no terminal do servidor inicia outra partida.
        Thread console = new Thread(() -> {
            java.util.Scanner sc = new java.util.Scanner(System.in);
            while (sc.hasNextLine()) {
                if (sc.nextLine().trim().equalsIgnoreCase("NOVA")) {
                    synchronized (travaJogo) {
                        numeroSecreto = new Random().nextInt(1001);
                        encerrado = false;
                        mensagemFinal = null;
                        System.out.println("Nova partida iniciada.");
                    }
                }
            }
        });
        console.setDaemon(true);
        console.start();

        try (ServerSocket servidor = new ServerSocket(PORTA)) {
            System.out.println("Servidor na porta " + PORTA
                    + " (digite NOVA para iniciar outra partida)");

            while (true) {
                Socket socket = servidor.accept();

                // TODO 2: um Jogador e uma Thread para cada conexão.
                try {
                    Jogador jogador = new Jogador(socket);
                    new Thread(jogador).start();
                } catch (IOException e) {
                    System.out.println("Falha ao iniciar jogador: " + e.getMessage());
                    try { socket.close(); } catch (IOException ignorado) { }
                }
            }
        }
    }

    // Chamado sempre dentro de synchronized(travaJogo).
    private static void enviarParaTodos(String mensagem) {
        // TODO 3
        for (Jogador j : jogadores) {
            j.enviar(mensagem);
        }
    }

    private static class Jogador implements Runnable {
        private final Socket socket;
        private final PrintWriter saida;
        private final String ip;
        private final String hostname;

        Jogador(Socket socket) throws IOException {
            this.socket = socket;
            this.saida = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.ip = socket.getInetAddress().getHostAddress();
            this.hostname = socket.getInetAddress().getHostName();
        }

        // Evita que duas threads escrevam simultaneamente neste cliente.
        synchronized void enviar(String mensagem) {
            saida.println(mensagem);
        }

        void fechar() {
            try { socket.close(); } catch (IOException ignorado) { }
        }

        @Override
        public void run() {
            try (Socket conexao = socket;
                 BufferedReader entrada = new BufferedReader(new InputStreamReader(
                         conexao.getInputStream(), StandardCharsets.UTF_8))) {

                // TODO 4: entrada na partida (ou resultado, se já terminou).
                synchronized (travaJogo) {
                    if (encerrado) {
                        enviar(mensagemFinal);
                        return;
                    }
                    jogadores.add(this);
                    enviar("BEM_VINDO Digite um número entre 0 e 1000");
                }
                System.out.println("Conectado: " + ip + ":" + socket.getPort());

                String linha;
                while ((linha = entrada.readLine()) != null) {
                    // TODO 5: SAIR, conversão e validação.
                    linha = linha.trim();

                    if (linha.equalsIgnoreCase("SAIR")) {
                        break;
                    }

                    int palpite;
                    try {
                        palpite = Integer.parseInt(linha);
                    } catch (NumberFormatException e) {
                        enviar("ERRO Digite um inteiro entre 0 e 1000");
                        continue;
                    }
                    if (palpite < 0 || palpite > 1000) {
                        enviar("ERRO Digite um inteiro entre 0 e 1000");
                        continue;
                    }

                    // TODO 6: verificação atômica do palpite e do vencedor.
                    synchronized (travaJogo) {
                        if (encerrado) {
                            enviar(mensagemFinal);
                            break;
                        }

                        if (palpite < numeroSecreto) {
                            enviar("MAIOR");
                        } else if (palpite > numeroSecreto) {
                            enviar("MENOR");
                        } else {
                            encerrado = true;
                            mensagemFinal = "VENCEDOR IP=" + ip
                                    + " HOST=" + hostname
                                    + " NUMERO=" + numeroSecreto;
                            System.out.println(mensagemFinal);
                            enviarParaTodos(mensagemFinal);
                            // Libera as threads bloqueadas em readLine().
                            for (Jogador j : jogadores) {
                                j.fechar();
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Conexão encerrada: " + ip);
            } finally {

                jogadores.remove(this);
            }
        }
    }
}