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

package xlogo.gui.components.fileslist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
 * This list item was programmed to provide general methods for a files list.
 * 
 * However, the component styles could try to follow the current look and feel.
 * At the moment, style and colors are hard-coded.
 * 
 * @author Marko Zivkovic
 */
public class FilesListItem extends JPanel implements IFilesListItem
{
	private static final long	serialVersionUID	= 3061129268414679076L;
	
	/*
	 * These are just estimations that are used for getMinimumSize
	 */
	//private static int MINIMUM_BUTTON_SIZE = 15; // This scales the button icons

	/*
	 * Item State and properties
	 */
	
	private boolean isEditEnabled = true;
	
	/**
	 * This is intentionally not included in the State enum (it would double the amount of states),
	 * because it affects only one property
	 */
	private State state = State.UNSELECTED;
	
	/**
	 * This enum denotes the different states that FilesListItem can have.
	 * It further provides all necessary state transitions and the state-dependent sizes.
	 * <p>
	 * Note that only properties are modeled here that affect more than one component's properties,
	 * or properties that depend on more than one variable.
	 * @author Marko
	 *
	 */
	private enum State
	{
		UNSELECTED, SELECTED, EDITING,
		// E prefix <=> File has error
		E_UNSELECTED, E_SELECTED, E_EDITING;
		
		/*
		 * State transitions
		 */
		
		public State selected()
		{
			switch(this)
			{
				case E_EDITING:
				case E_SELECTED:
				case E_UNSELECTED:
					return State.E_SELECTED;
				default:
					return State.SELECTED;
			}
		}
		
		public State unselected()
		{
			switch(this)
			{
				case E_EDITING:
				case E_SELECTED:
				case E_UNSELECTED:
					return State.E_UNSELECTED;
				default:
					return State.UNSELECTED;
			}
		}
		
		public State editing()
		{
			switch(this)
			{
				case E_EDITING:
				case E_SELECTED:
				case E_UNSELECTED:
					return State.E_EDITING;
				default:
					return State.EDITING;
			}
		}
				
		public State withError()
		{
			switch(this)
			{
				case EDITING:
				case E_EDITING:
					return State.E_EDITING;
				case SELECTED:
				case E_SELECTED:
					return State.E_SELECTED;
				default:
					return State.E_UNSELECTED;
			}
		}
		
		public State withoutError()
		{
			switch(this)
			{
				case EDITING:
				case E_EDITING:
					return State.EDITING;
				case SELECTED:
				case E_SELECTED:
					return State.SELECTED;
				default:
					return State.UNSELECTED;
			}
		}
		
		/*
		 * State dependent component properties
		 */
					
		public Color getBackgroundColor()
		{
			switch(this)
			{
				case E_SELECTED:
				case E_EDITING:
					return new Color(0xFF6666);
				case E_UNSELECTED:
					return new Color(0xFFCCCC);
				case SELECTED:
				case EDITING:
					return new Color(0xDBEBFF);
				case UNSELECTED:
				default:
					return new Color(0xA8CFFF);
				
			}
		}
		
		public Color getBorderColor()
		{
			switch(this)
			{
				case E_SELECTED:
				case E_EDITING:
					return new Color(0xFF4242);
				case E_UNSELECTED:
					return new Color(0xFFE0E0);
				case UNSELECTED:
					return new Color(0x5BA4FE);
				default:
					return new Color(0xEAF4FF);
				
			}
		}
		
		public int getBorderThickness()
		{
			switch(this)
			{
				case UNSELECTED:
				case E_UNSELECTED:
					return 1;
				default:
					return 2;
			}
		}
		
		public boolean isSelected()
		{
			switch(this)
			{
				case E_SELECTED:
				case SELECTED:
					return true;
				default:
					return false;
			}
		}
		
		public boolean isEditing()
		{
			switch(this)
			{
				case E_EDITING:
				case EDITING:
					return true;
				default:
					return false;
			}
		}
		
		public boolean hasError()
		{
			switch(this)
			{
				case E_UNSELECTED:
				case E_SELECTED:
				case E_EDITING:
					return true;
				default:
					return false;
			}
		}
		
	}
	
	/*
	 * Request handler
	 * 
	 * Note : this component does not change its state alone.
	 * It only asks the handler to handle some user requests.
	 * It is up to the handler to change the state of this component, based on the result of the request.
	 */
	
	ItemRequestHandler handler;
	
	/*
	 * Components
	 */
	
	private JButton			openButton				= new JButton();
	private JButton			closeButton				= new JButton();
	private JButton			editButton				= new JButton();
	private JButton			removeButton			= new JButton();
	private JButton			confirmButton			= new JButton();
	private JLabel			errorIcon				= new JLabel();
	private JTextField		textField				= new JTextField();
	private JLabel			messageLabel			= new JLabel();
	
	/*
	 * Constructor & Layout
	 */
	
	public FilesListItem()
	{

		closeButton.setIcon(createImageIcon("close_icon2.png", 20, 20));
		editButton.setIcon(createImageIcon("5_content_edit.png", 20, 20));
		removeButton.setIcon(createImageIcon("5_content_discard.png", 20, 20));
		confirmButton.setIcon(createImageIcon("1_navigation_accept.png", 20, 20));
		errorIcon.setIcon(createImageIcon("11_alerts_and_states_error.png", 20, 20));
		
		initComponents();
		initLayout();
		validate();
		initComponentListener();
	}

