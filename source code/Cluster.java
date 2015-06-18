//This object will keep track of each cluster.

//It will keep track of the center, coordinates, and also if it has a center or not.
import java.util.ArrayList;

public class Cluster {
	
	//No constructor because everything will start out empty.
    private Coord center = new Coord();
    private ArrayList<Coord> coordinates = new ArrayList<Coord>();
    private boolean centerBool = false;
    
    //Getters
    public Coord getCenter(){
    	return center;
    }
    
    public ArrayList<Coord> getCoords(){
    	return coordinates;
    }
    
    public boolean hasCenter(){
    	return centerBool;
    }
    
    //Setters
    public void setCenter(Coord c){
    	center = c;
    	centerBool = true;
    	coordinates.add(c);
    }
    
    public void setCoords(ArrayList<Coord> c){
    	coordinates = c;
    }
    
    public void setBoolCenter(boolean b){
    	centerBool = b;
    }
    
    //Adds a coordinate to the coordinates array.
    public void addCoord(Coord c){
    	coordinates.add(c);
    }
}
