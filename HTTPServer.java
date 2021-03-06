import java.io.*;
import java.net.*;
/*
 * class HTTPServer
 * 
 * It is very simple,just like the ShiYanYi.
 * 
 */
public class HTTPServer {
	static public int serverPort = 8888;
	static public String B_DIR = "C:/Users/Administrator/indix/HTTPServerWorkspace";
	static public void main(String[] args){
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(serverPort);
			while( true )
				new BrowserThread(ss.accept()).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class BrowserThread extends Thread{
	public Socket bro = null;
	public BufferedReader reader = null;
	public PrintWriter writer = null;
	public BufferedInputStream istream = null;
	public BufferedOutputStream ostream = null;
	public String fileName = null;
	public BrowserThread(Socket bro){
		this.bro = bro;
	}
	public void run(){
		boolean connect = true;
		try {
			reader = new BufferedReader(new InputStreamReader(bro.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(bro.getOutputStream()));
			ostream = new BufferedOutputStream(bro.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
			ret500(); connect = false;
		}												//Parameter initialization
		while(connect){
			try {
				String msg = reader.readLine();			//get line one
				if(msg == null)throw new IOException();
				fileName = HTTPServer.B_DIR+msg.split(" ")[1];
				while(!(msg=reader.readLine()).equals("")){
					if(msg.startsWith("Connection")&&msg.split(":")[1].equals("close")){
						connect = false;
					}
				}
				if(!new File(fileName).exists())throw new FileNotFoundException();
				ret200();								//return 200
			} catch (FileNotFoundException e){ret404();	//return 404
			} catch (IOException e) {ret500();connect = false;
														//return 500
			} catch (ArrayIndexOutOfBoundsException e){ret404();connect = false;
			}											//return 404
		}
		try {
			if(istream != null)istream.close();
			ostream.close();
			reader.close();
			writer.close();
		} catch (IOException e) {e.printStackTrace();}
		
	}
	public void ret200() throws IOException{			//the function can return 200
		writer.println("HTTP/1.1 200 ok");
        writer.flush();
        if(fileName.endsWith(".html"))writer.println("Content-type:text.html");
        else if(fileName.endsWith(".jpg"))writer.println("Content-type:image.jpg");
        else writer.println("Content-type:application/octet-stream");
        writer.flush();
    	istream = new BufferedInputStream(new FileInputStream(fileName));
		writer.println("Content-Length:"+ istream.available());
		writer.println();
        writer.flush();
        byte[] bb=new byte[1024];
        int len = 0;
        while ((len = istream.read(bb)) > 0 ) {
        	ostream.write(bb,0,len);
        }
        ostream.flush();
	}
	public void ret404(){								//the function can return 404
		String msg = "<script>window.location.href='error/404.html'</script>";
		writer.println("HTTP/1.1 404 Not Found");
		writer.flush();
		writer.println("Content-type:text/html");
		writer.println("Content-Length:" + msg.length());
		writer.println();
		writer.print(msg);
														//use JavaScript to jump to the 404 page 
		writer.flush();
	}
	public void ret500(){								//the function can return 500
		String msg = "<script>window.location.href='error/500.html'</script>";
		writer.println("HTTP/1.1 500 Internal Server Error");
		writer.println("Content-type:text/html");
		writer.println("Content-Length:" + msg.length());
		writer.println();
		writer.print(msg);
														//use JavaScript to jump to the 500 page
		writer.flush();
	}
}