package model;

/**
 * The node class creates objects which contains 
 * the XYZ-coordinates of a node.
 * @author hen_b
 *
 */
public class Node {
	
	private float xCoordinate;
	private float yCoordinate;
	private float zCoordinate;
	
	public Node(float xCoordinate, float yCoordinate) {
		super();
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}
	
	@Override
	public String toString() {
		return "(" + xCoordinate + "," + yCoordinate +")";
	}
		
	public float getXCoordinate() {
		return xCoordinate;
	}
	
	public float getYCoordinate() {
		return yCoordinate;
	}
	
	public void setZCoordinate(float z) {
		this.zCoordinate = z;
	}
	
	public float getZCoordinate() {
		return zCoordinate;
	}
}
