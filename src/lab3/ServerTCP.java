package lab3;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier; 

public class ServerTCP{ 
	public static void main(String[] args) throws IOException{ 
		//servidor escuchando en el puerto definido en la siguiente linea, puede ser modificado solo por aca
		ServerSocket serverSocket = new ServerSocket(6969); 
		String archivoEnviar = "";
		int numClientes = 0;
		int tamBuffer = 65536;
		System.out.println("TCP Server");
		String archivo1;
		archivo1= "elmo.mp4";
		String archivo2;
		archivo2= "testm.wmv";
		System.out.println("Packs disponibles: ");
		System.out.println(archivo1 + " Nombre 1(Large Footage)");
		System.out.println(archivo2 + " Nombre 2 (Medium Footage)");
		System.out.println("------------------------------------------------------------------");

		BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Escoga un archivo.");
		System.out.println("Escriba 1 para Nombre1 (Large Footage)");
		System.out.println("Escriba 2 para Nombre2 (Medium Footage)");
		String archivoEscogido = "";

		while (!archivoEscogido.equals("1") || !archivoEscogido.equals("2")) {
			archivoEscogido = buffReader.readLine();
			if(archivoEscogido.equals("1")){
				archivoEnviar = archivo1;
				tamBuffer = tamBuffer*4;
				break;
			} 
			else if(archivoEscogido.equals("2")){
				archivoEnviar = archivo2;
				break;
			}
			else{
				System.out.println("No es una entrada valida, sea serio");
			}
		}

		BufferedReader buffReader2 = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Indique numero de clientes a conectarse: ");
		String archivoEscogido2 = "";

		while (!archivoEscogido2.equals("1") || !archivoEscogido2.equals("2") || !archivoEscogido2.equals("3") ||
				!archivoEscogido2.equals("4") || !archivoEscogido2.equals("5") || !archivoEscogido2.equals("10") ||
				!archivoEscogido2.equals("25")) {
			archivoEscogido2 = buffReader2.readLine();
			
			if(archivoEscogido2.equals("1") || archivoEscogido2.equals("2") || archivoEscogido2.equals("3") ||
					archivoEscogido2.equals("4") || archivoEscogido2.equals("5") 
					|| archivoEscogido2.equals("10") || archivoEscogido2.equals("25")){
				numClientes = Integer.parseInt(archivoEscogido2);
				break;
			} 
			else{
				System.out.println("No es una entrada valida, sea serio");
			}
		}

		System.out.println("Archivo a enviar: " + archivoEnviar);

		final CyclicBarrier gate = new CyclicBarrier(numClientes+1);
		//Loop en espera del request de los clientes
		while (true)  
		{
			System.out.println("A" + numClientes + " se les enviará el archivo.");
			ArrayList<Socket> clientes = new ArrayList<>();
			ArrayList<Thread> threads = new ArrayList<>();
			int conectados = 0;
			while(conectados < numClientes){
				Socket socket = null;
				socket = serverSocket.accept();
				if(socket != null){
					clientes.add(socket);
					System.out.println("Un nuevo cliente se ha conectado al puerto: " + socket);
					conectados = clientes.size();
				}
			}
			try 
			{
				System.out.println("Asignando thread para cada cliente...");
				for(Socket socketBI: clientes){
					//Obteniendo streams de entrada y salida
					DataInputStream dis = new DataInputStream(socketBI.getInputStream()); 
					DataOutputStream dos = new DataOutputStream(socketBI.getOutputStream());
					Thread thread = new ManejadorClientes(socketBI, dis, dos, archivoEnviar, gate, tamBuffer);
					threads.add(thread);
				}  
				System.out.println("Threads listos: " + threads.size());
				for(Thread threadListo: threads){ 
					threadListo.start();
				}

				BufferedReader buffReader3 = new BufferedReader(new InputStreamReader(System.in));
				System.out.println();
				System.out.println("Pogase ahi S para empezar los threads: ");
				
				String si = "";
				gate.await();
				/**
				while(!(si.equals("S"))){
					si = buffReader3.readLine();
					if(si.equals("S")){
						gate.await();
						break;
					}
					else{
						System.out.println("No es una entrada valida, sea serio.");
					}
				}*/
				System.out.println("Threads activados.");
				buffReader.close();
				buffReader2.close();
				buffReader3.close();

				for (Thread thread : threads) {
					thread.join();
					System.out.println("Esperando al a finalización de los threads.");
				}
				for(Socket so: clientes){
					so.close();
					System.out.println("Breve.");
				}
				serverSocket.close();
				break;
			} 
			catch (Exception e){ 
				e.printStackTrace(); 
			}
		}
	} 
} 

