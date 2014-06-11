/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loïc Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Zürich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loïc Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were initially written by Loïc Le Coq,
 * a lot of modifications, extensions, refactorings have been applied by Marko Zivkovic 
 */

/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author LoÃ¯c Le Coq
 */
package xlogo;

import javax.media.j3d.VirtualUniverse;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.Language;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.gui.welcome.WelcomeScreen;
import xlogo.kernel.Primitive;
/**
 * @author loic
 *
 *@author Marko - this class generates the language,
 *displays the welcome screen for workspace and user selection,
 *and then starts {@link Application} when the user space is entered. 
 */
public class Logo {
	/**
	 * This ResourceBundle contains all messages for XLogo (menu, errors...)
	 */
	public static ResourceBundle messages=null;

	public static String translationLanguage[]=new String[14];
	
	/**
	 * Welcome screen is always displayed when the application is started.
	 * The user selected a workspace and a user to enter the application.
	 */
	private WelcomeScreen welcomeScreen=null;

	//  private Language language;
	

	/**
	 * The main methods
	 * @param args The file *.lgo to load on startup
	 */
	public static void main(String[] args)   {

		try{
			// Display the java3d version
			java.util.Map<String,String> map=VirtualUniverse.getProperties();
			System.out.println("Java3d :"+map.get("j3d.version"));
		}
		catch(Exception e){
			System.out.println("Java3d problem");
			e.printStackTrace();
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		//Recuperer les fichiers de dÃ©marrage correspondant au double clic de souris
		// ou au lancement en ligne de commande

		GlobalConfig gc = WSManager.getInstance().getGlobalConfigInstance();
		
		for(int i=0;i<args.length;i++){
			gc.getPath().add(args[i]);
		}

		gc.getPath().add(0,"#####");
		new Logo();
	}

	/**Builds Application with the valid Config*/
	public Logo() {
		Language lang = WSManager.getInstance().getWorkspaceConfigInstance().getLanguage();
		generateLanguage(lang);

		showWelcomeScreen();
	}
	
	/**
	 * Display {@link xlogo.gui.welcome.WelcomeScreen} when starting the application.
	 * @author Marko Zivkovic
	 */
	private void showWelcomeScreen() {
		
		try{
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
					welcomeScreen=new WelcomeScreen(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							enterApplication();
						}
					});	
				}
			});
			//UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
		} catch(Exception e){
			System.out.println("here " + e.toString()); //TODO error m
		}
		
	}
	
	/**
	 * 
	 */
	public void enterApplication(){
		WorkspaceConfig wc = WSManager.getInstance().getWorkspaceConfigInstance();
		
		welcomeScreen.dispose();
		generateLanguage(wc.getLanguage());
		
		// Initialize frame
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				new Application();
			}
		});
		
	}
	
	/**
	 * Sets the selected language for all messages
	 * @param id The integer that represents the language
	 */
	public static void generateLanguage(Language lang){ // fixe la langue utilisÃ©e pour les messages
		messages=ResourceBundle.getBundle("langage",lang.getLocale());
		translationLanguage[0]=Logo.messages.getString("pref.general.french");
		translationLanguage[1]=Logo.messages.getString("pref.general.english");
		translationLanguage[2]=Logo.messages.getString("pref.general.arabic");
		translationLanguage[3]=Logo.messages.getString("pref.general.spanish");
		translationLanguage[4]=Logo.messages.getString("pref.general.portuguese");
		translationLanguage[5]=Logo.messages.getString("pref.general.esperanto");
		translationLanguage[6]=Logo.messages.getString("pref.general.german");
		translationLanguage[7]=Logo.messages.getString("pref.general.galician");
		translationLanguage[8]=Logo.messages.getString("pref.general.asturian");
		translationLanguage[9]=Logo.messages.getString("pref.general.greek");
		translationLanguage[10]=Logo.messages.getString("pref.general.italian");
		translationLanguage[11]=Logo.messages.getString("pref.general.catalan");
		translationLanguage[12]=Logo.messages.getString("pref.general.hungarian");
		translationLanguage[13]=Logo.messages.getString("pref.general.abz.german.english");
		
		Primitive.buildPrimitiveTreemap(lang);
	}	
}