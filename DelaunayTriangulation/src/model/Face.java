package model;

/**
 * The face class creates the triangel objects 
 * which will contains a reference to a halfedge object.
 * @author hen_b
 *
 */
public class Face {

	private HalfEdge edge;

	public Face(HalfEdge edge) {
		this.edge = edge;
	}
	
	@Override
	public String toString() {
		return "entry: " + edge + " next: " + edge.getNext() + " last: " + edge.getNext().getNext();
	}
	
	public HalfEdge getEdge() {
		return edge;
	}
	
	public void setEdge(HalfEdge e) {
		this.edge = e;
	}
}
