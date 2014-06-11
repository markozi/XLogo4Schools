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

package xlogo.gui;

import javax.swing.text.BadLocationException;
import xlogo.StyledDocument.DocumentLogo;


public class EditorTextPane extends EditorTextZone{
	private  DocumentLogo dsd;
	EditorTextPane(Editor editor){
		super(editor);
		dsd=new DocumentLogo();
		jtc=new ZoneEdition(this);
		initGui();
		initHighlight();
	}
	protected void initHighlight(){
			jtc.setDocument(dsd);
			dsd.addUndoableEditListener(new MyUndoableEditListener());
		}
	protected DocumentLogo getDsd(){
	return dsd;
	}
	protected void ecris(String mot){
		try{
			int deb=jtc.getCaretPosition();
			dsd.insertString(deb,mot,null);
			jtc.setCaretPosition(deb+mot.length());
		}
		catch(BadLocationException e){}
	}
	/**
	 * Added 21.6.2013
	 * <p> this method is used to append a program to the end of the editor.
	 * It is used for the Logo command "define".
	 * @author Marko Zivkovic
	 */
	@Override
	protected void append(String program) {
		try{
			int deb= dsd.getLength();
			dsd.insertString(deb,program,null);
		}
		catch(BadLocationException e){}
		
	}
	
	public void setActive(boolean b){
		((ZoneEdition)jtc).setActive(b);
	}
	
	protected boolean supportHighlighting(){
		return true;
	}
	
}
