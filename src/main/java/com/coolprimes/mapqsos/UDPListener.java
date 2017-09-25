package com.coolprimes.mapqsos;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UDPListener extends Thread {
    private String hostname;
    private int port;
    private DatagramSocket socket;
    private volatile boolean running;
    private BlockingQueue<QMessage> queue;
    final Logger log = LoggerFactory.getLogger(UDPListener.class);

    UDPListener(String hostname, String portNumber, BlockingQueue<QMessage> queue)
            throws SocketException, UnknownHostException {
        this.queue = queue;
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
                QMessage msg = dcode.decode();
                if(msg != null){
                    try{
                        queue.add(msg);
                        if(msg instanceof QCloseMessage){ 
                            log.debug("Got a close message from WSJT-X");
                            running = false;
                        }
                    } catch(Exception e){
                        log.error("Failing to add to queue: " + e.getLocalizedMessage());
                        running = false;
                    }
                }
            } catch(IOException ie){
                log.error("Caught IO Exception: {}", ie.getLocalizedMessage());
                running = false;
            }
        }
        log.debug("Thread exiting");
    }
}