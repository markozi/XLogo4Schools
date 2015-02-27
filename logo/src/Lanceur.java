/*
 * XLogo4Schools - A Logo Interpreter specialized for use in schools, based on XLogo by Loic Le Coq
 * Copyright (C) 2013 Marko Zivkovic
 * Contact Information: marko88zivkovic at gmail dot com
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the
 * GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 * This Java source code belongs to XLogo4Schools, written by Marko Zivkovic
 * during his Bachelor thesis at the computer science department of ETH Zurich,
 * in the year 2013 and/or during future work.
 * It is a reengineered version of XLogo written by Loic Le Coq, published
 * under the GPL License at http://xlogo.tuxfamily.org/
 * Contents of this file were initially written by Loic Le Coq,
 * modifications, extensions, refactorings might have been applied by Marko Zivkovic
 */

/** Title : XLogo
 * Description : XLogo is an interpreter for the Logo
 * programming language
 * 
 * @author Loïc Le Coq */

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
public class Lanceur {
	private static int	DEFAULT_MEMORY_ALLOC	= 128;
	
	/**
	 * The process which contains the XLogo application
	 */
	private Process		p;
	/**
	 * The temporary folder which contains all files to start XLogo
	 */
	private File		tmpFolder				= null;
	
	/**
	 * Main method
	 * 
	 * @param args
	 *            The path toward "lgo" files
	 */
	public static void main(String[] args) {
		new Lanceur();
	}
	
