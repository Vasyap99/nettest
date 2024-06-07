import java.net.*;
import java.io.*;

import java.lang.*;

import java.util.*;


import java.text.*; 

import java.nio.file.Files;
import java.nio.file.Paths;

class httpReq{

    String mimetype=""; //тип mime для запрашиваемого файла(уст.в readData())
    String fl="";       //имя запрашиваемого файла
    String ext="";      //расширение запрашиваемого файла
    String argStr="";   //параметры передаваемые в адресной строке
    String reqType="";  //тип запроса(GET/POST)


    public httpReq(){}

    String getExt(String filename){
      String[]ps=filename.split("[.]");
      return ps[ps.length-1];
    }

    String getMimeType(String ext){
      if(ext.equals("htm"))
	return "text/html";
      else if(ext.equals("js"))
	return "text/javascript";
      else if(ext.equals("css"))
	return "text/css";
      else
	return "application/octet-stream";
    }

    /*byte h2d(char c){
    }
    String h2d(String s){
            
    } */
    String performHttpStr(String s){
        String buf="";
        int i=0;
        while(i<s.length()){
            if(s.charAt(i)!='%'){
                buf=buf+s.charAt(i); 
                i++;
            }else{
                i++; 
                int j=i; 
                String bf2="";
                while(j<s.length() && j-i<2){  //j<s.length() && (s.charAt(j)>='0' && s.charAt(j)<='9' || s.charAt(j)>='a' && s.charAt(j)<='f' || s.charAt(j)>='A' && s.charAt(j)<='F')
                    bf2+=s.charAt(j);
                    j++;
                }
                buf+=(char)Integer.parseInt(bf2,16);
                i=j;
            } 
        }
        print2(this,buf);
        return buf;
    }
    void parseFirstString(String s){
	String []ps1=s.split("[ ]");
	//print ps1
	reqType=ps1[0];
	String[]ps2=ps1[1].split("[?]");
	//print ps2
	fl=performHttpStr(ps2[0]);
	try{
	    argStr=ps2[1];
	}catch(Exception e){
	    argStr="";
        }
    }
	
    String testBuf0="";
    void readData(InputStream is) throws Exception{
        testBuf0="";
        System.out.println("::readData");
	String s=""; //#считываемая строка запроса
	boolean emptyS=false;
	int i=0;  //#счетчик строк запроса
	while(!emptyS){
	    int c=' ';
	    while(c!='\n'){
		c=is.read();
		if(c!='\n' && c!='\r')
		    s=s+(char)c;
            }
            testBuf0=testBuf0+"---"+s+"---\r\n";
	    if(i==0){
		parseFirstString(s);
		if(fl.equals("/")){
		    fl="/index.htm";
                }
		ext=getExt(fl);
		mimetype=getMimeType(ext);
            }
	    emptyS=s.equals("");
	    s="";
	    i++;
        }
        print1(this);
    }
    static synchronized void print1(httpReq hr){
        System.out.println(hr.testBuf0);
        System.out.println(hr.mimetype);
        System.out.println(hr.fl);
        System.out.println(hr.ext);
        System.out.println(hr.argStr);
        System.out.println(hr.reqType);/* */
    }
    static synchronized void print2(httpReq hr,String s){
        System.out.println(s);
    }
}


class clientServe extends Thread{
    OutputStream os;
    InputStream is;
    Socket cs;
    public clientServe(Socket cs) throws Exception{
      this.is=cs.getInputStream();
      this.os=cs.getOutputStream();
      this.cs=cs;
    }

    void writeS(OutputStream os,String data) throws Exception{
        os.write((data+"\r\n").getBytes());
    }

    void mysend(OutputStream os1,String CT,String fl) throws Exception{
      byte[]data;
      try{
        data=Files.readAllBytes(Paths.get("."+fl)); //kpy.open_and_read("."+fl); //#"<html><body>kok"
      }catch(Exception e){
        data=new byte[0];
      }
      //
      Calendar cal=Calendar.getInstance();
      SimpleDateFormat sd1=new SimpleDateFormat("EEE, dd");
      SimpleDateFormat sd2=new SimpleDateFormat("MM");
      SimpleDateFormat sdf=new SimpleDateFormat("yyyy HH:mm:ss");
      HashMap hm=new HashMap();
      hm.put("01","Jan");
      hm.put("02","Feb");
      hm.put("03","Mar");
      hm.put("04","Apr");
      hm.put("05","May");
      hm.put("06","Jun");
      hm.put("07","Jul");
      hm.put("08","Aug");
      hm.put("09","Sep");
      hm.put("10","Oct");
      hm.put("11","Nov");
      hm.put("12","Dec");
      //
      int siz=data.length;
      writeS(os1,"HTTP/1.1 200 OK");
      writeS(os1,"Server: kokoserv/4");
      String mnth=null;
      try{
          mnth=(String)hm.get(sd2.format(cal.getTime()));
      }catch(Exception e){
          mnth=sd2.format(cal.getTime());
      } 
      String dt=sd1.format(cal.getTime())+" "+mnth+" "+sdf.format(cal.getTime());  System.out.println("dt=="+dt);
      writeS(os1,"Date: "+dt+" GMT");
      writeS(os1,"Content-Type: "+CT);
      writeS(os1,"Content-Length: "+Integer.toString(siz));
      writeS(os1,"Last-Modified: Sat, 21 Feb 2009 11:08:01 GMT");
      writeS(os1,"Connection: close");
      writeS(os1,"Content-Disposition: inline");
      writeS(os1,"Pragma: public");
      writeS(os1,"Accept-Ranges: bytes");
      writeS(os1,"Content-Encoding: identity");
      writeS(os1,"");
      os1.write(data);
    }

    public void run(){
	httpReq req=new httpReq();
        try{ 
	    req.readData(is);
            mysend(os,req.mimetype,req.fl); //#conn.send(data.upper())
	    cs.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    } 
}

class serv{
  public static void main(String[]s){
    try{
      ServerSocket ss=new ServerSocket(8084);
      while(true){
        Socket cs=ss.accept();

        clientServe csv=new clientServe(cs);
        csv.start();
      }
    }catch(Exception e){
      e.printStackTrace(); 
    }
  }
}