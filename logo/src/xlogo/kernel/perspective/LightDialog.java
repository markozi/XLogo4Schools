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

package xlogo.kernel.perspective;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Color;

import xlogo.Logo;

import javax.vecmath.Color3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import org.jogamp.vecmath.Vector3f;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import java.awt.event.*;

import xlogo.gui.preferences.PanelColor;
import xlogo.storage.WSManager;

public class LightDialog extends JDialog implements ActionListener
{
	private static final long	serialVersionUID	= 1L;
	private final String[]			type				= { Logo.messages.getString("3d.light.none"),
			Logo.messages.getString("3d.light.ambient"), Logo.messages.getString("3d.light.directional"),
			Logo.messages.getString("3d.light.point"), Logo.messages.getString("3d.light.spot") };

	private JComboBox			comboType;

	PanelColor					panelColor;

	private PanelPosition		panelPosition;

	private PanelPosition		panelDirection;

	private PanelAngle			panelAngle;

	private JLabel				labelType;

	private JButton				ok;

	private JButton				refresh;

	private final Viewer3D			viewer3d;

	private final MyLight				light;

	LightDialog(final Viewer3D viewer3d, final MyLight light, final String title)
	{
		super(viewer3d, title, true);
		this.viewer3d = viewer3d;
		this.light = light;
		initGui();
	}

