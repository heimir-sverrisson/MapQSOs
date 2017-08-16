package com.coolprimes.mapqsos;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UDPListener implements Runnable {
    private String hostname;
    private int port;
    private DatagramSocket socket;
    private volatile boolean running;
    final Logger log = LoggerFactory.getLogger(UDPListener.class);

    UDPListener(String hostname, String portNumber) 
            throws SocketException, UnknownHostException {
        this.port = Integer.parseInt(portNumber);
        this.hostname = hostname;
        InetAddress [] hosts = InetAddress.getAllByName(hostname); 
        socket = new DatagramSocket(port, hosts[0]);
        log.debug("Constructed for host: {} on port: {}", hostname, portNumber);
    }

    void shutdown(){
        running = false;
        log.debug("Being shut down");
    }

    public void run(){
        byte[] receiveData = new byte[1500];
        running = true;
        log.debug("Running");
        while(running){
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try{
                socket.receive(receivePacket);
                int count = receivePacket.getLength();
                MessageDecoder dcode = new MessageDecoder(receiveData, count);
                dcode.decode();
            } catch(IOException ie){
                log.error("Caught IO Exception: {}", ie.getLocalizedMessage());
            }
        }
        log.debug("Thread exiting");
    }

}