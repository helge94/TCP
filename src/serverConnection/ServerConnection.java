package serverConnection;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;



public class ServerConnection implements Runnable{
	private Socket				m_Socket;
	private ObjectOutputStream	m_OutStream;
	private ObjectInputStream	m_InStream;
	private File				m_File;
	private FileInputStream		m_FileStream;
	private BufferedInputStream	m_BufferedFileStream;
	private Server				m_Server;
	private boolean				m_Connected;
	private Thread				t;
	private boolean				alreadyExecuted;

	
	public ServerConnection(Socket ClientSocket, Server Server)  throws IOException{		
		m_Socket 				= ClientSocket;
		m_OutStream 			= new ObjectOutputStream(m_Socket.getOutputStream());
		m_InStream 				= new ObjectInputStream(m_Socket.getInputStream());
		alreadyExecuted			= true;
		m_File 					= new File("D:\\TCPFader\\TCP\\outputNY123");
		m_FileStream			= new FileInputStream(m_File);
		m_BufferedFileStream	= new BufferedInputStream(m_FileStream);
		m_Connected = true;
		m_Server = Server;
		new Thread(this).start();
	}
	
	public void run() {
		ReceiveTCP();
		try {
			m_OutStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TCPThread(this);
	}
	
	//KOllar om meddelandet är av typen MsgMovement och uppdaterar
	public void TCPThread(final ServerConnection c){
		t = new Thread(new Runnable(){
			public void run(){
				//Lägger till denna connection till arrayen i Server
				m_Server.addConnection(c);
				while(m_Connected){
					if(alreadyExecuted) {
						SendFile();
					    //alreadyExecuted = false;
					}
					try{
						Thread.sleep(1000);
						Thread.yield();
					}catch(Exception pEx){
						pEx.printStackTrace();
					}
				}
			}
		});
		t.run();
	}
	public void SendFile()
	{
		 	byte[] contents;
	        long fileLength = m_File.length(); 
	        long current = 0;
	         
	        long start = System.nanoTime();
	        while(current!=fileLength){ 
	            int size = 10000000;
	            if(fileLength - current >= size)
	                current += size;    
	            else{ 
	                size = (int)(fileLength - current); 
	                current = fileLength;
	            } 
	            contents = new byte[size]; 
	            try {
					m_BufferedFileStream.read(contents, 0, size);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
	            SendTCP(contents);
	            System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
	        }  
	}
	
	
	public boolean ReceiveTCP(){
		Object msg = null;
		try {
			msg = m_InStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(msg != null)
			return true;
		
		return false;
	}
	
	//Skickar TCP meddelande
	/*public void SendTCP(String msg)
	{
		try {
			m_OutStream.writeObject(msg);
			m_OutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	public void SendTCP(byte[] file)
	{
		try {
			m_OutStream.writeObject(file);
			m_OutStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
