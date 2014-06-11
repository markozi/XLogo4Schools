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
 * Contents of this file were initially written by Loïc Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic 
 */

/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author LoÃ¯c Le Coq
 */
package xlogo.kernel;
import java.math.BigDecimal;
/**
 * This class saves all Loop Properties (repeat, while, for) such as increment, end integer ....
 * @author loic
 */
public class LoopProperties {
	/**
	 * Counter: The counter value for the current loop End: The end value for the loop Increment the increment between two values
	 * @uml.property  name="counter"
	 */
	private BigDecimal counter;
	/**
	 * Counter: The counter value for the current loop End: The end value for the loop Increment the increment between two values
	 * @uml.property  name="end"
	 */
	private BigDecimal end;
	/**
	 * Counter: The counter value for the current loop End: The end value for the loop Increment the increment between two values
	 * @uml.property  name="increment"
	 */
	private BigDecimal increment;
	/**
	 * The Instruction to execute on each iteration
	 * @uml.property  name="instr"
	 */
	String instr;
/**
 * The super constructor for all loops
 * @param counter The beginning integer
 * @param fin The end integer
 * @param increment The increment between two values
 * @param instr The instruction to execute each loop
 */
	LoopProperties(BigDecimal counter,BigDecimal end,BigDecimal increment,String instr){
		this.counter=counter;
		this.end=end;
		this.increment=increment;
		this.instr=instr;
	}
	

	/**
	 * Adds the increment to the variable counter
	 */
	
	protected void incremente(){
		counter=counter.add(increment);
		counter=new BigDecimal(MyCalculator.eraseZero(counter));
	}
	/**
	 * This method returns the Loop Id
	 * @return the Loop Id (TYPE_FOR, TYPE_WHILE...)
	 */
/*	protected int getId(){
		return id;
	}*/
	/**
	 * This method returns the Loop Id
	 * @return  the Loop Id (TYPE_FOR, TYPE_WHILE...)
	 * @uml.property  name="counter"
	 */
	protected BigDecimal getCounter(){
		return counter;
	}
	/**
	 * This method returns the end Value
	 * @return  the end value for the loop
	 * @uml.property  name="end"
	 */
	protected BigDecimal getEnd(){ 
		return end;
	}
	/**
	 * this method returns the increment for the loop
	 * @return  The variable increment
	 * @uml.property  name="increment"
	 */
	protected BigDecimal getIncrement(){
		return increment;
	}
	/**
	 * This method returns the instructions to execute each loop
	 * @return  the instruction block
	 * @uml.property  name="instr"
	 */
	protected String getInstr(){
		return instr;	
	}
	/**
	 * This method returns a loop description
	 */
	public String toString(){
		return(counter+" "+end+" "+increment+"\n"+instr+"\n");
	}	

	protected boolean isFor(){
		return false;
	}
	protected boolean isWhile(){
		return false;
	}
	protected boolean isForEach(){
		return false;
	}
	protected boolean isRepeat(){
		return false;
	}
	protected boolean isForEver(){
		return true;
	}
	protected boolean isFillPolygon(){
		return false;
	}
}
