package main;

import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import model.Node;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_SHORT;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES3.*;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import static com.jogamp.opengl.GL4.GL_MAP_COHERENT_BIT;
import static com.jogamp.opengl.GL4.GL_MAP_PERSISTENT_BIT;

/*
* This class is based on the code found at the following link
* https://github.com/java-opengl-labs/hello-triangle/blob/master/src/main/java/gl4/HelloTriangleSimple.java
* 
* The class creates a window capable of rendering graphics using OpenGL 4
*/
public class MinimalJOGLSimple implements GLEventListener, KeyListener {

    // OpenGL window reference
    private static GLWindow window;

    // The animator is responsible for continuous operation
    private static Animator animator;
    
    private static ArrayList<Integer> elementList;
    
    static int[] elementData;
    static float[] vertexData;

    // The program entry point
    public static void main(String[] args) {
    	Triangulation triangulation = new Triangulation();
    	triangulation.main();
    	   	
    	MinimalJOGLSimple mjs = new MinimalJOGLSimple();
    	MinimalJOGLSimple.elementList = new ArrayList<Integer>(Triangulation.getElementData());
    	elementData = new int[elementList.size()];
    	vertexData = new float[Triangulation.getNodeList().size()*6];
    	
    	elementData = mjs.convertElementData(elementList);
    	mjs.populateVertexData();

    	mjs.setup();
    }
    
    /**
     * Convert the arraylist containing elementdata into an array.
     * @param data
     * @return
     */
    public int[] convertElementData(ArrayList<Integer> data) {
    	int[] result = new int[data.size()];
    	int index = 0;
    	for(int i = 0; i < result.length; i++) {
    		result[index++] = data.get(i);
    	}
    	return result;
    }
    
    /*
     * populate the vertex data.
     * Uses the z value for coloring to give depth since
     * the triangluation can only triangulate in two dimensions.
     */
    public void populateVertexData() {
    	ArrayList<Node> nodeList = new ArrayList<Node>(Triangulation.getNodeList());
    	int index = 0;
    	
    	for(int i = 0; i < nodeList.size(); i++) {
    		vertexData[index++]=(nodeList.get(i).getXCoordinate());
    		vertexData[index++]=(nodeList.get(i).getYCoordinate());
    		vertexData[index++]=(nodeList.get(i).getZCoordinate());
    		if(nodeList.get(i).getZCoordinate() > 10) {
    			vertexData[index++]=(0.0f);
    			vertexData[index++]=(0.0f);
    			vertexData[index++]=(1.0f);
    		}
    		else if(nodeList.get(i).getZCoordinate() < 10 && nodeList.get(i).getZCoordinate() > 3) {
    			vertexData[index++]=(0.0f);
    			vertexData[index++]=(1.0f);
    			vertexData[index++]=(0.0f);
    		}
    		else {
    			vertexData[index++]=(1.0f);
    			vertexData[index++]=(1.0f);
    			vertexData[index++]=(0.0f);
    		}
    	}
    } 
    
    // Interface for creating final static variables for defining the buffers
    private interface Buffer {
        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MODEL_MATRIX = 3;
        int MAX = 4;
    }

