/**
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

/* Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author Loïc Le Coq
 */

package xlogo;
// TODO Use parts of this that might be useful, and then delete this file
import java.awt.*;
import java.io.*;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.event.*;

import javax.swing.*;

import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.workspace.Language;
import xlogo.utils.WebPage;
import xlogo.utils.Utils;
import xlogo.gui.AImprimer;
import xlogo.gui.MyTextAreaDialog;
import xlogo.kernel.DrawPanel;

/**
 * This class is the Controller for the main frame.<br>
 * All events are interpreted by this class
 * 
 * @author loic
 * 
 */
public class MenuListener extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private double zoomfactor = 1.25;
	protected static final String FILE_NEW = "new";
	protected static final String FILE_OPEN = "open";
	protected static final String FILE_SAVE_AS = "save_as";
	protected static final String FILE_SAVE = "save";
	protected static final String FILE_SAVE_IMAGE = "record_image";
	protected static final String FILE_COPY_IMAGE = "copy_image";
	protected static final String FILE_PRINT_IMAGE = "print_image";
	protected static final String FILE_SAVE_TEXT = "save_text";
	protected static final String FILE_QUIT = "quit";
	protected static final String EDIT_SELECT_ALL = "select_all";
	public static final String EDIT_COPY = "copy";
	public static final String EDIT_CUT = "cut";
	public static final String EDIT_PASTE = "paste";
	protected static final String TOOLS_PEN_COLOR = "pen_color";
	protected static final String TOOLS_SCREEN_COLOR = "screen_color";
	protected static final String TOOLS_TRANSLATOR = "translator";
	protected static final String TOOLS_OPTIONS = "preferences";
	protected static final String TOOLS_ERASER = "eraser";
	protected static final String HELP_LICENCE = "licence";
	protected static final String HELP_TRANSLATED_LICENCE = "translated_licence";
	protected static final String HELP_TRANSLATE_XLOGO = "translate_xlogo";
	protected static final String HELP_ABOUT = "about";
	public static final String PLAY = "play";
	public static final String ZOOMIN = "zoomin";
	public static final String ZOOMOUT = "zoomout";

	private static final String WEB_SITE = "http://xlogo.tuxfamily.org";
	private static final String MAIL = "loic@xlogo.tuxfamily.org";
	private Application cadre;

	/**
	 * Attached the controller MenuListener to the main Frame
	 * 
	 * @param cadre
	 *            main Frame
	 */
	public MenuListener(Application cadre) {
		this.cadre = cadre;
	}

	/**
	 * This method dispatches all events from the main Frame and executes the
	 * task corresponding to the incoming event.
	 */
	public void actionPerformed(ActionEvent e) {		
		Language lang = AppSettings.getInstance().getLanguage();
		
		String cmd = e.getActionCommand();
		if (MenuListener.EDIT_COPY.equals(cmd)) { // Copier
			cadre.copy();
		} else if (MenuListener.EDIT_CUT.equals(cmd)) { // Couper
			cadre.cut();
		} else if (MenuListener.EDIT_PASTE.equals(cmd)) { // Coller
			cadre.paste();
		} else if (MenuListener.EDIT_SELECT_ALL.equals(cmd)) { // select all
			cadre.select_all();
		} else if (MenuListener.FILE_COPY_IMAGE.equals(cmd)) { // Copier l'image
																// au
																// presse-papier
			Thread copie = new CopyImage();
			copie.start();
		} else if (MenuListener.FILE_PRINT_IMAGE.equals(cmd)) { // imprimer
																// l'image
			AImprimer can = new AImprimer(cadre.getDrawPanel()
					.getSelectionImage());
			Thread imprime = new Thread(can);
			imprime.start();
		} else if (MenuListener.HELP_ABOUT.equals(cmd)) { // Boite de dialogue A
															// propos
			String message = Logo.messages.getString("message_a_propos1")
					+ GlobalConfig.getVersion() + "\n\n"
					+ Logo.messages.getString("message_a_propos2") + " "
					+ MenuListener.WEB_SITE + "\n\n"
					+ Logo.messages.getString("message_a_propos3") + "\n     "
					+ MenuListener.MAIL;
			MyTextAreaDialog jt = new MyTextAreaDialog(message);
			ImageIcon icone = new ImageIcon(
					Utils.class.getResource("icone.png"));
			JOptionPane.showMessageDialog(null, jt,
					Logo.messages.getString("menu.help.about"),
					JOptionPane.INFORMATION_MESSAGE, (Icon) icone);

		} else if (MenuListener.ZOOMIN.equals(cmd)) {
			cadre.getDrawPanel().zoom(zoomfactor * DrawPanel.zoom, true);
		} else if (MenuListener.ZOOMOUT.equals(cmd)) {
			cadre.getDrawPanel().zoom(1 / zoomfactor * DrawPanel.zoom, false);
		}
		else if (MenuListener.HELP_LICENCE.equals(cmd) | MenuListener.HELP_TRANSLATED_LICENCE.equals(cmd))
		{ // Affichage
			// de la
			// licence
			JFrame frame = new JFrame(Logo.messages.getString("menu.help.licence"));
			frame.setIconImage(Toolkit.getDefaultToolkit().createImage(WebPage.class.getResource("icone.png")));
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setSize(500, 500);
			WebPage editorPane = new WebPage();
			editorPane.setEditable(false);
			String path = "gpl/gpl-";
			if (MenuListener.HELP_LICENCE.equals(cmd))
				path += "en";
			else
			{
				path += lang.getLanguageCode();
			}
			path += ".html";
			java.net.URL helpURL = MenuListener.class.getResource(path);
			if (helpURL != null)
			{
				try
				{
					editorPane.setPage(helpURL);
				}
				catch (IOException e1)
				{
					System.err.println("Attempted to read a bad URL: " + helpURL);
				}
			}
			else
			{
				System.err.println("Couldn't find file: " + path);
			}
			
			// Put the editor pane in a scroll pane.
			JScrollPane editorScrollPane = new JScrollPane(editorPane);
			editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			editorScrollPane.setPreferredSize(new Dimension(250, 145));
			editorScrollPane.setMinimumSize(new Dimension(10, 10));
			frame.getContentPane().add(editorScrollPane);
			frame.setVisible(true);
		} 

	}

	/**
	 * This class is a thread that copy the Image selection into the clipboard
	 */
	class CopyImage extends Thread implements Transferable {
		private BufferedImage image = null;
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

		CopyImage() {
			image = cadre.getDrawPanel().getSelectionImage();
		}

		public void run() {
			clip.setContents(this, null);
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}

}
