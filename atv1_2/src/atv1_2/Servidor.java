package atv1_2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Servidor {

	private static final String GRUPO_MULTICAST_AVISOS_GERAIS = "230.0.0.1";
	private static final String GRUPO_MULTICAST_ATIVIDADES = "230.0.0.2";
	private static final String GRUPO_MULTICAST_NOME = "230.0.0.3";

	public static void main(String[] args) throws IOException {
		ArrayList<String> alunos = new ArrayList<>();
		String mensagem = " ";
		byte[] envio = new byte[1024];
		Scanner sc = new Scanner(System.in);

		MulticastSocket socket = new MulticastSocket();
		InetAddress grupo_avisos_gerais = InetAddress.getByName(GRUPO_MULTICAST_AVISOS_GERAIS);
		InetAddress grupo_atividades = InetAddress.getByName(GRUPO_MULTICAST_ATIVIDADES);
		InetAddress grupo_nome = InetAddress.getByName(GRUPO_MULTICAST_NOME);

		MulticastSocket socketReceber = new MulticastSocket(4321);

		NetworkInterface ni_nome = NetworkInterface.getByInetAddress(grupo_nome);
		socketReceber.joinGroup(new InetSocketAddress(grupo_nome, 4321), ni_nome);

		new Thread(() -> {
			try {
				byte[] buffer = new byte[1024];
				while (true) {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length, grupo_nome, 4321);
					socketReceber.receive(packet);
					String nomeAluno = new String(packet.getData(), 0, packet.getLength());
					alunos.add(nomeAluno);
					System.out.println("[Servidor] Aluno cadastrado: " + nomeAluno);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		while (!mensagem.equals("Servidor Encerrado!")) {
			System.out.println("Digite pra qual topico você quer enviar a mensagem: ");

			System.out.println("1 - Avisos gerais");
			System.out.println("2 - Atividade Extracurriculares");

			int topico = sc.nextInt();
			sc.nextLine();

			System.out.print("[Servidor] Digite a mensagem:");

			Date agora = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
			String tempoFormatado = formatter.format(agora);

			String nomeTopico = "";
			
			mensagem = sc.nextLine();
			if (mensagem.equals("encerrar"))
				mensagem = "Servidor Encerrado!";

			if (topico == 1) {
				nomeTopico = "Avisos gerais : ";
				mensagem = nomeTopico + mensagem;
				envio = mensagem.getBytes();
				DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo_avisos_gerais, 4321);
				socket.send(pacote);

			} else if (topico == 2) {
				nomeTopico = "Atividade Extracurriculares : ";
				mensagem = nomeTopico + mensagem;
				envio = mensagem.getBytes();
				DatagramPacket pacote = new DatagramPacket(envio, envio.length, grupo_atividades, 4321);
				socket.send(pacote);

			}
			

		}

		System.out.print("[Servidor] Multicast Encerrado");
		socketReceber.leaveGroup(new InetSocketAddress(grupo_nome, 4321), ni_nome);
		socketReceber.close();
		socket.close();
	}
}