    // Create buffers for the names
    private IntBuffer bufferNames = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);

    // Create buffers for clear values
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[] {0, 0, 0, 0});
    private FloatBuffer clearDepth = GLBuffers.newDirectFloatBuffer(new float[] {1});

    // Create references to buffers for holding the matrices
    private ByteBuffer globalMatricesPointer, modelMatrixPointer;

    // https://jogamp.org/bugzilla/show_bug.cgi?id=1287
    private boolean bug1287 = true;

    // Program instance reference
    private Program program;
    
    private long start;

    // Application setup function
    private void setup() {

        // Get a OpenGL 4.x profile (x >= 0)
        GLProfile glProfile = GLProfile.get(GLProfile.GL4);

        // Get a structure for definining the OpenGL capabilities with default values
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

        // Create the window with default capabilities
        window = GLWindow.create(glCapabilities);

        // Set the title of the window
        window.setTitle("Delaunay triangulation");

        // Set the size of the window
        window.setSize(1024, 768);

        // Set debug context (must be set before the window is set to visible)
        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);

        // Make the window visible
        window.setVisible(true);

        // Add OpenGL and keyboard event listeners
        window.addGLEventListener(this);
        window.addKeyListener(this);

        // Create and start the animator
        animator = new Animator(window);
        animator.start();

        // Add window event listener
        window.addWindowListener(new WindowAdapter() {
            // Window has been destroyed
            @Override
            public void windowDestroyed(WindowEvent e) {
                // Stop animator and exit
                animator.stop();
                System.exit(1);
            }
        });
    }


    // GLEventListener.init implementation
    @Override
    public void init(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Initialize debugging
        initDebug(gl);

        // Initialize buffers
        initBuffers(gl);

        // Initialize vertex array
        initVertexArray(gl);

        // Set up the program
        program = new Program(gl, "main", "minimal_jogl", "minimal_jogl");

        // Enable Opengl depth buffer testing
        gl.glEnable(GL_DEPTH_TEST);
        
        start = System.currentTimeMillis();
    }

    // GLEventListener.display implementation
    @Override
    public void display(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Copy the view matrix to the server
        {
            // Create identity matrix
            float[] view = FloatUtil.makeTranslation(new float[16], 0, false, -10f, -10f, -50f);
            // Copy each of the values to the second of the two global matrices
            for (int i = 0; i < 16; i++)
                globalMatricesPointer.putFloat(16 * 4 + i * 4, view[i]);
        }


        // Clear the color and depth buffers
        gl.glClearBufferfv(GL_COLOR, 0, clearColor);
        gl.glClearBufferfv(GL_DEPTH, 0, clearDepth);

        // Copy the model matrix to the server
        {
        	long now = System.currentTimeMillis();
        	float diff = (float) (now - start) / 1_000;
            // Create a scale matrix 
            float[] scale = FloatUtil.makeScale(new float[16], true, 1f, 1f, 1f);     
            float[] rotate = FloatUtil.makeRotationAxis(new float[16], 0, diff, 0f, 1f, 0f, new float[3]);
            
            float[] shape = FloatUtil.multMatrix(scale, rotate, new float[16]);
            modelMatrixPointer.asFloatBuffer().put(scale);
        }

        // Activate the vertex program and vertex array
        gl.glUseProgram(program.name);
        gl.glBindVertexArray(vertexArrayName.get(0));

        // Bind the global matrices buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM0,
                bufferNames.get(Buffer.GLOBAL_MATRICES));

        // Bind the model matrix buffer to a specified index within the uniform buffers
        gl.glBindBufferBase(
                GL_UNIFORM_BUFFER,
                Semantic.Uniform.TRANSFORM1,
                bufferNames.get(Buffer.MODEL_MATRIX));

        // Draw the triangle
        gl.glDrawElements(
                GL_LINES,
                elementData.length,
                GL_UNSIGNED_INT,
                0);
        // Deactivate the program and vertex array
        gl.glUseProgram(0);
        gl.glBindVertexArray(0);
    }

    // GLEventListener.reshape implementation
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Create an orthogonal projection matrix 
        float[] ortho = FloatUtil.makePerspective(new float[16], 0, false, (float)Math.PI/2f, (float)width/height, 0.1f, 100f);

        // Copy the projection matrix to the server
        globalMatricesPointer.asFloatBuffer().put(ortho);

        // Set the OpenGL viewport
        gl.glViewport(x, y, width, height);
    }

    // GLEventListener.dispose implementation
    @Override
    public void dispose(GLAutoDrawable drawable) {

        // Get OpenGL 4 reference
        GL4 gl = drawable.getGL().getGL4();

        // Unmap the transformation matrices
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.GLOBAL_MATRICES));
        gl.glUnmapNamedBuffer(bufferNames.get(Buffer.MODEL_MATRIX));

        // Delete the program
        gl.glDeleteProgram(program.name);

        // Delete the vertex array
        gl.glDeleteVertexArrays(1, vertexArrayName);

        // Delete the buffers
        gl.glDeleteBuffers(Buffer.MAX, bufferNames);
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyPressed(KeyEvent e) {
        // Destroy the window if the esape key is pressed
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            new Thread(() -> {
                window.destroy();
            }).start();
        }
    }

    // KeyListener.keyPressed implementation
    @Override
    public void keyReleased(KeyEvent e) {
    }

    // Function for initializing OpenGL debugging
    private void initDebug(GL4 gl) {

        // Register a new debug listener
        window.getContext().addGLDebugListener(new GLDebugListener() {
            // Output any messages to standard out
            @Override
            public void messageSent(GLDebugMessage event) {
                System.out.println(event);
            }
        });

        // Ignore all messages
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DONT_CARE,
                0,
                null,
                false);

        // Enable messages of high severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_HIGH,
                0,
                null,
                true);

        // Enable messages of medium severity
        gl.glDebugMessageControl(
                GL_DONT_CARE,
                GL_DONT_CARE,
                GL_DEBUG_SEVERITY_MEDIUM,
                0,
                null,
                true);
    }

    // Function fo initializing OpenGL buffers
    private void initBuffers(GL4 gl) {
        // Create a new float direct buffer for the vertex data 
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData);

        // Create a new short direct buffer for the triangle indices
        IntBuffer elementBuffer = GLBuffers.newDirectIntBuffer(elementData);

        // Create the OpenGL buffers
        gl.glCreateBuffers(Buffer.MAX, bufferNames);

        // If the workaround for bug 1287 isn't needed
        if (!bug1287) {

            // Create and initialize a named buffer storage for the vertex data
            gl.glNamedBufferStorage(bufferNames.get(Buffer.VERTEX), vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL_STATIC_DRAW);

            // Create and initialize a named buffer storage for the triangle indices
            gl.glNamedBufferStorage(bufferNames.get(Buffer.ELEMENT), elementBuffer.capacity() * Integer.BYTES, elementBuffer, GL_STATIC_DRAW);

            // Create and initialize a named buffer storage for the global and model matrices 
            gl.glNamedBufferStorage(bufferNames.get(Buffer.GLOBAL_MATRICES), 16 * 4 * 2, null, GL_MAP_WRITE_BIT);
            gl.glNamedBufferStorage(bufferNames.get(Buffer.MODEL_MATRIX), 16 * 4, null, GL_MAP_WRITE_BIT);

        } else {

            // Create and initialize a buffer storage for the vertex data
            gl.glBindBuffer(GL_ARRAY_BUFFER, bufferNames.get(Buffer.VERTEX));
            gl.glBufferStorage(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, 0);
            gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Create and initialize a buffer storage for the triangle indices 
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferNames.get(Buffer.ELEMENT));
            gl.glBufferStorage(GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity() * Integer.BYTES, elementBuffer, 0);
            gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);


            // Retrieve the uniform buffer offset alignment minimum
            IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);
            gl.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
            // Set the required bytes for the matrices in accordance to the uniform buffer offset alignment minimum
            int globalBlockSize = Math.max(16 * 4 * 2, uniformBufferOffset.get(0));
            int modelBlockSize = Math.max(16 * 4, uniformBufferOffset.get(0));

            
            // Create and initialize a named storage for the global matrices 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.GLOBAL_MATRICES));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, globalBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

            // Create and initialize a named storage for the model matrix 
            gl.glBindBuffer(GL_UNIFORM_BUFFER, bufferNames.get(Buffer.MODEL_MATRIX));
            gl.glBufferStorage(GL_UNIFORM_BUFFER, modelBlockSize, null, GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT);
            gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
        }

        // map the global matrices buffer into the client space
        globalMatricesPointer = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.GLOBAL_MATRICES),
                0,
                16 * 4 * 2,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT); // flags

        // map the model matrix buffer into the client space
        modelMatrixPointer = gl.glMapNamedBufferRange(
                bufferNames.get(Buffer.MODEL_MATRIX),
                0,
                16 * 4,
                GL_MAP_WRITE_BIT | GL_MAP_PERSISTENT_BIT | GL_MAP_COHERENT_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);
    }

    // Function for initializing the vertex array
    private void initVertexArray(GL4 gl) {

        // Create a single vertex array object
        gl.glCreateVertexArrays(1, vertexArrayName);

        // Associate the vertex attributes in the vertex array object with the vertex buffer
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.POSITION, Semantic.Stream.A);
        gl.glVertexArrayAttribBinding(vertexArrayName.get(0), Semantic.Attr.COLOR, Semantic.Stream.A);
    
        // Set the format of the vertex attributes in the vertex array object
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.POSITION, 3, GL_FLOAT, false, 0);
        gl.glVertexArrayAttribFormat(vertexArrayName.get(0), Semantic.Attr.COLOR, 3, GL_FLOAT, false, 3 * 4);

        // Enable the vertex attributes in the vertex object
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.POSITION);
        gl.glEnableVertexArrayAttrib(vertexArrayName.get(0), Semantic.Attr.COLOR);


        // Bind the triangle indices in the vertex array object the triangle indices buffer
        gl.glVertexArrayElementBuffer(vertexArrayName.get(0), bufferNames.get(Buffer.ELEMENT));

        // Bind the vertex array object to the vertex buffer
        gl.glVertexArrayVertexBuffer(vertexArrayName.get(0), Semantic.Stream.A, bufferNames.get(Buffer.VERTEX), 0, (3 + 3) * 4);
    }

    // Private class representing a vertex program
    private class Program {

        // The name of the program
        public int name = 0;

        // Constructor
        public Program(GL4 gl, String root, String vertex, String fragment) {

            // Instantiate a complete vertex shader
            ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
                    "vert", null, true);

            // Instantiate a complete fragment shader
            ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
                    "frag", null, true);

            // Create the shader program
            ShaderProgram shaderProgram = new ShaderProgram();

            // Add the vertex and fragment shader
            shaderProgram.add(vertShader);
            shaderProgram.add(fragShader);

            // Initialize the program
            shaderProgram.init(gl);

            // Store the program name (nonzero if valid)
            name = shaderProgram.program();

            // Compile and link the program
            shaderProgram.link(gl, System.err);
        }
    }

    // Private class to provide an semantic interface between Java and GLSL
	private static class Semantic {

		public interface Attr {
			int POSITION = 0;
			int COLOR = 1;
		}

		public interface Uniform {
			int TRANSFORM0 = 1;
			int TRANSFORM1 = 2;
			int GLOBAL_MATRICES = 4;
		}

		public interface Stream {
			int A = 0;
		}
	}
}
