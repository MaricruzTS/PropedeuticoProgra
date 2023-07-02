/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectoservidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import javax.swing.JOptionPane;

/**
 *
 * @author macur
 */
public class ServidorP extends Thread{
    private ServerSocket serverSocket;
    //Uso de listas para los usuarios     */
    LinkedList<Hilos> usuarios;
    private final EntradaS ventana;
    private final String puerto;
    static int correlativo;
    /**
     * Constructor del servidor.
     * @param puerto
     * @param ventana 
     */
    public ServidorP(String puerto, EntradaS ventana) {
        correlativo=0;
        this.puerto=puerto;
        this.ventana=ventana;
        usuarios=new LinkedList<>();
        this.start();
    }
 
    public void run() {
        try {
            serverSocket = new ServerSocket(Integer.valueOf(puerto));
            ventana.ServidorIniciado();
            while (true) {
                Hilos h;
                Socket socket;
                socket = serverSocket.accept();
                System.out.println("Nueva conexion entrante: "+socket);
                h=new Hilos(socket, this);               
                h.start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ventana, "El servidor tuvo problemas,\n"
                                                 + "valide el puerto de conexion.\n"
                                                 + "Esta ventana se cerrará.");
            System.exit(0);
        }                
    }        
    /**
     * Lista con los id de los clientes conectados.
     * @return 
     */
    LinkedList<String> getUsuariosConectados() {
        LinkedList<String>usuariosConectados=new LinkedList<>();
        usuarios.stream().forEach(c -> usuariosConectados.add(c.getIdentificador()));
        return usuariosConectados;
    }
    
    void agregar(String texto) {
        ventana.agregar(texto);
    }
    
}
