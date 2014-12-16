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
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.gui.components;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import xlogo.interfaces.ProcedureMapper;
import xlogo.interfaces.ProcedureMapper.ProcedureMapListener;
import xlogo.utils.Utils;

public class ProcedureSearch extends JPanel
{
	private static final long	serialVersionUID	= 2275479142269992452L;

	private ProcedureMapper	procedureMapper		= null;

	private JComboBox	procedureSelection		= new JComboBox();
	private JButton		procedureSearchButton	= new JButton();
	
	public ProcedureSearch(ProcedureMapper procedureMapper)
	{
		this.procedureMapper = procedureMapper;
		setExecutables(procedureMapper.getAllProcedureNames());
		 initComponents();
		 //applyColorTheme();
		 initListeners();
	}

	private void initListeners()
	{
		procedureMapper.addProcedureMapListener(new ProcedureMapListener(){

			@Override
			public void ownerRenamed(String oldName, String newName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					setExecutables(procedureMapper.getAllProcedureNames());
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							setExecutables(procedureMapper.getAllProcedureNames());
						}
					});
				}
				catch (InterruptedException e)
				{
					ownerRenamed(oldName, newName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		
			@Override
			public void defined(String fileName, Collection<String> procedures)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					setExecutables(procedureMapper.getAllProcedureNames());
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							setExecutables(procedureMapper.getAllProcedureNames());
						}
					});
				}
				catch (InterruptedException e)
				{
					defined(fileName, procedures);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void defined(String fileName, String procedure)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					setExecutables(procedureMapper.getAllProcedureNames());
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							setExecutables(procedureMapper.getAllProcedureNames());
						}
					});
				}
				catch (InterruptedException e)
				{
					defined(fileName, procedure);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void undefined(String fileName, Collection<String> procedures)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					setExecutables(procedureMapper.getAllProcedureNames());
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							setExecutables(procedureMapper.getAllProcedureNames());
						}
					});
				}
				catch (InterruptedException e)
				{
					undefined(fileName, procedures);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}

			@Override
			public void undefined(String fileName, String procedure)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					setExecutables(procedureMapper.getAllProcedureNames());
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							setExecutables(procedureMapper.getAllProcedureNames());
						}
					});
				}
				catch (InterruptedException e)
				{
					undefined(fileName, procedure);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
		});
		
		procedureSearchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				String selected = (String) procedureSelection.getSelectedItem();
				if (selected == null || selected.length() == 0)
					return;
				notifySearchListeners(selected);
			}
		});
		
		procedureSelection.getEditor().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
            	String selected = (String) procedureSelection.getSelectedItem();
            	if (selected == null || selected.length() == 0)
					return;
            	notifySearchListeners(selected);
            }               
        });
	}
	
	private void setExecutables(Collection<String> procedures)
	{
		String[] sorted = new String[procedures.size()];
		sorted = procedures.toArray(sorted);
		Arrays.sort(sorted);
		
		procedureSelection.setModel(new DefaultComboBoxModel(sorted));
	}
	
	private void initComponents()
	{
		procedureSelection.setEditable(true);
		
		Image img = Toolkit.getDefaultToolkit().getImage(Utils.class.getResource("chercher.png"));
		procedureSearchButton.setIcon(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
	
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
	
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		add(procedureSelection, c);
		
		c.gridx = 1;
		c.weightx = 0;
		add(procedureSearchButton, c);
	}
	
	public JComponent getComponent()
	{
		return this;
	}
	
	@Override
	public void setFont(Font font)
	{
		super.setFont(font);
		if (procedureSelection != null) // Apparently this became called before object construction...
			procedureSelection.setFont(font);
	}
	
	/*
	 * PROCEDURE SEARCH REQUEST LISTENER
	 */
	
	public interface ProcedureSearchRequestListener
	{
		public void requestProcedureSearch(String procedureName);
	}
	
	private ArrayList<ProcedureSearchRequestListener> searchListeners = new ArrayList<ProcedureSearchRequestListener>();
	
	public void addSearchRequestListener(ProcedureSearchRequestListener listener)
	{
		searchListeners.add(listener);
	}
	
	public void removeSearchRequestListener(ProcedureSearchRequestListener listener)
	{
		searchListeners.remove(listener);
	}

	private void notifySearchListeners(String procedureName)
	{
		for (ProcedureSearchRequestListener listener : searchListeners)
			listener.requestProcedureSearch(procedureName);
	}
}
