package cn.dceast.platform.gateway.common.security;     
     
public class Base64 {     
    final static String baseTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";     
     
    /** 
     * 
     *   Base64 使用US-ASCII子集的64个字符,即大小写的26个英文字母，0－9，＋，/。
		编码总是基于3个字符，每个字符用8位二进制表示，因此一共24位，再分为4四组，每组6位，表示一个Base64的值。如下：
		"A", "B", "C", "D", "E", "F", "G", "H", "I","J", "K", "L", "M", "N", "O", "P",
		"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "a", "b", "c", "d", "e", "f",
		"g", "h", "i","j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v",
		"w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "+", "/" 
		Base64值为0就是A，为27的就是b。这样，每3个字符产生4位的Base64字符。如果被加密的字符串每3个一组，还剩1或2个字符，使用特殊字符"="补齐Base64的4字。
		
		如，编码只有2个字符“me”，m的ascii是109，e的是101，用二进制表示分别是01101101、01100101，连接起来就是0110110101100101，再按6位分为一组：011011、010110、010100（不足6位补0），分别ascii分别是27、22、20，即Base64值为bWU，Base64不足4字，用＝补齐，因此bWU＝就me的Base64值。
		
		用java的按位逻辑和移位运算就可以实现该算法。但实际上，并不用我们自己去编程实现。现有实现该加密解密算法的程序很多，如javamail的MimeUtility。
     * Encode a byte array.    
     *    
     * @param bytes a byte array to be encoded.    
     * @return encoded object as a String object.    
     */     
    public static String encode(byte[] bytes) {     
     
        StringBuffer tmp = new StringBuffer();     
        int i = 0;     
        byte pos;      
     
        for(i=0; i < (bytes.length - bytes.length%3); i+=3) {     
     
            pos = (byte) ((bytes[i] >> 2) & 63);      
            tmp.append(baseTable.charAt(pos));      
     
            pos = (byte) (((bytes[i] & 3) << 4) + ((bytes[i+1] >> 4) & 15));      
            tmp.append(baseTable.charAt( pos ));     
                         
            pos = (byte) (((bytes[i+1] & 15) << 2) + ((bytes[i+2]  >> 6) & 3));     
            tmp.append(baseTable.charAt(pos));     
             
            pos = (byte) (((bytes[i+2]) & 63));     
            tmp.append(baseTable.charAt(pos));     
             
        }     
     
        if(bytes.length % 3 != 0) {     
     
            if(bytes.length % 3 == 2) {     
     
                pos = (byte) ((bytes[i] >> 2) & 63);      
                tmp.append(baseTable.charAt(pos));      
     
                pos = (byte) (((bytes[i] & 3) << 4) + ((bytes[i+1] >> 4) & 15));      
                tmp.append(baseTable.charAt( pos ));     
                             
                pos = (byte) ((bytes[i+1] & 15) << 2);     
                tmp.append(baseTable.charAt(pos));     
                 
                tmp.append("=");     
     
            } else if(bytes.length % 3 == 1) {     
                     
                pos = (byte) ((bytes[i] >> 2) & 63);      
                tmp.append(baseTable.charAt(pos));      
     
                pos = (byte) ((bytes[i] & 3) << 4);      
                tmp.append(baseTable.charAt( pos ));     
                             
                tmp.append("==");     
            }     
        }     
        return tmp.toString();     
     
    }     
     
    /**   
     * Encode a String object.    
     *    
     * @param src a String object to be encoded with Base64 schema.    
     * @return encoded String object.    
     */     
    public static String encode(String src) {     
             
        return encode(src.getBytes());       
    }     
     
    public static byte[] decode(String src) throws Exception {     
     
        byte[] bytes = null;     
     
        StringBuffer buf = new StringBuffer(src);     
     
        // First, Remove white spaces (\r\n, \t, " ");     
        int i = 0;     
        char c = ' ';     
        char oc = ' ';     
        while( i < buf.length()) {                
            oc = c;      
            c = buf.charAt(i);     
            if( oc == '\r' && c == '\n') {     
                buf.deleteCharAt(i);     
                buf.deleteCharAt(i-1);     
                i -= 2;     
            } else if( c == '\t') {     
                buf.deleteCharAt(i);     
                i --;     
            } else if( c == ' ') {     
                i --;     
            }     
            i++;     
        }     
     
        // The source should consists groups with length of 4 chars.      
        if(buf.length() % 4 != 0) {     
            throw new Exception("Base64 decoding invalid length");     
        }     
     
        // pre-set byte array size.     
        bytes = new byte[3 * (buf.length() / 4)];     
        //int len = 3 * (buf.length() % 4);      
        //System.out.println("Size of Bytes array: " + len);     
        int index = 0;     
             
        // Now decode each group     
        for(i = 0; i < buf.length(); i+=4) {     
     
            byte data = 0;     
            int nGroup = 0;     
     
            for(int j = 0; j < 4; j++) {     
     
                char theChar = buf.charAt(i + j);      
     
                if(theChar == '=') {     
                    data = 0;     
                } else {     
                    data = getBaseTableIndex(theChar);      
                }     
     
                if(data == -1) {     
                    throw new Exception("Base64 decoding bad character");     
                }     
     
                nGroup = 64*nGroup + data;     
            }     
     
            bytes[index] = (byte) (255 & (nGroup >> 16));     
            index ++;     
     
            bytes[index] = (byte) (255 & (nGroup >> 8));     
            index ++;     
     
            bytes[index] = (byte) (255 & (nGroup));     
            index ++;     
        }     
             
        byte[] newBytes = new byte[index];     
        for(i = 0; i < index; i++) {     
            newBytes[i] = bytes[i];     
        }     
     
        return newBytes;     
    }     
     
    /**   
     * Find index number in base table for a given character.    
     *    
     */     
    protected static byte getBaseTableIndex(char c) {     
             
        byte index = -1;     
     
        for(byte i = 0; i < baseTable.length(); i ++) {     
             
            if(baseTable.charAt(i) == c) {     
                index = i;     
                break;     
            }     
        }     
     
        return index;     
    }     
         
    public static void main(String[] args) throws Exception {     
        String encodedString = Base64.encode("大幅度反对法地方".getBytes());     
        System.out.println(encodedString); 
        //System.out.println(new String(Base64.decode("XNjA1NjYxOTQw")));
    }     
}    