	public FilesListItem(String fileName, ItemRequestHandler handler)
	{
		setFileName(fileName);
		setRequestHandler(handler);
		initComponents();
		initLayout();
		validate();
		initComponentListener();
	}
	
	private void initLayout()
	{
		try
		{
			// Apparently, these properties were not set automatically
			Font font = (Font) UIManager.get("defaultFont");
			openButton.setFont(font);
			closeButton.setFont(font);
			messageLabel.setFont(font);
			textField.setFont(font);
		}catch(ClassCastException ignore) {}
		
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		add(errorIcon, c);
		add(removeButton, c);

		c.weightx=1; // This is the only one which should extend
		c.gridx = 1;
		add(openButton, c);
		add(closeButton, c);
		add(textField, c);
		
		c.weightx=0;
		c.gridx = 2;
		add(messageLabel, c);
		
		c.gridx = 3;
		add(editButton, c);
		add(confirmButton, c);
	}
	
	private void initComponents()
	{
		// The background of this JPanel will be visible below.
		// => state dependent background color must only be set once
		openButton.setOpaque(false);
		closeButton.setOpaque(false);
		editButton.setOpaque(false);
		removeButton.setOpaque(false);
		confirmButton.setOpaque(false);
		errorIcon.setOpaque(false);
		messageLabel.setOpaque(false);
	}
	
	private void initComponentListener()
	{
		openButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (handler != null)
					handler.openRequest(openButton.getText());
			}
		});

		closeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (handler != null)
					handler.closeRequest(openButton.getText());
			}
		});

		editButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				state = state.editing();
				validate();
			}
		});

		removeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (handler != null)
					handler.deleteRequest(openButton.getText());
			}
		});

		confirmButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String oldName = openButton.getText();
				String newName = textField.getText();
				
				if (handler != null && !oldName.equals(newName))
					handler.renameRequest(oldName, newName);
				else
				{
					state = state.selected();
					validate();
				}
			}
		});
		
		textField.addActionListener(new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String oldName = openButton.getText();
				String newName = textField.getText();
				
				if (handler != null && !oldName.equals(newName))
					handler.renameRequest(oldName, newName);
				else
				{
					state = state.selected();
					validate();
				}
			}
		});
	}
	
	@Override
	public void validate()
	{
		super.validate();
		// First the properties of components are set before super.validate()
		// Otherwise the changes are not validated, are they?
		boolean isSelected = state.isSelected();
		boolean isEditing = state.isEditing();
		boolean hasError = state.hasError();
		
		openButton.setVisible(!isSelected && !isEditing);
		closeButton.setVisible(isSelected);
		editButton.setVisible(isSelected && isEditEnabled);
		
		confirmButton.setVisible(isEditing);
		textField.setVisible(isEditing);
		removeButton.setVisible(isEditing);

		errorIcon.setVisible(hasError && !isEditing);
		
		Color backgroundColor = state.getBackgroundColor();
		setBackground(backgroundColor);

		this.setBorder(BorderFactory.createLineBorder(state.getBorderColor(), state.getBorderThickness()) );
		
		if (isEditing){
			textField.requestFocusInWindow();
			textField.selectAll();
		}
	}
			
	/*
	 * IFilesListItem
	 */

	@Override
	public JComponent getComponent()
	{
		return this;
	}

	@Override
	public void setRequestHandler(ItemRequestHandler handler)
	{
		this.handler = handler;
		validate();
	}
	
	@Override
	public void setFileName(String fileName)
	{
		this.openButton.setText(fileName);
		this.closeButton.setText(fileName);
		this.textField.setText(fileName);
		validate();
	}
	
	@Override
	public void setSelected(boolean isSelected)
	{
		state = isSelected ? state.selected() : state.unselected();
		invalidate();
		validate();
	}
	
	
	@Override
	public void setEditing(boolean isEditing)
	{
		state = isEditing ? state.editing() : state.selected();
		validate();
	}
	
	/**
	 * This is used to display the clock per file item in XLogo4Schools
	 * Generally, this can be any message.
	 * @param msg : if null, the message will be hidden, otherwise it is displayed
	 */
	@Override
	public void setMessage(String msg)
	{
		messageLabel.setVisible(msg != null);
		messageLabel.setText(msg);
		validate();
	}
	
	
	public String getMessage()
	{
		return messageLabel.getText();
	}
	
	
	@Override
	public void setError(boolean hasError)
	{
		state = hasError ? state.withError() : state.withoutError();
		validate();
	}
	

	public void setEditable(boolean enabled)
	{
		isEditEnabled = enabled;
		validate();
	}
		
	/*
	 * Helper
	 */
	private ImageIcon createImageIcon(String path, int width, int heigth) {
		Image img = Toolkit.getDefaultToolkit().getImage(getClass().getResource(path));
		return new ImageIcon(img.getScaledInstance(width, heigth, Image.SCALE_SMOOTH));
	}	
}
