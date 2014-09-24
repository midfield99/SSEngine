/*The MIT License (MIT)

Copyright (c) 2014 Brian Nakayama

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package world;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * Loads objects using SimpleWorldFactory.
 * 
 * TODO This class has been updated from pre 1.0, and needs to be tested.
 * 
 * @author Brian Nakayama
 * @version 1.0
 */
public class SimpleMapIO {
	private File f;
	private DataOutputStream dO;
	private DataInputStream dI;
	private boolean canPrint = false;
	private SimpleWorldFactory swf;
	
	/**
	 * Create a new IO object with the specified path.
	 * 
	 * @param path
	 */
	public SimpleMapIO(String path, SimpleWorldFactory swf) {
		this.f = new File(path);
		this.swf = swf;
	}

	/**
	 * Open a map stream.
	 * 
	 * @return True iff the stream was opened successfully.
	 */
	public boolean openMap(String direction) {
		if (!f.exists()) {
			try {
				if (!f.createNewFile()) {
					return false;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		if (openStream(direction)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean openStream(String direction) {
		try {
			if (direction == "out"){
				FileOutputStream fos = new FileOutputStream(f, false);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				dO = new DataOutputStream(bos);
			}
			else if (direction == "in"){
				FileInputStream fis = new FileInputStream(f);
				BufferedInputStream bis = new BufferedInputStream(fis);
				dI = new DataInputStream(bis);
			}
			else {
				JOptionPane.showMessageDialog(null, "Error: direction argument on openStream must be 'in' or 'out'", null, JOptionPane.PLAIN_MESSAGE);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		canPrint = true;
		return true;
	}

	/**
	 * Write a map to a file.
	 * 
	 * This method can save the specific properties of a SimpleObject through
	 * its getDescription() method which the user can override. This method is
	 * not optimized for efficiency, and it does not save any global state
	 * associated with the SimpleWorld such as which SimpleWorldObject should be
	 * used or which object SimpleWorld should follow with the camera. The user
	 * should implement their own methods for saving game state. This method is
	 * best used to load the initial state of the game.
	 * 
	 * Note: Introducing limited global state through a singleton can be used to
	 * set up default settings for a SimpleWorld.
	 * 
	 * 
	 * The format of the saved file will be:
	 * 
	 * &lt; int: number of objects &gt;<br>
	 * &lt; int: map width &gt;<br>
	 * &lt; int: map height &gt; <br>
	 * &lt; int: map cellWidth &gt; <br>
	 * &lt; int: map cellHeight &gt; <br>
	 * 
	 * # count = |map_objects| <br>
	 * for (s in map_objects) do <br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; int: s.id &gt;<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; int: s.coor_x &gt;<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; int: s.coor_y &gt;<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&lt; utf_string: s.description &gt; <br>
	 * done.
	 * 
	 * @param m
	 *            The map to be saved.
	 * @return True if the map was saved successfully.
	 * @see SimpleObject
	 * @see SimpleMap
	 */
	public boolean writeMap(SimpleMap m) {
		try {
			if (canPrint) {
				int count = 0;
				for (SimpleObject s : m.zArray) {
					if (s != null) {
						for (; s != null; s = s.drawNext) {
							count++;
						}
						break;
					}
				}

				dO.writeInt(count);
				dO.writeInt(m.map[0].length);
				dO.writeInt(m.map.length);
				dO.writeInt(m.cellWidth);
				dO.writeInt(m.cellHeight);

				for (SimpleObject s : m.zArray) {
					if (s != null) {
						for (; s != null; s = s.drawNext) {
							dO.writeInt(s.id());
							dO.writeInt(s.coor_x);
							dO.writeInt(s.coor_y);
							dO.writeUTF(s.getDescription());
						}
						break;
					}
				}
				dO.flush();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Read a mp from a file. Reverses the process defined by
	 * SimpleMapIO.writeMap.
	 * 
	 * @return The SimpleMap represented by the file.
	 * @see SimpleWorldFactory
	 */
	public SimpleMap readMap() {
		try {
			int count = dI.readInt();
			SimpleMap m = new SimpleMap(dI.readInt(), dI.readInt(),
					dI.readInt(), dI.readInt());
			while (count > 0) {
				swf.addSimpleObject(dI.readInt(), dI.readInt(), dI.readInt(),
						dI.readUTF(), m);
				count--;
			}

			return m;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error: Couldn't read world.");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Closes and input or output streams to the file.
	 */
	public void closeMap() {
		try {
			if (dI != null) {
				dI.close();
			}
			if (dO != null) {
				dO.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
