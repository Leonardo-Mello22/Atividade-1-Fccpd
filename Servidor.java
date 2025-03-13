import java.io.IOException;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Servidor {
    private static final int PORTA_NOME = 4320;
    private static final int PORTA_AVISOS_GERAIS = 4321;
    private static final int PORTA_ATIVIDADES = 4322;
    private static final String GRUPO_MULTICAST_AVISOS_GERAIS = "230.0.0.1";
    private static final String GRUPO_MULTICAST_ATIVIDADES = "230.0.0.2";

    private static Map<String, Integer> alunos = new HashMap<>(); // Nome do aluno -> Tópico(s) em que está inscrito

    public static void main(String[] args) throws IOException {
        DatagramSocket socketCadastro = new DatagramSocket(PORTA_NOME);
        MulticastSocket socketAvisos = new MulticastSocket(PORTA_AVISOS_GERAIS);
        MulticastSocket socketAtividades = new MulticastSocket(PORTA_ATIVIDADES);

        // Inscrição dos alunos
        Thread threadCadastro = new Thread(() -> {
            try {
                while (true) {
                    byte[] bufferCadastro = new byte[1024];
                    DatagramPacket pacoteCadastro = new DatagramPacket(bufferCadastro, bufferCadastro.length);
                    socketCadastro.receive(pacoteCadastro);

                    String dados = new String(pacoteCadastro.getData(), 0, pacoteCadastro.getLength());
                    String[] parts = dados.split(":");
                    String nome = parts[0];
                    int opcao = Integer.parseInt(parts[1]);

                    synchronized (alunos) {
                        alunos.put(nome, opcao);
                    }

                    System.out.println("[SERVIDOR] Aluno " + nome + " inscrito na opção " + opcao);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Envio de mensagens para o tópico "Avisos Gerais"
        Thread threadAvisos = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            try {
                InetAddress grupoAvisos = InetAddress.getByName(GRUPO_MULTICAST_AVISOS_GERAIS);
                while (true) {
                    System.out.print("[SERVIDOR] Digite a mensagem para Avisos Gerais: ");
                    String mensagem = sc.nextLine();
                    String dataHora = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
                    String msgCompleta = "[" + dataHora + "] Avisos Gerais: " + mensagem;

                    synchronized (alunos) {
                        for (Map.Entry<String, Integer> entry : alunos.entrySet()) {
                            if (entry.getValue() == 1 || entry.getValue() == 3) {
                                byte[] buffer = msgCompleta.getBytes();
                                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupoAvisos, PORTA_AVISOS_GERAIS);
                                socketAvisos.send(pacote);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Envio de mensagens para o tópico "Atividades Extracurriculares"
        Thread threadAtividades = new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            try {
                InetAddress grupoAtividades = InetAddress.getByName(GRUPO_MULTICAST_ATIVIDADES);
                while (true) {
                    System.out.print("[SERVIDOR] Digite a mensagem para Atividades Extracurriculares: ");
                    String mensagem = sc.nextLine();
                    String dataHora = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());
                    String msgCompleta = "[" + dataHora + "] Atividades Extracurriculares: " + mensagem;

                    synchronized (alunos) {
                        for (Map.Entry<String, Integer> entry : alunos.entrySet()) {
                            if (entry.getValue() == 2 || entry.getValue() == 3) {
                                byte[] buffer = msgCompleta.getBytes();
                                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length, grupoAtividades, PORTA_ATIVIDADES);
                                socketAtividades.send(pacote);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        threadCadastro.start();
        threadAvisos.start();
        threadAtividades.start();
    }
}
