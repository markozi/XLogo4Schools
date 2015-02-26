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

package xlogo.storage.workspace;

import java.util.Locale;

public enum LogoLanguage {
	FRENCH(0, "French", "fr", "FR"), 
	ENGLISH(1, "English", "en", "US"), 
	ARABIC(2, "Arabic", "ar", "MA"), 
	SPANISH(3, "Spanish", "es", "ES"), 
	PORTUESE(4, "Portuguese", "pt", "BR"), 
	ESPERANTO(5, "Esperanto", "eo", "EO"), 
	GERMAN(6, "German", "de", "DE"), 
	GALICIAN(7, "Galician","gl", "ES"), 
	ASTURIAN(8, "Asturian", "al", "ES"), 
	GREEK(9, "Greek", "el", "GR"), 
	ITALIAN(10, "Italian", "it", "IT"), 
	CATALAN(11, "Catalan", "ca", "ES"), 
	HUNGARIAN(12,"Hungarian", "hu", "HU");
	
	private int		value;
	private String	englishName;
	private String	languageCode;
	private String	countryCode;
		
	private LogoLanguage(int value, String englishName, String languageCode, String countryCode) {
		this.value = value;
		this.englishName = englishName;
		this.languageCode = languageCode;
		this.countryCode = countryCode;
	}
		
	public int getValue() {
		return value;
	}
    
	public static LogoLanguage valueOf(int value){
		switch(value){
			case 0: return LogoLanguage.FRENCH;
			case 1: return LogoLanguage.ENGLISH;
			case 2: return LogoLanguage.ARABIC;
			case 3: return LogoLanguage.SPANISH;
			case 4: return LogoLanguage.PORTUESE;
			case 5: return LogoLanguage.ESPERANTO;
			case 6: return LogoLanguage.GERMAN;
			case 7: return LogoLanguage.GALICIAN;
			case 8: return LogoLanguage.ASTURIAN;
			case 9: return LogoLanguage.GREEK;
			case 10: return LogoLanguage.ITALIAN;
			case 11: return LogoLanguage.CATALAN;
			case 12: return LogoLanguage.HUNGARIAN;
			default: return LogoLanguage.ENGLISH;
		}
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
