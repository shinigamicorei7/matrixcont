/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Interfaz;

import com.sun.awt.AWTUtilities;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

/**
 *
 * @web http://www.javaongeek.blogspot.com
 * @author daniel
 *
 */
public class Panel_Local extends javax.swing.JFrame {

	private Splash splashFrame;//invocamos el splashframe del inicio y lo concatenamos con este frame
	private static Connection con;//
	private String version = "0.1";//definimos la version del sistema de forma interna
	private int selecion = 0;//variable donde se almacena el tema seleccionado
	boolean ac;//variable que amacena la seleccion del usuario en tema de actualizaciones
	String carpeta;//se almacena el directorio que se usara para el almacenamiento de la BD y la config del sistema
	String so = System.getProperty("os.name");//obtiene el nombre del sistema operativo
	String home = System.getProperty("user.home");//Obtiene el home del usuario
	String separador = System.getProperty("file.separator");//obtiene el separador de los directoirios dependiendo el sistema operativo

	/**
	 * Creates new form Panel_Local
	 *
	 * @param SplashFrame
	 */
	public Panel_Local(Splash SplashFrame) {
		splashFrame = SplashFrame;
		UIManager.put("Synthetica.window.decoration", false);//se define como default la decoracion de ventanas predefinida del sistema operativo.
		UIManager.put("Synthetica.extendedFileChooser.enabled", false);//se deshavilita la configuracion del Filechoser dada por el look an feel de synthetica.
		setIconImage(new ImageIcon(getClass().getResource("/Icons/Icono.png")).getImage());//se determina un icono para el titulo del frame
		initComponents();//se carga los componentes creados con netbeans
		setLocationRelativeTo(null); //se define la posicion del frame principal en el escritorio
		Fac.addActionListener(abrirFacturas());//por motivos de logica creamos el evento actionpreformed del boton que invoca al internalframe de Facturas
		botonCobrar.addActionListener(abrirCobrar());
		botonPagar.addActionListener(abrirPagar());
		botonInv.addActionListener(abrirInventario());
		botonClientes.addActionListener(abrirClientes());
		botonProv.addActionListener(abrirProveedores());
		setProgress(10, "Cargando configuracion...");//hacemos avanzar el hilo del progresbar integrado en el splashframe y publicamos en el textfiel el avance de la carga
		cargarConfig();//ejecutamos el void o metodo nombrado
		if (ac) {//despues de cargar el archivo de configuracion las variables oftienen valores y se comprueba si esta actibada la busqueda de actualizaciones.
			checkUpdates(ac);//si la condicion se cumple se ejecuta el void para chequear actualizaciones.
		}
		setProgress(20, "Configuracion Cargada");
		setProgress(40, "Cargando Base de Datos...");
		if (buscaCarpeta()) {//en esta condicion se comprueva si existe la carpeta base del sistema en si
			conexion(sistemaOperativo());//si esto es real se carga la base de datos
		} else {//caso contrario
			JOptionPane.showMessageDialog(null, "Al parecer es la primera ves que abres el programa\n"
					+ "Procederemos a crear la base de datos del programa.", "Bienvenido", JOptionPane.INFORMATION_MESSAGE);
			crearBase();//se crea la base de datos
		}
		setProgress(60, "Conexion exitosa!!!");
		setProgress(80, "Cargando interfaz grafica...");
		setProgress(90, "Interfaz grafica cargada!!!");
		setProgress(100, "Bienvenido al sistema");

	}
	/*Void usado para ejecutar este frame independientemente de los archivos de carga*/

	Panel_Local() {
		UIManager.put("Synthetica.window.decoration", false);
		UIManager.put("Synthetica.extendedFileChooser.enabled", false);
		this.setIconImage(new ImageIcon(getClass().getResource("/Icons/Icono.png")).getImage());
		initComponents();
		Fac.addActionListener(abrirFacturas());
		botonCobrar.addActionListener(abrirCobrar());
		botonPagar.addActionListener(abrirPagar());
		botonInv.addActionListener(abrirInventario());
		botonClientes.addActionListener(abrirClientes());
		botonProv.addActionListener(abrirProveedores());
	}
	/*Void usado para mostrar el avance de la carga del sistema en el splashframe*/

