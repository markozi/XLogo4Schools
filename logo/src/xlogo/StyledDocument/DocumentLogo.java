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
 */package xlogo.StyledDocument;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import java.awt.*;

import javax.swing.text.*;

import xlogo.kernel.Primitive;
import xlogo.storage.WSManager;
import xlogo.storage.workspace.WorkspaceConfig;

/**
 * Title : XLogo Description : XLogo is an interpreter for the Logo programming
 * language
 * 
 * @author LoÃ¯c Le Coq
 */

/*
 * Cette classe permet de dÃ©finir la coloration syntaxique dans l'Ã©diteur
 * Coloration des primitives Coloration des nombres, variables ou mot Coloration
 * des commentaires Correspondance entre parenthÃ¨ses ou crochets
 */
public class DocumentLogo extends DefaultStyledDocument {
	private static final long serialVersionUID = 1L;
	private DefaultStyledDocument doc;
	private Element rootElement;

	private MutableAttributeSet parenthese;
	private MutableAttributeSet normal;
	private MutableAttributeSet keyword;
	private MutableAttributeSet comment;
	private MutableAttributeSet quote;
	private boolean coloration_activee = WSManager.getWorkspaceConfig().isSyntaxHighlightingEnabled();
	private boolean colore_parenthese = false;

	public void setColoration(boolean b) {
		coloration_activee = b;
	}

	public DocumentLogo() {
		WorkspaceConfig uc = WSManager.getWorkspaceConfig();

		doc = this;
		rootElement = doc.getDefaultRootElement();
		putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
		initStyles(uc.getCommentColor(), uc.getCommentStyle(),
				uc.getPrimitiveColor(), uc.getPrimitiveStyle(),
				uc.getBraceColor(), uc.getBraceStyle(),
				uc.getOperatorColor(), uc.getOperatorStyle());
	}

	public void initStyles(int c_comment, int sty_comment, int c_primitive,
			int sty_primitive, int c_parenthese, int sty_parenthese,
			int c_operande, int sty_operande) {
		Font font = WSManager.getWorkspaceConfig().getFont();

		normal = new SimpleAttributeSet();
		StyleConstants.setFontFamily(normal, font.getFamily());
		StyleConstants.setForeground(normal, Color.black);
		StyleConstants.setFontSize(normal, font.getSize());

		comment = new SimpleAttributeSet();
		StyleConstants.setForeground(comment, new Color(c_comment));
		setBoldItalic(sty_comment, comment);
		StyleConstants.setFontSize(comment, font.getSize());

		keyword = new SimpleAttributeSet();
		StyleConstants.setForeground(keyword, new Color(c_primitive));
		setBoldItalic(sty_primitive, keyword);
		StyleConstants.setFontSize(keyword, font.getSize());

		quote = new SimpleAttributeSet();
		StyleConstants.setForeground(quote, new Color(c_operande));
		setBoldItalic(sty_operande, quote);
		StyleConstants.setFontSize(quote, font.getSize());

		parenthese = new SimpleAttributeSet();
		StyleConstants.setForeground(parenthese, new Color(c_parenthese));
		setBoldItalic(sty_parenthese, parenthese);
		StyleConstants.setFontSize(parenthese, font.getSize());
	}

	void setBoldItalic(int id, MutableAttributeSet sty) {
		switch (id) {
		case 0: // aucun style
			StyleConstants.setBold(sty, false);
			StyleConstants.setItalic(sty, false);
			StyleConstants.setUnderline(sty, false);
			break;
		case 1: // Gras
			StyleConstants.setItalic(sty, false);
			StyleConstants.setBold(sty, true);
			StyleConstants.setUnderline(sty, false);
			break;
		case 2: // italique
			StyleConstants.setBold(sty, false);
			StyleConstants.setItalic(sty, true);
			StyleConstants.setUnderline(sty, false);
			break;
		case 3: // SoulignÃ©
			StyleConstants.setBold(sty, false);
			StyleConstants.setItalic(sty, false);
			StyleConstants.setUnderline(sty, true);
			break;
		}

	}

