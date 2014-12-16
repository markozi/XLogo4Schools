package xlogo;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.storage.workspace.Language;
import xlogo.storage.workspace.SyntaxHighlightConfig;

/**
 * This singleton class shall eliminate the accesses to Logo.messages.getString(),
 * provide a unique way to access current settings and to stay updated through change events.
 * @since 10th June 2014 - not yet consistently used throughout the application
 * @author Marko
 *
 */
public class AppSettings
{
	private static Logger logger = LogManager.getLogger(AppSettings.class.getSimpleName());
	
	private static AppSettings instance;
	
	public static AppSettings getInstance()
	{
		if (instance == null)
			instance = new AppSettings();
		return instance;
	}
		
	/* * * * * * *
	 * LANGUAGE
	 * * * * * * */
	
	private Language language;
	
	public Language getLanguage()
	{
		return language;
	}
	
	public void setLanguage(Language language)
	{
		if (language == this.language)
			return;
		logger.trace("Change language from " + this.language + " to " + language);
		this.language = language;
		Logo.generateLanguage(language);
		notifyLanguageChanged();
	}
	
	/**
	 * Translate the key into the current language.
	 * Shortcut for Logo.messages#getString()
	 * @param key
	 * @return
	 */
	public String translate(String key)
	{
		return Logo.messages.getString(key);
	}
	
	private ArrayList<ActionListener> languageChangeListeners = new ArrayList<ActionListener>();
	
	public void addLanguageChangeListener(ActionListener listener)
	{
		languageChangeListeners.add(listener);
	}
	
	public void removeLanguageChangeListener(ActionListener listener)
	{
		languageChangeListeners.remove(listener);
	}
	
	private void notifyLanguageChanged()
	{
		ActionEvent event = new ActionEvent(this, 0, "languageChange");
		for (ActionListener listener : languageChangeListeners)
			listener.actionPerformed(event);
	}

	/* * * * * * *
	 * FONT
	 * * * * * * */
	
	private Font font;
	
	public Font getFont()
	{
		return font;
	}
	
	public void setFont(Font font)
	{
		this.font = font;
		notifyFontChanged();
	}
	
	private ArrayList<ActionListener> fontChangeListeners = new ArrayList<ActionListener>();
	
	public void addFontChangeListener(ActionListener listener)
	{
		fontChangeListeners.add(listener);
	}
	
	public void removeFontChangeListener(ActionListener listener)
	{
		fontChangeListeners.remove(listener);
	}
	
	private void notifyFontChanged()
	{
		ActionEvent event = new ActionEvent(this, 0, "fontChange");
		for (ActionListener listener : fontChangeListeners)
			listener.actionPerformed(event);
	}
	
	/* * * * * * *
	 * SYNTAX HIGHLIGHTING STYLE
	 * * * * * * */
	
	private SyntaxHighlightConfig syntaxHighlightingStyles;
	
	public SyntaxHighlightConfig getSyntaxHighlightStyles()
	{
		return syntaxHighlightingStyles;
	}
	
	public void setSyntaxHighlightingStyles(SyntaxHighlightConfig syntaxHighlighStyles)
	{
		this.syntaxHighlightingStyles = syntaxHighlighStyles;
		notifySyntaxHighlightStyleChanged();
	}
	
	private ArrayList<ActionListener> syntaxHighlightStyleChangeListeners = new ArrayList<ActionListener>();
	
	public void addSyntaxHighlightStyleChangeListener(ActionListener listener)
	{
		syntaxHighlightStyleChangeListeners.add(listener);
	}
	
	public void removeSyntaxHighlightStyleChangeListener(ActionListener listener)
	{
		syntaxHighlightStyleChangeListeners.remove(listener);
	}
	
	private void notifySyntaxHighlightStyleChanged()
	{
		ActionEvent event = new ActionEvent(this, 0, "fontChange");
		for (ActionListener listener : syntaxHighlightStyleChangeListeners)
			listener.actionPerformed(event);
	}

}
