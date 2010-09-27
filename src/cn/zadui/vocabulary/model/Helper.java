package cn.zadui.vocabulary.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Helper {
	
	public static int currentSecTime(){
		Long l=System.currentTimeMillis()/1000;
		return l.intValue();
	}
	
	public static int millToSec(long mill){
		return new Long(mill/1000).intValue();
	}
	
    /**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArray2Int(byte[] b) {
        return byteArray2Int(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArray2Int(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }	
    
    public static short byteArray2Short(byte[] b, int offset){
    	ByteBuffer bb = ByteBuffer.allocate(2);
    	bb.order(ByteOrder.BIG_ENDIAN);
    	bb.put(b[0 + offset]);
    	bb.put(b[1 + offset]);
    	return bb.getShort(0);
    }
    
    public static String[] friendlyTime(int sec){
    	int cur=(int)System.currentTimeMillis();
    	int interval=cur-sec;
    	interval=interval/1000;
    	if (interval<(3600*24)){
    		return new String[]{String.valueOf(interval/3600),"hour"};
    	}else if (interval<3600*24*30){
    		return new String[]{String.valueOf(interval/(3600*24)),"day"};
    	}else{
    		return new String[]{String.valueOf(interval/(3600*24*20)),"month"};
    	}
    }
    
    
    public static void main(String[] args){
    	try {
    		byte[] buffer=new byte[8];
			InputStream in=new FileInputStream("/tmp/4/4.all");
			in.read(buffer,0,8);
			int i=byteArray2Int(buffer);
			System.out.println(i);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
