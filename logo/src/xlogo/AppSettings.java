package xlogo;

import java.awt.Font;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xlogo.interfaces.Observable;
import xlogo.interfaces.PropertyChangePublisher;
import xlogo.storage.WSManager;
import xlogo.storage.global.GlobalConfig;
import xlogo.storage.global.GlobalConfig.GlobalProperty;
import xlogo.storage.workspace.Language;
import xlogo.storage.workspace.LogoLanguage;
import xlogo.storage.workspace.SyntaxHighlightConfig;
import xlogo.storage.workspace.WorkspaceConfig;
import xlogo.storage.workspace.WorkspaceConfig.WorkspaceProperty;

/**
 * This singleton class shall eliminate the accesses to Logo.messages.getString(),
 * provide a unique way to access current settings and to stay updated through change events.
 * @since 10th June 2014 - not yet consistently used throughout the application
 * @author Marko
 *
 */
public class AppSettings implements Observable<AppSettings.AppProperty>{
	private static Logger		logger	= LogManager.getLogger(AppSettings.class.getSimpleName());
	
	private static AppSettings	instance;
	
	public static AppSettings getInstance() {
		if (instance == null)
			instance = new AppSettings();
		return instance;
	}
	
	private WorkspaceConfig	wc;
	
	private PropertyChangeListener	languageListener	= new PropertyChangeListener(){
		
		@Override
		public void propertyChanged() {
			setLanguage(wc.getLanguage());
		}
	};
	
	private PropertyChangeListener	logoLanguageListener= new PropertyChangeListener(){
		
		@Override
		public void propertyChanged() {
			setLogoLanguage(wc.getLogoLanguage());
		}
	};
	
	private PropertyChangeListener	syntaxListener	= new PropertyChangeListener(){
		
		@Override
		public void propertyChanged() {
			setSyntaxHighlightingStyles(wc.getSyntaxHighlightStyles());
		}
	};
	
	private PropertyChangeListener	workspaceListener	= new PropertyChangeListener(){
		
		@Override
		public void propertyChanged() {
			setWorkspace(WSManager.getWorkspaceConfig());
		}
	};
	
	private PropertyChangeListener	workspaceListListener	= new PropertyChangeListener(){
		
		@Override
		public void propertyChanged() {
			publisher.publishEvent(AppProperty.WORKSPACE_LIST);
		}
	};
	
	private AppSettings() {
		GlobalConfig gc = WSManager.getGlobalConfig();
		WorkspaceConfig wc = WSManager.getWorkspaceConfig();
		gc.addPropertyChangeListener(GlobalProperty.CURRENT_WORKSPACE, workspaceListener);
		gc.addPropertyChangeListener(GlobalProperty.WORKSPACES, workspaceListListener);
		if (wc != null) {
			setWorkspace(gc.getCurrentWorkspace().get());
		}
	}
	
	/* * * * * * *
	 * WORKSPACE
	 * * * * * * */
	
	protected void setWorkspace(WorkspaceConfig wc){
		if (this.wc == wc){
			return;
		}
		
		if (wc != null) {
			wc.removePropertyChangeListener(WorkspaceProperty.LANGUAGE, languageListener);
			wc.removePropertyChangeListener(WorkspaceProperty.LOGO_LANGUAGE, logoLanguageListener);
			wc.removePropertyChangeListener(WorkspaceProperty.SYNTAX_HIGHLIGHTING, syntaxListener);
		}
		
		this.wc = wc;
		
		if (wc != null) {
			setLanguage(wc.getLanguage());
			setLogoLanguage(wc.getLogoLanguage());
			setSyntaxHighlightingStyles(wc.getSyntaxHighlightStyles());
			
			wc.addPropertyChangeListener(WorkspaceProperty.LANGUAGE, languageListener);
			wc.addPropertyChangeListener(WorkspaceProperty.LOGO_LANGUAGE, logoLanguageListener);
			wc.addPropertyChangeListener(WorkspaceProperty.SYNTAX_HIGHLIGHTING, syntaxListener);
		}
		publisher.publishEvent(AppProperty.WORKSPACE);
	}
	
	public WorkspaceConfig getWorkspace(){
		return wc;
	}
	
	/* * * * * * *
	 * LANGUAGE
	 * * * * * * */
	
	private Language	language	= Language.ENGLISH;
	
	public Language getLanguage() {
		return language;
	}
	
	protected void setLanguage(Language language) {
		if (language == this.language)
			return;
		logger.trace("Change language from " + this.language + " to " + language);
		this.language = language;
		Logo.generateLanguage(language);
		publisher.publishEvent(AppProperty.LANGUAGE);
	}

	
	/* * * * * * *
	 * LANGUAGE
	 * * * * * * */
	
	private LogoLanguage logoLanguage = LogoLanguage.ENGLISH;
	
	public LogoLanguage getLogoLanguage() {
		return logoLanguage;
	}
	
	protected void setLogoLanguage(LogoLanguage language) {
		if (language == this.logoLanguage)
			return;
		logger.trace("Change language from " + this.language + " to " + language);
		this.logoLanguage = language;
		Logo.generateLogoLanguage(language);
		publisher.publishEvent(AppProperty.LOGO_LANGUAGE);
	}
	
	/**
	 * Translate the key into the current language.
	 * Shortcut for Logo.messages#getString()
	 * @param key
	 * @return
	 */
	public String translate(String key) {
		if (Logo.messages == null) {
			Logo.generateLanguage(Language.ENGLISH); // TODO this is a temporary bug fix
		}
		return Logo.messages.getString(key);
	}
	
	/* * * * * * *
	 * FONT
	 * * * * * * */
	
	private Font	font;
	
	public Font getFont() {
		return font;
	}
	
	public void setFont(Font font) {
		if(this.font == font){
			return;
		}
		this.font = font;
		publisher.publishEvent(AppProperty.FONT);
	}
		
	/* * * * * * *
	 * SYNTAX HIGHLIGHTING STYLE
	 * * * * * * */
	
	private SyntaxHighlightConfig	syntaxHighlightingStyles;
	
	public SyntaxHighlightConfig getSyntaxHighlightStyles() {
		return syntaxHighlightingStyles;
	}
	
	protected void setSyntaxHighlightingStyles(SyntaxHighlightConfig syntaxHighlighStyles) {
		this.syntaxHighlightingStyles = syntaxHighlighStyles;
		publisher.publishEvent(AppProperty.SYNTAX_HIGHLIGHTING);
	}
	
	
	/* * * * * * *
	 * EVENT HANDLING
	 * * * * * * */
	
	public enum AppProperty {
		LANGUAGE, LOGO_LANGUAGE, SYNTAX_HIGHLIGHTING, FONT, WORKSPACE, WORKSPACE_LIST;
	}
	
	private transient final PropertyChangePublisher<AppProperty> publisher = new PropertyChangePublisher<AppProperty>();
	
	@Override
	public void addPropertyChangeListener(AppProperty property, PropertyChangeListener listener) {
		publisher.addPropertyChangeListener(property, listener);
	}
	
	@Override
	public void removePropertyChangeListener(AppProperty property, PropertyChangeListener listener) {
		publisher.removePropertyChangeListener(property, listener);
	}
	
}
