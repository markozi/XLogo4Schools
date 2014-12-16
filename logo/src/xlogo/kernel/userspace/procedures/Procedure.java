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
 * Contents of this file were initially written Loic Le Coq.
 * The were heavily refactored, changed and extended by Marko Zivkovic
 */

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq
 */
package xlogo.kernel.userspace.procedures;

import java.util.Calendar;
import java.util.Stack;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.*;

import xlogo.Logo;
import xlogo.kernel.Primitive;
import xlogo.utils.Utils;

/**
 * The Procedure in XLogo4Schools receives pieces of text, where a single procedure is expected to be defined.
 * It parses the text and sets its fields accordingly.
 * It maintains states, so we know whether a procedure is executable or whether it has errors and what type of error.
 * With the new implementation, multiple types of errors can be detected at the same time.
 * 
 * <p>
 * Affichable (displayable) is removed. In XLogo4Schools, every procedure is displayable.
 * In XLogo, affichable was added to mark procedures that have been received over the network.
 * They should only be running while the TCPConnection is open.
 * They won't ever get displayed in the editor and they are removed, after the TCP connection is closed.
 * In the new implementation, I can treat the received workspace as a virtual file,
 * add its procedures to the UserSpace, without adding the file to the file list.
 * Hence it cannot be opened while the procedures can still be executed.
 * 
 * @author Marko Zivkovic, Loic
 */
public class Procedure
{
	public static final String	specialCharacters	= ":+-*/() []=<>&|";
	
	/* Marko : TODO the following properties need to be encapsulated completely (Legacy from XLogo)
	 * I removed those properties that were named _sauve etc. which was used to temporarily redefine procedures,
	 * and then restore them in case there was an error while parsing.
	 */
	
	// false lorsque c'est une procédure d'un fichier de démarrage
	/**
	 * Whitespace and comments above a procedure in the editor
	 */
	public String				comment;
	public int					nbparametre;
	public String				name;
	public ArrayList<String>	variable			= new ArrayList<String>();
	public Stack<String>		optVariables		= new Stack<String>();		// Marko : why Stack??? [so bad]
	public Stack<StringBuffer>	optVariablesExp		= new Stack<StringBuffer>(); // Marko : why Stack?? [so bad]
	public String				instruction			= "";
	public StringBuffer			instr				= null;
	
	// Marko : I added these
	private String 				text				= null;
	private String				ownerName			= null;
	private Calendar			defineTime			= null;
			
	/**
	 * Create a procedure from a piece of text from the editor.
	 * The expected structure is as follows.
	 * <p>
	 * [leading <b>empty lines</b> and <b>comments</b> => stored into comment] <br>
	 * <b>to procedureName</b> [<b>variables</b>] [<b>optional variables</b>] <br>
	 * [<b>body</b>] <br>
	 * <b>end</b><br>
	 * 
	 * @param text
	 * @throws IOException 
	 */
	public Procedure(String text)
	{
		try
		{
			defineTime = Calendar.getInstance();
			this.text = text;
			StringReader sr = new StringReader(text);
			BufferedReader br = new BufferedReader(sr);
			String line = parseComments(br);
			if (state == State.COMMENT_ONLY)
				return;
			StringTokenizer st = new StringTokenizer(line);
			parseName(st);
			if (errors.contains(ProcedureErrorType.MISSING_TO))
				return;
			parseVariables(st);
			parseBody(br);
			
			if (state == State.UNINITIALIZED)
				setState(State.EXECUTABLE);
		}
		catch(IOException ignore) {
			/* this should not happen, no actual IO */
		}
	}
	
	/**
	 * Create a procedure with all necessary values.<br>
	 * If name is not legal, the Procedure state will switch to error,
	 * otherwise it will be executable
	 */
	public void redefine(Procedure newDefinition)
	{
		defineTime = Calendar.getInstance();
		this.name = newDefinition.name;
		this.nbparametre = newDefinition.nbparametre;
		this.variable = newDefinition.variable;
		this.optVariables = newDefinition.optVariables;
		this.optVariablesExp = newDefinition.optVariablesExp;
		this.text = newDefinition.text;
		this.errors = newDefinition.errors;
		
		ProcedureErrorType e = checkName(name);
		if (e != ProcedureErrorType.NO_ERROR)
			addError(e);
		else
			setState(State.EXECUTABLE);
	}

	public Calendar getDefineTime()
	{
		return defineTime;
	}
	
	public String getText()
	{
		return text;
	}
		
	/*
	 * PROCEDURE STATE
	 */
	
	private State state = State.UNINITIALIZED;
	
