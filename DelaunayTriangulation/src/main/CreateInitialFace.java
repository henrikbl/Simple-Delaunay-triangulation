package main;

import java.util.ArrayList;

import model.AbstractNode;
import model.Face;
import model.HalfEdge;
import model.Node;

/**
 * Creates an initialface which will be used
 * to create a face that contains all the points
 * in the pointset.
 * @author hen_b
 *
 */
public class CreateInitialFace {

	private static AbstractNode leftUp, leftDown, rightUp, rightDown;
	private static AbstractNode topNode, leftNode, rightNode;
	private static ArrayList<Node> nodes = new ArrayList<Node>(Triangulation.getNodes());
	
	/*
	 * Create an initialFace that contains all the nodes in the pointset
	 * by using the AbstractNodes created in the createBoxPoints method.
	 * 
	 */
	public static void InitialFace() {	
		createBoxPoints();
		Triangulation triangulation = new Triangulation();
		
		// sides
		float a, b, c;
		// angles
		float A, B, C;
		
		a = rightUp.getXCoordinate()-leftUp.getXCoordinate();
		b = (rightUp.getXCoordinate()-leftUp.getXCoordinate())/2;
		c = (float)Math.sqrt(Math.pow(a, 2)-(Math.pow(b, 2)));
		
		// floor c to avoid decimal-irregularities when calculating Y-coordinate for the top node.
		float floorC = (float)Math.floor(c);	
		topNode = new AbstractNode(leftUp.getXCoordinate()+b, leftUp.getYCoordinate()+floorC);
		
		A = 90.0f;
		B = (float)Math.toDegrees(Math.asin((Math.sin(Math.toRadians(A))/a)*b));
		C = (float)(180)-(A+B);
		
		c = topNode.getYCoordinate()-leftDown.getYCoordinate();
		a = (float)((c/Math.sin(Math.toRadians(C)))*Math.sin(Math.toRadians(A)));
		b = (float)((a/Math.sin(Math.toRadians(A)))*Math.sin(Math.toRadians(B)));
		
		float tempHalf = (rightUp.getXCoordinate()-leftUp.getXCoordinate())/2;
		float tempMiddlePoint = leftUp.getXCoordinate()+tempHalf;
		
		// floor b to avoid decimal-irregularities when calculating X-coordinate for left and right node.
		float floorB = (float) Math.floor(b);		
		leftNode = new AbstractNode((tempMiddlePoint-floorB), leftDown.getYCoordinate());
		rightNode = new AbstractNode((tempMiddlePoint+floorB), leftDown.getYCoordinate());
		
		
		/**
		 * creates the initial edges and face.
		 * sets the sequence of edges in a counterclockwise sequence.
		 */
		triangulation.addToNode(topNode);
		triangulation.addToNode(leftNode);
		triangulation.addToNode(rightNode);
		
		HalfEdge top = new HalfEdge(topNode);
		HalfEdge topTwin = new HalfEdge(leftNode);
		top.setTwin(topTwin);
		topTwin.setTwin(top);
		
		Face initialFace = new Face(top);
		top.setFace(initialFace);
		
		triangulation.addToHalfEdges(top);
		triangulation.addToHalfEdges(topTwin);
		triangulation.addToFaces(initialFace);
		
		
		HalfEdge left = new HalfEdge(leftNode);
		HalfEdge leftTwin = new HalfEdge(rightNode);
		left.setTwin(leftTwin);
		leftTwin.setTwin(left);
		left.setFace(initialFace);
		
		triangulation.addToHalfEdges(left);
		triangulation.addToHalfEdges(leftTwin);
		
		
		HalfEdge right = new HalfEdge(rightNode);
		HalfEdge rightTwin = new HalfEdge(topNode);
		right.setTwin(rightTwin);
		rightTwin.setTwin(right);
		right.setFace(initialFace);
		
		triangulation.addToHalfEdges(right);
		triangulation.addToHalfEdges(rightTwin);
		
		top.setNext(left);
		left.setNext(right);
		right.setNext(top);
	}

	/**
	 * Creates a box which covers all the nodes in the pointset
	 * by finding the min/max XY values in the pointset.
	 */
	private static void createBoxPoints() {
		float minX = nodes.get(0).getXCoordinate();
		float minY = nodes.get(0).getYCoordinate();
		float maxX = nodes.get(0).getXCoordinate();
		float maxY = nodes.get(0).getYCoordinate();
		
		for(int i = 0; i < nodes.size(); i++) {
			if(minX > nodes.get(i).getXCoordinate()) {
				minX = nodes.get(i).getXCoordinate();
			}
			
			if(minY > nodes.get(i).getYCoordinate()) {
				minY = nodes.get(i).getYCoordinate();
			}
			
			if(maxX <= nodes.get(i).getXCoordinate()) {
				maxX = nodes.get(i).getXCoordinate();
			}
			
			if(maxY <= nodes.get(i).getYCoordinate()) {
				maxY = nodes.get(i).getYCoordinate();
			}
		}
		
		leftUp = new AbstractNode(minX-2, maxY+2);
		leftDown = new AbstractNode(minX-2, minY-2);
		rightDown = new AbstractNode(maxX+2, minY-2);
		rightUp = new AbstractNode(maxX+2, maxY+2);
	}
}
