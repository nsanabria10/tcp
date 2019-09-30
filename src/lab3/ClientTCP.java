package lab3;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar; 

// Client class 
public class ClientTCP{
	
	private static int tamBuffer = 65536;
	
	public static byte[] createChecksum(String archivo) throws Exception {
		InputStream inputIN =  new FileInputStream(archivo);

		byte[] buffer = new byte[tamBuffer];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = inputIN.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		inputIN.close();
		return complete.digest();
	}
	
	public static void main(String[] args) throws IOException{ 
		// obtener ip del host local 
		InetAddress ip = InetAddress.getByName("localhost"); 
		// establish the connection with server port 5056 
		Socket socket = new Socket(ip, 6969);
		try
		{   
			// obtener streams de entrata y salida 
			DataInputStream dataIN = new DataInputStream(socket.getInputStream()); 
			DataOutputStream dataOUT = new DataOutputStream(socket.getOutputStream()); 
			System.out.println("Conectado al servidor con status 'isConnected' = " + socket.isConnected());
			System.out.println("Notificando el servidor que el cliete se encuentra listo...");
			String archivo="";  
			try{ 
				
				dataOUT.writeUTF("ready"); 
				dataOUT.flush();  

				archivo=dataIN.readUTF(); 
				System.out.println("Reciviedo archivo: "+archivo);
				if(archivo.contains("testl")){
					tamBuffer = tamBuffer*4;
				}
				archivo = socket.getLocalPort() + archivo;
				System.out.println("Guardando como: "+archivo);

				BufferedInputStream bufferIN = 
						new BufferedInputStream(socket.getInputStream());

				BufferedOutputStream bufferOUT = 
						new BufferedOutputStream(new FileOutputStream(archivo));

				long tamano=Long.parseLong(dataIN.readUTF());
				System.out.println ("Tamaño del Archivo: "+(tamano/(1024*1024))+" MB");
				
				System.out.println("Reciviendo CHECKSUM...");
				String serverCheckSum = dataIN.readUTF();

				byte buffer[] = new byte[tamBuffer];
				System.out.println("Reciviendo archivo...");
				
				int len = 0;
				long tiempoIni = System.currentTimeMillis();
		        while ((len = bufferIN.read(buffer)) > 0) {
		            bufferOUT.write(buffer, 0, len);
		        }
		        bufferOUT.flush();
		        System.out.println("Comprobando integridad del archivo...");
		        long tiempoFin = System.currentTimeMillis();
		        byte[] digest = createChecksum(archivo);
		        String clientCheckSum = "";
				for(byte bytee: digest){
					clientCheckSum += bytee;
				}
				boolean completo = true;
				if(!clientCheckSum.equals(serverCheckSum)){
					System.out.println("Integridad del archivo incorrecta.");
					completo = false;
				}
				else{
					System.out.println("Integridad el archivo verificada.");
				}
				System.out.println("Completado.");
				
				
				BufferedWriter writer = null;
				try {
					//crear archivo temporal
					String logTiempo = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
					File archivoLog = new File(archivo+".txt");

					//La ruta a donde el archivo quedará guardado
					System.out.println(archivoLog.getCanonicalPath());

					writer = new BufferedWriter(new FileWriter(archivoLog));
					writer.write("Prueba del TCP"+";");
					writer.write("Log tiempo;" + logTiempo+";");
					writer.write("Nombre del archivo;"+ archivo+";");
					writer.write("Tamaño del archivo;"+(tamano/(1024*1024))+" MB"+";");
					writer.write("Ofrecido para el cliente;" + socket+";");
					if(completo){
						writer.write("Exitoso"+";");
					}
					else{
						writer.write("Ojo ahi que no fue exitoso y el archivo puede estar jodido"+";");
					}
					double tiempoTrans = (tiempoFin-tiempoIni)/1000;
					writer.write("Time elapsed in seconds;" + tiempoTrans+";");
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						//Cerrar el writer sin importar que pase
						writer.close();
					} catch (Exception e) {
					}
				}
				
				dataOUT.flush();
				bufferIN.close();
			    bufferOUT.close();
				dataIN.close();
				dataOUT.close();
			}
			catch(EOFException e)
			{
				//do nothing
			}
		}catch(Exception e){
			socket.close();
			e.printStackTrace(); 
		} 
	} 
}