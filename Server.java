import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class MessageLog {
	int numOfCommitsReceived;
	int numOfCohorts;
	boolean globalCommitOccurred;
	int numOfAckReceived;
	boolean finalLOgOccurred;

	public MessageLog(int numOfCommitsReceived) {
		this.numOfCommitsReceived = numOfCommitsReceived;
		this.numOfCohorts = 0;
		this.globalCommitOccurred = false;
		this.numOfAckReceived = 0;
		this.finalLOgOccurred = false;
	}
}

public class Server {
	private static ServerSocket server;
	private static int port = 9999;
	static MessageLog msgLog = new MessageLog(0);
	static ArrayList<Socket> allClients = new ArrayList<>();

	public static void main(String[] args) {
		try {
			server = new ServerSocket(port);
			while (true) {
				System.out.println("COORDINATOR listening for request");

				Socket socket = server.accept();
				allClients.add(socket);
				System.out.println("A new COHORT is connected : " + socket);
				msgLog.numOfCohorts++;
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				Thread t = new ClientHandler(socket, dis, dos, msgLog, allClients);
				t.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class ClientHandler extends Thread {
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;
	MessageLog msgLog;
	ArrayList<Socket> allClients;

	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, MessageLog msgLog,
			ArrayList<Socket> allClients) {
		this.s = s;
		this.dis = dis;
		this.dos = dos;
		this.msgLog = msgLog;
		this.allClients = allClients;
	}

	@Override
	public void run() {
		try {
			System.out.println("Sending COMMIT-REQUEST to Cohort(" + s + ")");
			dos.writeUTF("COMMIT-REQUEST");
			String reply = dis.readUTF();
			System.out.println("Server received " + reply + " from Cohort(" + s + ")");
			if (reply.equalsIgnoreCase("AGREED")) {
				incrementCommitCount();

				reply = dis.readUTF();
				System.out.println("Server received " + reply + " from Cohort(" + s + ")");

				if (reply.equalsIgnoreCase("ACK")) {
					msgLog.numOfAckReceived++;
					if (msgLog.numOfAckReceived == msgLog.numOfCohorts) {
						System.out.println("\nWriting Complete Record in the Log file\n");
						return;
					}
				}
				return;

			} else {
				sendGlobalAbort();
				reply = dis.readUTF();
				System.out.println("Server received " + reply + " from Cohort(" + s + ")");
				System.out.println("\nWriting Complete Record in the Log file\n");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	synchronized public void incrementCommitCount() throws IOException {
		msgLog.numOfCommitsReceived++;
		if (!msgLog.globalCommitOccurred && msgLog.numOfCommitsReceived == msgLog.numOfCohorts) {
			msgLog.globalCommitOccurred = true;
			System.out.println("Server sends GLOBAL-COMMIT");
			for (int i = 0; i < allClients.size(); i++) {
				DataOutputStream dos = new DataOutputStream(allClients.get(i).getOutputStream());
				dos.writeUTF("GLOBAL-COMMIT");
			}
		}
	}

	synchronized public void sendGlobalAbort() throws IOException {

		System.out.println("Server sends GLOBAL-ABORT");
		for (int i = 0; i < allClients.size(); i++) {
			DataOutputStream dos = new DataOutputStream(allClients.get(i).getOutputStream());
			dos.writeUTF("GLOBAL-ABORT");
		}

	}
}
