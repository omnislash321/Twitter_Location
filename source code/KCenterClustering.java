//Main clustering algorithm class

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class KCenterClustering {
	private int kValue;
    private ArrayList<Coord> coordinates;
    private ArrayList<Cluster> clusters;
    private ArrayList<Coord> centers;
    
    public KCenterClustering(int k, String filename) {
        kValue = k;
    	
		coordinates = new ArrayList<Coord>();
		centers = new ArrayList<Coord>();
		clusters = new ArrayList<Cluster>(k);
		
		FileReader file;
		try {
			file = new FileReader(filename);

			BufferedReader bFile = new BufferedReader(file);
			String line = null;
			int count = 0;
			while((line = bFile.readLine()) != null) {
				String[] x = line.split(" ");
				Coord c = new Coord(x);
				coordinates.add(c);
				count ++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }
    
    public void reset(){
		centers = new ArrayList<Coord>();
		clusters.clear();
		for(int loop = 0; loop < kValue; loop++)
			clusters.add(new Cluster());
		
		for(int loop = 0; loop < coordinates.size(); loop++)
			coordinates.get(loop).setClustered(false);
    }
    
    public ArrayList<Coord> setCenters(){
    	
    	//farthest-first traversal
    	Random ran = new Random();
    	int numCoords = coordinates.size();
    	int index = ran.nextInt(numCoords);
    	
    	clusters.get(0).setCenter( coordinates.get(index) );
    	coordinates.get(index).setClustered(true);
    	centers.add(coordinates.get(index));
    	
    	for(int loop = 1; loop < clusters.size(); loop++){
    		int z = maxDistance();
    		
    		clusters.get(loop).setCenter( coordinates.get(z) );
    		coordinates.get(z).setClustered(true);
    		centers.add( coordinates.get(z) );
    	}
    	
    	return centers;
    }
    
    public int maxDistance(){
    	//Calculate the maximum min-distance from centers
    	
    	int maxIndex = -1;

    	for(int loop = 0 ; loop < clusters.size(); loop++){
    		double maxDistance = 0;
        	
        	
    		if(clusters.get(loop).hasCenter()){
    			for(int loop2 = 0; loop2 < coordinates.size(); loop2++){
    				if(!coordinates.get(loop2).clustered()){
	    				double curDistance = clusters.get(loop).getCenter().getDistanceMiles( coordinates.get(loop2));
	    				//System.out.println("Distance " + clusters.get(loop).getCenter() + " : " + coordinates.get(loop2) + " = " + curDistance);
	    				if(curDistance > maxDistance){
	    					maxDistance = curDistance;
	    					maxIndex = loop2;
	    				}
    				}
    			}
    		}
    	}
    	
    	return maxIndex;
    }
    
    
    public double setRadius(){
    	//Since we do farthest-first traversal,
    	// The last two cluster centers are the 
    	
    	double maxmax = 0;
    	for(int loop = 0; loop < clusters.size()-1; loop++){
    		double maxDistance = 0;
	    	for(int loop2 = loop+1; loop2 < clusters.size(); loop2++){
				double distance = clusters.get(loop).getCenter().getDistanceMiles(clusters.get(loop2).getCenter());
				if(distance > maxDistance)
					maxDistance = distance;
			}
	    	if(maxDistance > maxmax)
	    		maxmax = maxDistance;
    	}
    	return maxmax/2;
    }
    
    
    public ArrayList<Cluster> cluster(double radius){
    	
    	for(int loop = 0; loop < clusters.size(); loop ++){
    		for(int loop2 = 0; loop2 < coordinates.size(); loop2++){
    			if(!coordinates.get(loop2).clustered()){
    				double distance = clusters.get(loop).getCenter().getDistanceMiles(coordinates.get(loop2));
    				if(distance <= radius){
    					clusters.get(loop).addCoord( coordinates.get(loop2) );
    					coordinates.get(loop2).setClustered(true);
    				}
    			}
    		}
    	}
    	return clusters;
    }
   
    public Coord predictCenter(Cluster c){
    	
    	
    	double x = 0.;
    	double y = 0.;
    	
    	int size = c.getCoords().size();
    	
    	for(int loop = 0; loop < size; loop++){
    		x += c.getCoords().get(loop).getLat();
    		y += c.getCoords().get(loop).getLong();
    	}
    	
    	Coord newCenter = new Coord(x/size, y/size, "NEW CENTER");
    	
    	return newCenter;
    }
    
    public int maxIndex(ArrayList<Cluster> c){
    	int maxIndex = -1;
    	int maxSize = -1;
    	for(int loop = 0; loop < c.size();loop++){
    		if(c.get(loop).getCoords().size() > maxSize){
    			maxSize = c.get(loop).getCoords().size();
    			maxIndex = loop;
    		}
    	}
    	return maxIndex;
    }
    
    public ArrayList<Coord> sparqlHome(Coord c){
		
		double maxLat = c.getLat() + 0.05;
		double minLat = c.getLat() - 0.05;
		
		double maxLong = c.getLong() + 0.05;
		double minLong = c.getLong() - 0.05;
	
		String filter = "&& ?lo > " + minLong + " && ?lo < "+  maxLong + " && ?la > "+ minLat +" && ?la < " + maxLat;
		String sparqlQuery = "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n"
	    		+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
	    		+ "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
	    		+ "SELECT * WHERE {\n"
	    		+ "?s a dbo:Place .\n"
	    		+ "?s geo:lat ?la .\n"
	    		+ "?s geo:long ?lo .\n"
	    		+ "?s rdfs:label ?name\n"
	    		+ "FILTER( langMatches(lang(?name), \"EN\") " + filter + " )\n"
	    		+ "}\n"
	    		+ "LIMIT 20";
	    
		//System.out.println(sparqlQuery + "\n\n\n");
	    
	    Query q = QueryFactory.create(sparqlQuery);
	    QueryExecution qe = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
	    ResultSet results = qe.execSelect();
	   
	    ArrayList<Coord> possibleLocs = new ArrayList<Coord>();
	    possibleLocs.add(new Coord(c.getLat(), c.getLong(), "Predicted Home"));
	    while(results.hasNext()){
	    	QuerySolution solution = results.nextSolution();
	    	String lat = solution.getLiteral("?la").toString().replace("^^http://www.w3.org/2001/XMLSchema#float", "");
	    	String lo = solution.getLiteral("?lo").toString().replace("^^http://www.w3.org/2001/XMLSchema#float", "");
	    	String name = solution.getLiteral("?name").toString();
	    	
	    	double la = Double.parseDouble(lat);
	    	double lng = Double.parseDouble(lo);
	    	Coord co = new Coord(la, lng, name);
	    	
	    	possibleLocs.add(co);
	    }
		return possibleLocs;
	}
}