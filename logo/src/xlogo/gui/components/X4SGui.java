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

import xlogo.AppSettings;
import xlogo.AppSettings.AppProperty;
import xlogo.interfaces.Observable.PropertyChangeListener;

/**
 * Automatically calls all convenience features in the following order:
 * <li> initComponent() </li>
 * <li> layoutComponent() </li>
 * <li> setText() </li>
 * <li> initEventListeners() </li>
 * <li> startListernForLanguageChangeEvents() </li>
 * <p> Additionally, setText is called when language is changed by the user.
 * If you do not need to update the language, just {@link X4SGui#stopListenForLanguageChangeEvents()}
 * @since June 10th 2014
 * @author Marko
 */
public abstract class X4SGui {

	public X4SGui() {
		initComponent();
		layoutComponent();
		setText();
		initEventListeners();
		startListenForLanguageChangeEvents();
	}
	/**
	 * Subclassses should make sure the component is completely initialized and ready to be used.
	 * Called before setText() and layotComponent()
	 */
	protected abstract void initComponent();
	
	protected abstract void layoutComponent();
	
	protected abstract void initEventListeners();
	
	public void stopEventListeners()
	{
		stopListenForLanguageChangeEvents();
	}
	
	private PropertyChangeListener languageChangeListener = new PropertyChangeListener(){
		@Override
		public void propertyChanged() {
			setText();
		}
	};
	
	/**
	 * Note: registers only once, even if called more than once.
	 */
	public void startListenForLanguageChangeEvents()
	{
		AppSettings.getInstance().addPropertyChangeListener(AppProperty.LANGUAGE, languageChangeListener);
	}
	
	public void stopListenForLanguageChangeEvents()
	{
		if (languageChangeListener == null)
			return;
		AppSettings.getInstance().removePropertyChangeListener(AppProperty.LANGUAGE, languageChangeListener);
		languageChangeListener = null;
	}
	
	/**
	 * Called whenever language is changed or the component is initialized.
	 */
	protected void setText() { }
	
	/**
	 * Shortcut to LanguageManager.getInstance().translate(key)
	 * @param key
	 * @return
	 */
	protected String translate(String key)
	{
		return AppSettings.getInstance().translate(key);
	}

}
