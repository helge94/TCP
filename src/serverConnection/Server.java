package serverConnection;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;


public class Server implements Runnable {
	private ArrayList<ServerConnection> m_ConnectedClients;
	private ServerSocket m_ServerSocket;
	private int m_PortNumber;
	private final Thread t;
	
	//Skapar en ny server med portnummer som input
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java Server portnumber");
			System.exit(-1);
		}
		try {
			@SuppressWarnings("unused")
			Server m_Server = new Server(Integer.parseInt(args[0]));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	

	public Server(int port) throws IOException{
		//Sätter portnumber till portnummer, skapar ny lista av connectedclients samt skapar en ny serversocket vid portnumret
		m_PortNumber = port;
		m_ConnectedClients = new ArrayList<ServerConnection>();
		m_ServerSocket = new ServerSocket(m_PortNumber);	
		//Skapar serverns värld samt två nya trådar
		t = new Thread(this);
		t.start();
	}

	//Lägger till en ny klient i ConnectedClients listan
	public synchronized void addConnection(ServerConnection c)
	{
		m_ConnectedClients.add(c);
	}
	
	
	public void Broadcast(String msg){
		//Skickar till alla klienter
		/*for(int i = 0; i < m_ConnectedClients.size(); i++){			
			m_ConnectedClients.get(i).SendTCP(msg);
		}*/
	}
	
	@Override
	public void run() {
	//Run funktionen som körs till dess att 4 klienter har anslutit
		do{
			Socket m_ClientSocket;
			@SuppressWarnings("unused")
			ServerConnection m_ClientConnection = null;
			try {
				m_ClientSocket = m_ServerSocket.accept();
				m_ClientConnection = new ServerConnection(m_ClientSocket, this);
				System.out.println("ServerSocket accepted, client is trying to connect");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}while(true);
	}

}