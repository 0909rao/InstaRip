package com.insta.rip;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainFrame extends JFrame {

	public final static int BUFFER_SIZE = 9 * 1024;
	
	public static String folder = null;
	public final static String SYSTEM = System.getProperty("user.home")+"\\Downloads\\Instagram\\";


	public JPanel main = new JPanel();

	public JTextField link = new JTextField(50);

	public JPanel content = new JPanel();
	public JLabel infolabel = new JLabel();
	public JButton downloadbtn = new JButton("Download Media");
	
	public ArrayList<String> imagelist = new ArrayList<String>();
	public ArrayList<String> videolist = new ArrayList<String>();
	
	public MainFrame() throws MalformedURLException {

		setTitle("Insta Rip by tt.exe");
		setSize(700, 500);
		setLayout(new BorderLayout());
		main = new JPanel();
		link = new JTextField(50);
		main.add(link,BorderLayout.NORTH);
		
		infolabel = new JLabel("Please insert an instagram post URL");
		downloadbtn = new JButton("Download Media");
		content = new JPanel();
		content.add(infolabel);
		content.add(downloadbtn);
		add(content,BorderLayout.SOUTH);
		add(main);

		downloadbtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				infolabel.setText("Fetching Files...");
				Thread t1 = new Thread(new Runnable() {
					@Override
					public void run() {
						String url = link.getText();
						String html = getHtml(url.trim());
						imagelist = getImages(html);
						videolist = getVideos(html);
						DownloadMedia(imagelist,videolist);
					}
					
					
				});
				
				t1.start();
				
				Thread t2 = new Thread(new Runnable() {
					@Override
					public void run() {
						int a = 0;
						while(t1.isAlive()) {
							switch(a%3) {
							case 0: infolabel.setText("Fetching Files.");break;
							case 1: infolabel.setText("Fetching Files..");break;
							case 2: infolabel.setText("Fetching Files...");break;
							default :infolabel.setText("Fetching Files"); break;
							}
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							a++;
						}				
						infolabel.setText("Done...");
						try {
							Desktop.getDesktop().open(new File(folder));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				});
				t2.start();
			}
		});
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	public Image getScaledImage(Image srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();
	    return resizedImg;
	}

	public static String getHtml(String url) {
		
		String html = null;
		
		URL link = null;
		try {
			link = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
        BufferedReader in = null;
		try {
			in = new BufferedReader(
			new InputStreamReader(link.openStream(),"UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
        String inputLine;
        try {
			while ((inputLine = in.readLine()) != null)
			    html +=inputLine;
	       
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return html;
	}
	
	public static ArrayList<String> getImages(String html) {
		Pattern p = Pattern.compile("\"display_url\":\"(.+?)\\.jpg?\",\"d");
		Matcher m = p.matcher(html);
		ArrayList<String> imagelist = new ArrayList<String>();
		while(m.find()) {
			String path = m.group(1)+".jpg";
			System.out.println(path);
				imagelist.add(path);
		}
		if(imagelist.size()>1) {
			imagelist.remove(0);
		}
			return imagelist;
	}

	public static ArrayList<String> getVideos(String html) {
		Pattern p = Pattern.compile("\"video_url\":\"(.+?)\\.mp4?\",\"");
		Matcher m = p.matcher(html);
		ArrayList<String> videolist = new ArrayList<String>();
		while(m.find()) {
			String path = m.group(1)+".mp4";
			System.out.println(path);
			videolist.add(path);
		}	
		return videolist;
	}

	protected void DownloadMedia(ArrayList<String> imagelist, ArrayList<String> videolist) {
		System.out.println(folder);
		do {
			folder = generateName();
			folder = SYSTEM+folder;
		}while(Files.exists(Paths.get(folder)));
		folder +="\\";
		new File(folder).mkdirs();
		int i=0;
		if(imagelist!=null&&!imagelist.isEmpty()) {
			for(String url : imagelist) {
				DownloadImage(url,folder+"image "+i+".jpg");
				i++;
			}
		}
		
		if(videolist!=null&&!videolist.isEmpty()) {
			for(String url : videolist) {
				DownloadVideo(url,folder+"video "+i+".mp4");
				i++;
			}
		}
	}	


	public static void DownloadImage(String url, String out) {
		// TODO Auto-generated method stub
		BufferedImage img = null;
		try {
			img = ImageIO.read(new URL(url));
			File outputfile = new File(out);
			ImageIO.write(img, "jpg", outputfile);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void DownloadVideo(String url, String path) {
		InputStream is = null;
		URLConnection conn;
		BufferedOutputStream outStream = null;
		try {
	        URL link;
	        byte[] buf;
	        int byteRead, byteWritten = 0;
	        link = new URL(getFinalLocation(url));
	        outStream = new BufferedOutputStream(new FileOutputStream(path));
	        conn = link.openConnection();
	        is = conn.getInputStream();
	        buf = new byte[BUFFER_SIZE];
	        while ((byteRead = is.read(buf)) != -1) {
	            outStream.write(buf, 0, byteRead);
	            byteWritten += byteRead;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	            outStream.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
		
	public static String getFinalLocation(String address) throws IOException{
	    URL url = new URL(address);
	    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	    int status = conn.getResponseCode();
	    if (status != HttpURLConnection.HTTP_OK) 
	    {
	        if (status == HttpURLConnection.HTTP_MOVED_TEMP
	            || status == HttpURLConnection.HTTP_MOVED_PERM
	            || status == HttpURLConnection.HTTP_SEE_OTHER)
	        {
	            String newLocation = conn.getHeaderField("Location");
	            return getFinalLocation(newLocation);
	        }
	    }
	    return address;
	}

	public String generateName() {
		// TODO Auto-generated method stub
		Random r = new Random();
		char[] str = new char[9];
		for(int i=0;i<9;i++) {
			str[i] = (char) (97+r.nextInt(26));
		}
		return new String(str);
	}

	public static void main(String[] args) throws MalformedURLException {
		new MainFrame();
	}
	
}