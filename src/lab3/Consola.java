package lab3;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JTextArea;

public class Consola extends JFrame{
	
	private JFrame frame = new JFrame();
	JTextArea texto;
	
	public Consola() throws Exception {
		texto = new JTextArea(24,80);
		texto.setBackground(Color.WHITE);
		texto.setForeground(Color.BLACK);
		texto.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		frame.add(texto);
		frame.pack();
		frame.setVisible(true);
		imprimirConsola();
		
		System.out.println("Hello world");
		System.out.println("test");
	}
	
	public PrintStream imprimirConsola() {
		
		OutputStream out = new OutputStream() {
			@Override
			public void write(int arg0) throws IOException {
				texto.append(String.valueOf((char) arg0));
				
			}
		};
		
		PrintStream ps = new PrintStream(out);
		
		System.setOut(ps);
		System.setErr(ps);
		
		return ps;
	}
	
	public JFrame getFrame() {
		return frame;
	}

}
