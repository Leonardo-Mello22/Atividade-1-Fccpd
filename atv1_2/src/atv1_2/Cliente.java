package atv1_2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class Cliente {
	private static final String GRUPO_MULTICAST_AVISOS_GERAIS = "230.0.0.1";
	private static final String GRUPO_MULTICAST_ATIVIDADES = "230.0.0.2";
	private static final String GRUPO_MULTICAST_NOME = "230.0.0.3";

	public static void main(String[] args) throws IOException {
		MulticastSocket socket = new MulticastSocket(4321);
		Scanner sc = new Scanner(System.in);

		InetAddress grupo_nome = InetAddress.getByName(GRUPO_MULTICAST_NOME);

		System.out.print("Digite o nome do aluno: ");
		String nome = sc.nextLine();
		byte[] envio = nome.getBytes();

		DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo_nome, 4321);
		socket.send(pacote);

		System.out.println("[Cliente] Nome enviado ao servidor");

		System.out.println("Cadastro de tópicos: ");
		System.out.println("1 - Avisos gerais");
		System.out.println("2 - Atividade Extracurriculares");
		System.out.println("3 - Os dois tópicos");

		int topico = sc.nextInt();
		sc.nextLine();

		InetSocketAddress grupo = null;
		NetworkInterface ni = null;
		InetSocketAddress grupo2 = null;
		NetworkInterface ni2 = null;

		if (topico == 1) {
			InetAddress ia = InetAddress.getByName(GRUPO_MULTICAST_AVISOS_GERAIS);
			grupo = new InetSocketAddress(ia, 4321);
			ni = NetworkInterface.getByInetAddress(ia);
			socket.joinGroup(grupo, ni);
		} else if (topico == 2) {
			InetAddress ia = InetAddress.getByName(GRUPO_MULTICAST_ATIVIDADES);
			grupo = new InetSocketAddress(ia, 4321);
			ni = NetworkInterface.getByInetAddress(ia);
			socket.joinGroup(grupo, ni);
		} else if (topico == 3) {
			InetAddress ia = InetAddress.getByName(GRUPO_MULTICAST_AVISOS_GERAIS);
			grupo = new InetSocketAddress(ia, 4321);
			ni = NetworkInterface.getByInetAddress(ia);

			InetAddress ia2 = InetAddress.getByName(GRUPO_MULTICAST_ATIVIDADES);
			grupo2 = new InetSocketAddress(ia2, 4321);
			ni2 = NetworkInterface.getByInetAddress(ia2);

			socket.joinGroup(grupo, ni);
			socket.joinGroup(grupo2, ni2);
		} else {
			System.out.println("Tópico não reconhecido");
			socket.close();
			return;
		}

		Thread receptor = new Thread(() -> {
            try {
                String msg;
                while (true) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    msg = new String(packet.getData(), 0, packet.getLength());

                    String tempoFormatado = new SimpleDateFormat("dd/MM/yyyy - HH:mm").format(new Date());

                    System.out.println("[" + tempoFormatado + "] " + msg);

                    if (msg.contains("Servidor Encerrado!")) {
                        System.out.println("[Cliente] Servidor encerrou a conexão.");
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("[Cliente] Erro na recepção de mensagens: " + e.getMessage());
            }
        });

        receptor.start();

        System.out.println("[Cliente] Pressione ENTER para sair...");
        sc.nextLine();

        System.out.println("[Cliente] Conexão Encerrada!");

		if (topico == 1 || topico == 3) {
			socket.leaveGroup(grupo, ni);
		}
		if (topico == 2 || topico == 3) {
			socket.leaveGroup(grupo2, ni2);
		}
		socket.close();

	}

}