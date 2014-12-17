/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * 
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
 * 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import xlogo.gui.components.fileslist.IFilesListItem.ItemRequestHandler;
import xlogo.interfaces.MessageBroadcaster.MessageListener;

public class FilesList {
	
	MessageListener							messageListener;
	JPanel									component;
	
	// Handlers
	private ActionListener					addFileRequestHandler;
	private ItemRequestHandler				fileItemRequestHandler;
	
	// GUI Components
	/**
	 * The class of file items to use
	 */
	private Class<? extends IFilesListItem>	listItemClass	= FilesListItem.class;
	
	//private JScrollPane						scroller;
	private JButton							addFileButton;
	private Map<String, IFilesListItem>		listItems		= new HashMap<String, IFilesListItem>();
	
	private boolean							isEditable		= true;
	
	/*
	 * Init
	 */
	
	public FilesList() {
		
		initComponents();
		initFileItemListeners();
		initItemsList();
		addFileButton.addActionListener(addFileRequestHandler);
	}
	
	private void initComponents() {
		component = new JPanel();
		component.setOpaque(false);
		
		Image img = Toolkit.getDefaultToolkit().getImage(getClass().getResource("new_content.png"));
		addFileButton = new JButton(new ImageIcon(img.getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
		addFileButton.setOpaque(true);
		
		component.setLayout(new GridBagLayout());
	}
	
	GridBagConstraints getLayoutConstraints() {
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		return c;
	}
	
	/**
	 * Empties the  filesList and listItems and then refills them according to the model
	 */
	private void initItemsList() {
		listItems.clear();
		//filesList.removeAll();
		component.removeAll();
		
		GridBagConstraints c = getLayoutConstraints();
		
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 0;
		c.weighty = 0;
		c.weightx = 1;
		component.add(addFileButton, c);
		addFileButton.setVisible(isEditable);
		
		component.revalidate();
	}
	
	/*
	 * Commands
	 */
	
	public void addFile(String fileName, boolean isEditable, boolean hasErrors) {
		// On initialization, the files list is fetched using set model
		// and the events are received, causing a duplication of items in the list.
		if (listItems.containsKey(fileName))
			return;
		
		IFilesListItem item = null;
		try {
			item = listItemClass.newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			return;
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		item.setRequestHandler(fileItemRequestHandler);
		item.setFileName(fileName);
		item.setEditable(isEditable);
		item.setError(hasErrors);
		GridBagConstraints c = getLayoutConstraints();
		component.add(item.getComponent(), c);
		listItems.put(fileName, item);
		component.revalidate();
	}
	
	public void removeFile(String fileName) {
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			return;
		component.remove(item.getComponent());
		listItems.remove(fileName);
		component.revalidate();
		component.validate();
	}
	
	public void renameFile(String oldName, String newName) {
		IFilesListItem item = listItems.get(oldName);
		if (item == null)
			throw new IllegalArgumentException(oldName + " does not exist.");
		item.setFileName(newName);
		item.setSelected(true);
		listItems.remove(oldName);
		listItems.put(newName, item);
		item.getComponent().revalidate();
	}
	
	public void openFile(String fileName) {
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			throw new IllegalArgumentException(fileName + " does not exist.");
		item.setSelected(true);
		component.revalidate();
	}
	
	public void closeFile(String fileName) {
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			throw new IllegalArgumentException(fileName + " does not exist.");
		item.setSelected(false);
		item.getComponent().revalidate();
	}
	
	public void editFile(String fileName) {
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			throw new IllegalArgumentException(fileName + " does not exist.");
		item.setEditing(true);
		item.getComponent().revalidate();
	}
	
	public void markError(String fileName, boolean hasError) {
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			throw new IllegalArgumentException(fileName + " does not exist.");
		item.setError(hasError);
		item.getComponent().revalidate();
	}
	
	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
		if (addFileButton != null)
			addFileButton.setVisible(isEditable);
		for (IFilesListItem item : listItems.values()) {
			item.setEditable(isEditable);
		}
		component.revalidate();
	}
	
	public void setItemMessage(String fileName, String message){
		IFilesListItem item = listItems.get(fileName);
		if (item == null)
			return;
		
		item.setMessage(message);
	}
	
	/*
	 * GETTERS & SETTERS
	 */
	
	public JComponent getComponent() {
		return component;
	}
	
	public JScrollPane getScrollPane() {
		return null;
	}
	
	public JButton getAddFileButton() {
		return addFileButton;
	}
	
	protected Map<String, IFilesListItem> getListItems() {
		return listItems;
	}
	
	/**
	 * @param newButton if null, no add button is shown. If this effect is only temporarily wished, use {@link #setEditFilesListEnabled(boolean)}
	 */
	public void setAddFileButton(JButton newButton) {
		if (addFileButton != null) {
			addFileButton.removeActionListener(addFileRequestHandler);
			//filesList.remove(addFileButton);
			component.remove(addFileButton);
		}
		if (newButton != null) {
			newButton.addActionListener(addFileRequestHandler);
			newButton.setVisible(isEditable);
			component.add(newButton);
		}
		addFileButton = newButton;
		component.revalidate();
	}
	
	public void setListItemClass(Class<? extends IFilesListItem> itemClass) throws InstantiationException,
			IllegalAccessException {
		if (itemClass == null)
			throw new IllegalArgumentException("List item class must not be null.");
		
		this.listItemClass = itemClass;
		
		initItemsList();
	}
	
	// ITEM LISTENERS
	
	private void initFileItemListeners() {
		fileItemRequestHandler = new ItemRequestHandler(){
			@Override
			public void renameRequest(String oldName, String newName) {
				requestRenameFile(oldName, newName);
			}
			
			@Override
			public void deleteRequest(String fileName) {
				requestDeleteFile(fileName);
			}
			
			@Override
			public void openRequest(String fileName) {
				requestOpenFile(fileName);
			}
			
			@Override
			public void closeRequest(String fileName) {
				requestCloseFile(fileName);
			}
		};
		
		addFileRequestHandler = new ActionListener(){
			
			@Override
			public void actionPerformed(ActionEvent event) {
				requestCreateFile();
			}
		};
	}
	
	/*
	 * Event Listeners
	 */
	
	private ArrayList<FilesListEventListener>	filesListEventListeners	= new ArrayList<FilesListEventListener>();
	
	public void addEventListener(FilesListEventListener listener) {
		filesListEventListeners.add(listener);
	}
	
	public void removeEventListener(FilesListEventListener listener) {
		filesListEventListeners.remove(listener);
	}
	
	private void requestCreateFile() {
		for (FilesListEventListener listener : filesListEventListeners) {
			listener.onFileCreateRequest();
		}
	}
	
	private void requestRenameFile(String oldName, String newName) {
		for (FilesListEventListener listener : filesListEventListeners) {
			listener.onFileRenameRequest(oldName, newName);
		}
	}
	
	private void requestDeleteFile(String fileName) {
		for (FilesListEventListener listener : filesListEventListeners) {
			listener.onFileDeleteRequest(fileName);
		}
	}
	
	private void requestOpenFile(String fileName) {
		for (FilesListEventListener listener : filesListEventListeners) {
			listener.onFileOpened(fileName);
		}
	}
	
	private void requestCloseFile(String fileName) {
		for (FilesListEventListener listener : filesListEventListeners) {
			listener.onFileClosed(fileName);
		}
	}
	
}
