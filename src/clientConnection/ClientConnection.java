package clientConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class ClientConnection {
	private static final String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
	private Socket							m_ServerSocket;
	private ObjectOutputStream				m_OutStream = null;
	private InputStream						m_InStream = null;
	private InetAddress						m_Address;
	private Client							m_Client;
	private boolean							m_Connected;
	private boolean							m_Read;
	private boolean							m_Overwrite;
	private byte[]							m_ByteArray;
	private int								m_FileAmount;
	private int								m_CurrentFile = 0;
	private long							m_FileSize;
	private ArrayList<String>				m_FileNameArray;
	private ArrayList<ArrayList<String>>	m_FilesVector;
	private	String							m_CurrentFileName;
	private	String							m_FileName;
	
	public ClientConnection(InetAddress Address, int Port, int Amount,
			int Size, String Name, boolean overwrite, boolean reconnected, Client Client) throws IOException{
		
		System.out.println(Address);
		m_ServerSocket 		= new Socket(Address, Port);
		m_Connected 		= true;
		m_Read				= true;
		m_Overwrite			= overwrite;
		m_FileAmount 		= Amount;
		m_FileSize 			= Size *= 1000000L;
		m_Address			= Address;
		m_Client			= Client;
		m_FilesVector = new ArrayList<ArrayList<String>>();
		m_FileName = Name;
		
		if(!reconnected)
			FilesList();
		
		m_OutStream 	= new ObjectOutputStream(m_ServerSocket.getOutputStream());
		m_OutStream.flush();
		m_InStream		=  m_ServerSocket.getInputStream();
	}
	
	public ArrayList<ArrayList<String>> getArrayList(){
		return m_FilesVector;
	}
	
	public void	returnArrayList(ArrayList<ArrayList<String>> list){
		m_FilesVector = list;
		AddDate();
	}
	//Connect, behövs ej egentligen
	public boolean Connect(){
		try {
			m_OutStream.writeObject("");
			m_OutStream.flush();
			m_OutStream.reset();
		} catch (IOException e){
			return false;
		}
		return true;
	}

	
	public static boolean isReachableByPing(String host) {
        try{
                String cmd = "ping -n 1 " + host;
                Process myProcess = Runtime.getRuntime().exec(cmd);
                myProcess.waitFor();
                
                ArrayList<String> adresser = new ArrayList<String>();
                Scanner scanner = new Scanner(myProcess.getInputStream());
                while (scanner.hasNext()) {
                    String temp = scanner.findInLine(Pattern.compile(IPADDRESS_PATTERN));
                    if(temp != null)
                    	adresser.add(temp);
                    scanner.next();
                }
                scanner.close();
                if((myProcess.exitValue() != 0) || (adresser.get(0).compareTo(adresser.get(1)) != 0)) {
                        return false;
                } else {
                        return true;
                }

        } catch( Exception e ) {
                e.printStackTrace();
                return false;
        }
}
	
	public void PingThread(){
		final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				  if(!m_ServerSocket.isClosed())
					  System.out.println("PINGING!!");				  
				  else
					  System.out.println("Trying to reconnect...");	
				  
				  
		    	  boolean reachable = isReachableByPing(m_Address.getHostAddress());
		    	  		
		    	  	if(!reachable && !m_ServerSocket.isClosed()){
						System.out.println("PINGING FAILED");
						m_Connected = false;
						try {
							m_ServerSocket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
		    	  	}
		    	  	
	    	  		if(m_ServerSocket.isClosed()){	
	    				if(reachable){
	    					System.out.println("Server reachable, reconnecting");
		    	  			m_Client.Reconnect();
		    	  			executor.shutdownNow();
		    	  		}
	    				else
	    					System.out.println("Server not reachable");
		    	  	}
		       }			
		}, 10000, 10000, TimeUnit.MILLISECONDS);
	}
	
	
	//TCPTråden
	public void TCPThread(){
		Thread t = new Thread(new Runnable(){
			public void run(){
				PingThread();				
				while(m_Connected)
					ReceiveTCP();
			}
		});
		t.run();
	}
	
	
	public void FilesList(){
		for(int i = 0; i < m_FileAmount; i++){
			String[] temp = m_FileName.split("(?=\\.log)");

			ArrayList<String> NameAndType = new ArrayList<>(Arrays.asList(temp));
			NameAndType.add(NameAndType.size() - 1, "");
			m_FileNameArray = NameAndType;
			
			String temp1 = Integer.toString(i);
			m_FileNameArray.set(1, temp1);
			m_FileNameArray.set(2, "");
			m_FilesVector.add(m_FileNameArray);
			
		}
		AddDate();
		m_CurrentFileName = String.join("", m_FilesVector.get(0));
	}
	
	public void AddDate(){
		//Tar bort föregående fil med nuvarande nummer
		String temp1 = String.join("", m_FilesVector.get(m_CurrentFile));
		File file = new File(temp1);
		file.delete();
		
		//Sparar ner nuvarande fil i en temp variabel och lägger till dagens datum med tidsstämpel
		ArrayList<String> temp = m_FilesVector.get(m_CurrentFile);	
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("_yyyyMMdd_HH.mm.ss");
		String text = date.format(formatter) + ".log";	
		temp.set(2, text);
		
		//Lägger till den nya filen med korrekt datummarkering
		m_FilesVector.set(m_CurrentFile, temp);
		
		//Sätter CurrentFileName till nya filen
		temp1 = String.join("", m_FilesVector.get(m_CurrentFile));
		m_CurrentFileName = temp1;
	}
	
	
	//Filnamnsbytesfunktion
	public void	ChangeFileName()
	{
		m_CurrentFile++;
		
		//Om antalet filer ej är maxantal		
		if(m_CurrentFile < m_FileAmount){
			AddDate();
			return;
		}
	
		//Om antalet filer är maxantal och overwrite är ikryssat
		else if (m_Overwrite){
			m_CurrentFileName = String.join("", m_FilesVector.get(0));
			m_CurrentFile = 0;
			AddDate();
		}
		
		//Om filerna ej skall skrivas över
		else{
			System.out.println("weho");
			m_Read = false;
		}
			
	}
	//Funktion som kollar filstorlek
	public boolean CheckFileSize()
	{
		File file = new File(m_CurrentFileName);
		if(file.length() >= m_FileSize){
			return false;
		}
		return true;
	}
	
	public String ReceiveTCP(){
		while(m_Read){
			//Storlek som skall tas emot
			byte[] msg = new byte[1024];
			try {
				//Läser data
				m_InStream.read(msg);
			} catch (Exception e) {
				//Om anslutning bryts avslutar denna del nuvarande fas.
				m_Connected = false;
				return null;
			}
			//Om meddelandet innehåller något
					m_ByteArray = msg;
					try {
						//Skriver över meddelandet till nuvarande fil
						FileOutputStream fos = new FileOutputStream(m_CurrentFileName, true);
						if(!CheckFileSize()){
							ChangeFileName();	
						}			
						fos.write(m_ByteArray);
						fos.flush();
						fos.close();
					} catch (IOException e) {
					}
				return null;
		}
		return null;
		
	}
}