	private ArrayList<ProcedureErrorType> errors = new ArrayList<ProcedureErrorType>();
	
	/**
	 * @see {@link State}
	 * @return
	 */
	public State getState()
	{
		return state;
	}
	
	private void setState(State state)
	{
		this.state = state;
	}
	
	/**
	 * Use this to indicate that this procedure is not executable because there is an ambiguity within its file.
	 */
	public void makeAmbiguous()
	{
		this.state = State.AMBIGUOUS_NAME;
		if (!errors.contains(ProcedureErrorType.AMBIGUOUS))
			errors.add(ProcedureErrorType.AMBIGUOUS);
	}
	
	/**
	 * States are: UNINITIALIZED, EXECUTABLE, COMMENT_ONLY, ERROR
	 * @author Marko
	 */
	public enum State
	{
		/**
		 * No values are set, or no check has been performed.
		 */
		UNINITIALIZED("procedure.unititialized"),
		/**
		 * The procedure has a correct structure and can be executed.
		 */
		EXECUTABLE("procedure.executable"),
		/**
		 * The procedure contains only a comment, no procedure definition. It is not executable
		 */
		COMMENT_ONLY("procedure.not.executable"),
		/**
		 * The procedure structure could no be parsed entirely because it contains errors.
		 */
		ERROR("procedure.error"),
		/**
		 * There is another procedures with the same name in the same file.
		 */
		AMBIGUOUS_NAME("procedure.ambiguous");
		
		private String description;
		
		private State(String description)
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

	/*
	 * ERROR REPORTING
	 */
		
	public ArrayList<ProcedureErrorType> getErrors()
	{
		return errors;
	}
	
	private void addError(ProcedureErrorType e)
	{
		errors.add(e);
		state = State.ERROR;
	}
	
	public String getName()
	{
		return name;
	}

	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * OLD XLOGO FEATURES ... TODO behavior not always clear
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

	/**
	 * @author Loic Le Coq
	 */
	public void decoupe()
	{
		// Only cut procedures which are visible in the editor
		if (null == instr)
		{
			instr = new StringBuffer();
			try
			{
				String line = "";
				StringReader sr = new StringReader(instruction);
				BufferedReader bfr = new BufferedReader(sr);
				int lineNumber = 0;
				// Append the number of the line
				instr.append("\\l");
				instr.append(lineNumber);
				instr.append(" ");
				while (bfr.ready())
				{
					lineNumber++;
					// read the line
					try
					{
						line = bfr.readLine().trim();
					}
					catch (NullPointerException e1)
					{
						break;
					}
					// delete comments
					line = deleteComments(line);
					line = Utils.decoupe(line).toString().trim();
					instr.append(line);
					if (!line.equals(""))
					{
						instr.append(" ");
						// Append the number of the line
						instr.append("\\l");
						instr.append(lineNumber);
						instr.append(" ");
					}
				}
			}
			catch (IOException e)
			{}
			// System.out.println("****************************"+name+"\n"+instr+"\n\n");
		}
	}

	/**
	 * @author Loic Le Coq
	 */
	private String deleteComments(String line)
	{
		int index = line.indexOf("#");
		while (index != -1)
		{
			if (index == 0)
			{
				return "";
			}
			else if (line.charAt(index - 1) != '\\')
			{
				return line.substring(0, index);
			}
			else
				index = line.indexOf("#", index + 1);
		}
		return line;
	}

	/**
	 * @author Loic Le Coq
	 */
	public String toString()
	{
		// return("nom "+name+" nombre paramètres "+nbparametre+" identifiant "+id+"\n"+variable.toString()+"\n"+instr+"\ninstrction_sauve"+instruction_sauve+"\ninstr_sauve\n"+instr_sauve);
		StringBuffer sb = new StringBuffer();

		sb.append(comment);
		sb.append(Logo.messages.getString("pour") + " " + name);
		for (int j = 0; j < nbparametre; j++)
		{
			sb.append(" :");
			sb.append(variable.get(j));
		}
		for (int j = 0; j < optVariables.size(); j++)
		{
			sb.append(" [ :");
			sb.append(optVariables.get(j));
			sb.append(" ");
			sb.append(optVariablesExp.get(j));
			sb.append(" ]");
		}
		sb.append("\n");
		sb.append(instruction);
		sb.append(Logo.messages.getString("fin"));
		sb.append("\n");
		// System.out.println("a"+sb+"a");
		return new String(sb);
	}
	
