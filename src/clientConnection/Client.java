package clientConnection;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Client{
	
	private		InetAddress						m_Addr				= null;
	private		int								m_PortNumber		= 0;
	private		int								m_FileAmount		= 0;
	private		int								m_FileSize			= 0;
	private		String							m_FileName			= null;
	private		boolean							m_Overwrite			= false;
	private		boolean							m_PersistConnect	= false;
	private		boolean							m_Reconnected		= false;
	private		boolean							m_DeleteRecon		= false;
	private		ClientConnection				m_Connection;
	private 	ArrayList<ArrayList<String>>	m_FilesVector;
	
	public static void main(String[] args) {		
		try {
			@SuppressWarnings("unused")
			Client m_Client = new Client();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Initering av variabler ifrån GUI
	public Client() throws IOException{
		GUI m_GUI = new GUI();
		ArrayList<String> temp = m_GUI.getArgs();
		m_Addr = InetAddress.getByName(temp.get(0));
		m_PortNumber = Integer.parseInt(temp.get(1));
		m_FileName = temp.get(2);
		m_FileName += ".log";
		m_FileSize = Integer.parseInt(temp.get(3));	
		m_FileAmount = Integer.parseInt(temp.get(4));	
		m_Overwrite = m_GUI.getOverwrite();
		m_PersistConnect = m_GUI.getPersistConnection();
		m_DeleteRecon = m_GUI.getReconnectionDelete();
		CreateOutputWindow();
		ConnectToServer();
		
	}
	
	private class CloseListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
	        //DO SOMETHING
	        System.exit(0);
	    }
	}
	private void CreateOutputWindow(){
		JFrame frame = new JFrame();
        frame.add( new JLabel(" Output" ), BorderLayout.NORTH );
        JTextArea ta = new JTextArea();
        TextAreaOutputStream taos = new TextAreaOutputStream( ta, 60 );
        PrintStream ps = new PrintStream( taos );
        System.setOut( ps );
        System.setErr( ps );
     
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton exit = new JButton("Exit");
        exit.addActionListener(new CloseListener());
        buttonPanel.add(exit);
        frame.add(buttonPanel,BorderLayout.SOUTH);
        
        frame.add( new JScrollPane( ta )  );
        frame.pack();
        frame.setVisible( true );
        frame.setSize(800,600);

	}
	//Serveranslutningsfunktion
	private void ConnectToServer() {
		try {
			m_Connection = new ClientConnection(m_Addr, 
					m_PortNumber, 
					m_FileAmount,
					m_FileSize,
					m_FileName,
					m_Overwrite,
					m_Reconnected,
					this
					);
		//Om ingen anslutning uppförs	
		} catch (IOException e) {
			SleepThread();
		}
		if(!m_Reconnected)
			m_FilesVector = m_Connection.getArrayList();
		if(m_Reconnected)
			m_Connection.returnArrayList(m_FilesVector);
		if(m_Connection.Connect()){
			System.out.println("Connected!");
			m_Connection.TCPThread();
		}
	}
	
	protected void Reconnect(){
		if(m_PersistConnect){
			if(m_DeleteRecon)
				m_Reconnected = true;
			ConnectToServer();
		}
	}
	
	//10 sekunders väntetid innan den testar att återansluta
	private void SleepThread(){
		Thread t = new Thread(new Runnable(){
			public void run(){
				System.out.println("Trying to connect...");
				try {
					Thread.sleep(10000);
					ConnectToServer();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		t.run();
	}
}