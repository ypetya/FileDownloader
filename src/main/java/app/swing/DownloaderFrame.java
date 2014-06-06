package app.swing;
import java.awt.HeadlessException;

import javax.swing.JFrame;


public class DownloaderFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	DownloaderPanel panel= null;

	public DownloaderFrame() throws HeadlessException {
		super();
		setTitle("File downloader");
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    panel = new DownloaderPanel();
	    add(panel);
	    setSize(600, 430);
	    //pack();
	}

}
