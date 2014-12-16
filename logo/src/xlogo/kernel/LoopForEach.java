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
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

package xlogo.kernel;

import java.math.BigDecimal;
import java.util.Vector;

public class LoopForEach extends LoopFor{
	private Vector<String> vec;
	/**
	 * Constructor Loop: For
	 * @param counter The beginning integer
	 * @param end The end integer
	 * @param increment The increment between two values
	 * @param instr The instruction to execute between two values
	 * @param var The name of the variable
	 * @param vec The Vec with all value for the variable
	 */
	LoopForEach(BigDecimal counter,BigDecimal end,BigDecimal increment,String instr,String var,Vector<String> vec){
		super(counter,end,increment,instr,var);
		this.vec=vec;
	}
	protected boolean isForEach(){
		return true;
	}
	protected boolean isForEver(){
		return false;
	}
	/**
	 * This method affects the variable counter the correct value 
	 * @param first boolean that indicates if it is the first affectation
	 */
	protected void AffecteVar(boolean first){
		String element=vec.get(getCounter().intValue());
		if (Interprete.locale.containsKey(var)){
			if (first) conserver=true;
			Interprete.locale.put(var, element);
		} 
		else {
			Interprete.locale.put(var,element);
		}
	}
}
