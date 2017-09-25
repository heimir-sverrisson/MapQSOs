package com.coolprimes.mapqsos;

import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Consumer extends Thread{
    private BlockingQueue<QMessage> queue;
    private volatile boolean running;
    final Logger log = LoggerFactory.getLogger(Consumer.class);

    Consumer(BlockingQueue<QMessage> queue){
        this.queue = queue;
        log.debug("Constructed");
    }

    public void shutdown(){
        running = false;
    }

    public void run(){
        running = true;
        while(running){
            QMessage msg = null;
            try{
                msg = queue.take(); // Blocking call to dequeue
            } catch(InterruptedException e){
                log.error("Interrupted taking from the queue: " + e.getLocalizedMessage());
                running = false;
                break;
            }
            switch(msg.getMessageType()){
                case CLOSE:
                    log.debug("Got a close message from WSJT-X");
                    running = false;
                    break;
                case DECODE:
                    String message = ((QDecodeMessage)msg).message;
                    log.debug("Message: " + message);
                    break;
                case STATUS:
                    QStatusMessage status = (QStatusMessage)msg;
                    log.debug("Frequency: " + status.dialFrequency +
                              ", Mode: " + status.mode);
                    break;
                case REPLAY:
                case QSO_LOGGED:
                case REPLY:
                case HEARTBEAT:
                case CLEAR:
                case FREE_TEXT:
                case HALT_TX:
                case WSPR_DECODE:
            }
        }
        log.debug("Thread exiting");
    }
}