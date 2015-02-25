package xlogo.storage.workspace;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum LogoLanguage {
	LANGUAGE_FRENCH(0, "French", "fr", "FR"), 
	LANGUAGE_ENGLISH(1, "English", "en", "US"), 
	LANGUAGE_ARABIC(2, "Arabic", "ar", "MA"), 
	LANGUAGE_SPANISH(3, "Spanish", "es", "ES"), 
	LANGUAGE_PORTUGAL(4, "Portuguese", "pt", "BR"), 
	LANGUAGE_ESPERANTO(5, "Esperanto", "eo", "EO"), 
	LANGUAGE_GERMAN(6, "German", "de", "DE"), 
	LANGUAGE_GALICIAN(7, "Galician","gl", "ES"), 
	LANGUAGE_ASTURIAN(8, "Asturian", "al", "ES"), 
	LANGUAGE_GREEK(9, "Greek", "el", "GR"), 
	LANGUAGE_ITALIAN(10, "Italian", "it", "IT"), 
	LANGUAGE_CATALAN(11, "Catalan", "ca", "ES"), 
	LANGUAGE_HUNGARIAN(12,"Hungarian", "hu", "HU");
	
	private int									value;
	private String								englishName;
	private String								languageCode;
	private String								countryCode;
	
	private static Map<Integer, LogoLanguage>	valueToLanguage;
	
	private LogoLanguage(int value, String englishName, String languageCode, String countryCode) {
		this.value = value;
		this.englishName = englishName;
		this.languageCode = languageCode;
		this.countryCode = countryCode;
	}
	
	public static LogoLanguage getLanguage(int i) {
		if (valueToLanguage == null)
			initMapping();
		return valueToLanguage.get(i);
	}
	
	public static void initMapping() {
		valueToLanguage = new HashMap<Integer, LogoLanguage>();
		for (LogoLanguage lang : values()) {
			valueToLanguage.put(lang.value, lang);
		}
	}
	
	public int getValue() {
		return value;
	}
	
	public String getEnglishName() {
		return englishName;
	}
	
	/**
	 * return English name
	 */
	@Override
	public String toString() {
		return getEnglishName();
	}
	
	public Locale getLocale() {
		return new Locale(languageCode, countryCode);
	}
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public String getCountryCode() {
		return countryCode;
	}
}
