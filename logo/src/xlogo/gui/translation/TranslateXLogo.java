/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * 
 * Contact Information: marko88zivkovic at gmail dot com
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.  This program is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General 
 * Public License for more details.  You should have received a copy of the 
 * GNU General Public License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA 02110-1301, USA.
 * 
 * 
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings migh have been applied by Marko Zivkovic 
 */

package xlogo.gui.translation;

import javax.swing.JFrame;
import javax.swing.JFileChooser;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import xlogo.Logo;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.storage.WSManager;
import xlogo.storage.workspace.Language;
import xlogo.utils.Utils;
import xlogo.gui.SearchFrame;
/**
 * Modifications made by Marko: <br>
 * IO error is displayed through DialogMessenger (singleton) instead of {@link Application#ecris()}. Result: This class is completely decoupled from the Application class.
 * @author loic Le Coq, slightly modified by Marko Zivkovic
 */
public class TranslateXLogo extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private String id="";
	private String action;

	protected static final String OK="ok";
	protected static final String SEND="send";
	protected static final String SEARCH="search";
	
	protected static final  String CONSULT="0";
	protected static final String MODIFY="1";
	protected static final String CREATE="2";
	protected static final String COMPLETE="3";

	private FirstPanel first;
	private TopPanel top;
	private BottomPanel bottom;
	
	private SearchFrame sf=null;
	public TranslateXLogo(){
		initGui();
	}
	private void initGui(){
		setIconImage(Toolkit.getDefaultToolkit().createImage(
				Utils.class.getResource("icone.png")));
		setTitle(Logo.messages.getString("menu.help.translatexlogo"));
		first=new FirstPanel(this);
		getContentPane().add(first);
		setVisible(true);
	}
	public void actionPerformed(ActionEvent e){
		String cmd=e.getActionCommand();
		if (cmd.equals(TranslateXLogo.OK)){
			action=first.getAction();
			
			if (null==action) return;
			else if (action.equals(TranslateXLogo.MODIFY)) id=first.getLang();
			else if (action.equals(TranslateXLogo.COMPLETE)) id=first.getLang();
			//else if (action.equals(TranslateXLogo.CREATE)) id=first.getNewLang();
			remove(first);
			bottom=new BottomPanel(this,action,id);
			getContentPane().setLayout(new BorderLayout());
			if (!action.equals(TranslateXLogo.CONSULT)){
				top=new TopPanel(this);
				getContentPane().add(top,BorderLayout.NORTH);

			}

			getContentPane().add(bottom,BorderLayout.CENTER);
			this.getContentPane().validate();
		}
		else if (cmd.equals(TranslateXLogo.SEND)){
			String path="";
			JFileChooser jf=new JFileChooser(Utils.SortieTexte(WSManager.getUserConfig().getDefaultFolder()));
		    int retval=jf.showDialog(this,Logo.messages
						.getString("menu.file.save"));
		        if (retval==JFileChooser.APPROVE_OPTION){
		          path=jf.getSelectedFile().getPath();
		          StringBuffer sb=new StringBuffer();
		          try {
		        	  Locale locale=null;
		        	  if (action.equals(TranslateXLogo.CREATE)){
		        		  locale = Language.getLanguage(0).getLocale();
		        		}
		    			else if (!action.equals(TranslateXLogo.CONSULT)){
		    				  locale = Language.getLanguage(Integer.parseInt(id)).getLocale();
		    			}
		        	java.util.Vector<String> v=bottom.getPrimTable().getKeys();
		        	ResourceBundle rb = ResourceBundle.getBundle("primitives", locale);
		    		for (int i=0;i<v.size();i++)	{
		    			String key=v.get(i);
		    			if (action.equals(TranslateXLogo.CREATE)){
		    				writeLine(sb,key,bottom.getPrimValue(i,0));
		        		}
		    			else if (!action.equals(TranslateXLogo.CONSULT)){
		    				String element=bottom.getPrimValue(i, Integer.parseInt(id));
		    //				System.out.println(element+" clé "+key);
		    				if (!rb.getString(key).equals(element)) writeLine(sb,key,element);
		    			}
		    		}
		    		sb.append("\n---------------------------------------\n");
		    		v=bottom.getMessageTable().getKeys();
		    		rb = ResourceBundle.getBundle("langage", locale);
		    		for(int i=0;i<v.size();i++){
		    			String key=v.get(i);
		    			if (action.equals(TranslateXLogo.CREATE)){
		    				writeLine(sb,key,bottom.getMessageValue(i,0).replaceAll("\\n","\\\\n"));
		        		}
		    			else if (!action.equals(TranslateXLogo.CONSULT)){
		    				String element=bottom.getMessageValue(i, Integer.parseInt(id));
		    				if (!rb.getString(key).equals(element)) writeLine(sb,key,element.replaceAll("\\n","\\\\n"));
		    			}
		    		}		    		
		        Utils.writeLogoFile(path,sb.toString());
		      }
		      catch(NullPointerException e3){System.out.println("annulation");} //Si l'utilisateur annule
		      catch(IOException e2){
		    	  DialogMessenger.getInstance().dispatchError("general.error.title", Logo.messages.getString("error.ioecriture"));
		      }
			}
		}
		else if (cmd.equals(TranslateXLogo.SEARCH)){
	    	if (null==sf) {
	    		sf=new SearchFrame(this,bottom.getVisibleTable());
	    		sf.setSize(350, 350);
	    		sf.setVisible(true);
	    	}
		}
	}
	protected void resetSearchFrame(){
		sf=null;
	}
	private void writeLine(StringBuffer sb,String key, String value){
		sb.append(key);
		sb.append("=");
		sb.append(value);
		sb.append("\n");
		
	}
	protected void processWindowEvent(WindowEvent e){
		super.processWindowEvent(e);
		if (e.getID()==WindowEvent.WINDOW_CLOSING){
			//app.close_TranslateXLogo(); TODO Application does not need this. Maybe in settings @ welcome
		}
		
	}
}
