//A simple object to keep track of coordinates.

public class Coord {
	//Has latitude, longitude for coordinates.
	private double latitude;
	private double longitude;
	
	//Label is for the name.
	public String label;
	
	// This boolean will check whether or not it is already in a cluster.
	private boolean inCluster;
	
	//Generic constructor.
	public Coord(){
		latitude = 0;
		longitude = 0;
		label = "No name!";
		inCluster = false;
	}
	
	
	//This constructor accepts an array of String.
	//Index 0 : latitude
	//Index 1 : longitude
	//Index 2 : name
	public Coord(String[] x){
		latitude = Double.parseDouble(x[0]);
		longitude = Double.parseDouble(x[1]);
		label = x[2];
		
		for(int loop = 3; loop < x.length; loop++){
			label += " " + x[loop];
		}
		
		inCluster = false;
	}
	
	//This constructor accepts everything in proper format.
	public Coord(double x, double y, String z){
		latitude = x;
		longitude = y;
		label = z;
		inCluster = false;
	}
	
	//Getters
	public double getLat(){
		return latitude;
	}
	
	public double getLong(){
		return longitude;
	}
	
	public String getLabel(){
		return label;
	}
	
	public boolean clustered(){
		return inCluster;
	}
	
	//Setters
	public void setLat(double x){
		latitude = x;
	}
	
	public void setLong(double x){
		longitude = x;
	}
	
	public void setLabel(String x){
		label = x;
	}
	
	public void setClustered(boolean x){
		inCluster = x;
	}
	
	//Returns the distance in miles using the spherical law of cosines.
	public double getDistanceMiles(Coord y){
		double R = 3959; //in Miles
		
		double lat1 = Math.toRadians(latitude);
		double lat2 = Math.toRadians(y.getLat());
		
		
		double theta = Math.toRadians(longitude - y.getLong());
		
		double dist = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta);
		
		dist = Math.acos(dist) * R;

		return dist;
	}
	

	//The Haversine Formula. Great Circle method.
	//Unused, but could be a possible way of comparison.
	/*
	public double getDistanceMiles2(Coord y){
		double lat1 = latitude;
		double lat2 = y.getLat();
		
		double lon1 = longitude;
		double lon2 = y.getLong();
		
	    double earthRadius = 3963; //In miles
	    double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        
	    
	    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
	    double dist = earthRadius * c;
	    
	    return dist;
	}
	*/
	//overides the toString function, so it's easier to print out.
	public String toString(){
		return this.getLat() + " " + this.getLong() + " " + this.getLabel();
		
	}
}
