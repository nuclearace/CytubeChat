package com.milkbartube.tracy;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class CytubeChat {

    public static void main(String args[]) {

	String os = System.getProperty("os.name");
	System.out.println("Starting CytubeChat");
	if (os.equals("Mac OS X")) {
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    try {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch (ClassNotFoundException | InstantiationException
		    | IllegalAccessException | UnsupportedLookAndFeelException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	new ChatFrame();
    }
}
