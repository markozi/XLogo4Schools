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

import xlogo.Application;
import xlogo.Logo;
import xlogo.kernel.LogoError;
import xlogo.storage.WSManager;

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq
 */

public class NetworkClientChat
{
	private InetAddress		ip;
	private String			st;
	private Application		app;
	private PrintWriter		out;
	private BufferedReader	in;
	private Socket			socket;
	ChatFrame				cf;
	
	public NetworkClientChat(Application app, String ip, String st) throws LogoError
	{
		this.app = app;
		try
		{
			this.ip = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e)
		{
			throw new LogoError(Logo.messages.getString("no_host") + " " + ip);
		}
		this.st = st;
		init();
	}
	
	private void init() throws LogoError
	{
		try
		{
			socket = new Socket(ip, WSManager.getUserConfig().getTcpPort());
			java.io.OutputStream os = socket.getOutputStream();
			BufferedOutputStream b = new BufferedOutputStream(os);
			OutputStreamWriter osw = new OutputStreamWriter(b, "UTF8");
			out = new PrintWriter(osw, true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
			String cmd = NetworkServer.CHATTCP + "\n";
			cmd += st + "\n";
			cmd += NetworkServer.END_OF_FILE;
			cf = new ChatFrame(out, app);
			cf.append("local", st);
			out.println(cmd);
			while ((cmd = in.readLine()) != null)
			{
				if (cmd.equals(NetworkServer.END_OF_FILE))
				{
					cf.append("local", Logo.messages.getString("stop_chat"));
					break;
				}
				cf.append("distant", cmd);
			}
			out.close();
			in.close();
			socket.close();
		}
		catch (IOException e)
		{
			throw new LogoError(Logo.messages.getString("no_host") + ip.getHostAddress());
		}
		
	}
	
}
