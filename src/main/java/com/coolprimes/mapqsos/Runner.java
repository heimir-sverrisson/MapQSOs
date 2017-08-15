package com.coolprimes.mapqsos;

public class Runner{
    public static void main(String[] args) throws Exception{
        if(args.length < 2){
            System.err.println("Must specify hostname and port as parameters!");
            System.exit(1);
        }
        String hostname = args[0];
        String portNumber = args[1];
        UDPListener l = new UDPListener(hostname, portNumber);
        l.run();
    }
}