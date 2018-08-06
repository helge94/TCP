package clientConnection;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class GUI {
	private ArrayList<String>	Args = null;
	private Boolean 			b_Overwrite;
	private ArrayList<Boolean>	b_Args = null;
	
	//Sätter alla basvärden på de olika inputparametrarna
	public void	setStandardValues(PlayerEditorPanel playerEditorPane){
	
		for (PlayerEditorPanel.FieldTitle fieldTitle : 
			PlayerEditorPanel.FieldTitle.values()) {
			
			if(fieldTitle.getTitle() == "IP")
				playerEditorPane.setFieldText(fieldTitle,"localhost");
			
			else if(fieldTitle.getTitle() == "Port")
				playerEditorPane.setFieldText(fieldTitle,"25700");
			
			else if(fieldTitle.getTitle() == "File name")
				playerEditorPane.setFieldText(fieldTitle,"MUX_loog");
			
			else if(fieldTitle.getTitle() == "File size (Mb)")
				playerEditorPane.setFieldText(fieldTitle,"10");
			
			else if(fieldTitle.getTitle() == "File amount")
				playerEditorPane.setFieldText(fieldTitle,"10");
				
		}
	}
	//Hämtar strängargumenten
	public ArrayList<String> getArgs(){
		return Args;
	}
	//hämtar overwrite variabel
	public boolean getOverwrite(){
		return b_Args.get(0);
	}
	//Hämtar persistconnection variabel
	public boolean getPersistConnection(){
		return b_Args.get(1);
	}
	public boolean getReconnectionDelete(){
		return b_Args.get(2);
	}
	
	public GUI() {
		Args = new ArrayList<String>();
		b_Args = new ArrayList<Boolean>();
		
		PlayerEditorPanel playerEditorPane = new PlayerEditorPanel();
		setStandardValues(playerEditorPane);
		int result = JOptionPane.showConfirmDialog(null, playerEditorPane,
				"Edit Player", JOptionPane.OK_CANCEL_OPTION,
            	JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			for (PlayerEditorPanel.FieldTitle fieldTitle : 
				PlayerEditorPanel.FieldTitle.values()) {
				if(!fieldTitle.getIsButton())
					Args.add(playerEditorPane.getFieldText(fieldTitle));
				else
					b_Args.add(playerEditorPane.getButtonValue(fieldTitle));			
          }
         
      }
   }
}

@SuppressWarnings("serial")
class PlayerEditorPanel extends JPanel {
	enum FieldTitle {
		//De olika fälten som finns
      IP("IP", false), PORT("Port", false), FILENAME("File name", false), FILESIZE("File size (Mb)", false),
      FILEAMOUNT("File amount", false), OVERWRITE("Overwrite", true), PERSCONNECTION("Persistent Connection", true), DELETERECON("Delete on Reconnection", true);
		
      private String title;
      private boolean isButton;
      
      //Skapanden av ett fält med Titel och boolesk variabel gällande om det är eller inte är en knapp
      private FieldTitle(String title, boolean button) {
         this.title = title;
         this.isButton = button;
      }
      public boolean getIsButton(){
    	  return isButton;
      }
      public String getTitle() {
         return title;
      }
   };

   private static final Insets WEST_INSETS = new Insets(5, 0, 5, 5);
   private static final Insets EAST_INSETS = new Insets(5, 5, 5, 0);
   //Mappning för strängtitlar
   private Map<FieldTitle, JTextField> fieldMap = new HashMap<FieldTitle, JTextField>();
   //Mappning för knapptitlar
   private Map<FieldTitle, JRadioButton> buttonMap = new HashMap<FieldTitle, JRadioButton>();
   
   public PlayerEditorPanel() {
	      setLayout(new GridBagLayout());
	      setBorder(BorderFactory.createCompoundBorder(
	            BorderFactory.createTitledBorder("Player Editor"),
	            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	      GridBagConstraints gbc;
	      for (int i = 0; i < FieldTitle.values().length; i++) {
	    	  //Om det är strängtitlar
	    	  if(i < FieldTitle.values().length - 3){
	    		FieldTitle fieldTitle = FieldTitle.values()[i];
	    	  	gbc = createGbc(0, i);
	    	  	add(new JLabel(fieldTitle.getTitle() + ":", JLabel.LEFT), gbc);
	         	gbc = createGbc(1, i);
	         	JTextField textField = new JTextField(10);
	         	add(textField, gbc);
	         	fieldMap.put(fieldTitle, textField);
	    	  }
	    	  //Annars är det knapptitlar
	    	  else{
	    		  FieldTitle fieldTitle = FieldTitle.values()[i];	  
		    	  gbc = createGbc(0, i);
		    	  add(new JLabel(fieldTitle.getTitle() + ":", JLabel.LEFT), gbc);
		    	  gbc = createGbc(1, i);
		    	  JRadioButton button = new JRadioButton();
		    	  add(button, gbc);
		    	  buttonMap.put(fieldTitle, button);
	    	  }
      }
      
   }

   
   private GridBagConstraints createGbc(int x, int y) {
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = x;
      gbc.gridy = y;
      gbc.gridwidth = 1;
      gbc.gridheight = 1;

      gbc.anchor = (x == 0) ? GridBagConstraints.WEST : GridBagConstraints.EAST;
      gbc.fill = (x == 0) ? GridBagConstraints.BOTH
            : GridBagConstraints.HORIZONTAL;

      gbc.insets = (x == 0) ? WEST_INSETS : EAST_INSETS;
      gbc.weightx = (x == 0) ? 0.1 : 1.0;
      gbc.weighty = 1.0;
      return gbc;
   }

   //hämtar texten ifrån strängfälten
   public String getFieldText(FieldTitle fieldTitle) {
	      return fieldMap.get(fieldTitle).getText();
	   }
   public void setFieldText(FieldTitle fieldTitle, String string){
	   fieldMap.get(fieldTitle).setText(string);
   }
   //hämtar värdet på knapp ifr knappfält
   public boolean getButtonValue(FieldTitle buttonTitle){
	   return buttonMap.get(buttonTitle).isSelected();
   }

}