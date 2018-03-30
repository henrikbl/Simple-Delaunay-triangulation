package model;

/**
 * The HalfEdge class creates halfedges which 
 * will be bound to node objects, faces and other halfedge
 * objects to create the DCEL-structure.
 * @author hen_b
 *
 */
public class HalfEdge {

	private Node origin;
	private Face face;
	
	protected HalfEdge twin;
	protected HalfEdge next;	
	
	public HalfEdge(Node origin) {
		super();
		this.origin = origin;
	}
	
	@Override
	public String toString() {
		return origin + "";
	}
	
	public void setFace(Face face) {
		this.face = face;
	}
	
	public Face getFace() {
		return face;
	}
	
	public void setNext(HalfEdge next) {
		this.next = next;
	}
	
	public HalfEdge getNext() {
		return next;
	}
	
	public void setOrigin(Node origin) {
		this.origin = origin;
	}
	
	public Node getOrigin() {
		return origin;
	}
	
	public void setTwin(HalfEdge twin) {
		this.twin = twin;
	}
	
	public HalfEdge getTwin() {
		return twin;
	}
}
