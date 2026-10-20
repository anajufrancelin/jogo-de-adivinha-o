package redes2;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente2 {

    private static volatile boolean encerrado = false;

    public static void main(String[] args) throws IOException {
        String servidor = args.length > 0 ? args[0] : "localhost";

        try (Socket socket = new Socket(servidor, 5000);
             BufferedReader entrada = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter saida = new PrintWriter(new OutputStreamWriter(
                     socket.getOutputStream(), StandardCharsets.UTF_8), true);
             Scanner teclado = new Scanner(System.in)) {

            Thread receptor = new Thread(() -> {
                try {
                    // TODO 8
                    String linha;
                    while ((linha = entrada.readLine()) != null) {
                        System.out.println(linha);
                        if (linha.startsWith("VENCEDOR")) {
                            encerrado = true;
                            System.out.println("Partida encerrada. Pressione ENTER para sair.");
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Recepção encerrada.");
                } finally {
                    encerrado = true;
                }
            });
            receptor.setDaemon(true);
            receptor.start();

            System.out.println("Digite um número de 0 a 1000 ou SAIR.");

            // TODO 9
            while (!encerrado && teclado.hasNextLine()) {
                String linha = teclado.nextLine();
                if (encerrado) {
                    break;
                }
                saida.println(linha);
                if (linha.trim().equalsIgnoreCase("SAIR")) {
                    break;
                }
            }
        }
    }
}