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

package xlogo.kernel.userspace.procedures;

import xlogo.messages.MessageKeys;

public enum ProcedureErrorType
{
	NO_ERROR(MessageKeys.LOGO_ERROR_NO_ERROR),
	MISSING_TO(MessageKeys.LOGO_ERROR_MISSING_TO),
	MISSING_NAME(MessageKeys.LOGO_ERROR_MISSING_NAME),
	NAME_IS_NUMBER(MessageKeys.LOGO_ERROR_NAME_IS_NUMBER), //Logo.messages.getString("erreur_nom_nombre_procedure")
	/*
	 * Logo.messages.getString("caractere_special1") + "\n" + Logo.messages.getString("caractere_special2")
	 *			+ "\n" + Logo.messages.getString("caractere_special3") + " " + token
	 */
	NAME_HAS_SPECIAL_CHAR(MessageKeys.LOGO_ERROR_NAME_SPECIAL),
	VAR_WHITESPACE_AFTER_COLON(MessageKeys.LOGO_ERROR_VAR_WHITE_AFTER_COLON),
	/*
	 * Logo.messages.getString("caractere_special_variable") + "\n"
				+ Logo.messages.getString("caractere_special2") + "\n"
				+ Logo.messages.getString("caractere_special3") + " :" + var
	 */
	VAR_HAS_SPECIAL(MessageKeys.LOGO_ERROR_VAR_SPECIAL),
	VAR_IS_NUMBER(MessageKeys.LOGO_ERROR_VAR_IS_NUMBER),
	VAR_COLON_EXPECTED(MessageKeys.LOGO_ERROR_VAR_MISSING_COLON),
	OPT_VAR_BRACKET(MessageKeys.LOGO_ERROR_OPT_VAR_BRACKET),
	VAR_EXTRA_CHARS(MessageKeys.LOGO_ERROR_VAR_EXTRA_CHARS),
	VAR_MISSING_EXPRESSION(MessageKeys.LOGO_ERROR_MISSING_EXPR),
	CHARS_AFTER_END(MessageKeys.LOGO_ERROR_MORE_CHARS_AFTER_END),
	MISSING_END(MessageKeys.LOGO_ERROR_MISSING_END),
	TO_BEFORE_END(MessageKeys.LOGO_ERROR_TO_BEFORE_END),
	NAME_IS_KEY_WORD(MessageKeys.LOGO_ERROR_NAME_IS_KEY_WORD),
	AMBIGUOUS(MessageKeys.LOGO_ERROR_AMBIGUOUS);
	
	private String description;
	
	private ProcedureErrorType(String description)
	{
		this.description = description;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * @return {@link #getDescription()}
	 */
	public String toString()
	{
		return getDescription();
	}
}