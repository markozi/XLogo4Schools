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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import xlogo.Logo;
import xlogo.interfaces.BasicFileContainer;
import xlogo.interfaces.BasicFileContainer.FileContainerChangeListener;
import xlogo.interfaces.BroadcasterErrorFileContainer;
import xlogo.interfaces.ErrorDetector.FileErrorCollector.ErrorListener;
import xlogo.interfaces.MessageBroadcaster.MessageListener;
import xlogo.messages.async.dialog.DialogMessenger;
import xlogo.gui.components.fileslist.IFilesListItem.ItemRequestHandler;

public class FilesList extends JPanel
{
	private static final long				serialVersionUID	= -3330227288228959914L;
	
	BroadcasterErrorFileContainer			model;
	FileContainerChangeListener				fileContainerModelListener;
	ErrorListener							errorListener;
	MessageListener							messageListener;
	
	// Handlers
	private ActionListener					addFileRequestHandler;
	private ItemRequestHandler				fileItemRequestHandler;
	
	// GUI Components
	/**
	 * The class of file items to use
	 */
	private Class<? extends IFilesListItem>	listItemClass		= FilesListItem.class;
	
	//private JScrollPane						scroller;
	private JButton							addFileButton;
	private Map<String, IFilesListItem>		listItems			= new HashMap<String, IFilesListItem>();
	
	private boolean							editEnabled			= true;
	
	/*
	 * Init & Model
	 */
	
	public FilesList(BroadcasterErrorFileContainer fileContainerModel)
	{
		
		initComponents();
		initFileItemListeners();
		initErrorListener();
		initFileContainerListener();
		intitMessageListener();
		setModel(fileContainerModel);
		addFileButton.addActionListener(addFileRequestHandler);
	}
	
