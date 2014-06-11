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
 * Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author LoÃ¯c Le Coq
 */

import java.io.*;
import java.util.StringTokenizer;

import java.util.Calendar;

/**
 * @author loic
 * 
 *         This class extracts the file tmp_xlogo.jar from the main archive
 *         in the temporary file's directory
 *         and then launch the command: <br>
 *         java -D-jar -Xmx64m -Djava.library.path=path_to_tmp -cp path_to_tmp
 *         tmp_xlogo.jar<br>
 * <br>
 *         XLogo executes with a predefined heap size for the Virtual Machine * 
 * 
 * @author Marko Zivkovic
 *         The maximum heap size property is now fixed 128MB. In the future, the application preferences should be used.
 *         Command line arguments are ignored, because this application is GUI-based and for
 *         children. I see no reason for a GUI application to have command line arguments.
 */
public class Lanceur
{
	private static String	PROPERTIES_PREFIX		= "ch.ethz.abz.xlogo4schools";
	private static int		DEFAULT_MEMORY_ALLOC	= 128;
	
	/**
	 * The process which contains the XLogo application
	 */
	private Process			p;
	/**
	 * The temporary folder which contains all files to start XLogo
	 */
	private File			tmpFolder				= null;
	private File[]			files					= new File[10];
	
	/**
	 * Main method
	 * 
	 * @param args
	 *            The path toward "lgo" files
	 */
	public static void main(String[] args)
	{
		new Lanceur();
	}
	
	Lanceur()
	{
		// Look for old files from XLogo crash
		cleanTmp();
		// Look for the memory that should be allocated to the JVM heap size
		//Preferences prefs = Preferences.systemRoot().node(PROPERTIES_PREFIX); TODO this does not work ... :-(
		//int memory = prefs.getInt("appMemory", DEFAULT_MEMORY_ALLOC);
		int memory = DEFAULT_MEMORY_ALLOC;
		
		// extract application in the java.io.tmp directory
		extractApplication();
		startApplicationProcess(memory);
		restorePath();
		deleteTmpFiles();
	}
	
