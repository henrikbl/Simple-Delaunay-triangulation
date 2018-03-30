package main;

import java.util.ArrayList;
import model.*;

public class Triangulation {

	private static ArrayList<Node> nodes = new ArrayList<Node>();
	private static ArrayList<HalfEdge> halfEdges = new ArrayList<HalfEdge>();
	private static ArrayList<Face> faces = new ArrayList<Face>();
	
	// support lists
	private static ArrayList<Node> nodeList = new ArrayList<Node>();	
	private static ArrayList<Integer> elementData = new ArrayList<Integer>();
	
	public void main() {
		Triangulation triangulation = new Triangulation();

		// creates nodes by iterating through the pointCloud.pts file.
		PointCloudReader pcr = new PointCloudReader();
    	pcr.readFile();
		
    	// creates the initialface before triangulating.
		CreateInitialFace.InitialFace();
		triangulation.populateNodeList();
		
		// inserts nodes in the trianguletion
		for(int i = 0; i < nodeList.size(); i++) {
			triangulation.insertNewNode(nodeList.get(i));
		}
		
		// creates the elementdata to be used by MinimalJOGLSimple for graphic viewing.
		for(int i = 0; i < faces.size(); i++) {
			triangulation.createElementData(faces.get(i));
		} 
	}
	
	/**
	 * Inserts a new node into the triangulation.
	 * Finds which face that contains the new node
	 * and creates new halfedges and faces.
	 * calls swapTest method to check the Delaunay requirements.
	 * @param n
	 */
	public void insertNewNode(Node n) {
		Face contains = null;
		HalfEdge entry = null;
		HalfEdge next = null;
		HalfEdge last = null;
		
		for(int i = 0; i < faces.size(); i++) {
			if(faceContainsNode(faces.get(i), n)) {
				contains = faces.get(i);
				break;	
			}
		}
		
		entry = contains.getEdge();
		next = contains.getEdge().getNext();
		last = contains.getEdge().getNext().getNext();
		
		HalfEdge h1 = createNewEdge(n, entry);
		HalfEdge h2 = createNewEdge(n, next);
		HalfEdge h3 = createNewEdge(n, last);
		
		createFace(h1, entry, h2.getTwin());
		createFace(h2, next, h3.getTwin());
		createFace(h3, last, h1.getTwin());
		
		for(int i = 0; i < faces.size(); i++) {
			if(faces.get(i) == contains) {
				faces.remove(i);
			}
		}
		
		swapTest(entry);
		swapTest(next);
		swapTest(last);
	}
	
	/**
	 * Test the Delaunay requirements by checking 
	 * if a Node is inside the circumcircle of a triangle.
	 * If it is edge will be flipped.
	 * @param h
	 * @param twin
	 */
	public void swapTest(HalfEdge h) {		
		if(h.getOrigin().getClass() == AbstractNode.class && h.getTwin().getOrigin().getClass() == AbstractNode.class) {
			return;
		}
		
		if(h.getTwin().getNext().getNext().getOrigin().getClass() == AbstractNode.class) {
			return;
		}
		
		if(inCircle(h.getNext().getNext().getOrigin(), h.getOrigin(), h.getNext().getOrigin(), h.getTwin().getNext().getNext().getOrigin())) {
			HalfEdge hNext = h.getNext();
			HalfEdge twinNext = h.getTwin().getNext();
			
			// sets new origin nodes for h and h.twin and new next for both.	
			h.setOrigin(h.getTwin().getNext().getNext().getOrigin());
			h.setNext(h.getNext().getNext());
			
			h.getTwin().setOrigin(h.getNext().getOrigin());
			h.getTwin().setNext(h.getTwin().getNext().getNext());
						
			// sets new h.next.next and sets correct face reference.
			h.getNext().setNext(twinNext);
			twinNext.setFace(h.getFace());
			twinNext.setNext(h);
			
			// sets new h.twin.next.next and sets correct face reference.
			h.getTwin().getNext().setNext(hNext);
			hNext.setFace(h.getTwin().getFace());
			hNext.setNext(h.getTwin());
			
			// sets h.face.edge to h and twin.face.edge to h.twin to avoid referencing bugs.
			h.getFace().setEdge(h);
			h.getTwin().getFace().setEdge(h.getTwin());
			
			swapTest(h.getNext().getNext());
			swapTest(h.getTwin().getNext());
		}
	} 
	
