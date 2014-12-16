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

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq
 */

import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.UnknownHostException;

import xlogo.storage.WSManager;

import java.net.InetAddress;

import xlogo.Logo;
import xlogo.kernel.Kernel;
import xlogo.kernel.LogoError;

public class NetworkClientExecute
{
	private InetAddress		ip;
	private String			cmd;
	private PrintWriter		out;
	private BufferedReader	in;
	private Socket			socket;
	private Kernel			kernel;
	
	public NetworkClientExecute(Kernel kernel, String ip, String cmd) throws LogoError
	{
		this.kernel = kernel;
		try
		{
			this.ip = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e)
		{
			throw new LogoError(Logo.messages.getString("no_host") + " " + ip);
		}
		this.cmd = cmd;
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
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String wp = NetworkServer.EXECUTETCP + "\n";
			wp += kernel.getWorkspace().getSerializedContext(); // Marko : changed
			wp += NetworkServer.END_OF_FILE;
			out.println(wp);
			String input;
			while ((input = in.readLine()) != null)
			{
				// System.out.println("je vais envoyer: OK");
				if (input.equals("OK"))
				{
					// System.out.println("chargement reussi");
					break;
				}
			}
			out.println(cmd);
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
