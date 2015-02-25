/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the
 * GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic
 */

package xlogo.gui.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import xlogo.Logo;
import xlogo.kernel.DrawPanel;
import xlogo.storage.WSManager;

public abstract class AbstractPanelColor extends JPanel implements ActionListener {
	private static final long		serialVersionUID	= 1L;
	//	private ImageIcon[] images=new ImageIcon[17];
	private Integer[]				intArray			= new Integer[17];
	private JButton					bchoisir			= new JButton(Logo.messages.getString("pref.highlight.other"));
	protected JComboBox<Integer>	combo_couleur;
	private Color					couleur_perso		= Color.WHITE;
	
	public AbstractPanelColor(Color c) {
		setBackground(Color.blue);
		for (int i = 0; i < 17; i++) {
			intArray[i] = new Integer(i);
		}
		combo_couleur = new JComboBox<>(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		combo_couleur.setRenderer(renderer);
		setColorAndStyle(c);
		combo_couleur.setActionCommand("combo");
		combo_couleur.addActionListener(this);
		bchoisir.setFont(WSManager.getWorkspaceConfig().getFont());
		bchoisir.setActionCommand("bouton");
		bchoisir.addActionListener(this);
		add(combo_couleur);
		add(bchoisir);
	}
	
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		combo_couleur.setEnabled(b);
		bchoisir.setEnabled(b);
		
	}
	
	void setColorAndStyle(Color rgb) {
		int index = -1;
		for (int i = 0; i < 17; i++) {
			if (DrawPanel.defaultColors[i].equals(rgb)) {
				index = i;
			}
		}
		if (index == -1) {
			couleur_perso = rgb;
			index = 7;
		}
		combo_couleur.setSelectedIndex(index);
	}
	
	abstract public void actionPerformed(ActionEvent e);
	
	private class ComboBoxRenderer extends JPanel implements ListCellRenderer {
		private static final long	serialVersionUID	= 1L;
		int							id					= 0;
		
		public ComboBoxRenderer() {
			setOpaque(true);
			setPreferredSize(new Dimension(50, 20));
		}
		
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			//Get the selected index. (The index param isn't
			//always valid, so just use the value.)
			int selectedIndex = ((Integer) value).intValue();
			this.id = selectedIndex;
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			//Set the icon and text.  If icon was null, say so.
			
			setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			return this;
		}
		
		public void paint(Graphics g) {
			super.paint(g);
			if (id != 7)
				g.setColor(DrawPanel.defaultColors[id]);
			else
				g.setColor(couleur_perso);
			g.fillRect(5, 2, 40, 15);
		}
	}
	
	public Color getValue() {
		int id = combo_couleur.getSelectedIndex();
		if (id != 7)
			return DrawPanel.defaultColors[id];
		return couleur_perso;
	}
	
	protected void actionButton() {
		Color color = JColorChooser.showDialog(this, "", DrawPanel.defaultColors[combo_couleur.getSelectedIndex()]);
		if (null != color) {
			couleur_perso = color;
			combo_couleur.setSelectedIndex(7);
			combo_couleur.repaint();
		}
	}
}
