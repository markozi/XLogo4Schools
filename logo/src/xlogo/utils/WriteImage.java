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
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

package xlogo.utils;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import xlogo.Logo;
import xlogo.storage.WSManager;

import java.awt.image.BufferedImage;
import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JDialog;

public class WriteImage extends Thread{
	private BufferedImage image;
	private JFrame owner;
	private String path;
	   public WriteImage(JFrame owner,BufferedImage image){
	     this.image=image;
	     this.owner=owner;
	   }
	public void setImage(BufferedImage img){
		   image=img;
	   }
	   public int chooseFile(){
		   JFileChooser jf = new JFileChooser();
		    String[] ext={".jpg",".png"};
		    jf.addChoosableFileFilter(new ExtensionFichier(Logo.messages.getString("imagefile"),
		        ext  ));
		    int retval = jf.showDialog(owner, Logo.messages.getString("menu.file.save"));
	      // Si l'utilisateur appuie sur enregistrer du JFileChooser
		    if (retval == JFileChooser.APPROVE_OPTION) {
	    	  // On rajoute l'extension convenable au fichier
	  		  path=jf.getSelectedFile().getPath();
	  		  String copie_path=path.toLowerCase(); //
	  		  if (!copie_path.endsWith(".jpg") && !copie_path.endsWith(".png")) {
	  			  String st = jf.getFileFilter().getDescription().toLowerCase();
	  			  if (st.endsWith("jpg)"))
	  				  path += ".jpg";
	  			  else if (st.endsWith("png)"))
	  				  path += ".png";
	  			  else
	  				  path += ".jpg";
	  		  }   
		    }
		    return retval;
	   }
	   
	   public void run(){
		   
		  ProgressDialog progress=new ProgressDialog(owner);
		   
		   
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	     // On Ã©crit le fichier
	     try {
	       if (path.endsWith(".jpg")) {
	         File f = new File(path);
	         ImageIO.write(image, "jpg", f);
	       }
	       else if (path.endsWith(".png")) {
	         File f = new File(path);
	         ImageIO.write(image, "png", f);
	       }
	     }
	     catch (IOException ex) {System.out.println(ex.toString());}
	     progress.dispose();
	   }
	   private class ProgressDialog extends JDialog{

		private static final long serialVersionUID = 1L;
			private JProgressBar prog=new JProgressBar();
			ProgressDialog(JFrame owner){
				super(owner);
			   initGui();
		   }
			private void initGui(){
				setFont(WSManager.getWorkspaceConfig().getFont());
				setTitle(Logo.messages.getString("titredialogue2"));
				prog.setIndeterminate(true);
				java.awt.FontMetrics fm = owner.getGraphics()
				.getFontMetrics(WSManager.getWorkspaceConfig().getFont());
				int width = fm.stringWidth(Logo.messages.getString("titredialogue2"));
				setSize(new Dimension(width+150,100));
				getContentPane().add(prog);
				setVisible(true);
			}
	   }
}