	private void startApplicationProcess(int memoire)
	{
		try
		{
			// Add the tmp to the path
			String newPath = tmpFolder.getAbsolutePath();
			
			String javaLibraryPath = newPath + File.pathSeparatorChar + System.getProperty("java.library.path");
			// Bug when launching under Windows with java webstart
			javaLibraryPath = javaLibraryPath.replaceAll("\"", "");
			System.out.println("Path: " + javaLibraryPath + "\n");
			String[] commande = new String[5];
			commande[0] = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			commande[1] = "-jar";
			commande[2] = "-Xmx" + memoire + "m";
			commande[3] = "-Djava.library.path=" + javaLibraryPath;
			commande[4] = files[0].getAbsolutePath();
			
			System.out.println("<----- Starting XLogo ---->");
			String cmd = "";
			for (int i = 0; i < commande.length; i++)
			{
				cmd += commande[i] + " ";
			}
			System.out.println(cmd + "\n\n");
			p = Runtime.getRuntime().exec(commande);
			// Receive Messages from the Process
			startStreamForward(p.getInputStream());
			startStreamForward(p.getErrorStream());
			p.waitFor();
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
	
	private void deleteTmpFiles()
	{
		System.out.println("Closing XLogo. Cleaning tmp file");
		for (int i = 0; i < files.length; i++)
		{
			if (null != files[i])
				files[i].delete();
		}
		tmpFolder.delete();
	}
	
	private void restorePath()
	{
		String pathToFolder = tmpFolder.getAbsolutePath();
		String path = System.getProperty("java.library.path");
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		String newPath = "";
		while (st.hasMoreTokens())
		{
			if (!newPath.equals(""))
				newPath += File.pathSeparator;
			String element = st.nextToken();
			if (!element.equals(pathToFolder))
				newPath += element;
		}
		System.setProperty("java.library.path", newPath);
	}
	
	/**
	 * Used to catch application streams and write them to System.out
	 * @param stream
	 */
	private void startStreamForward(final InputStream stream)
	{
		new Thread(){
			public void run()
			{
				BufferedReader reader = null;
				try
				{
					reader = new BufferedReader(new InputStreamReader(stream));
					String line = "";
					while ((line = reader.readLine()) != null)
						System.out.println(line);
				}
				catch (IOException e)
				{
					System.out.println(e.toString());
				}
				finally
				{
					if (reader != null)
						try
						{
							reader.close();
						}
						catch (IOException e)
						{}
				}
			}
		}.start();
	}
	
	/**
	 * This method checks for unused old XLogo files in temporary directory<br>
	 * If it found files older than 24 hours with the prefix tmp_xlogo, these
	 * files are deleted.
	 */
	private void cleanTmp()
	{
		String path = System.getProperty("java.io.tmpdir");
		File f = new File(path);
		File[] files = f.listFiles();
		if (null != files)
		{
			for (int i = 0; i < files.length; i++)
			{
				try
				{
					if (files[i].getName().startsWith("tmp_xlogo"))
					{
						long fileTime = files[i].lastModified();
						long time = Calendar.getInstance().getTimeInMillis();
						// Delete file if it's more than 24 hours old
						if (time - fileTime > 24 * 3600 * 1000)
						{
							if (files[i].isDirectory())
								deleteDirectory(files[i]);
							files[i].delete();
							
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method extracts the file tmp_xlogo.jar from the archive and copy it
	 * into the temporary directory.
	 */
	private void extractApplication()
	{
		// Create in the "java.io.tmpdir" a directory called tmp_xlogo
		int i = 0;
		String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "tmp_xlogo";
		while (true)
		{
			tmpFolder = new File(tmpPath + i);
			if (!tmpFolder.exists())
				break;
			else
				i++;
		}
		boolean b = tmpFolder.mkdir();
		System.out.println("Creating tmp_xlogo directory - success: " + b);
		
		// extract the file tmp_xlogo.jar in this folder
		InputStream src = Lanceur.class.getResourceAsStream("tmp_xlogo.jar");
		files[0] = new File(tmpFolder.getAbsolutePath() + File.separator + "tmp_xlogo.jar");
		b = copier(src, files[0]);
		System.out.println("Copying tmp_xlogo.jar - success: " + b);
		
		// extract the file jh.jar in this folder
		src = Lanceur.class.getResourceAsStream("jh.jar");
		files[1] = new File(tmpFolder.getAbsolutePath() + File.separator + "jh.jar");
		b = copier(src, files[1]);
		System.out.println("Copying jh.jar - success: " + b);
		
		// extract the file vecmath.jar in this folder
		src = Lanceur.class.getResourceAsStream("vecmath.jar");
		files[2] = new File(tmpFolder.getAbsolutePath() + File.separator + "vecmath.jar");
		b = copier(src, files[2]);
		System.out.println("Copying vecmath.jar - success: " + b);
		
		// extract the file j3dcore.jar in this folder
		src = Lanceur.class.getResourceAsStream("j3dcore.jar");
		files[3] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore.jar");
		b = copier(src, files[3]);
		System.out.println("Copying j3dcore.jar - success: " + b);
		
		// extract the file j3dutils.jar in this folder
		src = Lanceur.class.getResourceAsStream("j3dutils.jar");
		files[4] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dutils.jar");
		b = copier(src, files[4]);
		System.out.println("Copying j3dutils.jar - success: " + b);
		
		// extract the file jl1.0.1 in this folder (JLayer library for mp3
		// playing)
		src = Lanceur.class.getResourceAsStream("jl1.0.1.jar");
		files[5] = new File(tmpFolder.getAbsolutePath() + File.separator + "jl1.0.1.jar");
		b = copier(src, files[5]);
		System.out.println("Copying jl1.0.1.jar - success: " + b);
		
		// extract the native driver for java 3d in this folder
		String os = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		System.out.println("Operating system: " + os);
		System.out.println("Architecture: " + arch);
		
		// Linux
		if (os.indexOf("linux") != -1)
		{
			if (arch.indexOf("86") != -1)
			{
				InputStream lib = Lanceur.class.getResourceAsStream("linux/x86/libj3dcore-ogl.so");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "libj3dcore-ogl.so");
				copier(lib, files[6]);
				lib = Lanceur.class.getResourceAsStream("linux/x86/libj3dcore-ogl-cg.so");
				files[7] = new File(tmpFolder.getAbsolutePath() + File.separator + "libj3dcore-ogl-cg.so");
				copier(lib, files[7]);
			}
			else
			{
				InputStream lib = Lanceur.class.getResourceAsStream("linux/amd64/libj3dcore-ogl.so");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "libj3dcore-ogl.so");
				copier(lib, files[6]);
			}
		}
		// windows
		else if (os.indexOf("windows") != -1)
		{
			if (arch.indexOf("86") != -1)
			{
				InputStream lib = Lanceur.class.getResourceAsStream("windows/x86/j3dcore-d3d.dll");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore-d3d.dll");
				b = copier(lib, files[6]);
				System.out.println("Copying library 1 - success: " + b);
				lib = Lanceur.class.getResourceAsStream("windows/x86/j3dcore-ogl.dll");
				files[7] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore-ogl.dll");
				b = copier(lib, files[7]);
				System.out.println("Copying library 2 - success: " + b);
				lib = Lanceur.class.getResourceAsStream("windows/x86/j3dcore-ogl-cg.dll");
				files[8] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore-ogl-cg.dll");
				b = copier(lib, files[8]);
				System.out.println("Copying library 3 - success: " + b);
				lib = Lanceur.class.getResourceAsStream("windows/x86/j3dcore-ogl-chk.dll");
				files[9] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore-ogl-chk.dll");
				b = copier(lib, files[9]);
				System.out.println("Copying library 4 - success: " + b);
			}
			else
			{
				InputStream lib = Lanceur.class.getResourceAsStream("windows/amd64/j3dcore-ogl.dll");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "j3dcore-ogl.dll");
				b = copier(lib, files[6]);
				System.out.println("Copying library 1 - success: " + b);
			}
		}
		// Mac os
		else if (os.indexOf("mac") != -1)
		{	
			
		}
		// solaris
		else if (os.indexOf("sunos") != -1)
		{
			if (arch.indexOf("86") != -1)
			{
				InputStream lib = Lanceur.class.getResourceAsStream("solaris/i386/libj3dcore-ogl.so");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "libj3dcore-ogl.so");
				b = copier(lib, files[6]);
				System.out.println("Copying library 1 - success: " + b);
			}
			else if (arch.indexOf("amd64") != -1)
			{
				InputStream lib = Lanceur.class.getResourceAsStream("solaris/amd64/libj3dcore-ogl.so");
				files[6] = new File(tmpFolder.getAbsolutePath() + File.separator + "libj3dcore-ogl.so");
				b = copier(lib, files[6]);
				System.out.println("Copying library 1 - success: " + b);
			}
		}
		
	}
	
	/**
	 * This method copy the file tmp_xlogo.jar from the archive to the file
	 * Destination.
	 * 
	 * @param destination
	 *            The output file
	 * @return true if success, false otherwise
	 */
	private boolean copier(InputStream src, File destination)
	{
		boolean resultat = false;
		// Declaration des flux
		java.io.FileOutputStream destinationFile = null;
		try
		{
			// CrÃ©ation du fichier :
			destination.createNewFile();
			// Ouverture des flux
			
			destinationFile = new java.io.FileOutputStream(destination);
			// Lecture par segment de 0.5Mo
			byte buffer[] = new byte[512 * 1024];
			int nbLecture;
			while ((nbLecture = src.read(buffer)) != -1)
			{
				destinationFile.write(buffer, 0, nbLecture);
			}
			// Copie rÃ©ussie
			resultat = true;
		}
		catch (java.io.FileNotFoundException f)
		{}
		catch (java.io.IOException e)
		{}
		finally
		{
			// Quoi qu'il arrive, on ferme les flux
			try
			{
				src.close();
			}
			catch (Exception e)
			{}
			try
			{
				destinationFile.close();
			}
			catch (Exception e)
			{}
		}
		return (resultat);
	}
	
	/**
	 * Delete a the directory created by Logo in /tmp
	 * 
	 * @param path
	 *            The Directory path
	 * @return true if success
	 */
	private boolean deleteDirectory(File path)
	{
		boolean resultat = true;
		if (path.exists())
		{
			File[] files = path.listFiles();
			if (null != files)
			{
				for (int i = 0; i < files.length; i++)
				{
					if (files[i].isDirectory())
					{
						resultat &= deleteDirectory(files[i]);
					}
					else
					{
						resultat &= files[i].delete();
					}
				}
			}
		}
		resultat &= path.delete();
		return (resultat);
	}
}
