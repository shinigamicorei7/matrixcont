package Interfaz;

import javax.swing.*;

import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Random;

public class Progreso implements ActionListener, PropertyChangeListener {

	private JFrame frame;
	private JProgressBar prog;
	private Task task;
	private CallableStatement cs;
	private String dir;

	public Progreso(CallableStatement cs, String dir) {
		this.cs = cs;
		this.dir = dir;
		progreso();
	}

	public void progreso() {

		frame = new JFrame("Progreso");
		frame.setSize(300, 100);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);

		ImageIcon iconoProgreso = new ImageIcon(this.getClass().getResource("/Icons/Backup.png"));
		frame.setIconImage(iconoProgreso.getImage());
		//------------------------------------------------------------------------------
		JPanel panel = new JPanel();
		prog = new JProgressBar(0, 50);
		prog.setValue(0);
		prog.setStringPainted(true);		//Pintar 0%
		panel.add(prog);

		ActionEvent evt = null;
		actionPerformed(evt);

		frame.add(panel);
		//-------------------------------------------------------------------------------
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}
		});
		frame.setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			prog.setValue(progress);
			try {
				cs.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
			}

		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//Instances of javax.swing.SwingWorker are not reusuable, so
		//we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();
	}

	class Task extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */

		@Override
		public Void doInBackground() {
			Random random = new Random();
			int progress = 0;
			//Initialize progress property.
			setProgress(0);
			while (progress < 50) {
				//Sleep for up to one second.
				try {
					Thread.sleep(random.nextInt(1000));
				} catch (InterruptedException ignore) {
				}
				//Make random progress.
				progress += random.nextInt(10);
				setProgress(Math.min(progress, 100));
			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			int seleccion = JOptionPane.showOptionDialog(frame, "BackUp exitoso!\n\nNota: el backUp se encuentra en\n" + dir + "", "BackUp", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{"aceptar"}, "aceptar");

			if (seleccion != -1) {
				frame.dispose();
			}

			//if(JOptionPane.showMessageDialog(frame, "BackUp realizado!","BackUp",JOptionPane.INFORMATION_MESSAGE) == JOptionPane.OK_OPTION)
			//System.exit(0);
		}
	}
}