	/**
	 * Checks if a Node is inside a triangle circumcircle by
	 * calling the calculateDet method.
	 * @param n1
	 * @param n2
	 * @param n3
	 * @param n4
	 * @return
	 */
	public boolean inCircle(Node n1, Node n2, Node n3, Node n4) {
		float det;
		
		det = calculateDet(n1, n2, n3, n4);
		if(det > 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Calculates the determinant of four nodes.
	 * @return determinant
	 */
	public float calculateDet(Node n1, Node n2, Node n3, Node n4) {
		float a, b, c;
		float[][] detArray = new float[3][3];
		
		detArray[0][0] = n1.getXCoordinate()-n4.getXCoordinate();
		detArray[0][1] = n1.getYCoordinate()-n4.getYCoordinate();
		detArray[0][2] = (float)Math.pow(n1.getXCoordinate()-n4.getXCoordinate(),2)+(float)Math.pow(n1.getYCoordinate()-n4.getYCoordinate(), 2);
		detArray[1][0] = n2.getXCoordinate()-n4.getXCoordinate();
		detArray[1][1] = n2.getYCoordinate()-n4.getYCoordinate();
		detArray[1][2] = (float)Math.pow(n2.getXCoordinate()-n4.getXCoordinate(), 2)+(float)Math.pow(n2.getYCoordinate()-n4.getYCoordinate(), 2);
		detArray[2][0] = n3.getXCoordinate()-n4.getXCoordinate();
		detArray[2][1] = n3.getYCoordinate()-n4.getYCoordinate();
		detArray[2][2] = (float)Math.pow(n3.getXCoordinate()-n4.getXCoordinate(), 2)+(float)Math.pow(n3.getYCoordinate()-n4.getYCoordinate(), 2);
		
		a = detArray[0][0]*((detArray[1][1]*detArray[2][2])-(detArray[1][2]*detArray[2][1]));
		b = detArray[0][1]*((detArray[1][0]*detArray[2][2])-(detArray[1][2]*detArray[2][0]));
		c = detArray[0][2]*((detArray[1][0]*detArray[2][1])-(detArray[1][1]*detArray[2][0]));
		return a-b+c;
	}
	
	/**
	 * Create a new edge be creating the two halfedges.
	 * @param newNode
	 * @param h
	 * @return
	 */
	public HalfEdge createNewEdge(Node newNode, HalfEdge h) {
		Node existing = h.getOrigin();
		
		HalfEdge h1 = createHalfEdge(existing);
		HalfEdge h2 = createHalfEdge(newNode);
		h1.setTwin(h2);
		h2.setTwin(h1);
		
		return h2;
	}
	
	/**
	 * Finds which face that contains the new node n
	 * by comparing the area of the face and the total area 
	 * of the three faces which would be created by the node n.
	 * @param face
	 * @param n
	 * @return if face contains n
	 */
	public boolean faceContainsNode(Face face, Node n) {
		Node n1 = face.getEdge().getOrigin();
		Node n2 = face.getEdge().getNext().getOrigin();
		Node n3 = face.getEdge().getNext().getNext().getOrigin();
		float area, area1, area2, area3;
		
		area = calculateArea(n1.getXCoordinate(), n1.getYCoordinate(), n2.getXCoordinate(), n2.getYCoordinate(), n3.getXCoordinate(), n3.getYCoordinate());
		area1 = calculateArea(n.getXCoordinate(), n.getYCoordinate(), n2.getXCoordinate(), n2.getYCoordinate(), n3.getXCoordinate(), n3.getYCoordinate());
		area2 = calculateArea(n1.getXCoordinate(), n1.getYCoordinate(), n.getXCoordinate(), n.getYCoordinate(), n3.getXCoordinate(), n3.getYCoordinate());
		area3 = calculateArea(n1.getXCoordinate(), n1.getYCoordinate(), n2.getXCoordinate(), n2.getYCoordinate(), n.getXCoordinate(), n.getYCoordinate());

		float areaRounded = Math.round((area*10000)/10000);
		float totalRounded = Math.round(((area1+area2+area3)*10000)/10000);

		if(areaRounded == totalRounded) {
			return true;	
		}
		else {
			return false;
		}
	}
	
	/**
	 * Calculates the area of a triangle.
	 * @return
	 */
	private float calculateArea(float x1, float y1, float x2, float y2, float x3, float y3) {
		return Math.abs((x1*(y2-y3)+x2*(y3-y1)+x3*(y1-y2))/2.0f);
	}
	
	/**
	 * Create element data by traversing the faces.
	 * The elementdata is created with focus on drawing lines
	 * aka the edges.
	 */
	public void createElementData(Face f) {
		Node entry = f.getEdge().getOrigin();
		Node next = f.getEdge().getNext().getOrigin();
		Node last = f.getEdge().getNext().getNext().getOrigin();
			
		if(entry.getClass() == AbstractNode.class || next.getClass() == AbstractNode.class || last.getClass() == AbstractNode.class) {
			return;
		}
		
		int entryPos = 0;
		int nextPos = 0;
		int lastPos = 0;
		
		for(int i = 0; i < nodeList.size(); i++) {
			if(nodeList.get(i) == entry) {
				entryPos = i;
			}
		}
		
		for(int j = 0; j < nodeList.size(); j++) {
			if(nodeList.get(j)==next) {
				nextPos = j;
			}
		}
		
		for(int k = 0; k < nodeList.size(); k++) {
			if(nodeList.get(k)== last) {
				lastPos = k;
			}
		}
		// creates three lines per face.
		elementData.add(entryPos);
		elementData.add(nextPos);
		
		elementData.add(nextPos);
		elementData.add(lastPos);
		
		elementData.add(lastPos);
		elementData.add(entryPos); 
	}
	
	/**
	 * Populates the list which will be used in incremental
	 * adding of nodes for triangulation.
	 */
	public void populateNodeList() {
		for(int i = 0; i < nodes.size(); i++) {
			if(nodes.get(i).getClass() == Node.class) {
				nodeList.add(nodes.get(i));
			}
		}
	}
	
	/**
	 * Creates a face and sets Next reference for the halfEdges
	 * by adding them in a counterclockwise sequence. 
	 * @param h1
	 * @param h2
	 * @param h3
	 */
	public void createFace(HalfEdge h1, HalfEdge h2, HalfEdge h3) {
		Node n1 = h1.getOrigin();
		Node n2 = h2.getOrigin();
		Node n3 = h3.getOrigin();
		
		Face face = new Face(h1);
		faces.add(face);
		h1.setFace(face);
		h2.setFace(face);
		h3.setFace(face);
		
		// calculates a value which will be used to see if the three faces is in a counterclockwise sequence.
		float dotValue = ((n3.getXCoordinate()-n1.getXCoordinate())*(n2.getYCoordinate()-n1.getYCoordinate())-(n3.getYCoordinate()-n1.getYCoordinate())*(n2.getXCoordinate()-n1.getXCoordinate()));
		
		// sets the next value for each edge.
		if(dotValue <= 0) {
			h1.setNext(h2);
			h2.setNext(h3);
			h3.setNext(h1);
		}
		else {
			h1.setNext(h3);
			h3.setNext(h2);
			h2.setNext(h1);
		}
	}
	
	public HalfEdge createHalfEdge(Node n) {
		HalfEdge halfEdge = new HalfEdge(n);
		halfEdges.add(halfEdge);
		return halfEdge;
	}
	
	public void createNode(float x, float y, float z) {
		Node node = new Node(x, y);
		nodes.add(node);
		node.setZCoordinate(z);
	}
	
	public void addToFaces(Face face) {
		faces.add(face);
	}
	
	public void addToHalfEdges(HalfEdge edge) {
		halfEdges.add(edge);
	}
	
	public void addToNode(Node node) {
		nodes.add(node);
	}
	
	public static ArrayList<Node> getNodes() {
		return nodes;
	}
	
	public static ArrayList<Node> getNodeList() {
		return nodeList;
	}
	
	public static ArrayList<Integer> getElementData() {
		return elementData;
	}
}
