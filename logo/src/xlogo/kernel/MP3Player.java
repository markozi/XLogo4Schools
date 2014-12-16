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
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

package xlogo.kernel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import xlogo.Application;
import xlogo.Logo;
import xlogo.storage.WSManager;
import xlogo.utils.Utils;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
public class MP3Player extends Thread{

	private AdvancedPlayer player;

	MP3Player(Application app,String path) throws LogoError{
		try {
			// Build absolutePath
				String absolutePath= Utils.SortieTexte(WSManager.getUserConfig().getDefaultFolder())
				+ File.separator + Utils.SortieTexte(path);
			player=new AdvancedPlayer(new FileInputStream(absolutePath));
		}
		catch(FileNotFoundException e){ 
	          // tentative fichier réseau 
         	  try{
         		  URL url =new java.net.URL(path);
         		  java.io.InputStream fr = url.openStream();
         		  player=new AdvancedPlayer(fr);
         	  }
         	  catch( java.net.MalformedURLException e1){
         		  
             		  throw new LogoError(Logo.messages.getString("error.iolecture"));         			  
         		 

         	  }
         	  catch(IOException e2){}
      		catch(JavaLayerException e3){}
		}
		catch(JavaLayerException e4){}

	}
	public void run(){
		try{
			player.play();
		}
		catch(JavaLayerException e){}
	}
	/**
	 * @return
	 * @uml.property  name="player"
	 */
	protected AdvancedPlayer getPlayer(){
		return player;
	}
		
}
