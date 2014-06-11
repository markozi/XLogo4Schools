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

package xlogo.kernel.network;

/**
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author LoÃ¯c Le Coq
 */
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import xlogo.storage.WSManager;
import xlogo.Application;
import xlogo.Logo;
import xlogo.kernel.LogoError;
import xlogo.kernel.Kernel;
import xlogo.messages.MessageKeys;
import xlogo.messages.async.history.HistoryMessenger;

public class NetworkServer
{
	public static boolean			isActive;
	private ServerSocket			serverSocket;
	private Application				app;
	private PrintWriter				out;
	private BufferedReader			in;
	private ChatFrame				cf;
	
	protected static final String	EXECUTETCP	= "executetcp" + "\u001B";
	protected static final String	CHATTCP		= "chattcp" + "\u001B";
	protected static final String	END_OF_FILE	= "*/EOF\\*" + "\u001B";
	private Kernel					kernel;
	
	public NetworkServer(Application app) throws LogoError
	{
		isActive = true;
		this.app = app;
		this.kernel = app.getKernel();
			init();
		
	}
	
	private void init() throws LogoError
	{
		try
		{
			serverSocket = new ServerSocket(WSManager.getUserConfig().getTcpPort());
		}
		catch (IOException e)
		{
			throw (new LogoError(Logo.messages.getString("pb_port") + WSManager.getUserConfig().getTcpPort()));
		}
		Socket clientSocket = null;
		try
		{
			clientSocket = serverSocket.accept();
			java.io.OutputStream os = clientSocket.getOutputStream();
			BufferedOutputStream b = new BufferedOutputStream(os);
			OutputStreamWriter osw = new OutputStreamWriter(b, "UTF8");
			out = new PrintWriter(osw, true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));
			String inputLine = "";
			inputLine = in.readLine();
			// ******************* Executetcp **************************
			if (inputLine.equals(NetworkServer.EXECUTETCP))
			{
				StringBuffer remoteNetworkContext = new StringBuffer();
				while ((inputLine = in.readLine()) != null)
				{
					if (inputLine.equals(NetworkServer.END_OF_FILE))
						break;
					remoteNetworkContext.append(inputLine);
					remoteNetworkContext.append("\n");
				}
				
				/*
				 * Marko Zivkovic : New implementation of this context switch
				 */
				
				try
				{
					kernel.getWorkspace().pushNetworkMode(remoteNetworkContext.toString());
				}
				catch(Exception e)
				{
					throw new LogoError(Logo.messages.getString(MessageKeys.ERROR_NETWORK_CONTEXT));
				}
				
				// We say the workspace is fully created
				out.println("OK");
				// What is the command to execute?
				inputLine = in.readLine();
				// System.out.println("a exÃ©cuter: "+inputLine);
				out.close();
				in.close();
				clientSocket.close();
				serverSocket.close();
				
				kernel.execute(new StringBuffer(inputLine + "\\x "));
				
			}
			// ******************* Chattcp **************************
			else if (inputLine.equals(NetworkServer.CHATTCP))
			{
				String sentence = "";
				while ((inputLine = in.readLine()) != null)
				{
					if (inputLine.equals(NetworkServer.END_OF_FILE))
						break;
					sentence = inputLine;
				}
				cf = new ChatFrame(out, app);
				cf.append("distant", sentence);
				while ((sentence = in.readLine()) != null)
				{
					if (sentence.equals(NetworkServer.END_OF_FILE))
					{
						cf.append("local", Logo.messages.getString("stop_chat"));
						break;
					}
					cf.append("distant", sentence);
				}
				out.close();
				in.close();
				clientSocket.close();
				serverSocket.close();
			}
			// ******************* Envoietcp **************************
			else
			{
				HistoryMessenger.getInstance().dispatchMessage(inputLine);
				out.println(Logo.messages.getString("pref.ok"));
				out.close();
				in.close();
				clientSocket.close();
				serverSocket.close();
				
			}
		}
		catch (IOException e)
		{
			throw new LogoError(Logo.messages.getString("erreur_tcp"));
		}
		isActive = false;
	}
	
	public static void stopServer()
	{
		String st = NetworkServer.EXECUTETCP + "\n" + NetworkServer.END_OF_FILE + "\n\n";
		try
		{
			Socket socket = new Socket("127.0.0.1", WSManager.getUserConfig().getTcpPort());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(st);
		}
		catch (IOException e)
		{}
	}
}
