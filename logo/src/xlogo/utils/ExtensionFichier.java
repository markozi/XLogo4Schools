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
 */package xlogo.utils;
import javax.swing.filechooser.*;
import java.io.File;
//Permet de filtrer les fichiers dans un FileChooser
// You can add a filter in a FileChooser

/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author LoÃ¯c Le Coq
 */
public class ExtensionFichier extends FileFilter {
/**
 * @uml.property  name="description"
 */
private String description;   //Description du type de fichiers (Ex: "Fichiers JPEG")
/**
 * @uml.property  name="extension" multiplicity="(0 -1)" dimension="1"
 */
private String[] extension;     //Extension (incluant le '.') Ex: .jpg .java
  public ExtensionFichier() {
  }
  public ExtensionFichier(String description,String[] extension){
  this.description=description;
  this.extension=extension;
  }
  public boolean accept(File f) {
    if (f.isDirectory()) return true;
    String nomFichier = f.getPath().toLowerCase();
    for (int i=0;i<extension.length;i++){
    	if (nomFichier.endsWith(extension[i])) return true;
    	
    }
    return false;
  }
  public String getDescription() {
	  StringBuffer sb=new StringBuffer();
	  sb.append(description);
	  sb.append(" (");
	  for (int i=0;i<extension.length;i++){
		  sb.append("*");
		  sb.append(extension[i]);
		  if (i!=extension.length-1) sb.append(", ");
	  }
	  sb.append(")");
	  return new String(sb);
  }
}