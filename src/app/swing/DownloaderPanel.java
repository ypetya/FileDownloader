package app.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class DownloaderPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** 9--> DEBUG, 1--> normal */
	private static final int LOG_LEVEL = 8;
	// we need this for scrollpane :(
	JPanel panel;

	JTextField url = new JTextField("enter_url_here", 50);
	JTextField regex = new JTextField("href=[\"'][^\"']*\\.(mp3|ogg|avi|inf|nfo|jpg|png|gif)[^\"']*[\"']", 50);
	JButton okButton = new JButton("OK");
	JButton cancelButton = new JButton("Cancel");
	JProgressBar progress = new JProgressBar(0, 100);
	JScrollPane sp = null;
	JTextArea output = new JTextArea(18, 50);
	Downloader downloader = null;

	public DownloaderPanel() {
		super();
		panel = new JPanel(new FlowLayout());
		add(url);
		add(regex);
		okButton.addActionListener(new OkButtonAction());
		add(okButton);
		cancelButton.addActionListener(new CancelButtonAction());
		add(cancelButton);
		add(progress);
		panel.add(output);
		sp = new JScrollPane(panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp);
	}

	private class Downloader implements Runnable {

		private boolean success = false;
		private StringBuilder sb = null;
		List<String> links = null;

		private void reset() {
			progress.setValue(0);
			output.append("Started for url : " + url.getText() + " matching links with " + regex.getText() + "\n");
			success = false;
			sb = new StringBuilder();
			links = new ArrayList<String>();
		}

		private void downloadMainPage() {
			InputStreamReader reader = null;
			try {
				reader = new InputStreamReader(new URL(url.getText()).openStream());
				BufferedReader bufferedReader = new BufferedReader(reader);
				String s;
				while ((s = bufferedReader.readLine()) != null) {
					sb.append(s);
					log(s,8);
				}
			}// On exceptions, print error message and usage message.
			catch (Exception e) {
				log(e.getMessage(),0);
				success = false;
			} finally { // Always close the streams, no matter what.
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {

				}
			}
			progress.setValue(5);
		}

		private void getTheLinks() {
			try {
				Pattern p = Pattern.compile(regex.getText(), Pattern.CASE_INSENSITIVE);
				// this is my link matcher
				Matcher tagmatch = p.matcher(sb.toString());

				while (tagmatch.find()) {
					StringBuilder linkLog = new StringBuilder();
					linkLog.append(tagmatch.group() + " : ");
					linkLog.append(" index: " + tagmatch.start());
					linkLog.append("-" + tagmatch.end());
					log(linkLog.toString(),8);
					String link = "";
					try {
						link = tagmatch.group().replaceFirst("href=[\"']", "").replaceFirst("[\"'].*$", "");
						String validLink = (new URL(makeAbsolute(url.getText(), link))).toString();
						links.add(validLink);
						log("valid link : " + validLink,5);
					} catch (Exception e) {
						log("not valid link : " + link,7);
					}
				}
			} catch (Exception e) {
				log(e.getMessage(),1);
				success = false;
			}
			progress.setValue(10);
		}

		private String makeAbsolute(String url, String link) {
			if (link.matches("http://.*")) {
				return link;
			}
			if (link.matches("/.*") && url.matches(".*$[^/]")) {
				return url + "/" + link;
			}
			if (link.matches("[^/].*") && url.matches(".*[^/]")) {
				return url + "/" + link;
			}
			if (link.matches("[^/].*") && url.matches(".*[/]")) {
				return url + link;
			}
			if (link.matches("/.*") && url.matches(".*[/]")) {
				return url + link;
			}
			if (link.matches("/.*") && url.matches(".*[^/]")) {
				return url + link;
			}
			throw new RuntimeException("Cannot make the link absolute. Url: " + url + " Link " + link);
		}
		
		private void download(String urlInput, String fileOutput) {
			   InputStream in = null;   
		        OutputStream out = null;
		        try {
		            // Check the arguments
		            if (urlInput == null || urlInput.length() == 0) 
		                throw new IllegalArgumentException("Wrong input");
		            
		            // Set up the streams
		            URL url = new URL(urlInput);   // Create the URL
		            in = url.openStream( );        // Open a stream to it
		          
		            String fileName = url.getFile();
		            while(fileName.matches(".*/.*")) {
		            	fileName = fileName.replaceFirst(".*[/]", "");
		            }		            
		            
		            out = new FileOutputStream(fileOutput + "/" + fileName);
		            		            
		            // Now copy bytes from the URL to the output stream
		            byte[  ] buffer = new byte[4096];
		            int bytes_read;
		            while((bytes_read = in.read(buffer)) != -1)
		                out.write(buffer, 0, bytes_read);
		        }
		        // On exceptions, print error message and usage message.
		        catch (Exception e) {
		           log(e.getMessage(),2);
		        }
		        finally {  // Always close the streams, no matter what.
		            try { in.close( );  out.close( ); } catch (Exception e) {  }
		        }
		}

		@Override
		public void run() {
			reset();

			downloadMainPage();
			getTheLinks();
			log("got " + links.size() + " new links to download.",3);
			if(links.size() > 0 ) {
				JFileChooser f = new JFileChooser( new File("."));
				f.setDialogTitle("choose a directory, to save files!");
				f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				f.setAcceptAllFileFilterUsed(false);
				if(f.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION ) {
					double delta = (double)100 / links.size();
					int n = 1;
					for(String link : links) {
						log("downloading " + link,3);
						download(link, f.getSelectedFile().getAbsolutePath());
						progress.setValue((int)(delta*n++));
					}
					success = true;
				}
				else { success = false; }
			}

			log(success ? "completed successfuly." : "there was an error in execution.",0);

			okButton.setEnabled(true);
			progress.setValue(100);
		}

		private void log(String text, int level) {
			if (level < LOG_LEVEL)
				output.append(" > " + text + "\n");
		}

	}

	private class CancelButtonAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}

	}

	private class OkButtonAction implements ActionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			okButton.setEnabled(false);
			if (downloader == null)
				downloader = new Downloader();
			new Thread(downloader).start();
		}

	}
}
