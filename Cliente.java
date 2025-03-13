import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private static final String GRUPO_MULTICAST_AVISOS_GERAIS = "230.0.0.1";
    private static final String GRUPO_MULTICAST_ATIVIDADES = "230.0.0.2";
    private static final int PORTA_AVISOS_GERAIS = 4321;
    private static final int PORTA_ATIVIDADES = 4322;
    private static final int PORTA_NOME = 4320;

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        // Cadastro do aluno
        System.out.print("Digite seu nome de usuário: ");
        String nome = sc.nextLine();
        System.out.println("Escolha o(s) tópico(s) para inscrição:");
        System.out.println("1. Avisos Gerais");
        System.out.println("2. Atividades Extracurriculares");
        System.out.println("3. Os dois tópicos");
        int opcao = sc.nextInt();
        sc.nextLine();  // Consome a quebra de linha do nextInt

        // Inscrição no servidor (porta 4320)
        DatagramSocket socketCadastro = new DatagramSocket();
        InetAddress grupoCadastro = InetAddress.getByName("127.0.0.1");
        String mensagemCadastro = nome + ":" + opcao;
        byte[] bufferCadastro = mensagemCadastro.getBytes();
        DatagramPacket pacoteCadastro = new DatagramPacket(bufferCadastro, bufferCadastro.length, grupoCadastro, PORTA_NOME);
        socketCadastro.send(pacoteCadastro);
        socketCadastro.close();

        // Recebendo mensagens dos tópicos para os quais o aluno se inscreveu
        if (opcao == 1 || opcao == 3) {
            receberMensagens(GRUPO_MULTICAST_AVISOS_GERAIS, PORTA_AVISOS_GERAIS, nome);
        }

        if (opcao == 2 || opcao == 3) {
            receberMensagens(GRUPO_MULTICAST_ATIVIDADES, PORTA_ATIVIDADES, nome);
        }
    }

    private static void receberMensagens(String grupo, int porta, String nome) throws IOException {
        MulticastSocket socketMulticast = new MulticastSocket(porta);
        InetAddress grupoMulticast = InetAddress.getByName(grupo);
        socketMulticast.joinGroup(grupoMulticast);
        System.out.println("[CLIENTE] Aguardando mensagens no tópico " + grupo + "...");

        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
            socketMulticast.receive(pacote);
            String mensagem = new String(pacote.getData(), 0, pacote.getLength());
            if (mensagem.contains(nome)) {
                System.out.println("[CLIENTE] Mensagem recebida: " + mensagem);
            }
        }
    }
}
