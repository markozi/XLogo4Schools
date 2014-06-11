/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Lo�c Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Z�rich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Lo�c Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were initially written by Lo�c Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author Loïc Le Coq
 */
package xlogo.utils;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Component;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import xlogo.kernel.MyCalculator;
import xlogo.kernel.Affichage;
import xlogo.storage.WSManager;
import xlogo.storage.workspace.Language;
import xlogo.Logo;

public class Utils {
		
	/**
	 * Marko : Here a technique was used for loading images, that happens to be from the last milenium.
	 * https://weblogs.java.net/blog/chet/archive/2004/07/imageio_just_an.html
	 * 
	 * The way of the 3rs millenium is ImageIO
	 * 
	 * @param nom
	 * @param jf
	 * @return
	 */
	public static ImageIcon dimensionne_image(String name,Component jf){

		Image img = Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(name));
		
		return new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH));
		
		/*
		Image image;
		try
		{
			image = ImageIO.read(new File("xlogo/utils/"+nom));
			return image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		/ *
		Image image=null;
		image= Toolkit.getDefaultToolkit().getImage(Utils.class.getResource(nom));
		MediaTracker tracker=new MediaTracker(jf);
		tracker.addImage(image,0);
		try{tracker.waitForID(0);}
		catch(InterruptedException e1){}
		double largeur_ecran=Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int largeur=image.getWidth(jf); 
		int hauteur=image.getHeight(jf);
		// On fait attention à la résolution de l'utilisateur
		double facteur = largeur_ecran/1024.0; //les images sont prévues pour 1024x768
		if ((int)(facteur+0.001)!=1){
			image=image.getScaledInstance((int)(facteur*largeur),(int)(facteur*hauteur),Image.SCALE_SMOOTH);
			tracker=new MediaTracker(jf);
			tracker.addImage(image,0);
			try{tracker.waitForID(0);}
			catch(InterruptedException e1){}
		}
		return image;
		*/
	  }
	public static void recursivelySetFonts(Component comp, Font font) {
	    comp.setFont(font);
	    if (comp instanceof Container) {
	      Container cont = (Container) comp;
	      for(int j=0, ub=cont.getComponentCount(); j<ub; ++j)
			recursivelySetFonts(cont.getComponent(j), font);
	    }
	}
	public static String rajoute_backslash(String st){
		StringBuffer tampon=new StringBuffer();
		for(int j=0;j<st.length();j++){
			char c=st.charAt(j);
			if (c=='\\') tampon.append("\\\\");
			else if (c==' ') tampon.append("\\e");
			else if ("()[]#".indexOf(c)!=-1) tampon.append("\\"+c);
			else tampon.append(c);
		}
		return(new String(tampon));
	  }
	
	/**
	 * Escape string, backslash ...
	 * @param chaine
	 * @return
	 */
	public static String SortieTexte(String chaine){ // Enlève les backslash
		StringBuffer buffer=new StringBuffer();
		boolean backslash=false;
		boolean ignore=false;
		for (int j=0;j<chaine.length();j++){
			char c=chaine.charAt(j);
			if (backslash) {
				if (c=='e') buffer.append(' ');
//				else if (c=='\\') buffer.append('\\');
				else if (c=='n') buffer.append("\n");
				else if(c=='v') buffer.append("");
				else if(c=='l') {
					ignore=true;
				}
				else if("[]()#\\".indexOf(c)>-1) buffer.append(c);
				backslash=false;
			}
			else {
				if (c=='\\') backslash=true;
				else if (!ignore) buffer.append(c);
				else if (c==' ') ignore=false;
			}
		}
		return MyCalculator.getOutputNumber(new String(buffer));
	}
	/**
	 * This method is formatting the String st.<br>
	 * - Unused white spaces are deleted.<br>
	 * - The character \ is modified to \\ <br>
	 * - The sequence "\ " is modified to "\e"<br>
	 * - The sequence "\ " is modified to "\e"<br>
 	 * - The sequence "\ " is modified to "\e"<br>
 	 * - The sequence "\ " is modified to "\e"<br>
	 * @param st The String instruction to format
	 * @return The formatted instructions
	 */
	public static StringBuffer decoupe(String st) {  
		StringBuffer buffer = new StringBuffer();
		// If last character is a white space
		boolean espace=false;
		// If last character is a backslash
		boolean backslash=false;
		// If last character is a word
		boolean mot=false;
		
		int crochet_liste=0;
//		boolean variable=false;
		// If XLogo is running a program
		boolean execution_lancee=Affichage.execution_lancee;
		for(int i=0;i<st.length();i++){
			char c=st.charAt(i);
			if (c==' ') {
				if (!espace&&buffer.length()!=0) {
					if (backslash) buffer.append("\\e");
					else {
						buffer.append(c);
						espace=true;
						mot=false;
	//					variable=false;
					}
					backslash=false;
				}
			}
			else if(c=='\\'&&!backslash) {
				espace=false;
				backslash=true;
			}
			else if(c=='\"'){
				if (espace&&crochet_liste<=0){
					mot=true;
				}
				buffer.append(c);
				espace=false;
				backslash=false;
			}
			else if (c==':'){
		/*		if (espace&&crochet_liste<=0){
					variable=true;
				}*/
				buffer.append(c);
				espace=false;
				backslash=false;
			}
			else if (c=='['||c==']'||c=='('||c==')'){
				//Modifications apportées
				if (backslash) {
					buffer.append("\\"+c);
					backslash=false;
				}
				else {
					if (c=='[') crochet_liste++;
					else if (c==']') crochet_liste--;
					if (espace||buffer.length()==0) {buffer.append(c+" ");espace=true;}
					else {
						buffer.append(" "+c+" ");
						mot=false;
						espace=true;
					}
				}
			}
			else if (c=='+'||c=='-'||c=='*'||c=='/'||c=='='||c=='<'||c=='>'||c=='&'||c=='|'){
				//System.out.println(mot+" "+espace);
				// à modifier (test + fin)
				if (mot||crochet_liste>0) {
					buffer.append(c);
					if (espace) espace=false;
				}
				else { 
					String op=String.valueOf(c);
					// Looking for operator <= or >=
					if (c=='<'||c=='>'){
						if (i+1<st.length()){
							if (st.charAt(i+1)=='='){
								op+="=";
								i++;
							}
						}
					}
					if (espace) buffer.append(op+" ");
					else {
						espace=true;
						if (buffer.length()!=0) buffer.append(" "+op+" ");
						// If buffer is empty no white space before
						else buffer.append(op+" ");
					}
				}
			}
			else{
				if (backslash){
					if (c=='n')	buffer.append("\\n");
					else if (c=='\\') buffer.append("\\\\"); 
					else if (c=='v'&& execution_lancee) buffer.append("\"");
					else if(c=='e'&& execution_lancee) buffer.append("\\e");
					else if (c=='#') buffer.append("\\#");
					else if (c=='l'&&execution_lancee) buffer.append("\\l");
					else { 
						buffer.append(c);
					}
				}
				else {
					buffer.append(c);	
				}
				backslash=false;
				espace=false;
			}
		}
//		System.out.println(buffer);
		// Remove the space when the user write only "*" or "+" in the command line
		//if (buffer.length()>0&&buffer.charAt(0)==' ') buffer.deleteCharAt(0);
		return (buffer);
	}
	
	
	
	public static String specialCharacterXML(String st){
		st=st.replaceAll("&","&amp;");
		st=st.replaceAll("<","&lt;");
		st=st.replaceAll("\"","&quot;");
		st=st.replaceAll(">","&gt;");
		st=st.replaceAll("'","&apos;");
		
		return st;
	}
	public static String readLogoFile(String path) throws IOException{		// ADAPT READ LOGO FILE
		String txt="";
		// The old format before XLogo 0.9.23 is no longer supported from version 0.9.30
		try{
          	// New format for XLogo >=0.923
          	// Encoded with UTF-8
    		StringBuffer sb=new StringBuffer();
          	 FileInputStream fr = new FileInputStream(path);
              InputStreamReader isr = new  InputStreamReader(fr,  "UTF8");
              BufferedReader brd=new BufferedReader(isr);
              while (brd.ready()){
              	sb.append(brd.readLine());
              	sb.append("\n");
              }
              txt=new String(sb);
           }
           catch(FileNotFoundException e1){
             // tentative fichier réseau 
           	  try{
           		  URL url =new java.net.URL(path);
           		  StringBuffer sb=new StringBuffer();
           		  java.io.InputStream fr = url.openStream();
           		  InputStreamReader isr = new  InputStreamReader(fr,  "UTF8");
           		  BufferedReader brd=new BufferedReader(isr);
           		  while (brd.ready()){
           			  String st=brd.readLine();
           			  sb.append(st);
           			  sb.append("\n");
                     }
           		  txt=new String(sb);
           	  }
           	  catch( java.net.MalformedURLException e){
           		  System.out.println("File not found: "+path.toString());	
           	  }
          }
          catch(Exception e){e.printStackTrace();}
          if (txt.startsWith("# "+Logo.messages.getString("mainCommand"))){
        	  int id=txt.indexOf("\n");
        	  if (id!=-1){
        		  WSManager.getUserConfig().setMainCommand(txt.substring(("# "+Logo.messages.getString("mainCommand")).length(),id).trim());
        		  txt=txt.substring(id+1);
        	  }
          };
          return txt;
	}
	
	/**
	 * Store a string to a logo file path, UTF9 encoding,
	 * Write the main command to the head of the file with a #
	 * @param path
	 * @param txt
	 * @throws IOException
	 */
	public static void writeLogoFile(String path,String txt) throws IOException{	// ADAPT write logo file
		try{
			if (!WSManager.getUserConfig().getMainCommand().trim().equals("")) {
				String heading="# "+Logo.messages.getString("mainCommand")+" "+WSManager.getUserConfig().getMainCommand()+"\n";
				txt=heading+txt;
			}
			FileOutputStream f = new FileOutputStream(path);
			BufferedOutputStream b = new BufferedOutputStream(f);	
			OutputStreamWriter osw = new  OutputStreamWriter(b,  "UTF8");
	        osw.write(txt);
	        osw.close();
	        b.close();
	        f.close();

		}
		catch(FileNotFoundException e1){e1.printStackTrace();}
	}
	public static boolean fileExists(String name){
		File f=new File(name);
		return f.exists();
	}
	
	/**
	 * @param name
	 * @return
	 * @author Marko Zivkovic
	 */
	public static boolean isFile(String path){
		File f = new File(path);
		return f.isFile();
	}
	/**
	 * @param path
	 * @return
	 * @author Marko Zivkovic
	 */
	public static boolean isDirectory(String path){
		File f = new File(path);
		return f.isDirectory();
	}
	
	/**
	 * Implementation inspired by "JAVA ist auch eine Insel" - Christian Ullenboom, Galileo Computing
	 * <p> If destination exists, it will be replaced
	 * @param src
	 * @param dest
	 * @throws IOException If there is a problem with either src or dest
	 * @author Marko Zivkovic
	 */
	public static void copyFile(String src, String dest) throws IOException
	{
		copyFile(new File(src), new File(dest));
	}
	/**
	 * Implementation inspired by "JAVA ist auch eine Insel" - Christian Ullenboom, Galileo Computing
	 * <p> If destination exists, it will be replaced
	 * @param src
	 * @param dest
	 * @throws IOException If there is a problem with either src or dest
	 * @author Marko Zivkovic
	 */
	public static void copyFile(File src, File dest) throws IOException
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try
		{
			fis = new FileInputStream(src);
			fos = new FileOutputStream(dest);
			byte[] buffer = new byte[0xFFFF];
			for (int len; (len = fis.read(buffer)) != -1;)
			{
				fos.write(buffer, 0, len);
			}
		}catch (IOException e) {
			throw e;
		}
		finally {
			if (fis != null)
				try { fis.close(); } catch (IOException e) {}
			if (fos != null)
				try { fos.close(); } catch (IOException e) {}
		}
	}
	
	/**
	 * First copy file to dest and then delete file.
	 * @param file
	 * @param dest
	 * @throws IOException If there is a problem with either file or dest
	 * @author Marko Zivkovic
	 */
	public static void moveFile(String file, String dest) throws IOException
	{
		moveFile(new File(file), new File(dest));
	}
	
	/**
	 * First copy file to dest and then delete file.
	 * @param file
	 * @param dest
	 * @throws IOException If there is a problem with either file or dest
	 * @author Marko Zivkovic
	 */
	public static void moveFile(File file, File dest) throws IOException
	{
		copyFile(file, dest);
		file.delete();
	}
	

  public static String primitiveName(String generic){
	  		Language lang = WSManager.getInstance().getWorkspaceConfigInstance().getLanguage();
			Locale locale = lang.getLocale();
			ResourceBundle prim = ResourceBundle.getBundle(
					"primitives", locale);
			String st = prim.getString(generic);
			StringTokenizer str = new StringTokenizer(st);
			while (str.hasMoreTokens()) {
				st = str.nextToken();
			}
			return st;
	  }
}
