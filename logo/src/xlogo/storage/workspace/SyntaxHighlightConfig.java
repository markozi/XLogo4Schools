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
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage.workspace;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

public class SyntaxHighlightConfig implements Serializable{

	private static final long serialVersionUID = 7137977560063481685L;
	
	/**
	 *  syntax Highlighting: Color for primitives
	 */
	private int primitiveColor=new Color(0,128,0).getRGB();
	/**
	 *  syntax Highlighting: Style for primitives
	 */
	private int primitiveStyle=Font.PLAIN;
	/**
	 *  syntax Highlighting: Color for operands: numbers....
	 */
	private int operatorColor=Color.BLUE.getRGB();
	/**
	 *  syntax Highlighting: Style for operands
	 */
	private int operatorStyle=Font.PLAIN;
	/**
	 *  syntax Highlighting: Color for comments 
	 */
	private int commentColor=Color.GRAY.getRGB();
	/**
	 *  syntax Highlighting: Style for comments
	 */
	private int commentStyle=Font.PLAIN;
	/**
	 *  syntax Highlighting: Color for parenthesis
	 */
	private int braceColor=Color.RED.getRGB();
	/**
	 *  syntax Highlighting: Style for parenthesis
	 */
	private int braceStyle=Font.BOLD;
	/**
	 *  boolean that indicates if syntax Highlighting is enabled
	 */
	private boolean colorEnabled=true;
	
	public SyntaxHighlightConfig() {
		
	}
	
	public int getPrimitiveColor() {
		return primitiveColor;
	}

	public void setPrimitiveColor(int primitiveColor) {
		this.primitiveColor = primitiveColor;
	}

	public int getPrimitiveStyle() {
		return primitiveStyle;
	}

	public void setPrimitiveStyle(int primitiveStyle) {
		this.primitiveStyle = primitiveStyle;
	}

	public int getOperatorColor() {
		return operatorColor;
	}

	public void setOperatorColor(int operatorColor) {
		this.operatorColor = operatorColor;
	}

	public int getOperatorStyle() {
		return operatorStyle;
	}

	public void setOperatorStyle(int operatorStyle) {
		this.operatorStyle = operatorStyle;
	}

	public int getCommentColor() {
		return commentColor;
	}

	public void setCommentColor(int commentColor) {
		this.commentColor = commentColor;
	}

	public int getCommentStyle() {
		return commentStyle;
	}

	public void setCommentStyle(int commentStyle) {
		this.commentStyle = commentStyle;
	}

	public int getBraceColor() {
		return braceColor;
	}

	public void setBraceColor(int braceColor) {
		this.braceColor = braceColor;
	}

	public int getBraceStyle() {
		return braceStyle;
	}

	public void setBraceStyle(int braceStyle) {
		this.braceStyle = braceStyle;
	}

	public boolean isColorEnabled() {
		return colorEnabled;
	}

	public void setColorEnabled(boolean colorEnabled) {
		this.colorEnabled = colorEnabled;
	}
	
}
