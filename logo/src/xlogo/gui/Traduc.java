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

package xlogo.gui;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import xlogo.storage.WSManager;
import xlogo.storage.workspace.Language;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.utils.Utils;
import xlogo.AppSettings;
import xlogo.Logo;
/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author Loïc Le Coq
 */

/** Frame For translating Logo Code from a language to another
 *  * */

public class Traduc extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JLabel traduire_de=new JLabel(Logo.messages.getString("traduire_de")+" ");
	private JLabel vers=new JLabel(" "+Logo.messages.getString("vers")+" ");

	private JComboBox combo_origine=new JComboBox(Logo.translationLanguage);
	private JComboBox combo_destination=new JComboBox(Logo.translationLanguage);
	private JScrollPane js_source=new JScrollPane();
	private JScrollPane js_destination=new JScrollPane();
	private JTextArea origine=new JTextArea();
	private JTextArea destination=new JTextArea();
	private JPanel p_nord_origine =new JPanel();
	private JPanel p_nord_destination =new JPanel();
	private JPanel p_ouest =new JPanel();
	private JPanel p_est=new JPanel();
	private JPanel p_edition_origine=new JPanel();
	private JPanel p_edition_destination=new JPanel();
	private JButton traduire=new JButton(Logo.messages.getString("traduire"));
	private ImageIcon icopier=Utils.dimensionne_image("editcopy.png",this);
	private ImageIcon icoller=Utils.dimensionne_image("editpaste.png",this);
	private ImageIcon icouper=Utils.dimensionne_image("editcut.png",this);
	private JButton copier_origine=new JButton(icopier);
	private JButton coller_origine=new JButton(icoller);
	private JButton couper_origine=new JButton(icouper);
	private JButton copier_destination=new JButton(icopier);
	private JButton coller_destination=new JButton(icoller);
	private JButton couper_destination=new JButton(icouper);
	
	private ResourceBundle primitives_origine=null;
	private ResourceBundle primitives_destination=null;
	private TreeMap<String,String> tre=new TreeMap<String,String>();
		
	public Traduc(){
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		Font font = wc.getFont();
		Language lang = AppSettings.getInstance().getLanguage();
		
		setTitle(Logo.messages.getString("menu.tools.translate"));
		setIconImage(Toolkit.getDefaultToolkit().createImage(Utils.class.getResource("icone.png")));
		setFont(font);
		traduire.setFont(font);
		vers.setFont(font);
		traduire_de.setFont(font);
		getContentPane().setLayout(new BorderLayout());
		combo_origine.setSelectedIndex(lang.getValue());
		combo_destination.setSelectedIndex(lang.getValue());
		
		p_nord_origine.add(traduire_de);
		p_nord_origine.add(combo_origine);
		p_nord_destination.add(vers);
		p_nord_destination.add(combo_destination);
		
		p_edition_origine.add(copier_origine);
		p_edition_origine.add(couper_origine);
		p_edition_origine.add(coller_origine);

		p_ouest.setLayout(new BorderLayout());
		p_ouest.add(js_source,BorderLayout.CENTER);
		p_ouest.add(p_edition_origine,BorderLayout.SOUTH);
		p_ouest.add(p_nord_origine,BorderLayout.NORTH);
		
		getContentPane().add(p_ouest,BorderLayout.WEST);

		p_edition_destination.add(copier_destination);
		p_edition_destination.add(couper_destination);
		p_edition_destination.add(coller_destination);

		p_est.setLayout(new BorderLayout());
		p_est.add(js_destination,BorderLayout.CENTER);
		p_est.add(p_edition_destination,BorderLayout.SOUTH);
		p_est.add(p_nord_destination,BorderLayout.NORTH);
		
		getContentPane().add(p_est,BorderLayout.EAST);
		getContentPane().add(traduire,BorderLayout.CENTER);
		
		js_source.getViewport().add(origine);
		js_destination.getViewport().add(destination);
		js_source.setPreferredSize(new Dimension(300,300));
		js_destination.setPreferredSize(new Dimension(300,300));

		traduire.addActionListener(this);
		copier_destination.addActionListener(this);
		couper_destination.addActionListener(this);
		coller_destination.addActionListener(this);
		copier_origine.addActionListener(this);
		couper_origine.addActionListener(this);
		coller_origine.addActionListener(this);
		copier_origine.setActionCommand("copier_origine");
		couper_origine.setActionCommand("couper_origine");
		coller_origine.setActionCommand("coller_origine");
		coller_destination.setActionCommand("coller_destination");
		copier_destination.setActionCommand("copier_destination");
		couper_destination.setActionCommand("couper_destination");

		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e){
		 String cmd=e.getActionCommand();
		if (Logo.messages.getString("traduire").equals(cmd)){
			String texte=origine.getText();
			texte=texte.replaceAll("\\t", "  ");
			primitives_origine=genere_langue(combo_origine);
			primitives_destination=genere_langue(combo_destination);
			Enumeration<String> en=primitives_origine.getKeys();
			while (en.hasMoreElements()){
				String element=en.nextElement().toString();
				String primitives=primitives_origine.getString(element);
				String primitives2=primitives_destination.getString(element);
				StringTokenizer st=new StringTokenizer(primitives);
				StringTokenizer st2=new StringTokenizer(primitives2);
				int compteur=st.countTokens();
				for (int i=0;i<compteur;i++){
					while (st2.hasMoreTokens()) element=st2.nextToken();
					tre.put(st.nextToken(),element);				
				}
			}
			// ajout des mots clés pour et fin
			int id=combo_origine.getSelectedIndex();
			Locale locale=Language.valueOf(id).getLocale();
			ResourceBundle res1=ResourceBundle.getBundle("langage",locale);
			id=combo_destination.getSelectedIndex();
			locale=Language.valueOf(id).getLocale();
			ResourceBundle res2=ResourceBundle.getBundle("langage",locale);
			tre.put(res1.getString("pour"),res2.getString("pour"));
			tre.put(res1.getString("fin"),res2.getString("fin"));
			StringTokenizer st=new StringTokenizer(texte," */+-\n|&()[]",true);
			String traduc="";
			while (st.hasMoreTokens()){
				String element=st.nextToken().toLowerCase();
				if (tre.containsKey(element)) traduc+=tre.get(element);	
				else traduc+=element;
			}
			destination.setText(traduc);
		}
		else if ("copier_origine".equals(cmd)) {
			origine.copy();
		}
		else if ("couper_origine".equals(cmd)) {
			origine.cut();
		}
		else if ("coller_origine".equals(cmd)) {
			origine.paste();
		}
		else if ("copier_destination".equals(cmd)) {
			destination.copy();
		}
		else if ("couper_destination".equals(cmd)) {
			destination.cut();
		}
		else if ("coller_destination".equals(cmd)) {
			destination.paste();
		}
	}
 private ResourceBundle genere_langue(JComboBox jc){ // fixe la langue utilisée pour les messages
	  Locale locale=null;
	  int id=jc.getSelectedIndex();
		locale=Language.valueOf(id).getLocale();
		return ResourceBundle.getBundle("primitives",locale);
	}
}