	private void intitMessageListener()
	{
		messageListener = new MessageListener(){
			
			@Override
			public void messageEvent(final String fileName, final String message)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onMessageEvent(fileName, message);
					return;
				}
				
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onMessageEvent(fileName, message);
						}
					});
				}
				catch (InterruptedException e)
				{
					messageEvent(fileName, message);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onMessageEvent(String fileName, String message)
			{
				IFilesListItem item = listItems.get(fileName);
				if (item == null)
					return;
				
				item.setMessage(message);
			}
		};
	}
	
	private void initComponents()
	{
		this.setOpaque(false);
		
		Image img = Toolkit.getDefaultToolkit().getImage(getClass().getResource("new_content.png"));
		addFileButton = new JButton(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
		addFileButton.setOpaque(true);
		
		setLayout(new GridBagLayout());
	}
	
	GridBagConstraints getLayoutConstraints()
	{
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		return c;
	}
	
	private void initFileContainerListener()
	{
		fileContainerModelListener = new FileContainerChangeListener(){
			
			@Override
			public void fileAdded(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onFileAdded(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onFileAdded(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					fileAdded(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onFileAdded(String fileName)
			{
				// On initialization, the files list is fetched using set model
				// and the events are received, causing a duplication of items in the list.
				if (listItems.containsKey(fileName))
					return;
				
				IFilesListItem item = null;
				try
				{
					item = listItemClass.newInstance();
				}
				catch (InstantiationException e)
				{
					e.printStackTrace();
					return;
				}
				catch (IllegalAccessException e)
				{
					e.printStackTrace();
					return;
				}
				item.setRequestHandler(fileItemRequestHandler);
				item.setFileName(fileName);
				item.setEditEnabled(model.isFilesListEditable());
				item.setError(model.hasErrors(fileName));
				GridBagConstraints c = getLayoutConstraints();
				add(item.getComponent(), c);
				listItems.put(fileName, item);
				revalidate();
			}
			
			@Override
			public void fileRemoved(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onFileRemoved(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onFileRemoved(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					fileRemoved(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onFileRemoved(String fileName)
			{
				IFilesListItem item = listItems.get(fileName);
				remove(item.getComponent());
				listItems.remove(fileName);
				revalidate();
				validate();
			}
			
			@Override
			public void fileRenamed(final String oldName, final String newName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onFileRenamed(oldName, newName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onFileRenamed(oldName, newName);
						}
					});
				}
				catch (InterruptedException e)
				{
					fileRenamed(oldName, newName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onFileRenamed(String oldName, String newName)
			{
				IFilesListItem item = listItems.get(oldName);
				item.setFileName(newName);
				item.setSelected(true);
				listItems.remove(oldName);
				listItems.put(newName, item);
				item.getComponent().revalidate();
			}
			
			/**
			 * This implementation allows multiple files open, if the model decides so.
			 * The Model is responsible to close a specific file before opening another.
			 */
			@Override
			public void fileOpened(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onFileOpened(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onFileOpened(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					fileOpened(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onFileOpened(String fileName)
			{
				IFilesListItem item = listItems.get(fileName);
				item.setSelected(true);
				revalidate();
			}
			
			@Override
			public void fileClosed(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onFileClosed(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onFileClosed(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					fileClosed(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onFileClosed(String fileName)
			{
				IFilesListItem item = listItems.get(fileName);
				item.setSelected(false);
				item.getComponent().revalidate();
			}
			
			@Override
			public void editRightsChanged(final boolean enabled)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onEditRightsChanged(enabled);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onEditRightsChanged(enabled);
						}
					});
				}
				catch (InterruptedException e)
				{
					editRightsChanged(enabled);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onEditRightsChanged(boolean enabled)
			{
				editEnabled = enabled;
				if (addFileButton != null)
					addFileButton.setVisible(editEnabled);
				for (IFilesListItem item : listItems.values())
				{
					item.setEditEnabled(editEnabled);
				}
				revalidate();
			}
			
		};
	}
	
	private void initErrorListener()
	{
		errorListener = new ErrorListener(){
			
			@Override
			public void errorsDetected(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onErrorsDetected(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onErrorsDetected(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					errorsDetected(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onErrorsDetected(String fileName)
			{
				IFilesListItem item = listItems.get(fileName);
				if (item == null)
					return;
				item.setError(true);
				item.getComponent().revalidate();
			}
			
			@Override
			public void allErrorsCorrected(final String fileName)
			{
				if (SwingUtilities.isEventDispatchThread())
				{
					onAllErrorsCorrected(fileName);
					return;
				}
				try
				{
					SwingUtilities.invokeAndWait(new Runnable(){
						
						@Override
						public void run()
						{
							onAllErrorsCorrected(fileName);
						}
					});
				}
				catch (InterruptedException e)
				{
					allErrorsCorrected(fileName);
				}
				catch (InvocationTargetException e)
				{
					e.printStackTrace();
				}
			}
			
			private void onAllErrorsCorrected(String fileName)
			{
				IFilesListItem item = listItems.get(fileName);
				item.setError(false);
				item.getComponent().revalidate();
			}
		};
	}
	
	public void setModel(BroadcasterErrorFileContainer fileContainerModel)
	{
		if (this.model != null)
		{
			this.model.removeFileListener(fileContainerModelListener);
			this.model.removeErrorListener(errorListener);
			this.model.removeBroadcastListener(messageListener);
		}
		
		this.model = fileContainerModel;
		
		if (this.model != null)
		{
			this.model.addFileListener(fileContainerModelListener);
			this.model.addErrorListener(errorListener);
			this.model.addBroadcastListener(messageListener);
		}
		
		initItemsList();
	}
	
	/**
	 * Empties the  filesList and listItems and then refills them according to the model
	 */
	private void initItemsList()
	{
		listItems.clear();
		//filesList.removeAll();
		removeAll();
		if (model == null)
			return;
		
		GridBagConstraints c = getLayoutConstraints();
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 1;
		add(addFileButton, c);
		addFileButton.setVisible(editEnabled);
		
		for (String fileName : model.getFileNames())
			fileContainerModelListener.fileAdded(fileName);
		revalidate();
	}
	
	public JComponent getComponent()
	{
		return this;
	}
	
	public BasicFileContainer getModel()
	{
		return model;
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	public JScrollPane getScrollPane()
	{
		return null;
	}
	
	public JButton getAddFileButton()
	{
		return addFileButton;
	}
	
	protected Map<String, IFilesListItem> getListItems()
	{
		return listItems;
	}
	
	/**
	 * @param newButton if null, no add button is shown. If this effect is only temporarily wished, use {@link #setEditFilesListEnabled(boolean)}
	 */
	public void setAddFileButton(JButton newButton)
	{
		if (addFileButton != null)
		{
			addFileButton.removeActionListener(addFileRequestHandler);
			//filesList.remove(addFileButton);
			remove(addFileButton);
		}
		if (newButton != null)
		{
			newButton.addActionListener(addFileRequestHandler);
			newButton.setVisible(model.isFilesListEditable());
			add(newButton);
		}
		addFileButton = newButton;
		revalidate();
	}
	
	public void setListItemClass(Class<? extends IFilesListItem> itemClass) throws InstantiationException,
			IllegalAccessException
	{
		if (itemClass == null)
			throw new IllegalArgumentException("List item class must not be null.");
		
		this.listItemClass = itemClass;
		
		initItemsList();
	}
	
	// ITEM LISTENERS
	
	private void initFileItemListeners()
	{
		fileItemRequestHandler = new ItemRequestHandler(){
			@Override
			public void renameRequest(String oldName, String newName)
			{
				model.renameFile(oldName, newName);
			}
			
			@Override
			public void deleteRequest(String fileName)
			{
				model.closeFile(fileName);
				model.removeFile(fileName);
			}
			
			@Override
			public void openRequest(String fileName)
			{
				model.openFile(fileName);
			}
			
			@Override
			public void closeRequest(String fileName)
			{
				model.closeFile(fileName);
			}
		};
		
		addFileRequestHandler = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent event)
			{
				String name = model.makeUniqueFileName(Logo.messages.getString("new.file")); // TODO remove dependency
				try
				{
					model.createFile(name);
				}
				catch (Exception e)
				{
					DialogMessenger.getInstance().dispatchError(Logo.messages.getString("ws.error.title"),  // TODO remove dependency
							Logo.messages.getString("ws.error.could.not.create.logo.file") + "\n" + e.toString());
				}
			}
		};
	}
	
}
