import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private static int port = 9999;

	public static void main(String[] args)
			throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException {

		InetAddress host = InetAddress.getLocalHost();
		Socket socket = null;
		DataOutputStream dos = null;
		DataInputStream dis = null;
		Scanner sc = new Scanner(System.in);

		socket = new Socket(host.getHostName(), port);
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
		System.out.println("Enter type of msg to send 1.AGREED  2.ABORT");
		int choice = sc.nextInt();
		String received = dis.readUTF();
		System.out.println("Received " + received + " from server");
		if (choice == 1 && received.equalsIgnoreCase("COMMIT-REQUEST")) {
			System.out.println("Sending AGREED to Server");
			dos.writeUTF("AGREED");
			received = dis.readUTF();
			if (received.equalsIgnoreCase("GLOBAL-COMMIT")) {
				System.out.println("Received " + received + " from server");
				System.out.println("Releasing resources and locks for transactions...\nSending ACK to server");
				dos.writeUTF("ACK");
			} else {
				System.out.println("Received " + received + " from server");
				System.out.println("Undo transactions...\nSending ACK to server");
				dos.writeUTF("ACK");
			}
		} else {
			System.out.println("Sending ABORT to Server");
			dos.writeUTF("ABORT");
			received = dis.readUTF();
			if (received.equalsIgnoreCase("GLOBAL-ABORT")) {
				System.out.println("Received " + received + " from server");
				System.out.println("Releasing resources and locks for transactions...\nSending ACK to server");
				dos.writeUTF("ACK");
			} else {
				System.out.println("Received " + received + " from server");
				System.out.println("Undo transactions...\nSending ACK to server");
				dos.writeUTF("ACK");
			}
		}

		dis.close();
		dos.close();
		sc.close();
	}
}
