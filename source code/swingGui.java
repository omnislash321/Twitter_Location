import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class swingGui implements ActionListener{
	private JLabel rateTweet, rateTime, text1, text2;
	private JTextField username;
	private JButton submitUser, continueButton;
	private TwitObj twit;
	private JFrame frame;
	private JPanel panel;
	private String user, s;
	private JTextArea area1, area2;
	private JScrollPane scroll1, scroll2;
	
	public swingGui(){
		twit = new TwitObj();
		
		frame = new JFrame("Twitter GeoLocation");
		frame.setVisible(true);
		frame.setSize(1200, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel(new GridBagLayout());
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0,0,5,5);
		
		
		JLabel apiRateTitle = new JLabel("Twitter API Rate Limits");
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 5;
		panel.add(apiRateTitle, c);
		
		rateTime = new JLabel("");
		c.gridy = 1;
		panel.add(rateTime, c);
		
		rateTweet = new JLabel("");
		c.gridy = 2;
		
		panel.add(rateTweet, c);
		
		JLabel userLabel = new JLabel("Enter Twitter Username: ");
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		panel.add(userLabel, c);
		
		username = new JTextField(20);
		c.gridx = 2;
		c.gridy = 3;
		panel.add(username, c);
		
		submitUser = new JButton("Submit");
		submitUser.addActionListener(this);
		c.gridx = 4;
		c.gridy = 3;
		c.gridwidth = 1;
		panel.add(submitUser,c);
		
		text1 = new JLabel("");
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 2;
		panel.add(text1, c);
		
		text2 = new JLabel("");
		c.gridx = 2;
		c.gridy = 4;
		c.gridwidth = 2;
		panel.add(text2, c);
		
		area1 = new JTextArea(20,30);
		
		area2 = new JTextArea(20,30);
		scroll1 = new JScrollPane(area1);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 2;
		panel.add(scroll1, c);
		
		scroll2 = new JScrollPane(area2);
		c.gridx = 2;
		c.gridy = 5;
		c.gridwidth = 2;
		panel.add(scroll2, c);
		
		continueButton = new JButton("");
		continueButton.addActionListener(this);
		c.gridx = 4;
		c.gridy = 5;
		c.gridwidth = 1;
		panel.add(continueButton,c);
		
		updateRates();
	}
	
	public void updateRates(){
		ArrayList<String> timeline = twit.getRateStatus("/statuses/user_timeline");
		ArrayList<String> user = twit.getRateStatus("/users/show/:id");
		
		String rate1 = "User: " + user.get(0) + "/" + user.get(1) + " TimeLeft: " + user.get(2) + "s"; 
		String rate2 = "Tweets: " + timeline.get(0) + "/" + timeline.get(1) + " TimeLeft: " + timeline.get(2)+"s";
		
		rateTime.setText(rate1);
		rateTweet.setText(rate2);
	}
	
	public void startTwitter(String name){
		
		twit.start(name);

	}
	
	public void dataPanelLoad(String s){
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		
		
		text1.setText("Raw Data");
		text2.setText("Parsed Data");
		
		
		JLabel userinfo = new JLabel("");
		area1.setText("");
		
		FileReader file;
		try {
			file = new FileReader("nlp_"+user+".txt");
		
			BufferedReader bFile = new BufferedReader(file);
			
			String line = null;			
			int count = 1;
			while((line = bFile.readLine()) != null) {
				area1.append(count + ". " + line+"\n");
				count++;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		area2.setText("");
		
		try {
			file = new FileReader("ngram_"+user+".txt");
		
			BufferedReader bFile = new BufferedReader(file);
			
			String line = null;	
			int count = 1;
			while((line = bFile.readLine()) != null) {
				area2.append(count + ". " + line+"\n");
				count ++;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		continueButton.setText("Next Step");
	
		JTextArea userInfo = new JTextArea(s, 3, 30);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 5;
		panel.add(userInfo,c);
		
		panel.revalidate();
		panel.repaint();
	}
	
	public void dbpediaLoad(boolean alreadyCreated){
		if(!alreadyCreated){
			twit.sparqlData(user, "ngram_"+user+".txt", false);
		}
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		
		
		text1.setText("Coordinates");
		
		text2.setText("Possible Locations");
		
		
		area1.setText("");
		
		FileReader file;
		try {
			file = new FileReader("dbpedia_"+user+".txt");
		
			BufferedReader bFile = new BufferedReader(file);
			
			String line = null;			
			int count = 1;
			while((line = bFile.readLine()) != null) {
				area1.append(count + ". " + line+"\n");
				count++;
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		KCenterClustering kcenter = new KCenterClustering(4, "dbpedia_"+user+".txt");
		
		ArrayList<ArrayList<Cluster>> clusterArray = new ArrayList<ArrayList<Cluster>>();
		
		// Getting the least sum squared error
		int leastSumIndex = 0;
		double leastSum = Double.MAX_VALUE;
		for(int iteration = 0; iteration < 500; iteration++){
			kcenter.reset();
			
			ArrayList<Coord> centers = kcenter.setCenters();
			double radius = kcenter.setRadius();
			ArrayList<Cluster> clusters = kcenter.cluster(radius);
			clusterArray.add(clusters);
			double sum = 0;
			
			for(int loop = 0; loop < clusters.size(); loop++){
				Coord center = kcenter.predictCenter(clusters.get(loop));
				
				for(int loop2 = 0; loop2 < clusters.get(loop).getCoords().size(); loop2++){
					Coord b = clusters.get(loop).getCoords().get(loop2);
					double dist = center.getDistanceMiles(b);
					dist = dist * dist;
					sum += dist;
				}
			}
			if(sum < leastSum){
				leastSumIndex = iteration;
				leastSum = sum;
			}
		}
		//System.out.println(leastSum + " " + leastSumIndex);
		
		//Using the least sum squared error as the best cluster.
		ArrayList<Cluster> bestCluster = clusterArray.get(leastSumIndex);
		
		int maxIndex = kcenter.maxIndex(bestCluster);
		ArrayList<Coord> possible = kcenter.sparqlHome( kcenter.predictCenter(bestCluster.get(maxIndex)) );
		
		area2.setText("");
		for(int loop = 0; loop < possible.size(); loop++){
			int count = loop+1;
			area2.append(count + ". "+possible.get(loop) + "\n");
		}
		
		
		continueButton.setText("Reload");
		
		
		
		panel.revalidate();
		panel.repaint();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
		if(e.getSource() == submitUser){
			if(!username.getText().isEmpty()){
				user = username.getText();
				startTwitter(user);
				ArrayList<String> info = twit.getUserInfo(user);
				s = "Username: "+ info.get(0) + "\n"
						+ "Description: " + info.get(1) + "\n"
						+ "Location: " + info.get(2);
				updateRates();
				dataPanelLoad(s);
				
			}
		}else if(e.getSource() == continueButton){
			
			String currentdir = System.getProperty("user.dir");
			File directory = new File(currentdir);
			boolean alreadyCreated = false;
			if(directory.isDirectory()){
				for (File f :  directory.listFiles()){
					if(f.getName().startsWith("dbpedia_"+user) )
						alreadyCreated = true;
				}
			}

			dbpediaLoad(alreadyCreated);
			updateRates();
			//System.out.println("" + alreadyCreated);
		}
	}
}
