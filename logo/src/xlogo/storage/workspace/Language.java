/* XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loïc Le Coq
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
 * during his Bachelor thesis at the computer science department of ETH Zürich,
 * in the year 2013 and/or during future work.
 * 
 * It is a reengineered version of XLogo written by Loïc Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * 
 * Contents of this file were entirely written by Marko Zivkovic
 */

package xlogo.storage.workspace;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum Language {
	LANGUAGE_FRENCH(0, "French", "fr", "FR"),
	LANGUAGE_ENGLISH(1, "English", "en", "US"),
	LANGUAGE_ARABIC(2, "Arabic", "ar", "MA"),
	LANGUAGE_SPANISH(3, "Spanish", "es", "ES"),
	LANGUAGE_PORTUGAL(4, "Portuguese", "pt", "BR"),
	LANGUAGE_ESPERANTO(5, "Esperanto", "eo", "EO"),
	LANGUAGE_GERMAN(6, "German", "de", "DE"),
	LANGUAGE_GALICIAN(7, "Galician", "gl", "ES"),
	LANGUAGE_ASTURIAN(8, "Asturian", "al", "ES"),
	LANGUAGE_GREEK(9, "Greek", "el", "GR"),
	LANGUAGE_ITALIAN(10, "Italian", "it", "IT"),
	LANGUAGE_CATALAN(11, "Catalan", "ca", "ES"),
	LANGUAGE_HUNGARIAN(12, "Hungarian", "hu", "HU"),
	LANGUAGE_ENGLISH_GERMAN(13, "ABZ German/English", "en", "DE");
	
	private int value;
	private String englishName;
	private String languageCode;
	private String countryCode;
	
    private static Map<Integer, Language> valueToLanguage;

    private Language(int value, String englishName, String languageCode, String countryCode) {
        this.value = value;
        this.englishName = englishName;
        this.languageCode = languageCode;
        this.countryCode = countryCode;
    }
    
    public static Language getLanguage(int i)
    {
    	if (valueToLanguage == null)
    		initMapping();
    	return valueToLanguage.get(i);
    }
    
    public static void initMapping()
    {
    	valueToLanguage = new HashMap<Integer, Language>();
    	for (Language lang : values()) {
    		valueToLanguage.put(lang.value, lang);
        }
    }
    
    public int getValue()
    {
    	return value;
    }
    
    public String getEnglishName()
    {
    	return englishName;
    }
    
    /**
     * return English name
     */
    @Override
    public String toString() {
        return getEnglishName();
    }
    
    public Locale getLocale()
    {
    	return new Locale(languageCode, countryCode);
	}
    
    public String getLanguageCode()
    {
    	return languageCode;
    }
    
    public String getCountryCode()
    {
    	return countryCode;
    }
}
