package com.coolprimes.mapqsos;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

class MessageDecoder{
    private int schemaVersion;
    private int messageType;
    byte [] rData;
    int byteCount;
    int nextByte = 0;
    final Logger log = LoggerFactory.getLogger(MessageDecoder.class);
    
    MessageDecoder(byte[] rData, int byteCount){
        this.rData = rData;
        this.byteCount = byteCount;
    }
    
    private byte[] nextFour(){
        byte[] res = new byte[4];
        for(int i=0; i < 4; i++){
            res[i] = rData[nextByte++];
        }
        return res;
    }

    private double nextDouble(){
        byte[] res = new byte[8];
        for(int i=0; i < 8; i++){
            res[i] = rData[nextByte++];
        }
        return ByteBuffer.wrap(res).getDouble();
    }

    private String nextString(){
        if(byteCount <= nextByte){
            log.debug("nextString when byteCount is {} and nextByte is {}", byteCount, nextByte);
            return "";
        }
        int strSize = nextInt();
        if(strSize == 0xffffffff){
            log.debug("nextString is empty!");
            return "";
        }
        byte [] bytes = new byte[strSize];
        System.arraycopy(rData, nextByte, bytes, 0, strSize);
        String str = new String(rData,nextByte,strSize);
        nextByte += strSize;
        return str;
    }

    private int getIntInOrder(ByteBuffer bb){
        return bb.order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private int nextInt(){
        byte[] theWord = nextFour();
        ByteBuffer bb = ByteBuffer.wrap(theWord);
        return getIntInOrder(bb);
    }

    private void parseDecode(){
        String id = nextString();
        log.debug("Id: {}", id);
        boolean newDecode = nextBool();
        log.debug("New: {}", newDecode);
        int time = nextTime();
        log.debug("Time: {}", formatTime(time));
        int snr = nextInt();
        log.debug("SNR: {}", snr);
        double deltaTime = nextDouble();
        int deltaF = nextInt();
        log.debug("dT: {}, dF {}", 
            formatDeltaTime(deltaTime), deltaF);
        String mode = nextString();
        log.debug("mode: {}", mode);
        String message = nextString();
        log.debug("message: {}", message);
        boolean lowConfidence = nextBool();
        log.debug("lowConfidence: {}", lowConfidence);
    }

    private String formatDeltaTime(double deltaTime){
        return String.format("%4.1f", deltaTime);
    }

    private String formatTime(int msec){
        int t = msec;
        int hours = msec/(60 * 60000);
        t -= 60 * 60000 * hours;
        int mins = t/(60000);
        t -= 60000 * mins;
        int secs = t/1000;
        t -= 1000 * secs;
        int msecs = t;
        return String.format("%02d:%02d:%02d.%03d", hours, mins, secs, msecs); 
    }

    private boolean nextBool(){
        byte b = rData[nextByte++];
        return (b == 0) ? false : true;
    }

    private int nextTime(){
        return nextInt();
    }
    boolean decode() {
        byte[] theMagic = {(byte)0xad, (byte)0xbc, (byte)0xcb, (byte)0xda};
        byte[] theWord = nextFour();
        if(Arrays.equals(theWord, theMagic)){
            log.debug("Magic start is correct");
        } else {
            log.debug("Magic start of stream is wrong: {}", theWord);
        }
        schemaVersion = nextInt();
        log.debug("The schema version is: {}", schemaVersion);
        messageType = nextInt();
        switch(messageType){
            case 0: 
                log.debug("Got Heartbeat");
                break;
            case 1: 
                log.debug("Got Status");
                break;
            case 2:
                log.debug("Got Decode");
                parseDecode();
                break;
            case 3:
                log.debug("Got Clear");
                break;
            case 4:
                log.debug("Got Reply");
                break;
            case 5:
                log.debug("Got QSO Logged");
                break;
            case 6:
                log.debug("Got Close");
                break;
            case 7:
                log.debug("Got Relay");
                break;
            case 8:
                log.debug("Got Halt Tx");
                break;
            case 9:
                log.debug("Got Free Text");
                break;
            case 10:
                log.debug("Got WSPRDecode");
                break;
            default:
                log.debug("Unknown messageType: {}", messageType);
                return false;
        }
        return true;
    }
}