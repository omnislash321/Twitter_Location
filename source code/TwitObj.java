//This class is what collects all the information from tweets

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.FriendsFollowersResources;
import twitter4j.api.HelpResources;
import twitter4j.api.TimelinesResources;
import twitter4j.api.UsersResources;
import twitter4j.conf.ConfigurationBuilder;


public class TwitObj {
	
	private TwitterFactory tf;
	private Twitter twitter;
	private Twitter t;
	private TimelinesResources timeline;
	private FriendsFollowersResources follow;
	private HelpResources help; 
	
	public TwitObj(){
		//The app information.
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("kLQeKzbJzmHwCByLq2eLM4kwZ")
		  .setOAuthConsumerSecret("KZ1iwbO0tjptHn2dThHNGc73zg5L5qYGx6PfMl22tjZmk5GG8u")
		  .setOAuthAccessToken("3177952578-0Aik8Navf620bUNvv5ulbFRx3F5YKrLd4GPar0C")
		  .setOAuthAccessTokenSecret("SY7rNxJTZFwMSZ37pLnbSblhrYdwWR4EWHjQlNXZ0ucK1");
		
		
		tf = new TwitterFactory(cb.build());
		
	
		twitter = tf.getInstance();
		t = tf.getInstance();
		help = twitter.help();
		follow = twitter.friendsFollowers();
		timeline = twitter.timelines();
		
	}
	
	//This will start to print out the user tweets from just a username.
	public void start(String name){
		printUserTweets(name,t);	
	}
	