	private void setProgress(int percent, String information) {
		splashFrame.getJLabel().setText(information);
		splashFrame.getJProgressBar().setValue(percent);
		Random random = new Random();
		try {
			Thread.sleep(random.nextInt(1000));
		} catch (InterruptedException ex) {
			Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private String sistemaOperativo() {

		String url = null;
		setProgress(41, "Comprobando sistema Operativo...");
		if (so.equals("Linux")) {
			System.out.println("su sistema operativo es linux");
			return url = "jdbc:derby:" + getDirectorio() + separador + "Base";//La base de datos se llamara base, si no existe la crea
		} else {
			System.out.println("su sistema operativo es windows");
			return url = "jdbc:derby:" + getDirectorio() + separador + "Base";//La base de datos se llamara base, si no existe la crea
		}
	}

	private String getDirectorio() {
		if (so.equals("Linux")) {
			String carL = home + separador + ".MatrixCont";
			return carL;
		} else {
			String carW = home + separador + "MatrixCont";
			return carW;
		}

	}

	private boolean buscaCarpeta() {
		setProgress(43, "Buscando base....");
		File bCarpeta = new File(getDirectorio() + separador + "Base");
		if (bCarpeta.exists()) {
			setProgress(44, "Base encontrada....");
			return true;
		} else {
			setProgress(44, "Base No encontrada....");
		}
		return false;
	}

	private void checkUpdates(boolean silent) {

		String nextLine;
		String versionObtenida, web;
		URL url;
		JTextArea texto;
		URLConnection urlConn;
		InputStreamReader inStream;
		BufferedReader buff;
		JEditorPane editor;
		try {
			//System.out.println("Buscamos actualizaciones");
			// Create the URL obect that points
			// at the default file index.html
			url = new URL("http://dl.dropbox.com/u/52195924/version.txt");

			urlConn = url.openConnection();
			inStream = new InputStreamReader(urlConn.getInputStream());
			buff = new BufferedReader(inStream);

			nextLine = buff.readLine(); //Encabezado
			versionObtenida = buff.readLine(); //version
			nextLine = buff.readLine(); //Encabezado
			web = buff.readLine(); //Link
			if (Float.parseFloat(versionObtenida) > Float.parseFloat(version)) {
				String prueba = "<html><a href='http://www.facebook.com/JavaForGeeks' target='_blank'><span style='font-family: Georgia, 'Times New Roman', serif;'>JavaForGeeks</span></a><br/><p>";
				editor = new JEditorPane(prueba);
				JOptionPane.showMessageDialog(null, editor, "Actualizacion disponible", JOptionPane.INFORMATION_MESSAGE);
			} else if (silent) {
				System.out.println("MatrixCont esta actualizado");
				setProgress(25, "Matrix Esta en su version mas actual");
			} else {
				JOptionPane.showMessageDialog(null, "MatrixCont se encuentra actualizado", "Buscar actualizaciones", JOptionPane.INFORMATION_MESSAGE);
			}

		} catch (IOException e1) {
			System.err.println("Error al conectar con el servidor");
			JOptionPane.showMessageDialog(null, "No fue posible conectar con el servidor", "Ups", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void conexion(String url) {
		try {
			//Cargar controlador
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			//Establecer la conexion
			con = DriverManager.getConnection(url);
			setProgress(50, "Base Cargada...");
			System.out.println("La base ha sido cargada " + url);
		} catch (ClassNotFoundException | SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error al cargar Driver");
			System.exit(0);
		}

	}

	private void crearBase() {
		try {
			//Cargar controlador
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			//Establecer la conexion
			con = DriverManager.getConnection("jdbc:derby:" + getDirectorio() + separador + "Base;create=true");
			setProgress(50, "Creando Base de Datos");
			System.out.println("La base ha sido creada " + getDirectorio());
			crearTablas();
		} catch (ClassNotFoundException | SQLException ex) {
			System.exit(0);
		}

	}

	public void guardarConfig() {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			File fileCarp = new File(getDirectorio());
			boolean existecarpeta = fileCarp.exists();
			if (!existecarpeta) {
				if (fileCarp.mkdir()) {
				}
			}
			fos = new FileOutputStream(getDirectorio() + separador + "config.dat");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(version);//encabezado
			oos.writeObject(busActualizar.isSelected());//seleccion
			oos.writeObject("[Tema]");//encabezado
			oos.writeObject(selecion);
			System.out.println("tema aplicado: " + selecion);
			oos.close();
		} catch (IOException ex) {
			Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				fos.close();
			} catch (IOException ex) {
				Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
			}
			try {
				oos.close();
			} catch (IOException ex) {
				Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	private void cargarConfig() {
		String versionObtenida = null;
		File fileCarp = new File(getDirectorio());
		boolean existecarpeta = fileCarp.exists();
		if (!existecarpeta) {
			setProgress(14, "No se encontro archivo de configuracion");
		} else {
			FileInputStream fos = null;
			ObjectInputStream oos = null;
			try {
				fos = new FileInputStream(getDirectorio() + separador + "config.dat");
				oos = new ObjectInputStream(fos);
				versionObtenida = (String) oos.readObject();//encabezado
				ac = (boolean) oos.readObject();
				oos.readObject();//encabezado
				selecion = (int) oos.readObject();
				oos.close();
			} catch (ClassNotFoundException ex) {
				Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				try {
					fos.close();
				} catch (IOException ex) {
					Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
				}
				try {
					oos.close();
				} catch (IOException ex) {
					Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		System.out.println("-----------Cargando config---------------");
		System.out.println("Version del archivo config:" + versionObtenida);
		System.out.println("Buscar Actualizaciones:" + ac);
		if (ac) {
			busActualizar.setSelected(true);
		} else {
			busActualizar.setSelected(false);
		}
		System.out.println("Tema a Cargar:" + selecion);

		switch (selecion) {
			case 0:
				selecion = 0;
				lookDefault();
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");//tema aplicado blackstar
				Tema1.setForeground(Color.black);
				Tema2.setText("Aplicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Applicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 1:
				selecion = 1;
				lookBlackStar();
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//ingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicado");//tema aplicado blackstar
				Tema1.setForeground(Color.red);
				Tema2.setText("Aplicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Applicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 2:
				selecion = 2;
				lookClassy();
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Aplicado");//tema aplicado classy
				Tema2.setForeground(Color.red);
				Tema3.setText("Applicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 3:
				selecion = 3;
				lookBlackEye();
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicado");//Tema aplicado blackeye
				Tema3.setForeground(Color.red);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 4:
				lookBlueMoon();
				selecion = 4;
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicado");//tema aplicado
				Tema4.setForeground(Color.red);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 5:
				lookBlueIce();
				selecion = 5;
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicado");//tema aplicado
				Tema5.setForeground(Color.red);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 6:
				lookBlueSteel();
				selecion = 6;
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicado");//tema aplicado
				Tema6.setForeground(Color.red);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 7:
				lookMauveMetallic();
				selecion = 7;
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicado");//tema aplicado
				Tema7.setForeground(Color.red);
				Tema8.setText("Aplicable");
				Tema8.setForeground(Color.black);
				break;
			case 8:
				lookSimple2d();
				selecion = 8;
				SwingUtilities.updateComponentTreeUI(this);
				SwingUtilities.updateComponentTreeUI(jFrame1);
				SwingUtilities.updateComponentTreeUI(About);
				SwingUtilities.updateComponentTreeUI(jFileChooser1);
				//SwingUtilities.updateComponentTreeUI(splashFrame);
				Tema1.setText("Aplicable");
				Tema1.setForeground(Color.black);
				Tema2.setText("Applicable");
				Tema2.setForeground(Color.black);
				Tema3.setText("Aplicable");
				Tema3.setForeground(Color.black);
				Tema4.setText("Aplicable");
				Tema4.setForeground(Color.black);
				Tema5.setText("Aplicable");
				Tema5.setForeground(Color.black);
				Tema6.setText("Aplicable");
				Tema6.setForeground(Color.black);
				Tema7.setText("Aplicable");
				Tema7.setForeground(Color.black);
				Tema8.setText("Aplicado");//tema aplicado
				Tema8.setForeground(Color.red);
				break;
		}
		System.out.println("-----------Config Cargado---------------");
	}

	private void crearTablas() {

		Statement st;
		try {
			st = con.createStatement();
			st.execute("CREATE TABLE Usuarios(CI INT PRIMARY KEY, Nombre VARCHAR (20),Apellido VARCHAR(20))");
			st.execute("CREATE TABLE Facturas(ID INT PRIMARY KEY)");
		} catch (SQLException ex) {
			System.err.println("error en la creacion de tablas " + ex);
		}

	}

	private void agregar() {
		try {
			Statement st = con.createStatement();
			ResultSet rs;
			st.executeUpdate("INSERT INTO Usuarios (ID, Nombre, Apellido) VALUES (23, 'Daniel', 'Velastegui')");
			rs = st.executeQuery("select Nombre from Usuarios");
			while (rs.next()) {
				System.out.println("Nombre =" + rs.getString("Nombre"));
			}
		} catch (SQLException ex) {
			Logger.getLogger(Panel_Local.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void lookBlackEye() {
		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlackEyeLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error" + e);
		}
	}

	public void lookBlackStar() {
		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlackStarLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookSimple2d() {
		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookClassy() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaClassyLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookBlueIce() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueIceLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookBlueMoon() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueMoonLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookBlueSteel() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueSteelLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookGreenDream() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaGreenDreamLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookMauveMetallic() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaMauveMetallicLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookSilverMoon() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaSilverMoonLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void SkyMetallic() {

		try {
			UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaSkyMetallicLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			System.out.println("mierda aki esta el error");
		}
	}

	public void lookDefault() {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        About = new javax.swing.JDialog();
        jButton4 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jFrame1 = new javax.swing.JFrame();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel7 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        BlackStar = new javax.swing.JButton();
        Tema1 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        Classy = new javax.swing.JButton();
        Tema2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        BlackEye = new javax.swing.JButton();
        Tema3 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        BlackMoon = new javax.swing.JButton();
        Tema4 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel11 = new javax.swing.JPanel();
        BlueIce = new javax.swing.JButton();
        Tema5 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        BlueSteel = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        Tema6 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();
        Tema7 = new javax.swing.JLabel();
        MuveMetallic = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jPanel16 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        Classy1 = new javax.swing.JButton();
        Tema8 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jFileChooser1 = new javax.swing.JFileChooser();
        jLabel23 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        busActualizar = new javax.swing.JCheckBox();
        jToolBar1 = new javax.swing.JToolBar();
        Fac = new javax.swing.JButton();
        botonCobrar = new javax.swing.JButton();
        botonPagar = new javax.swing.JButton();
        botonInv = new javax.swing.JButton();
        botonClientes = new javax.swing.JButton();
        botonProv = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        Salir = new javax.swing.JButton();
        desktopPane = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        jButton4.setText("Aceptar");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        jTabbedPane1.setName("");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Icono.png"))); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("<html>"+
            "<head>"+
            "</head>"+
            "<body>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu' size='7'>MaTriXConT</font><font face='Ubuntu'>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>Versi&#243;n "+version+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<font face='Ubuntu'>Publicado bajo la <b>Licencia P&#250;blica General de GNU </b>"+
            "</font>    </p>"+
            "<p style='margin-top: 0' align='center'>"+
            "<b><font face='Ubuntu'>&#169;</font></b><font face='Ubuntu'> 2012&#8211;2013 "+
            "Bryan Velastegui</font>"+
            "</p>"+
            "</body>"+
            "</html>");
        jLabel8.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("MatrixCont v0.1", jPanel2);

        jEditorPane1.setContentType("text/html");
        jScrollPane3.setViewportView(jEditorPane1);
        jEditorPane1.getAccessibleContext().setAccessibleName("");
        jEditorPane1.getAccessibleContext().setAccessibleDescription("text/html");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 593, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Desarrollador", jPanel3);

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setText("                   GNU LESSER GENERAL PUBLIC LICENSE\n                       Version 3, 29 June 2007\n\n Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>\n Everyone is permitted to copy and distribute verbatim copies\n of this license document, but changing it is not allowed.\n\n\n  This version of the GNU Lesser General Public License incorporates\nthe terms and conditions of version 3 of the GNU General Public\nLicense, supplemented by the additional permissions listed below.\n\n  0. Additional Definitions.\n\n  As used herein, \"this License\" refers to version 3 of the GNU Lesser\nGeneral Public License, and the \"GNU GPL\" refers to version 3 of the GNU\nGeneral Public License.\n\n  \"The Library\" refers to a covered work governed by this License,\nother than an Application or a Combined Work as defined below.\n\n  An \"Application\" is any work that makes use of an interface provided\nby the Library, but which is not otherwise based on the Library.\nDefining a subclass of a class defined by the Library is deemed a mode\nof using an interface provided by the Library.\n\n  A \"Combined Work\" is a work produced by combining or linking an\nApplication with the Library.  The particular version of the Library\nwith which the Combined Work was made is also called the \"Linked\nVersion\".\n\n  The \"Minimal Corresponding Source\" for a Combined Work means the\nCorresponding Source for the Combined Work, excluding any source code\nfor portions of the Combined Work that, considered in isolation, are\nbased on the Application, and not on the Linked Version.\n\n  The \"Corresponding Application Code\" for a Combined Work means the\nobject code and/or source code for the Application, including any data\nand utility programs needed for reproducing the Combined Work from the\nApplication, but excluding the System Libraries of the Combined Work.\n\n  1. Exception to Section 3 of the GNU GPL.\n\n  You may convey a covered work under sections 3 and 4 of this License\nwithout being bound by section 3 of the GNU GPL.\n\n  2. Conveying Modified Versions.\n\n  If you modify a copy of the Library, and, in your modifications, a\nfacility refers to a function or data to be supplied by an Application\nthat uses the facility (other than as an argument passed when the\nfacility is invoked), then you may convey a copy of the modified\nversion:\n\n   a) under this License, provided that you make a good faith effort to\n   ensure that, in the event an Application does not supply the\n   function or data, the facility still operates, and performs\n   whatever part of its purpose remains meaningful, or\n\n   b) under the GNU GPL, with none of the additional permissions of\n   this License applicable to that copy.\n\n  3. Object Code Incorporating Material from Library Header Files.\n\n  The object code form of an Application may incorporate material from\na header file that is part of the Library.  You may convey such object\ncode under terms of your choice, provided that, if the incorporated\nmaterial is not limited to numerical parameters, data structure\nlayouts and accessors, or small macros, inline functions and templates\n(ten or fewer lines in length), you do both of the following:\n\n   a) Give prominent notice with each copy of the object code that the\n   Library is used in it and that the Library and its use are\n   covered by this License.\n\n   b) Accompany the object code with a copy of the GNU GPL and this license\n   document.\n\n  4. Combined Works.\n\n  You may convey a Combined Work under terms of your choice that,\ntaken together, effectively do not restrict modification of the\nportions of the Library contained in the Combined Work and reverse\nengineering for debugging such modifications, if you also do each of\nthe following:\n\n   a) Give prominent notice with each copy of the Combined Work that\n   the Library is used in it and that the Library and its use are\n   covered by this License.\n\n   b) Accompany the Combined Work with a copy of the GNU GPL and this license\n   document.\n\n   c) For a Combined Work that displays copyright notices during\n   execution, include the copyright notice for the Library among\n   these notices, as well as a reference directing the user to the\n   copies of the GNU GPL and this license document.\n\n   d) Do one of the following:\n\n       0) Convey the Minimal Corresponding Source under the terms of this\n       License, and the Corresponding Application Code in a form\n       suitable for, and under terms that permit, the user to\n       recombine or relink the Application with a modified version of\n       the Linked Version to produce a modified Combined Work, in the\n       manner specified by section 6 of the GNU GPL for conveying\n       Corresponding Source.\n\n       1) Use a suitable shared library mechanism for linking with the\n       Library.  A suitable mechanism is one that (a) uses at run time\n       a copy of the Library already present on the user's computer\n       system, and (b) will operate properly with a modified version\n       of the Library that is interface-compatible with the Linked\n       Version.\n\n   e) Provide Installation Information, but only if you would otherwise\n   be required to provide such information under section 6 of the\n   GNU GPL, and only to the extent that such information is\n   necessary to install and execute a modified version of the\n   Combined Work produced by recombining or relinking the\n   Application with a modified version of the Linked Version. (If\n   you use option 4d0, the Installation Information must accompany\n   the Minimal Corresponding Source and Corresponding Application\n   Code. If you use option 4d1, you must provide the Installation\n   Information in the manner specified by section 6 of the GNU GPL\n   for conveying Corresponding Source.)\n\n  5. Combined Libraries.\n\n  You may place library facilities that are a work based on the\nLibrary side by side in a single library together with other library\nfacilities that are not Applications and are not covered by this\nLicense, and convey such a combined library under terms of your\nchoice, if you do both of the following:\n\n   a) Accompany the combined library with a copy of the same work based\n   on the Library, uncombined with any other library facilities,\n   conveyed under the terms of this License.\n\n   b) Give prominent notice with the combined library that part of it\n   is a work based on the Library, and explaining where to find the\n   accompanying uncombined form of the same work.\n\n  6. Revised Versions of the GNU Lesser General Public License.\n\n  The Free Software Foundation may publish revised and/or new versions\nof the GNU Lesser General Public License from time to time. Such new\nversions will be similar in spirit to the present version, but may\ndiffer in detail to address new problems or concerns.\n\n  Each version is given a distinguishing version number. If the\nLibrary as you received it specifies that a certain numbered version\nof the GNU Lesser General Public License \"or any later version\"\napplies to it, you have the option of following the terms and\nconditions either of that published version or of any later version\npublished by the Free Software Foundation. If the Library as you\nreceived it does not specify a version number of the GNU Lesser\nGeneral Public License, you may choose any version of the GNU Lesser\nGeneral Public License ever published by the Free Software Foundation.\n\n  If the Library as you received it specifies that a proxy can decide\nwhether future versions of the GNU Lesser General Public License shall\napply, that proxy's public statement of acceptance of any version is\npermanent authorization for you to choose that version for the\nLibrary.");
        jScrollPane1.setViewportView(jTextArea1);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/commercial_YES.png"))); // NOI18N
        jLabel4.setToolTipText("Puede ser negosiado");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/derivations_YES.png"))); // NOI18N
        jLabel5.setToolTipText("Se Pueden hacer derivados");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/distribution_YES.png"))); // NOI18N
        jLabel1.setToolTipText("Se puede copiar y distribuir");

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/recognition_YES.png"))); // NOI18N
        jLabel2.setToolTipText("Se debe reconocer la auditoría");

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/gplv3-127x51.png"))); // NOI18N
        jLabel6.setToolTipText("GNU General Public Licence");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 240, Short.MAX_VALUE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(16, 16, 16)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );

        jTabbedPane1.addTab("Licencia", jPanel1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 613, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 367, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Creditos", jPanel4);

        javax.swing.GroupLayout AboutLayout = new javax.swing.GroupLayout(About.getContentPane());
        About.getContentPane().setLayout(AboutLayout);
        AboutLayout.setHorizontalGroup(
            AboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AboutLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(AboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        AboutLayout.setVerticalGroup(
            AboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AboutLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap())
        );

        jFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        jFrame1.setTitle("Preferencias del Sistema");
        jFrame1.setResizable(false);
        jFrame1.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                jFrame1WindowClosing(evt);
            }
        });

        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/blackstar.png"))); // NOI18N

        BlackStar.setText("Aplicar");
        BlackStar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlackStarActionPerformed(evt);
            }
        });

        Tema1.setText("Aplicable");

        jLabel10.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel10.setText("Shyntetica Black Start");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(BlackStar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel10))
                    .addComponent(Tema1))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(Tema1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BlackStar))
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/classy.png"))); // NOI18N

        Classy.setText("Aplicar");
        Classy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ClassyActionPerformed(evt);
            }
        });

        Tema2.setText("Aplicable");

        jLabel11.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel11.setText("Shyntetica Glassy");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(Classy, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Tema2)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(Tema2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Classy))
                    .addComponent(jLabel13))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/blackeye.png"))); // NOI18N

        BlackEye.setText("Aplicar");
        BlackEye.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlackEyeActionPerformed(evt);
            }
        });

