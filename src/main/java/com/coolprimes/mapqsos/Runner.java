package com.coolprimes.mapqsos;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Runner{
    BlockingQueue<QMessage> queue = new ArrayBlockingQueue<>(256);

    private void runThreads(String hostname, String portNumber) throws Exception 
    {
        Consumer c = new Consumer(queue);
        c.start();
        UDPListener l = new UDPListener(hostname, portNumber, queue);
        l.start();
    }

    public static void main(String[] args) throws Exception
    {
        if(args.length < 2){
            System.err.println("Must specify hostname and port as parameters!");
            System.exit(1);
        }
        Runner r = new Runner();
        String hostname = args[0];
        String portNumber = args[1];
        r.runThreads(hostname, portNumber);
    }
}