	public ArrayList<String> getRateStatus(String endpoint){
		ArrayList<String> s = new ArrayList<String>();
		
		try {
			 Map<String, RateLimitStatus> rateLimitStatus = t.getRateLimitStatus();
			 
			 if(rateLimitStatus.containsKey(endpoint)){
				 RateLimitStatus status = rateLimitStatus.get(endpoint);
	             /*
				 System.out.println("Endpoint: " + endpoint);
	             System.out.println(" Limit: " + status.getLimit());
	             System.out.println(" Remaining: " + status.getRemaining());
	             System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
	             System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
				*/
	             s.add("" + status.getRemaining());
	             s.add("" + status.getLimit());
	             s.add("" + status.getSecondsUntilReset());
	             
			 }else{
				 s.add("No endpoint found");
			 }
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
	}
	
	
	//Gets the description, name, and location field of each user.
	public ArrayList<String> getUserInfo(String username){
		UsersResources users = t.users();
		User user;
		
		ArrayList<String> info = new ArrayList<String>();
		
		try {
			user = users.showUser(username);
			info.add(user.getScreenName());
			info.add(user.getDescription());
			info.add(user.getLocation());
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return info;
	}
	
	//Will create the parsed data files.
	public String printUserTweets(String username, Twitter t){
		UsersResources users = t.users();
		User user;

		String filename = "user_"+username+".txt";
		String filename2 = "nlp_"+username+".txt";
		String filename3 = "ngram_"+username+".txt";
		try {
			user = users.showUser(username);
			Tagger tagger = new Tagger();
			tagger.loadModel("model.20120919");
			
	        int numTweets = user.getStatusesCount();
	        timeline = t.timelines();
	        int totalPages = 16;
	        if(numTweets <= 3000){
	        	totalPages = numTweets / 200;
	        	totalPages = totalPages + 1;
	        }
	        
			PrintWriter writer, writer2, writer3;
			try {
				
				writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
				writer2 = new PrintWriter(new BufferedWriter(new FileWriter(filename2)));
				writer3 = new PrintWriter(new BufferedWriter(new FileWriter(filename3)));
		        for(int page = 1; page <= totalPages; page++){
		        	Paging paging = new Paging(page,200);
		            ResponseList<Status> statuses;
					
		            try {
						statuses = timeline.getUserTimeline(username, paging);
				
			            for(int loop = 0; loop < statuses.size(); loop++){
			                String text = statuses.get(loop).getText().replace(',',' ').replace('\n', ' ').replace('\r',' ');
			            	writer.print(text+",");
			            	writer.println("");
			            	
			            	//Uses Ark-nlp to tokenize and tag the text.
			            	
			            	List<TaggedToken> taggedTokens = tagger.tokenizeAndTag(text);
			            	String tags = "";
			            	String tokens = "";
			            	
			            	ArrayList<String> ngrams = new ArrayList<String>();
			            	String curGram = "";
			            	
			            	for(TaggedToken token : taggedTokens){
			            		
			            		tags = tags + token.tag + " ";
			            		tokens = tokens + token.token + " ";
			            		
			            		//This will add multiple ^ tags in a row because it can be more than one word for a noun.
			            		if(token.tag.equals("^")){
			            			curGram = curGram + token.token.replaceAll("[^a-zA-Z ]", "") + " ";
			            		}else{
			            			//If its not a proper noun, then the current gram has ended, so add it and reset.
			            			if(!curGram.isEmpty())
			            				ngrams.add(curGram);
			            			
			            			curGram = "";
			            		}
			            	}
			            	writer2.println(tokens + " " + tags);
			            	
			            	for(int loop2 = 0; loop2 < ngrams.size(); loop2++){
			            		writer3.println(ngrams.get(loop2));
			            	}
			            	
			            	writer.flush();
			            	writer2.flush();
			            	writer3.flush();
			            }
			         			          
					} catch (TwitterException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		        }
		        writer.close();
		        writer2.close();
		        writer3.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (TwitterException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		return filename;
	}
	
	
	//This is used to run the data through the DBPedia gazetteer
	public String sparqlData(String username, String fileRead, boolean append){
		
		FileReader file;
		try {
			file = new FileReader(fileRead);
		
			BufferedReader bFile = new BufferedReader(file);
			
			String filename4 = "dbpedia_"+username+".txt";
			PrintWriter writer4;
			if(append){
				writer4 = new PrintWriter(new BufferedWriter(new FileWriter(filename4, true)));
			}else{
				writer4 = new PrintWriter(new BufferedWriter(new FileWriter(filename4)));
			}
			
			String line = null;			
			while((line = bFile.readLine()) != null) {
				if(line.length() >= 1 ){
					if(line.charAt(0) == '#'){
						line = line.substring(1);
					}
					String[] stringArray = line.split(" ");
					String x = stringArray[0];
					for(int loop = 1; loop < stringArray.length; loop++){
						x += " " + stringArray[loop];
					}
					
					String sparqlQuery = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
			        		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			        		+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
			        		+ "SELECT * WHERE {\n"
			        		+ "?s a dbo:Place .\n"
			        		+ "?s geo:lat ?la .\n"
			        		+ "?s geo:long ?lo .\n"
			        		+ "?s rdfs:label ?name\n"
			        		+ "FILTER( langMatches(lang(?name), \"EN\") && contains(?name,\""+x+"\"))\n"
			        		+ "}\n"
			        		+ "LIMIT 1";
			        
					//System.out.println(sparqlQuery + "\n\n\n");
			        
			        Query q = QueryFactory.create(sparqlQuery);
			        QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
			        ResultSet results = qe.execSelect();
			        while(results.hasNext()){
			        	QuerySolution solution = results.nextSolution();
			        	String lat = solution.getLiteral("?la").toString().replace("^^http://www.w3.org/2001/XMLSchema#float", "");
			        	String lo = solution.getLiteral("?lo").toString().replace("^^http://www.w3.org/2001/XMLSchema#float", "");
			        	String name = solution.getLiteral("?name").toString();
			        	writer4.println(lat + " " + lo + " " + name);
			        	writer4.flush();
			        }
					
				}
			}

			writer4.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "test";
	}
	
	
	
	
	//These down here are not used in the project, but rather for data collection.
	// To get followers and tweets.
	/*
	public void getFollowers(String screenname, String append){
		long cursor = -1;
		
		String filename = "users_"+append+".csv";
		
		try {
			
			do{
				PagableResponseList<User> followers = follow.getFollowersList(screenname,cursor,200);
				
				printFollowers(followers, filename);
				
				try {
					Thread.sleep(60*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				cursor = followers.getNextCursor();
				
			}while (cursor != 0); 
			
		} catch (TwitterException te) {
			te.printStackTrace();
			System.out.println("Failed to get followers: " + te.getMessage());
	        	System.exit(-1);
		}
	}
	
	public String printFollowers(PagableResponseList<User> followers, String filename){

		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
			for(int loop = 0; loop < followers.size(); loop++){
				if(!followers.get(loop).isProtected()){
					String text = followers.get(loop).getDescription().replace(',',' ').replace('\n', ' ').replace('\r',' ');
					
					writer.print(followers.get(loop).getScreenName() + ", ");
					writer.print(followers.get(loop).getId() + ", ");
					writer.print(followers.get(loop).getStatusesCount() + ", ");
					writer.print(followers.get(loop).getName().replace(',', ' ') + ", ");
					writer.print(text + ", ");
					writer.print(followers.get(loop).isGeoEnabled() + ", ");
					writer.print(followers.get(loop).getLocation().replace(',', ' '));
					writer.println("");
				}
			}
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return filename;
	}
	
	public String printTweets(String append){
		String filename = "users_"+append+".csv";
		String filename2 = "tweets_"+append+".csv";
		
		int count = 0;
		
		try {
			FileReader file = new FileReader(filename);
			BufferedReader bFile = new BufferedReader(file);
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename2, true)));
			
			String line = null;			
			while((line = bFile.readLine()) != null) {
                count++;
				
				String[] user = line.split(",");
                String userhead = user[0] + "," + user[1]+",";
                int numTweets = Integer.parseInt(user[2].replaceAll("\\s",""));
                int totalPages = 16;
                if(numTweets <= 3000){
                	totalPages = numTweets / 200;
                	totalPages = totalPages + 1;
                }
                
                for(int page = 1; page <= totalPages; page++){
                	Paging paging = new Paging(page,200);
                    ResponseList<Status> statuses = timeline.getUserTimeline(user[0], paging);
                
                    for(int loop = 0; loop < statuses.size(); loop++){
                        writer.write(userhead);
                        String text = statuses.get(loop).getText().replace(',',' ').replace('\n', ' ').replace('\r',' ');
                        
                    	writer.print(statuses.get(loop).getId()+",");
                    	writer.print(text+",");
                    	writer.print(statuses.get(loop).getCreatedAt()+",");
                    	writer.print("\""+statuses.get(loop).getGeoLocation()+"\",");
                    	writer.print("\""+statuses.get(loop).getPlace()+"\"");
                    	writer.println("");
                    	
                    }
                    
                    try {
        				Thread.sleep(5*1000);
        			} catch (InterruptedException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                }
            }
			bFile.close();
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(append + " : # " + count);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(append + " : # " + count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(append + " : # " + count);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(append + " : # " + count);
		}
		
		return filename2;
	}
	
	
	
	public void printRateStatus(Twitter t){
		
		try {
			 Map<String, RateLimitStatus> rateLimitStatus = t.getRateLimitStatus();
	         for (String endpoint : rateLimitStatus.keySet()) {
	             RateLimitStatus status = rateLimitStatus.get(endpoint);
	             System.out.println("Endpoint: " + endpoint);
	             System.out.println(" Limit: " + status.getLimit());
	             System.out.println(" Remaining: " + status.getRemaining());
	             System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
	             System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
	         }
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void printRateStatus(Twitter t, String endpoint){
		
		try {
			 Map<String, RateLimitStatus> rateLimitStatus = t.getRateLimitStatus();
			 
			 if(rateLimitStatus.containsKey(endpoint)){
				 RateLimitStatus status = rateLimitStatus.get(endpoint);
	             System.out.println("Endpoint: " + endpoint);
	             System.out.println(" Limit: " + status.getLimit());
	             System.out.println(" Remaining: " + status.getRemaining());
	             System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
	             System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
			 }else{
				 System.out.println("Endpoint not found");
				 
			 }
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	*/
	
}