	/**
	 * @author Loic Le Coq
	 */
	public StringBuffer cutInList()
	{
		// Only cut procedures which are visible in the editor
		StringBuffer sb = new StringBuffer();
		try
		{
			String line = "";
			StringReader sr = new StringReader(instruction);
			BufferedReader bfr = new BufferedReader(sr);
			while (bfr.ready())
			{
				try
				{
					line = bfr.readLine().trim();
				}
				catch (NullPointerException e1)
				{
					break;
				}
				// delete comments
				// line=deleteComments(line);
				line = Utils.decoupe(line).toString().trim();
				sb.append("[ ");
				sb.append(line);
				sb.append(" ] ");
			}
		}
		catch (IOException e)
		{}
		return sb;
	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * PARSING
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * Read and save the comments that appear before the procedure
	 * @param br
	 * @return the first line that (probably) contains a procedure definition,
	 *         or null if there are no more procedures
	 * @throws IOException
	 * @throws DocumentStructureException
	 */
	String parseComments(BufferedReader br) throws IOException
	{
		String line = null;
		comment = "";
		while (br.ready())
		{
			line = br.readLine();
			if (line == null)
				break;
			
			if (isComment(line))
			{
				comment += line + "\n";
				line = null;
			}
			else if (line.trim().equals(""))
			{
				comment += "\n";
				line = null;
			}
			else
				break;
		}
		
		if (line == null)
		{
			setState(State.COMMENT_ONLY);
		}
		
		return line;
	}
	
	/**
	 * Expects a line that starts with "to procedName"
	 * @return Error, NO_ERROR if name is ok
	 * @throws DocumentStructureException
	 */
	void parseName(StringTokenizer st)
	{
		String token = st.nextToken();

		if (!token.toLowerCase().equals(Logo.messages.getString("pour").toLowerCase()))
		{
			addError(ProcedureErrorType.MISSING_TO);
			return;
		}

		if (!st.hasMoreTokens())
		{
			addError(ProcedureErrorType.MISSING_NAME);
			return;
		}
		
		name = st.nextToken().toLowerCase();
		
		ProcedureErrorType e = checkName(name);
		if (!e.equals(ProcedureErrorType.NO_ERROR))
			addError(e);
	}
	
	void parseVariables(StringTokenizer st)
	{
		// Then, We read the variables
		// :a :b :c :d .....
		String token = null;
		while (st.hasMoreTokens())
		{
			token = st.nextToken();
			if (token.startsWith(":"))
			{
				String var = token.substring(1);
				ProcedureErrorType e = checkValidVariable(var);
				if (e != ProcedureErrorType.NO_ERROR)
				{
					addError(e);
					return;
				}
				var = var.toLowerCase();
				variable.add(var);
			}
			else
				break;
		}

		nbparametre = variable.size();
		
		if (token == null || token.startsWith(":"))
			return;
			
		// read all remaining tokens into string buffer
		StringBuffer sb = new StringBuffer();
		sb.append(token);
		//sb.append(token);
		while (st.hasMoreTokens())
		{
			sb.append(" ");
			sb.append(st.nextToken());
		}
		// And finally, optional variables if there are some.
		// [:a 100] [:b 20] [:c 234] ...........
		
		while (sb.length() > 0)
		{
			if (sb.indexOf("[") != 0)
			{
				addError(ProcedureErrorType.VAR_EXTRA_CHARS);
				return;
			}
			
			sb.deleteCharAt(0);
			String[] arg = new String[2];
			extractOptionalVariable(sb, arg);
			optVariables.push(arg[0].toLowerCase());
			/* Bug Fixed: list as Optionnal arguments
			** Eg: 
			** to a [:var [a b c]]
			* end 
			* when the string is formatted, we check that a white space 
			* is needed at the end of the argument
			*/
			
			StringBuffer exp = Utils.decoupe(arg[1]);
			if (exp.charAt(exp.length() - 1) != ' ')
				exp.append(" ");
			optVariablesExp.push(exp);
		}
	}
	
	/**
	 * Reads from sb into args the name and the default value of an optional variable [:a value]. <br>
	 * value can be expression: number, word, list, ...
	 * @author Loic Le Coq
	 * @author Marko Zivkovic refactored
	 */
	void extractOptionalVariable(StringBuffer sb, String[] args)
	{
	
	    String variable="";
	    String expression="";
	    int compteur=1;
	    int id=0;
	    int id2=0;
		boolean espace = false;
		for (int i = 0; i < sb.length(); i++)
		{
			char ch = sb.charAt(i);
			if (ch == '[')
				compteur++;
			else if (ch == ']')
			{
				if (id == 0)
				{
					addError(ProcedureErrorType.OPT_VAR_BRACKET);
					return;
				}
				compteur--;
			}
			else if (ch == ' ')
			{
				if (!variable.equals(""))
				{
					if (!espace)
						id = i;
					espace = true;
				}
			}
			else
			{
				if (!espace)
					variable += ch;
			}
			if (compteur == 0)
			{
				id2 = i;
				break;
			}
		}
		if (!variable.startsWith(":"))
		{
			addError(ProcedureErrorType.VAR_COLON_EXPECTED);
			return;
		}

		variable = variable.substring(1, variable.length()).toLowerCase();
		ProcedureErrorType pet = checkValidVariable(variable);
		
		if (pet != ProcedureErrorType.NO_ERROR)
		{
			addError(pet);
			return;
		}
		
		
		if (compteur != 0)
		{
			addError(ProcedureErrorType.OPT_VAR_BRACKET);
			return;
		}
		
		expression = sb.substring(id + 1, id2).trim();
		
		if (expression.equals(""))
		{
			addError(ProcedureErrorType.VAR_MISSING_EXPRESSION);
			return;
		}
		/*  System.out.println(id+" "+id2);
		  System.out.println("a"+expression+"a");
		  System.out.println("a"+variable+"a");*/
		sb.delete(0, id2 + 1);
		// delete unnecessary space
		while (sb.length() != 0 && sb.charAt(0) == ' ')
			sb.deleteCharAt(0);
		args[0] = variable;
		args[1] = expression;
	}
	
	/**
	 * Sets as body everything up the 'end' key word.
	 */
	private void parseBody(BufferedReader br) throws IOException
	{
		StringBuffer body = new StringBuffer();
		String to = Logo.messages.getString("pour").toLowerCase() + " ";
		String end = Logo.messages.getString("fin").toLowerCase();
		instruction = null;
		String line;
		String lower;

		while (br.ready())
		{
			line = br.readLine();
			if (null == line)
				break;
			lower = line.toLowerCase();
			if (lower.trim().equals(end)) // end of procedure
			{
				instruction = body.toString();
				if (br.ready() && br.readLine() != null) // Additional characters after end
					addError(ProcedureErrorType.CHARS_AFTER_END);
				return; // We are done
			}
			if (lower.startsWith(to)) // new procedure definition before end
			{
				addError(ProcedureErrorType.TO_BEFORE_END);
				return;
			}
			body.append(line);
			body.append("\n");
		}
		
		// no end was found
		addError(ProcedureErrorType.MISSING_END);

	}
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * UTILITY
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	/**
	 * @return true if and only if line starts with '#'
	 */
	static boolean isComment(String line)
	{
		if (line.trim().startsWith("#"))
			return true;
		else
			return false;
	}
	
	static boolean isNumber(String token)
	{
		try
		{
			Double.parseDouble(token);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	/**
	 * This method is essentially changed to fit with the new workspace (=UserSpace & LogoFile).<br>
	 * On the fly it was rewritten completely, because of its initial programming style and inefficiency. <br>
	 * (note that it was very long, and now it is only 3 lines)
	 * 
	 * @param token
	 * @return Error - NO_ERROR if token is a legal procedure name
	 * @author Marko Zivkovic
	 */
	static ProcedureErrorType checkName(String token)
	{
		if (isNumber(token))
			return ProcedureErrorType.NAME_IS_NUMBER;
		ProcedureErrorType e = checkSpecialCharacter(token);
		if (e != ProcedureErrorType.NO_ERROR)
			return e;
		
		if(Primitive.primitives.containsKey(token))
			return ProcedureErrorType.NAME_IS_KEY_WORD;
		
		return ProcedureErrorType.NO_ERROR;
	}

	/**
	 * @param token
	 * @return null if a valid variable name, error message otherwise
	 */
	static ProcedureErrorType checkValidVariable(String token)
	{
		if (token.length() == 0)
			return ProcedureErrorType.VAR_WHITESPACE_AFTER_COLON;
		
		if (isNumber(token))
			return ProcedureErrorType.VAR_IS_NUMBER;
		
		return checkSpecialCharacter(token);
	}
	
	static ProcedureErrorType checkSpecialCharacter(String var)
	{
		StringTokenizer check = new StringTokenizer(var, specialCharacters, true);
		
		if ((check.countTokens() > 1) || ":+-*/() []=<>&|".indexOf(check.nextToken()) > -1)
			return ProcedureErrorType.VAR_HAS_SPECIAL;
		return ProcedureErrorType.NO_ERROR;
	}
	
	/*
	 * OWNER : This is typically a file, but a procedure must not know whether it is owned by a file or something else
	 */

	public String getOwnerName()
	{
		return ownerName;
	}

	public void setOwnerName(String ownerName)
	{
		this.ownerName = ownerName;
	}
	
}
