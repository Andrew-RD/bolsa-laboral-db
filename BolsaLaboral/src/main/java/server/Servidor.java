package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor extends Thread {

	private int puerto;

	public Servidor(int puerto) {
		this.puerto = puerto;
	}

	@Override
	public void run() {
		try (ServerSocket sfd = new ServerSocket(puerto)) {
			System.out.println("Servidor iniciado en puerto " + puerto);
			while (true) {
				try {

					Socket nsfd = sfd.accept();
					DataInputStream oos = new DataInputStream(nsfd.getInputStream());
					String nombreArchivo = oos.readUTF();
					File archivoDestino = generarArchivoRespaldo(nombreArchivo);
					
					System.out.println("Conexión aceptada de: " + nsfd.getInetAddress());
					
					try (DataOutputStream escritor = new DataOutputStream(new FileOutputStream(archivoDestino))){
						
						int unByte;
						while ((unByte = oos.read()) != -1) {
							escritor.write(unByte);
						}
						escritor.flush();
						System.out.println("Archivo respaldado: " + archivoDestino.getName());
					}
					
				} catch (IOException e) {
					System.out.println("Error al manejar cliente: " + e.getMessage());
				}
			}
		} catch (IOException ioe) {
			System.out.println("No se pudo iniciar el servidor: " + ioe.getMessage());
		}
	}

	private File generarArchivoRespaldo(String tipo) {
		int contador = 1;
		File archivo;

		do {
			archivo = new File(tipo + "_respaldo_" + contador + ".dat");
			contador++;
		} while (archivo.exists());

		return archivo;
	}


}