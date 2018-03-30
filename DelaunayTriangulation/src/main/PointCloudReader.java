package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class creates Node-objects by reading a point cloud file.
 * The read-file part of this class was found on 
 * https://stackoverflow.com/questions/32613133/how-to-read-in-file-that-is-inside-the-same-package
 * @author hen_b
 */
public class PointCloudReader {
	private String filename = "/main/pointCloud.pts";
	private String pointCloud;
	
	Triangulation tri = new Triangulation();

	/**
	 * Read the file stored in the filename variable 
	 * and creates node objects.
	 */
	public void readFile() {
		try {
			BufferedReader fr = new BufferedReader(new InputStreamReader(PointCloudReader.class.getResourceAsStream(filename), "UTF-8"));
			int charNumber;
			StringBuffer sb = new StringBuffer();
			
			while((charNumber = fr.read()) != -1) {
				sb.append((char)charNumber);
			}
			
			pointCloud = sb.toString();
			String[] elementList = pointCloud.split("\n");
			
			for(int i = 0; i < elementList.length; i++) {
				String[] temp = elementList[i].split(" ");
				
				float tempX = Float.parseFloat(temp[0]);
				float tempY = Float.parseFloat(temp[1]);
				float tempZ = Float.parseFloat(temp[2]);
				
				tri.createNode(tempX, tempY, tempZ);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
