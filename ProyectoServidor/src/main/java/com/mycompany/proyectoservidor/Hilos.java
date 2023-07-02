/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectoservidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;

/**
 *
 * @author macur
 */
public class Hilos extends Thread{
    private final Socket socket;    
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;            
    private final ServidorP servidorP;
    private String identificador;
    private boolean esperando;
   /**
    * Método constructor de la clase hilo cliente.
    * @param socket
    * @param server 
    */
    public Hilos(Socket socket,ServidorP server) {
        this.servidorP =server;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            System.err.println("Error en la inicialización del ObjectOutputStream y el ObjectInputStream");
        }
    }
   
    public void desconnectar() {
        try {
            socket.close();
            esperando = false;
        } catch (IOException ex) {
            System.err.println("Error al cerrar el socket de comunicación con el cliente.");
        }
    }
       
    public void run() {
        try{
            escuchar();
        } catch (Exception ex) {
            System.err.println("Error al llamar al método readLine del hilo del cliente.");
        }
        desconnectar();
    }
        
    /**
     * Método que constantemente esta escuchando todo lo que es enviado por 
     * el cliente que se comunica con él.
     */        
    public void escuchar(){        
        esperando=true;
        while(esperando){
            try {
                Object aux=objectInputStream.readObject();
                if(aux instanceof LinkedList){
                    ejecutar((LinkedList<String>)aux);
                }
            } catch (Exception e) {                    
                System.err.println("Error al leer lo enviado por el cliente.");
            }
        }
    }

    public void ejecutar(LinkedList<String> lista){
        // 0 - El primer elemento de la lista es siempre el tipo
        String tipo=lista.get(0);
        switch (tipo) {
            case "SOLICITUD_CONEXION":
                // 1 - Identificador propio del nuevo usuario
                confirmarConexion(lista.get(1));
                break;
            case "SOLICITUD_DESCONEXION":
                // 1 - Identificador propio del nuevo usuario
                confirmarDesConexion();
                break;                
            case "MENSAJE":
                // 1      - Cliente emisor
                // 2      - Cliente receptor
                // 3      - Mensaje
                String destinatario=lista.get(2);
                servidorP.usuarios
                        .stream()
                        .filter(h -> (destinatario.equals(h.getIdentificador())))
                        .forEach((h) -> h.enviarMensaje(lista));
                break;
            default:
                break;
        }
    }
      
    private void enviarMensaje(LinkedList<String> lista){
        try {
            objectOutputStream.writeObject(lista);            
        } catch (Exception e) {
            System.err.println("Error al enviar el objeto al cliente.");
        }
    }    

    private void confirmarConexion(String identificador) {
        ServidorP.correlativo++;
        this.identificador=ServidorP.correlativo+" - "+identificador;
        System.out.println("Cliente" + identificador);
        LinkedList<String> lista=new LinkedList<>();
        lista.add("CONEXION_ACEPTADA");
        lista.add(this.identificador);
        lista.addAll(servidorP.getUsuariosConectados());
        enviarMensaje(lista);
        servidorP.agregar("\nNuevo cliente: "+this.identificador);
        //enviar a todos los clientes el nombre del nuevo usuario conectado excepto a él mismo
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("NUEVO_USUARIO_CONECTADO");
        auxLista.add(this.identificador);
        servidorP.usuarios
                .stream()
                .forEach(cliente -> cliente.enviarMensaje(auxLista));
        servidorP.usuarios.add(this);
    }

    public String getIdentificador() {
        return identificador;
    }

    private void confirmarDesConexion() {
        LinkedList<String> auxLista=new LinkedList<>();
        auxLista.add("USUARIO_DESCONECTADO");
        auxLista.add(this.identificador);
        servidorP.agregar("\nEl cliente \""+this.identificador+"\" se ha desconectado.");
        this.desconnectar();
        for(int i=0;i<servidorP.usuarios.size();i++){
            if(servidorP.usuarios.get(i).equals(this)){
                servidorP.usuarios.remove(i);
                break;
            }
        }
        servidorP.usuarios
                .stream()
                .forEach(h -> h.enviarMensaje(auxLista));        
    }
    
}
