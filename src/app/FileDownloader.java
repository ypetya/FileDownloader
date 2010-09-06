package app;

import java.awt.EventQueue;

import app.swing.DownloaderFrame;

public class FileDownloader {

	private void launchPanel() {
		EventQueue.invokeLater(new Runnable(){
			@Override
			public void run() {
				DownloaderFrame df = new DownloaderFrame();
				df.setVisible(true);
			}
		});
	}

	public static void main(String[] args) {
		FileDownloader fd = new FileDownloader();
		fd.launchPanel();
	}
}
