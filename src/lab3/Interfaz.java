package lab3;

import javax.swing.JFrame;

public class Interfaz extends JFrame{
	
	private Interfaz() {
		super();
		setSize(600,600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		Consola consola = new Consola();
		Interfaz i = new Interfaz();
		i.setVisible(false);
		consola.getFrame().setLocation(i.getX() + i.getWidth() + i.getInsets().right, i.getY());
		System.out.println("1");
		System.out.println("2");
		System.out.println("3");
		System.out.println("Hola");
		System.out.println("test");
		ClientTCP tcp = new ClientTCP();
		tcp.ejecutar();
	}

}