        Tema3.setText("Aplicable");

        jLabel14.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel14.setText("Shyntetica Black Eye");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(Tema3)
                    .addComponent(BlackEye, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addGap(18, 18, 18)
                        .addComponent(Tema3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BlackEye)))
                .addContainerGap())
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel16.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel16.setText("Synthetica Black Moon");

        BlackMoon.setText("Aplicar");
        BlackMoon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlackMoonActionPerformed(evt);
            }
        });

        Tema4.setText("Aplicable");

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/blackmoon.png"))); // NOI18N

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16)
                    .addComponent(Tema4)
                    .addComponent(BlackMoon, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addGap(18, 18, 18)
                        .addComponent(Tema4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BlackMoon))
                    .addComponent(jLabel15))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        BlueIce.setText("Aplicar");
        BlueIce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlueIceActionPerformed(evt);
            }
        });

        Tema5.setText("Aplicable");

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/blueice.png"))); // NOI18N
        jLabel17.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel18.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel18.setText("Shyntetica Blue Ice");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel18)
                    .addComponent(Tema5)
                    .addComponent(BlueIce, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel18)
                        .addGap(18, 18, 18)
                        .addComponent(Tema5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BlueIce)))
                .addContainerGap())
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        BlueSteel.setText("Aplicar");
        BlueSteel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BlueSteelActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel20.setText("Shyntetica Blue Steel");

        Tema6.setText("Aplicable");

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/bluesteel.png"))); // NOI18N
        jLabel19.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20)
                    .addComponent(Tema6)
                    .addComponent(BlueSteel, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel20)
                        .addGap(18, 18, 18)
                        .addComponent(Tema6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BlueSteel)))
                .addContainerGap())
        );

        jPanel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        Tema7.setText("Aplicable");

        MuveMetallic.setText("Aplicar");
        MuveMetallic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MuveMetallicActionPerformed(evt);
            }
        });

        jLabel21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/mauvemetallic.png"))); // NOI18N
        jLabel21.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel22.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel22.setText("Shyntetica Mauve Metallic");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel22)
                    .addComponent(Tema7)
                    .addComponent(MuveMetallic, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel22)
                        .addGap(18, 18, 18)
                        .addComponent(Tema7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(MuveMetallic)))
                .addContainerGap())
        );

        jButton9.setText("Aplicar tema por default");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jPanel16.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Temas/simple2D.png"))); // NOI18N

        Classy1.setText("Aplicar");
        Classy1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Classy1ActionPerformed(evt);
            }
        });

        Tema8.setText("Aplicable");

        jLabel26.setFont(new java.awt.Font("Ubuntu Condensed", 1, 15)); // NOI18N
        jLabel26.setText("Shyntetica Simple 2D");

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(Classy1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(Tema8)))
                .addContainerGap())
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jLabel26)
                        .addGap(18, 18, 18)
                        .addComponent(Tema8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(Classy1))
                    .addComponent(jLabel25))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel13, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 571, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 5, Short.MAX_VALUE))
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton9)
                .addGap(17, 17, 17)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel7);

        jTabbedPane2.addTab("Tema", jScrollPane2);

        jFileChooser1.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        jFileChooser1.setControlButtonsAreShown(false);
        jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        jLabel23.setText("Seleccione el lugar de almacenamiento para el respaldo");

        ImageIcon iconres = new ImageIcon(getClass().getResource("/Icons/Backup.png"));
        Image imgs10 = iconres.getImage();
        jButton7.setIcon(new javax.swing.ImageIcon(imgs10.getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH)));
        jButton7.setText("Crear Respaldo");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setText("Cargar Respaldo");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 31, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel23)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jFileChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE)
                    .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("BackUp", jPanel6);

        jButton11.setText("Guardar");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton12.setText("Cancelar");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        busActualizar.setText("Buscar actualizaciones al Inicio");

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFrame1Layout.createSequentialGroup()
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jFrame1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFrame1Layout.createSequentialGroup()
                        .addGap(76, 76, 76)
                        .addComponent(busActualizar)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFrame1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 372, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(busActualizar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton11)
                    .addComponent(jButton12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("MatrixCont - v0.1");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setBorderPainted(false);
        jToolBar1.setEnabled(false);

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/Icons/osmo.png"));
        Image imgs1 = icon1.getImage();
        Fac.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        Fac.setIcon(new javax.swing.ImageIcon(imgs1.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        Fac.setText("Facturas");
        Fac.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        Fac.setFocusable(false);
        Fac.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Fac.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(Fac);
        Fac.getAccessibleContext().setAccessibleDescription("");

        ImageIcon icon3 = new ImageIcon(getClass().getResource("/Icons/start-here-chakra.png"));
        Image imgs3 = icon3.getImage();
        botonCobrar.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        botonCobrar.setIcon(new javax.swing.ImageIcon(imgs3.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        botonCobrar.setText("Ctas. Por Cobrar");
        botonCobrar.setFocusable(false);
        botonCobrar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonCobrar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(botonCobrar);

        ImageIcon icon4 = new ImageIcon(getClass().getResource("/Icons/start-here-arch.png"));
        Image imgs4 = icon4.getImage();
        botonPagar.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        botonPagar.setIcon(new javax.swing.ImageIcon(imgs4.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        botonPagar.setText("Ctas. Por Pagar");
        botonPagar.setFocusable(false);
        botonPagar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonPagar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(botonPagar);

        ImageIcon icon2 = new ImageIcon(getClass().getResource("/Icons/opera-widget-manager.png"));
        Image imgs2 = icon2.getImage();
        botonInv.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        botonInv.setIcon(new javax.swing.ImageIcon(imgs2.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        botonInv.setText("Inventario");
        botonInv.setFocusable(false);
        botonInv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonInv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(botonInv);

        ImageIcon icon5 = new ImageIcon(getClass().getResource("/Icons/edit-find-user.png"));
        Image imgs5 = icon5.getImage();
        botonClientes.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        botonClientes.setIcon(new javax.swing.ImageIcon(imgs5.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        botonClientes.setText("Clientes");
        botonClientes.setFocusable(false);
        botonClientes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonClientes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(botonClientes);

        ImageIcon icon = new ImageIcon(getClass().getResource("/Icons/extract-archive.png"));
        Image imgs = icon.getImage();
        botonProv.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        botonProv.setIcon(new javax.swing.ImageIcon(imgs.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        botonProv.setText("Proveedores");
        botonProv.setFocusable(false);
        botonProv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        botonProv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(botonProv);

        ImageIcon icon7 = new ImageIcon(getClass().getResource("/Icons/Faenza/Config.png"));
        Image imgs7 = icon7.getImage();
        jButton6.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        jButton6.setIcon(new javax.swing.ImageIcon(imgs7.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        jButton6.setText("Configuracion");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        ImageIcon icon8 = new ImageIcon(getClass().getResource("/Icons/Icono.png"));
        Image imgs8 = icon8.getImage();
        jButton5.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        jButton5.setIcon(new javax.swing.ImageIcon(imgs8.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        jButton5.setText("Acerca de");
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        ImageIcon icon6 = new ImageIcon(getClass().getResource("/Icons/system-shutdown.png"));
        Image imgs6 = icon6.getImage();
        Salir.setFont(new java.awt.Font("Ubuntu Condensed", 0, 14)); // NOI18N
        Salir.setIcon(new javax.swing.ImageIcon(imgs6.getScaledInstance(64, 64, java.awt.Image.SCALE_SMOOTH)));
        Salir.setText("Salir");
        Salir.setFocusable(false);
        Salir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        Salir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        Salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SalirActionPerformed(evt);
            }
        });
        jToolBar1.add(Salir);

        desktopPane.setBorder(new ImagenMDI());
        desktopPane.setToolTipText("Escritorio de ventanas");
        desktopPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        desktopPane.setDoubleBuffered(true);

        jMenu1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Faenza/emblem-default.png"))); // NOI18N
        jMenu1.setText("Menu");
        jMenu1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N

        jMenuItem2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMenuItem2.setText("Configuracion");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);
        jMenu1.add(jSeparator1);

        jMenuItem4.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMenuItem4.setText("Salir");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuBar1.add(jMenu1);

        jMenu2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons/Faenza/important.png"))); // NOI18N
        jMenu2.setText("Ayuda");
        jMenu2.setToolTipText("Acerca de y Actualizaciones");
        jMenu2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jMenu2.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N

        ImageIcon re = new ImageIcon(getClass().getResource("/Icons/Faenza/Actualizaciones.png"));
        Image re1 = re.getImage();
        jMenuItem1.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMenuItem1.setIcon(new javax.swing.ImageIcon(re1.getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
        jMenuItem1.setText("Buscar Actualizaciones");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem3.setFont(new java.awt.Font("Ubuntu", 0, 12)); // NOI18N
        jMenuItem3.setText("Acerca de");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1082, Short.MAX_VALUE)
            .addComponent(desktopPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(desktopPane, javax.swing.GroupLayout.DEFAULT_SIZE, 524, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void SalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SalirActionPerformed
		int ex = JOptionPane.showConfirmDialog(this, "Esta seguro de salir", "Exit", JOptionPane.YES_NO_OPTION);
		switch (ex) {
			case 0:
				System.exit(0);
				break;
		}

    }//GEN-LAST:event_SalirActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
		About.setVisible(false);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		int op = JOptionPane.showConfirmDialog(this, "Seguro que desea salir", "Salir", JOptionPane.YES_NO_OPTION);
		if (op == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
    }//GEN-LAST:event_formWindowClosing

    private void BlackStarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlackStarActionPerformed

		selecion = 1;
		lookBlackStar();
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		Tema1.setText("Aplicado");//tema aplicado blackstar
		Tema1.setForeground(Color.red);
		Tema2.setText("Aplicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Applicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);

    }//GEN-LAST:event_BlackStarActionPerformed

    private void ClassyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ClassyActionPerformed
		int des = JOptionPane.showConfirmDialog(this, "El tema aun se encuentra en etapa de prueba\n¿Quieres aplicarlo?", "Advertencia", JOptionPane.YES_NO_OPTION);
		if (des == JOptionPane.YES_OPTION) {
			selecion = 2;
			lookClassy();

			SwingUtilities.updateComponentTreeUI(this);
			SwingUtilities.updateComponentTreeUI(jFrame1);
			SwingUtilities.updateComponentTreeUI(About);
			SwingUtilities.updateComponentTreeUI(jFileChooser1);
			Tema1.setText("Aplicable");
			Tema1.setForeground(Color.black);
			Tema2.setText("Aplicado");//tema aplicado classy
			Tema2.setForeground(Color.red);
			Tema3.setText("Applicable");
			Tema3.setForeground(Color.black);
			Tema4.setText("Aplicable");
			Tema4.setForeground(Color.black);
			Tema5.setText("Aplicable");
			Tema5.setForeground(Color.black);
			Tema6.setText("Aplicable");
			Tema6.setForeground(Color.black);
			Tema7.setText("Aplicable");
			Tema7.setForeground(Color.black);
			Tema8.setText("Aplicable");
			Tema8.setForeground(Color.black);

		}

    }//GEN-LAST:event_ClassyActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
		guardarConfig();
		jFrame1.dispose();
    }//GEN-LAST:event_jButton11ActionPerformed

    private void BlackEyeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlackEyeActionPerformed
		int des = JOptionPane.showConfirmDialog(this, "El tema aun se encuentra en etapa de prueba\n¿Quieres aplicarlo?", "Advertencia", JOptionPane.YES_NO_OPTION);
		if (des == JOptionPane.YES_OPTION) {
			selecion = 3;
			lookBlackEye();
			SwingUtilities.updateComponentTreeUI(this);
			SwingUtilities.updateComponentTreeUI(jFrame1);
			SwingUtilities.updateComponentTreeUI(About);
			SwingUtilities.updateComponentTreeUI(jFileChooser1);
			Tema1.setText("Aplicable");
			Tema1.setForeground(Color.black);
			Tema2.setText("Applicable");
			Tema2.setForeground(Color.black);
			Tema3.setText("Aplicado");//Tema aplicado blackeye
			Tema3.setForeground(Color.red);
			Tema4.setText("Aplicable");
			Tema4.setForeground(Color.black);
			Tema5.setText("Aplicable");
			Tema5.setForeground(Color.black);
			Tema6.setText("Aplicable");
			Tema6.setForeground(Color.black);
			Tema7.setText("Aplicable");
			Tema7.setForeground(Color.black);
			Tema8.setText("Aplicable");
			Tema8.setForeground(Color.black);
		}
    }//GEN-LAST:event_BlackEyeActionPerformed

    private void BlackMoonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlackMoonActionPerformed
		lookBlueMoon();
		selecion = 4;
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		Tema1.setText("Aplicable");
		Tema1.setForeground(Color.black);
		Tema2.setText("Applicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicado");//tema aplicado
		Tema4.setForeground(Color.red);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);
    }//GEN-LAST:event_BlackMoonActionPerformed

    private void BlueIceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlueIceActionPerformed
		lookBlueIce();
		selecion = 5;
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		Tema1.setText("Aplicable");
		Tema1.setForeground(Color.black);
		Tema2.setText("Applicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicado");//tema aplicado
		Tema5.setForeground(Color.red);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);

    }//GEN-LAST:event_BlueIceActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
		About.setVisible(true);
		About.setSize(600, 450);
		About.setLocationRelativeTo(this);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
		jFrame1.setVisible(true);
		jFrame1.setSize(700, 470);
		jFrame1.setLocationRelativeTo(this);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void BlueSteelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BlueSteelActionPerformed
		lookBlueSteel();
		selecion = 6;
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		Tema1.setText("Aplicable");
		Tema1.setForeground(Color.black);
		Tema2.setText("Applicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicado");//tema aplicado
		Tema6.setForeground(Color.red);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);
    }//GEN-LAST:event_BlueSteelActionPerformed

    private void MuveMetallicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MuveMetallicActionPerformed
		lookMauveMetallic();
		selecion = 7;
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		Tema1.setText("Aplicable");
		Tema1.setForeground(Color.black);
		Tema2.setText("Applicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicado");//tema aplicado
		Tema7.setForeground(Color.red);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);
    }//GEN-LAST:event_MuveMetallicActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
		int op = JOptionPane.showConfirmDialog(this, "No has guardado nada aun\n¿Seguro que deseas salir?", "Atencion..!!", JOptionPane.YES_NO_OPTION);
		if (op == JOptionPane.YES_OPTION) {
			cargarConfig();
			jFrame1.dispose();
		}
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed

		String BackUp = jFileChooser1.getCurrentDirectory().toString();
		System.out.println("el respaldo se almaceno en" + BackUp + separador + "Base");
		try {
			CallableStatement cs = con.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
			cs.setString(1, BackUp);
			BackUp backUp = new BackUp(cs, BackUp);
			//cs.execute();
			//cs.close();

		} catch (SQLException e1) {
		}    // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
		selecion = 0;
		lookDefault();
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		SwingUtilities.updateComponentTreeUI(splashFrame);
		Tema1.setText("Aplicable");//tema aplicado blackstar
		Tema1.setForeground(Color.black);
		Tema2.setText("Aplicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicable");
		Tema8.setForeground(Color.black);
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
		checkUpdates(false);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
		About.setVisible(true);
		About.setSize(600, 450);
		About.setLocationRelativeTo(this);    // TODO add your handling code here:
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
		jFrame1.setVisible(true);
		jFrame1.setSize(700, 450);
		jFrame1.setLocationRelativeTo(this);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
		int ex = JOptionPane.showConfirmDialog(this, "Esta seguro de salir", "Exit", JOptionPane.YES_NO_OPTION);
		switch (ex) {
			case 0:
				System.exit(0);
				break;
		}
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void Classy1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Classy1ActionPerformed
		lookSimple2d();
		selecion = 8;
		SwingUtilities.updateComponentTreeUI(this);
		SwingUtilities.updateComponentTreeUI(jFrame1);
		SwingUtilities.updateComponentTreeUI(About);
		SwingUtilities.updateComponentTreeUI(jFileChooser1);
		SwingUtilities.updateComponentTreeUI(splashFrame);
		Tema1.setText("Aplicable");//tema aplicado blackstar
		Tema1.setForeground(Color.black);
		Tema2.setText("Aplicable");
		Tema2.setForeground(Color.black);
		Tema3.setText("Aplicable");
		Tema3.setForeground(Color.black);
		Tema4.setText("Aplicable");
		Tema4.setForeground(Color.black);
		Tema5.setText("Aplicable");
		Tema5.setForeground(Color.black);
		Tema6.setText("Aplicable");
		Tema6.setForeground(Color.black);
		Tema7.setText("Aplicable");
		Tema7.setForeground(Color.black);
		Tema8.setText("Aplicado");
		Tema8.setForeground(Color.red);
    }//GEN-LAST:event_Classy1ActionPerformed

    private void jFrame1WindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_jFrame1WindowClosing
		int op = JOptionPane.showConfirmDialog(this, "No has guardado nada aun\n¿Seguro que deseas salir?", "Atencion..!!", JOptionPane.YES_NO_OPTION);
		if (op == JOptionPane.YES_OPTION) {
			cargarConfig();
			jFrame1.dispose();
		}    // TODO add your handling code here:
    }//GEN-LAST:event_jFrame1WindowClosing

	private ActionListener abrirFacturas() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JInternalFrame ji = validador.getJInternalFrame(Facturas.class.getName());

				if (ji == null || ji.isClosed()) {
					ji = new Facturas();
					desktopPane.add(ji);
					validador.addJIframe(Facturas.class.getName(), ji);
				}
				ji.setVisible(true);
				ji.setLocation(150, 40);
				ji.toFront();
			}
		};

	}

	private ActionListener abrirCobrar() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Cobrar jIF = new Cobrar();
				jIF.setVisible(true);
				desktopPane.add(jIF);
			}
		};

	}

	private ActionListener abrirPagar() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Pagar jIF = new Pagar();
				jIF.setVisible(true);
				desktopPane.add(jIF);
			}
		};

	}

	private ActionListener abrirInventario() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Inventario jIF = new Inventario();
				jIF.setVisible(true);
				desktopPane.add(jIF);
			}
		};

	}

	private ActionListener abrirClientes() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Clientes jIF = new Clientes();
				jIF.setVisible(true);
				desktopPane.add(jIF);
			}
		};

	}

	private ActionListener abrirProveedores() {
		return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Proveedores jIF = new Proveedores();
				jIF.setVisible(true);
				desktopPane.add(jIF);
			}
		};

	}

	public static void main(String args[]) {
		/*
		 * Set the Nimbus look and feel
		 */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>

		/*
		 * Create and display the form
		 */
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new Panel_Local().setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog About;
    private javax.swing.JButton BlackEye;
    private javax.swing.JButton BlackMoon;
    private javax.swing.JButton BlackStar;
    private javax.swing.JButton BlueIce;
    private javax.swing.JButton BlueSteel;
    private javax.swing.JButton Classy;
    private javax.swing.JButton Classy1;
    private javax.swing.JButton Fac;
    private javax.swing.JButton MuveMetallic;
    private javax.swing.JButton Salir;
    private javax.swing.JLabel Tema1;
    private javax.swing.JLabel Tema2;
    private javax.swing.JLabel Tema3;
    private javax.swing.JLabel Tema4;
    private javax.swing.JLabel Tema5;
    private javax.swing.JLabel Tema6;
    private javax.swing.JLabel Tema7;
    private javax.swing.JLabel Tema8;
    private javax.swing.JButton botonClientes;
    private javax.swing.JButton botonCobrar;
    private javax.swing.JButton botonInv;
    private javax.swing.JButton botonPagar;
    private javax.swing.JButton botonProv;
    private javax.swing.JCheckBox busActualizar;
    private static javax.swing.JDesktopPane desktopPane;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

	public static JDesktopPane getDesktopPane() {
		return desktopPane;
	}

}

class validador {

	public static HashMap<String, JInternalFrame> JIframes = new HashMap<>();

	public static void addJIframe(String key, JInternalFrame jiframe) {
		JIframes.put(key, jiframe);
	}

	public static JInternalFrame getJInternalFrame(String key) {
		return JIframes.get(key);
	}
}
