import java.net.*;
import java.io.*;

import java.lang.*;
//System.out.println("");

class client{
  public static void main(String[]as){
    try{
      Socket s=new Socket("localhost",8084);

      OutputStream os=s.getOutputStream();
      InputStream is=s.getInputStream();
      System.out.println(""+(char)is.read());
    }catch(Exception e){}
  }
}