//Clase que maneja los clientes 
class ManejadorClientes extends Thread{  
	final DataInputStream dataIN; 
	final DataOutputStream dataOUT; 
	final Socket socket;
	final String archivoEnviar;
	final CyclicBarrier gate;
	final int tamBuffer;
	
	public ManejadorClientes(Socket socket, DataInputStream dataIN, DataOutputStream dataOUT, String archivoEnviar, CyclicBarrier gate, int tamBuffer)  
	{ 
		this.socket = socket; 
		this.dataIN = dataIN; 
		this.dataOUT = dataOUT;
		this.archivoEnviar = archivoEnviar;
		this.gate = gate;
		this.tamBuffer = tamBuffer;
	}

	public byte[] crearChecksum(String archivo) throws Exception {
		InputStream fileINS =  new FileInputStream(archivo);

		byte[] buffer = new byte[tamBuffer];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fileINS.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fileINS.close();
		return complete.digest();
	}

	@Override
	public void run()  
	{
		try{
			gate.await();
			System.out.println("Esperando al cliente.");
			String str="";  

			str=dataIN.readUTF();
			System.out.println("dataIN = " + str);
			if(!str.equals("stop")){  
				System.out.println("Enviando archivo: "+archivoEnviar);
				dataOUT.writeUTF(archivoEnviar);  
				dataOUT.flush();  

				BufferedInputStream buffINS = 
						new BufferedInputStream(
								new FileInputStream(archivoEnviar));
				BufferedOutputStream buffOUTS = 
						new BufferedOutputStream(socket.getOutputStream());

				File archivo = new File(archivoEnviar);
				System.out.println("Intentando tomar archivo de: "+archivo.getCanonicalPath());
				long tamArchivo=(int) archivo.length();
				//262144 (65536*4) for large file
				//65536 for medium file
				byte buffer[]=new byte [tamBuffer];

				dataOUT.writeUTF(Long.toString(tamArchivo)); 
				dataOUT.flush(); 
				
				System.out.println("");
				System.out.println ("Tamaño: "+tamArchivo);
				System.out.println ("Tamaño Buffer: "+ socket.getReceiveBufferSize());
				
				byte[] digest = crearChecksum(archivoEnviar);
				
				String stringDigest = "";
				
				for(byte by: digest){
					stringDigest += by;
				}
				System.out.println("Enviando Checksum del archivo al cliente.");
				dataOUT.writeUTF(stringDigest);
				dataOUT.flush();

				int tamano = 0;
				long tiempoIni = System.currentTimeMillis();
				while ((tamano = buffINS.read(buffer)) > 0){
					buffOUTS.write(buffer, 0, tamano);
				}
				dataOUT.flush();
				buffOUTS.flush();
				long tiempoFin = System.currentTimeMillis();
				System.out.println("Tiempo trasncurrido: " + ((tiempoFin-tiempoIni)/1000));
				System.out.println("..ok");
				System.out.println("Envio terminado.");
				buffINS.close();
				buffOUTS.close();
			}   
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Zonas ahi, hubo un error.");
		}
		try
		{ 
			// closing resources 
			this.dataIN.close(); 
			this.dataOUT.close(); 

		}catch(IOException e){ 
			e.printStackTrace(); 
		} 
	} 
} 