	private void initGui()
	{
		getContentPane().setLayout(new GridBagLayout());
		setSize(500, 200);
		labelType = new JLabel(Logo.messages.getString("3d.light.type"));
		comboType = new JComboBox(type);
		comboType.setSelectedIndex(light.getType());
		
		Color3f col = light.getColor();
		if (null != col)
			panelColor = new PanelColor(col.get());
		else
			panelColor = new PanelColor(Color.white);
		panelColor.setBackground(comboType.getBackground());

		panelPosition = new PanelPosition(Logo.messages.getString("3d.light.position"), light.getPosition());
		panelDirection = new PanelPosition(Logo.messages.getString("3d.light.direction"), light.getDirection());
		panelAngle = new PanelAngle(light.getAngle());
		ok = new JButton(Logo.messages.getString("pref.ok"));
		refresh = new JButton(Logo.messages.getString("3d.light.apply"));

		final Font font = WSManager.getWorkspaceConfig().getFont();

		labelType.setFont(font);
		comboType.setFont(font);
		ok.setFont(font);
		refresh.setFont(font);

		comboType.addActionListener(this);
		comboType.setActionCommand("combo");
		ok.addActionListener(this);
		ok.setActionCommand("ok");
		refresh.addActionListener(this);
		refresh.setActionCommand("refresh");

		getContentPane().add(
				labelType,
				new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				comboType,
				new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				panelColor,
				new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				panelPosition,
				new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				panelDirection,
				new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				panelAngle,
				new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				refresh,
				new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		getContentPane().add(
				ok,
				new GridBagConstraints(1, 4, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		selectComponents();
		setVisible(true);

	}

	public void actionPerformed(final ActionEvent e)
	{
		final String cmd = e.getActionCommand();
		// The selected item in the combo Box has changed
		if (cmd.equals("combo"))
			selectComponents();
		// Button Ok pressed
		else if (cmd.equals("ok"))
		{
			updateLight();
			dispose();
		}
		// Button Apply pressed
		else if (cmd.equals("refresh"))
		{
			updateLight();
		}
	}

	private void updateLight()
	{
		int t = comboType.getSelectedIndex();
		Color3f c = new Color3f(panelColor.getValue());
		Point3f p = panelPosition.getPosition();
		Vector3f d = panelDirection.getDirection();
		float a = panelAngle.getAngleValue();
		light.setType(t);
		light.setColor(c);
		light.setPosition(p);
		light.setDirection(d);
		light.setAngle(a);
		light.detach();
		light.removeAllChildren();
		light.createLight();
		// System.out.println(c+" "+" "+p+" "+d);
		viewer3d.addNode(light);

	}

	private void selectComponents()
	{
		final int id = comboType.getSelectedIndex();
		// None
		if (id == MyLight.LIGHT_OFF)
		{
			panelColor.setEnabled(false);
			panelPosition.setEnabled(false);
			panelDirection.setEnabled(false);
			panelAngle.setEnabled(false);
		}
		// Ambient
		else if (id == MyLight.LIGHT_AMBIENT)
		{
			panelColor.setEnabled(true);
			panelPosition.setEnabled(false);
			panelDirection.setEnabled(false);
			panelAngle.setEnabled(false);
		}
		// Directional
		else if (id == MyLight.LIGHT_DIRECTIONAL)
		{
			panelColor.setEnabled(true);
			panelPosition.setEnabled(false);
			panelDirection.setEnabled(true);
			panelAngle.setEnabled(false);
		}
		// PointLight
		else if (id == MyLight.LIGHT_POINT)
		{
			panelColor.setEnabled(true);
			panelPosition.setEnabled(true);
			panelDirection.setEnabled(false);
			panelAngle.setEnabled(false);
		}
		// Spot
		else if (id == MyLight.LIGHT_SPOT)
		{
			panelColor.setEnabled(true);
			panelPosition.setEnabled(true);
			panelDirection.setEnabled(true);
			panelAngle.setEnabled(true);
		}
	}

	class PanelAngle extends JPanel
	{
		private static final long	serialVersionUID	= 1L;
		private JLabel				label;
		private JTextField			angle;
		private final float				angleValue;

		PanelAngle(final float angleValue)
		{
			this.angleValue = angleValue;

			initGui();
		}

		private void initGui()
		{
			label = new JLabel(Logo.messages.getString("3d.light.angle"));
			label.setFont(WSManager.getWorkspaceConfig().getFont());
			angle = new JTextField(String.valueOf(angleValue));
			angle.setSize(30, WSManager.getWorkspaceConfig().getFont().getSize() + 10);
			add(label);
			add(angle);
		}

		public void setEnabled(final boolean b)
		{
			super.setEnabled(b);
			label.setEnabled(b);
			angle.setEnabled(b);
		}

		/**
		 * @return
		 * @uml.property name="angleValue"
		 */
		float getAngleValue()
		{
			try
			{
				final float f = Float.parseFloat(angle.getText());
				return f;
			}
			catch (final NumberFormatException e)
			{}
			return MyLight.DEFAULT_ANGLE;
		}
	}

	class PanelPosition extends JPanel
	{
		private static final long	serialVersionUID	= 1L;
		private final String				title;
		private JTextField			Xpos;
		private JTextField			Ypos;
		private JTextField			Zpos;
		private JLabel				sep1;
		private JLabel				sep2;
		Tuple3f						tuple;

		PanelPosition(final String title, final Tuple3f tuple)
		{
			this.title = title;
			this.tuple = tuple;
			initGui();
		}

		private void initGui()
		{
			final TitledBorder tb = BorderFactory.createTitledBorder(title);
			tb.setTitleFont(WSManager.getWorkspaceConfig().getFont());
			setBorder(tb);
			sep1 = new JLabel("x");
			sep2 = new JLabel("x");
			Xpos = new JTextField(4);
			Ypos = new JTextField(4);
			Zpos = new JTextField(4);
			if (null != tuple)
			{
				Xpos.setText(String.valueOf((int) (tuple.x * 1000)));
				Ypos.setText(String.valueOf((int) (tuple.y * 1000)));
				Zpos.setText(String.valueOf((int) (tuple.z * 1000)));
			}
			add(Xpos);
			add(sep1);
			add(Ypos);
			add(sep2);
			add(Zpos);
		}

		Point3f getPosition()
		{
			try
			{
				final float x = Float.parseFloat(Xpos.getText());
				final float y = Float.parseFloat(Ypos.getText());
				final float z = Float.parseFloat(Zpos.getText());
				return new Point3f(x / 1000, y / 1000, z / 1000);
			}
			catch (final NumberFormatException e)
			{}
			return (new Point3f(0, 0, 0));
		}

		Vector3f getDirection()
		{
			try
			{
				final float x = Float.parseFloat(Xpos.getText());
				final float y = Float.parseFloat(Ypos.getText());
				final float z = Float.parseFloat(Zpos.getText());
				return new Vector3f(x / 1000, y / 1000, z / 1000);
			}
			catch (final NumberFormatException e)
			{}
			return (new Vector3f(0, 0, 0));

		}

		public void setEnabled(final boolean b)
		{
			super.setEnabled(b);
			sep1.setEnabled(b);
			sep2.setEnabled(b);
			Xpos.setEnabled(b);
			Ypos.setEnabled(b);
			Zpos.setEnabled(b);
		}

	}

}
