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

/**
 * Title :        XLogo
 * Description :  XLogo is an interpreter for the Logo 
 * 						programming language
 * @author Loïc Le Coq
 */
package xlogo.gui;

import java.awt.print.*;
import javax.swing.*;

import java.awt.Image;
import java.awt.Graphics;

public class AImprimer extends JPanel implements Printable, Runnable {
	private static final long serialVersionUID = 1L;
	private Image image;

	public AImprimer(Image image) {
		this.image = image;
	}

	public void run() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPrintable(this);
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException ex) {
				System.out.println(ex.getMessage());
			}
		}
	}

	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		double largeur = image.getWidth(this);
		double hauteur = image.getHeight(this);
		double facteur = pf.getImageableWidth() / largeur; // largeur
															// imprimable sur la
															// feuille
		double facteur2 = pf.getImageableHeight() / hauteur; // hauteur
																// imprimable
																// sur la
																// feuille

		if (facteur < 1 | facteur2 < 1) {
			facteur = Math.min(facteur, facteur2);
			image = image.getScaledInstance((int) (largeur * facteur),
					(int) (hauteur * facteur), Image.SCALE_SMOOTH);
		}
		largeur = image.getWidth(this); // permet d'attendre que l'image soit
										// bien créée
		hauteur = image.getHeight(this);
		if (pi < 1) {
			g.drawImage(this.image, (int) pf.getImageableX(), (int) pf
					.getImageableY(), this);
			return (Printable.PAGE_EXISTS);
		} else
			return Printable.NO_SUCH_PAGE;
	}
}