	Lanceur() {
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
	
	private void startApplicationProcess(int memoire) {
		try {
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
			commande[4] = Library.TMP_XLOGO.getFile(tmpFolder).getAbsolutePath();
			
			System.out.println("<----- Starting XLogo ---->");
			String cmd = "";
			for (int i = 0; i < commande.length; i++) {
				cmd += commande[i] + " ";
			}
			System.out.println(cmd + "\n\n");
			p = Runtime.getRuntime().exec(commande);
			// Receive Messages from the Process
			startStreamForward(p.getInputStream());
			startStreamForward(p.getErrorStream());
			p.waitFor();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void restorePath() {
		String pathToFolder = tmpFolder.getAbsolutePath();
		String path = System.getProperty("java.library.path");
		StringTokenizer st = new StringTokenizer(path, File.pathSeparator);
		String newPath = "";
		while (st.hasMoreTokens()) {
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
	private void startStreamForward(final InputStream stream) {
		new Thread(){
			public void run() {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(stream));
					String line = "";
					while ((line = reader.readLine()) != null)
						System.out.println(line);
				}
				catch (IOException e) {
					System.out.println(e.toString());
				}
				finally {
					if (reader != null)
						try {
							reader.close();
						}
						catch (IOException e) {}
				}
			}
		}.start();
	}
	
	/**
	 * This method checks for unused old XLogo files in temporary directory<br>
	 * If it found files older than 24 hours with the prefix tmp_xlogo, these
	 * files are deleted.
	 */
	private void cleanTmp() {
		String path = System.getProperty("java.io.tmpdir");
		File f = new File(path);
		File[] files = f.listFiles();
		if (null != files) {
			for (int i = 0; i < files.length; i++) {
				try {
					if (files[i].getName().startsWith("tmp_xlogo")) {
						long fileTime = files[i].lastModified();
						long time = Calendar.getInstance().getTimeInMillis();
						// Delete file if it's more than 24 hours old
						if (time - fileTime > 24 * 3600 * 1000) {
							if (files[i].isDirectory())
								deleteDirectory(files[i]);
							files[i].delete();
							
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * This method extracts the file tmp_xlogo.jar from the archive and copy it
	 * into the temporary directory.
	 */
	private void extractApplication() {
		initTmpFolder();
		
		for (Library lib : Library.values()) {
			if (lib.getPath() == null){
				copy2Tmp(lib);
			}
		}
		// extract the native driver for java 3d in this folder
		String osName = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch");
		System.out.println("Operating system: " + osName);
		System.out.println("Architecture: " + arch);
		
		// Linux
		//InputStream lib;
		OS os = OS.valueOf(osName, arch);
		
		for (Library lib : os.getLibraries()) {
			copy2Tmp(lib);
		}
		
	}
	
	private void initTmpFolder() {
		// Create in the "java.io.tmpdir" a directory called tmp_xlogo
		int i = 0;
		String tmpPath = System.getProperty("java.io.tmpdir") + File.separator + "tmp_xlogo";
		while (true) {
			tmpFolder = new File(tmpPath + i);
			if (!tmpFolder.exists())
				break;
			else
				i++;
		}
		
		System.out.println("Creating tmp_xlogo directory - success: " + tmpFolder.mkdir());
	}
		
	private void copy2Tmp(Library lib) {
		// extract the jar file in this folder
		InputStream src = Lanceur.class.getResourceAsStream(lib.getResourcePath());
		File file = lib.getFile(tmpFolder);
		System.out.println("Get library " + lib.getResourcePath());
		System.out.println("Copying " + lib.getLibraryName() + " - success: " + copy(src, file));
	}
	
	private void deleteTmpFiles() {
		System.out.println("Closing XLogo. Cleaning tmp file");
		for (Library lib : Library.values()) {
			File file = lib.getFile(tmpFolder);
			if (file.exists()) {
				file.delete();
			}
		}
		tmpFolder.delete();
	}
	
	/**
	 * This method copy the file tmp_xlogo.jar from the archive to the file
	 * Destination.
	 * 
	 * @param destination
	 *            The output file
	 * @return true if success, false otherwise
	 */
	private boolean copy(InputStream src, File destination) {
		boolean resultat = false;
		// Declaration des flux
		java.io.FileOutputStream destinationFile = null;
		try {
			// Création du fichier :
			destination.createNewFile();
			// Ouverture des flux
			
			destinationFile = new java.io.FileOutputStream(destination);
			// Lecture par segment de 0.5Mo
			byte buffer[] = new byte[512 * 1024];
			int nbLecture;
			while ((nbLecture = src.read(buffer)) != -1) {
				destinationFile.write(buffer, 0, nbLecture);
			}
			// Copie réussie
			resultat = true;
		}
		catch (java.io.FileNotFoundException f) {}
		catch (java.io.IOException e) {}
		finally {
			// Quoi qu'il arrive, on ferme les flux
			try {
				src.close();
			}
			catch (Exception e) {}
			try {
				destinationFile.close();
			}
			catch (Exception e) {}
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
	private boolean deleteDirectory(File path) {
		boolean resultat = true;
		if (path.exists()) {
			File[] files = path.listFiles();
			if (null != files) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						resultat &= deleteDirectory(files[i]);
					}
					else {
						resultat &= files[i].delete();
					}
				}
			}
		}
		resultat &= path.delete();
		return (resultat);
	}
	
	/**
	 * These represent libraries that should be packed into the new temporary jar.
	 * @author Marko
	 *
	 */
	enum Library {
		TMP_XLOGO("tmp_xlogo.jar"),
		
		JH("jh.jar"), 
		VECMATH("vecmath.jar"), 
		J3D_CORE("j3dcore.jar"), 
		J3D_UTILS("j3dutils.jar"), 
		JLAYER("jl1.0.1.jar"), 
		LOG4J_API("log4j-api-2.1.jar"), 
		LOG4J_CORE("log4j-core-2.1.jar"), 
		ORG_JSON("org.json-20131017.jar"),
		
		LIN_AMD64_J3D_CORE("linux/amd64/", "libj3dcore-ogl.so"), 
		LIN_X86_J3D_CORE_OGL("linux/x86/", "libj3dcore-ogl.so"), 
		LIN_X86_J3D_CORE_OGL_CG("linux/x86/", "libj3dcore-ogl-cg.so"),
		
		WIN_AMD64_J3D_CORE("windows/amd64/", "j3dcore-ogl.dll"), 
		WIN_X86_J3D_CORE_D3D("windows/x86/", "j3dcore-d3d.dll"), 
		WIN_X86_J3D_CORE_OGL("windows/x86/", "j3dcore-ogl.dll"), 
		WIN_X86_J3D_CORE_CG("windows/x86/", "j3dcore-ogl-cg.dll"), 
		WIN_X86_J3D_CORE_OGL_CHK("windows/x86/", "j3dcore-ogl-chk.dll"), 
		WIN_AMD64_J3D_CORE_OGL("windows/amd64/", "j3dcore-ogl.dll"),
		
		SOL_X86_LIB_J3D_CORE("solaris/i386/", "libj3dcore-ogl.so"), 
		SOL_AMD64_LIB_J3D_CORE("solaris/amd64/","libj3dcore-ogl.so"), ;
		
		private String	path;
		private String	libName;
		
		Library(String jarName) {
			this.libName = jarName;
		}
		
		Library(String path, String libName) {
			this.path = path;
			this.libName = libName;
		}
		
		public String getPath() {
			return path;
		}
		
		public String getLibraryName() {
			return libName;
		}
		
		public String getResourcePath() {
			if (path != null){
				return path + libName;
			} else {
				return libName;
			}
			
		}
		
		public File getFile(File location) {
			return new File(location.getAbsolutePath() + File.separator + libName);
		}
	}
	
	/**
	 * These represent different operating systems and architectures. They provide system specific {@link Library}s
	 * @author Marko
	 *
	 */
	enum OS {
		WINDOWS_AMD64(
				Library.WIN_AMD64_J3D_CORE, 
				Library.WIN_AMD64_J3D_CORE_OGL), 
		WINDOWS_X86(
				Library.WIN_X86_J3D_CORE_D3D, 
				Library.WIN_X86_J3D_CORE_OGL, 
				Library.WIN_X86_J3D_CORE_CG,
				Library.WIN_X86_J3D_CORE_OGL_CHK), 
		LINUX_AMD64(
				Library.LIN_AMD64_J3D_CORE), 
		LINUX_86(
				Library.LIN_X86_J3D_CORE_OGL, 
				Library.LIN_X86_J3D_CORE_OGL_CG), 
		SOLARIS_86(
				Library.SOL_X86_LIB_J3D_CORE), 
		SOLARIS_AMD64(
				Library.SOL_AMD64_LIB_J3D_CORE), 
		MAC, 
		UNKNOWN;
		
		private Library[]	osLibs	= new Library[0];
		
		OS(Library... osLibs) {
			this.osLibs = osLibs;
		}
		
		public Library[] getLibraries() {
			return osLibs;
		}
		
		public static OS valueOf(String os, String arch) {
			if (os.indexOf("linux") != -1) {
				return (arch.indexOf("86") != -1) ? LINUX_86 : LINUX_AMD64;
			}
			else if (os.indexOf("windows") != -1) {
				return (arch.indexOf("86") != -1) ? WINDOWS_X86 : WINDOWS_AMD64;
			}
			else if (os.indexOf("mac") != -1) {
				return MAC;
			}
			else if (os.indexOf("sunos") != -1) {
				if (arch.indexOf("86") != -1) {
					return SOLARIS_86;
				}
				else if (arch.indexOf("amd64") != -1) {
					return SOLARIS_AMD64;
				}
				else {
					return UNKNOWN;
				}
			}
			else {
				return UNKNOWN;
			}
		}
	}
}
