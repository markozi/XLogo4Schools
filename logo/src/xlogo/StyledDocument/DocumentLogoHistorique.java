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
import java.awt.Color;
import java.awt.Font;

import javax.swing.text.*;

import xlogo.gui.HistoryPanel;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author LoÃ¯c Le Coq
 */

public class DocumentLogoHistorique extends DocumentLogo {
	private static final long serialVersionUID = 1L;
	private Color couleur_texte = Color.BLUE;
	private int taille_texte = 12;
	private String style="normal";
	private MutableAttributeSet normal;
	private MutableAttributeSet erreur;
	private MutableAttributeSet commentaire;
	private MutableAttributeSet perso;
	private boolean tape=false;
	public DocumentLogoHistorique() {
		super();
		
		Font font = WSManager.getWorkspaceConfig().getFont();
		
		//Style normal
		normal = new SimpleAttributeSet();
		StyleConstants.setFontSize(normal, font.getSize());
		StyleConstants.setFontFamily(normal, font.getName());

		// Style pour l'Ã©criture des erreurs
		erreur = new SimpleAttributeSet();
		StyleConstants.setForeground(erreur, Color.RED);
		StyleConstants.setFontSize(erreur, font.getSize());
		StyleConstants.setFontFamily(erreur, font.getName());

		//Style pour les commentaires (Vous venez de dÃ©finir ...)
		commentaire = new SimpleAttributeSet();
		StyleConstants.setForeground(commentaire, Color.BLUE);
		StyleConstants.setFontSize(commentaire, font.getSize());
		StyleConstants.setFontFamily(commentaire, font.getName());

		// Style pour la primitive Ã©cris et la primitive tape
		perso = new SimpleAttributeSet();
		StyleConstants.setForeground(perso, Color.BLACK);
		StyleConstants.setFontFamily(perso, font.getName());
		
		
	}
	public void setStyle(String sty){
		style=sty;
	}
	public Color getCouleurtexte() {
		return couleur_texte;
	}

	public int police() {
		return taille_texte;
	}
	public void fixecouleur(Color color) {
		couleur_texte = color;
		StyleConstants.setForeground(perso, couleur_texte);
	}
	public void fixepolice(int taille) {
		taille_texte = taille;
		StyleConstants.setFontSize(perso, taille_texte);
	}
	public void fixenompolice(int id) {
		StyleConstants.setFontFamily(perso,
				GlobalConfig.fonts[HistoryPanel.fontPrint].getName());
	}
	public void fixegras(boolean b){
		StyleConstants.setBold(perso,b);
	}
	public void fixeitalique(boolean b){
		StyleConstants.setItalic(perso,b);
	}
	public void fixesouligne(boolean b){
		StyleConstants.setUnderline(perso,b);
	}
	public void fixeexposant(boolean b){
		StyleConstants.setSuperscript(perso,b);
	}
	public void fixeindice(boolean b){
		StyleConstants.setSubscript(perso,b);
	}
	public void fixebarre(boolean b){
		StyleConstants.setStrikeThrough(perso,b);
	}
	public boolean estgras(){
		return StyleConstants.isBold(perso);
	}
	public boolean estitalique(){
		return StyleConstants.isItalic(perso);
	}
	public boolean estsouligne(){
		return StyleConstants.isUnderline(perso);
	}
	public boolean estexposant(){
		return StyleConstants.isSuperscript(perso);
	}
	public boolean estindice(){
		return StyleConstants.isSubscript(perso);
	}
	public boolean estbarre(){
		return StyleConstants.isStrikeThrough(perso);
	}
	public Font getFont(){
		return GlobalConfig.fonts[HistoryPanel.fontPrint].deriveFont(Font.BOLD,(float)taille_texte);
		
	}
	public void change_police_interface(Font font, int taille) {
		
		String famille = font.getName();

		StyleConstants.setFontSize(normal, taille);
		StyleConstants.setFontFamily(normal, famille);

		StyleConstants.setFontSize(commentaire, taille);
		StyleConstants.setFontFamily(commentaire, famille);

		StyleConstants.setFontSize(erreur, taille);
		StyleConstants.setFontFamily(erreur, famille);
	}
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException
	{
		if (tape) {
			tape=false;	
			super.insertStyleNormal(offset,str,a);
		}
		else super.insertString( offset, str, a);
		if (style.equals("erreur")) this.setCharacterAttributes(offset,str.length(),erreur,true);
		else if (style.equals("commentaire")) this.setCharacterAttributes(offset,str.length(),commentaire,true);
		else if (style.equals("perso")) this.setCharacterAttributes(offset,str.length(),perso,true);		
		if (!str.endsWith("\n")) tape=true;
	}

}