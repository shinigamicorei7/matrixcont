/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaz;

import javax.swing.JInternalFrame;

/**
 *
 * @author bryan
 */
public class ventanita extends JInternalFrame {

	private JInternalFrame[] frames;

	public void setJInternalFrames(JInternalFrame[] frames) {
		this.frames = frames;
	}

	public JInternalFrame[] getJInternalFrames() {
		return frames;
	}
}
