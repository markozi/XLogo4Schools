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

package xlogo.kernel.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import xlogo.Logo;
import xlogo.kernel.LogoError;
import xlogo.storage.WSManager;

public class NetworkClientSend
{
	
	private InetAddress		ip;
	private String			data;
	private BufferedReader	in;
	private PrintWriter		out;
	private Socket			socket;
	private String			answer;
	
	public NetworkClientSend(String ip, String data) throws LogoError
	{
		try
		{
			this.ip = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e)
		{
			throw new LogoError(Logo.messages.getString("no_host") + " " + ip);
		}
		this.data = data;
		init();
	}
	
	/**
	 * @return
	 * @uml.property name="answer"
	 */
	public String getAnswer()
	{
		return answer;
	}
	
	private void init() throws LogoError
	{
		try
		{
			socket = new Socket(ip, WSManager.getUserConfig().getTcpPort());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			java.io.OutputStream os = socket.getOutputStream();
			BufferedOutputStream b = new BufferedOutputStream(os);
			OutputStreamWriter osw = new OutputStreamWriter(b, "UTF8");
			out = new PrintWriter(osw, true);
			out.println(data);
			answer = "";
			answer = in.readLine();
			out.close();
			socket.close();
		}
		catch (IOException e)
		{
			throw new LogoError(Logo.messages.getString("no_host") + ip.getHostAddress());
		}
	}
}