	/*
	 * Override to apply syntax highlighting after the document has been updated
	 */
	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		if (str.equals("\t"))
			str = "  ";
		else if (str.equals("[")) {
			if (offset > 0) {
				String element = doc.getText(offset - 1, 1);
				if (!element.equals(" ") && !element.equals("\\"))
					str = " [";
			}
		} else if (str.equals("(")) {
			if (offset > 0) {
				String element = doc.getText(offset - 1, 1);
				if ("\\ *-+/&|><=(".indexOf(element) == -1)
					str = " (";
			}
		}
		super.insertString(offset, str, a);
		if (coloration_activee)
			processChangedLines(offset, str.length());
	}

	/*
	 * Override to apply syntax highlighting after the document has been updated
	 */
	public void remove(int offset, int length) throws BadLocationException {
		/*
		 * if (getText(offset+length-1,1).equals(" ")){
		 * if(getLength()>offset+length) { String
		 * element=getText(offset+length,1); if (element.equals("[")) length--;
		 * } }
		 */
		super.remove(offset, length);

		if (coloration_activee)
			processChangedLines(offset, 0);
	}

	/*
	 * Determine how many lines have been changed, then apply highlighting to
	 * each line
	 */
	public void processChangedLines(int offset, int length)
			throws BadLocationException {
		String content = doc.getText(0, doc.getLength());

		// The lines affected by the latest document update

		int startLine = rootElement.getElementIndex(offset);
		int endLine = rootElement.getElementIndex(offset + length);

		// Do the actual highlighting

		for (int i = startLine; i <= endLine; i++) {
			applyHighlighting(content, i);
		}
	}

	/*
	 * Parse the line to determine the appropriate highlighting
	 */
	private void applyHighlighting(String content, int line)
			throws BadLocationException {
		int startOffset = rootElement.getElement(line).getStartOffset();
		int endOffset = rootElement.getElement(line).getEndOffset() - 1;
		int lineLength = endOffset - startOffset;
		int contentLength = content.length();

		if (endOffset > contentLength)
			endOffset = contentLength - 1;

		// set normal attributes for the line

		doc.setCharacterAttributes(startOffset, lineLength, normal, true);

		// check for single line comment
		// On enlÃ¨ve les Ã©ventuels commentaires
		int index = content.indexOf(getSingleLineDelimiter(), startOffset);
		while (index != -1) {
			if (index == 0) {
				break;
			} else if (!content.substring(index - 1, index).equals("\\")) {
				break;
			}
			index = content.indexOf(getSingleLineDelimiter(), index + 1);
		}

		if ((index > -1) && (index < endOffset)) {
			doc.setCharacterAttributes(index, endOffset - index + 1, comment,
					false);
			endOffset = index - 1;
		}

		// check for tokens
		colore(content, startOffset, endOffset);
	}

	protected boolean isOperator(char character) {
		String operands = "+-/*<=>&|";

		if (operands.indexOf(character) != -1)
			return true;
		else
			return false;
	}

	/*
	 * Override for other languages
	 */
	protected boolean isKeyword(String token) {
		token = token.toLowerCase();
		return Primitive.primitives.containsKey(token);
	}

	protected String getSingleLineDelimiter() {
		return "#";
	}

	public void Montre_Parenthese(int offset) {
		doc.setCharacterAttributes(offset, 1, parenthese, false);
	}

	public void setColore_Parenthese(boolean b) {
		colore_parenthese = b;
	}

	public void colore(String content, int startOffset, int endOffset) {
		int debut = startOffset;
		boolean nouveau_mot = true;
		boolean backslash = false;
		boolean mot = false;
		boolean variable = false;
		for (int i = startOffset; i < endOffset; i++) {
			try { // Sometimes, Exception happens on next line
				char c = content.charAt(i);
				if (c == ' ' || c == '(' || c == ')' || c == '[' || c == ']') {
					if (!backslash) {
						String element = content.substring(debut, i);
						if (mot) {
							doc.setCharacterAttributes(debut, i - debut, quote,
									false);
						} else if (variable) {
							doc.setCharacterAttributes(debut, i - debut, quote,
									false);
						} else if (isKeyword(element)) {
							doc.setCharacterAttributes(debut, i - debut,
									keyword, false);
						} else
							try {
								Double.parseDouble(element);
								doc.setCharacterAttributes(debut, i - debut,
										quote, false);
							} catch (NumberFormatException e) {
							}
						mot = false;
						variable = false;
						debut = i + 1;
						nouveau_mot = true;
						if (c != ' ') {
							if (colore_parenthese)
								doc.setCharacterAttributes(i, 1, parenthese,
										false);
							else {
								doc.setCharacterAttributes(i, 1, normal, false);
								// System.out.println(i+" normal "+StyleConstants.getFontFamily(normal)+" "+StyleConstants.isBold(normal));
							}
						}
					}
					backslash = false;
				} else if (nouveau_mot) {
					if (c == '\"') {
						mot = true;
						backslash = false;
						nouveau_mot = false;
					} else if (c == ':') {
						variable = true;
						backslash = false;
						nouveau_mot = false;
					} else if (isOperator(c)) {
						backslash = false;
						mot = false;
						variable = false;
						nouveau_mot = true;
						debut = i + 1;
						doc.setCharacterAttributes(i, 1, keyword, false);
					} else
						nouveau_mot = false;
				} else if (c == '\\') {
					if (!backslash) {
						backslash = true;
					} else
						backslash = false;
					nouveau_mot = false;
				} else if (isOperator(c)) {
					if (!mot) {
						String element = content.substring(debut, i);
						if (variable)
							doc.setCharacterAttributes(debut, i - debut, quote,
									false);
						else if (isKeyword(element))
							doc.setCharacterAttributes(debut, i - debut,
									keyword, false);
						else
							try {
								Double.parseDouble(element);
								doc.setCharacterAttributes(debut, i - debut,
										quote, false);
							} catch (NumberFormatException e) {
							}
						backslash = false;
						mot = false;
						variable = false;
						nouveau_mot = true;
						debut = i + 1;
						doc.setCharacterAttributes(i, 1, keyword, false);
					}
				}
			} catch (StringIndexOutOfBoundsException e22) {
			}
		}
		if (!nouveau_mot) {
			String element = content.substring(debut, endOffset);
			if (mot) {
				doc.setCharacterAttributes(debut, endOffset - debut, quote,
						false);
			} else if (variable) {
				doc.setCharacterAttributes(debut, endOffset - debut, quote,
						false);
			} else if (isKeyword(element)) {
				doc.setCharacterAttributes(debut, endOffset - debut, keyword,
						false);
			} else
				try {
					Double.parseDouble(element);
					doc.setCharacterAttributes(debut, endOffset - debut, quote,
							false);
				} catch (NumberFormatException e) {
				}
		}

	}

	public void insertStyleNormal(int offset, String str, AttributeSet a) {
		try {
			super.insertString(offset, str, a);
		} catch (BadLocationException e) {
		}
	}
}
