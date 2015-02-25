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
 * a lot of modifications, extensions, refactorings have been applied by Marko Zivkovic 
 */

package xlogo.gui.components;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;

import xlogo.kernel.DrawPanel;
import xlogo.Logo;

/**
 * Title : XLogo Description : XLogo is an interpreter for the Logo programming
 * language
 * 
 * @author Loïc Le Coq, Marko refactored
 */
public class ColorStyleSelectionPanel {
	
	private JPanel component = new JPanel();
	
	public JPanel getComponent()
	{
		return component;
	}
	
	private Integer[] intArray = new Integer[17];
	private JButton bchoisir = new JButton(
			Logo.messages.getString("pref.highlight.other"));
	private JComboBox<Integer> combo_couleur;
	private String[] msg = {
			Logo.messages.getString("style.none"),
			Logo.messages.getString("style.bold"),
			Logo.messages.getString("style.italic"),
			Logo.messages.getString("style.underline") };
	private JComboBox<String> style = new JComboBox<>(msg);
	private JLabel titre = new JLabel();
	private Color couleur_perso = Color.WHITE;
	private GridBagLayout gb = new GridBagLayout();

	public ColorStyleSelectionPanel(int rgb, int sty, String title) {
		//WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		//Font font = wc.getFont();

		component.setLayout(gb);

		for (int i = 0; i < 17; i++) {
			intArray[i] = new Integer(i);
		}
		// Create the combo box.
		//titre.setFont(font);
		titre.setText(title + ":");

		combo_couleur = new JComboBox<>(intArray);
		ComboBoxRenderer renderer = new ComboBoxRenderer();
		combo_couleur.setRenderer(renderer);
		setColorAndStyle(rgb, sty);
		combo_couleur.setActionCommand("combo");
		combo_couleur.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyActionListeners(e);
			}
		});
		//bchoisir.setFont(font);
		//style.setFont(font);
		bchoisir.setActionCommand("bouton");
		bchoisir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Color color = selectColor();
				if (null != color) {
					couleur_perso = color;
					combo_couleur.setSelectedIndex(7);
					combo_couleur.repaint();
				}
				notifyActionListeners(e);
			}
		});

		style.setActionCommand("style");
		style.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				notifyActionListeners(e);
			}
		});
		//int hauteur = font.getSize() + 5;
		// jt.setPreferredSize(new Dimension(240,hauteur));

		// Lay out the demo.
		component.add(combo_couleur, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		component.add(bchoisir, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		component.add(style, new GridBagConstraints(2, 1, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 0, 0));
		component.add(titre, new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0,
				GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
				new Insets(5, 5, 0, 5), 0, 0));

		component.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		//component.setPreferredSize(new Dimension(300, hauteur * 2 + 20));
	}
	
	public void setTitle(String title)
	{
		titre.setText(title);
	}

	public void setColorAndStyle(int rgb, int sty) {
		style.setSelectedIndex(sty);
		int index = -1;
		for (int i = 0; i < 17; i++) {
			if (DrawPanel.defaultColors[i].getRGB() == rgb) {
				index = i;
			}
		}
		if (index == -1) {
			couleur_perso = new Color(rgb);
			index = 7;
		}
		combo_couleur.setSelectedIndex(index);
	}
	
	private Color selectColor(){
		return JColorChooser.showDialog(component, "",
				DrawPanel.defaultColors[combo_couleur.getSelectedIndex()]);
	}

	private class ComboBoxRenderer extends JPanel implements ListCellRenderer<Object> {
		private static final long serialVersionUID = 1L;
		int id = 0;

		public ComboBoxRenderer() {
			setOpaque(true);
			setPreferredSize(new Dimension(50, 20));
		}

		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			// Get the selected index. (The index param isn't
			// always valid, so just use the value.)
			int selectedIndex = ((Integer) value).intValue();
			this.id = selectedIndex;
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			// Set the icon and text. If icon was null, say so.

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

	public int color() {
		int id = combo_couleur.getSelectedIndex();
		if (id != 7)
			return DrawPanel.defaultColors[id].getRGB();
		return couleur_perso.getRGB();
	}

	public int style() {
		int id = style.getSelectedIndex();
		return id;
	}

	public void setEnabled(boolean b) {
		component.setEnabled(b);
		combo_couleur.setEnabled(b);
		style.setEnabled(b);
		bchoisir.setEnabled(b);
	}

	
	private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public void addStyleChangeListener(ActionListener listener)
	{
		actionListeners.add(listener);
	}
	
	private void notifyActionListeners(ActionEvent e)
	{
		for (ActionListener listener : actionListeners)
			listener.actionPerformed(e);
	}
}
