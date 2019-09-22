import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyTLSFileServer {
    private static final ExecutorService exec = Executors.newFixedThreadPool(10);
    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: java MyTLSFileServer <server_port>");
            System.exit(0);
        }
        try{
            //Set up Keystore
            KeyStore ks = KeyStore.getInstance("JKS");
            char[] password = "password".toCharArray();
            ks.load(new FileInputStream("server.jks"), password);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks,password);
            //Set up SSL
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), null, null);
            //Create factory
            ServerSocketFactory ssf = ctx.getServerSocketFactory();
            //Create SSL server socket
            SSLServerSocket sserverSocket = (SSLServerSocket) ssf.createServerSocket(Integer.parseInt(args[0]));
            sserverSocket.setEnabledProtocols(new String[]{"TLSv1.2", "TLSv1.1"});
            //write server info to console
            System.out.println("Web server starting! Running on port " + sserverSocket.getLocalPort());

            //Infinite loop
            while(true){

                SSLSocket sclient = (SSLSocket) sserverSocket.accept();
                System.out.println("New connection! " + sclient.getLocalAddress());
                MyTLSFileServerSession task = new MyTLSFileServerSession(sclient);
                exec.execute(task);
            }

        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }
    }
}

class MyTLSFileServerSession implements Runnable {
    //Datafields ##########################################
    private Socket socket;
    private BufferedReader reader;

    //Constructor #########################################
    //Creates a server session that handles a single client socket
    public MyTLSFileServerSession(Socket s) {
        try {
            socket = s;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    //Public Methods ######################################
    @Override
    public void run() {
        try {
            //Declare variables
            String filename;
            //Readline to starts TLS handshake
            reader.readLine();

            filename = reader.readLine();
            System.out.println(filename);
            writeFile("/home/jmg66/Documents/COMPX204/COMPX204/" + filename);
            //Close connection
            socket.close();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    //Sends a file to the client
    private void writeFile(String filename) {

        try {
            //Declare objects
            byte[] buffer = new byte[1024];
            //Looks for the file
            File file = new File(filename);
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            FileInputStream reader = new FileInputStream(file);

            //Read a chunk of the file
            int length = reader.read(buffer);
            //While not at the end of the file...
            while (length != -1) {
                out.write(buffer, 0, length);
                out.flush();
                //Read in the next chunk
                length = reader.read(buffer);
            }
            //Tidy up
            out.close();
            reader.close();
            System.out.println("Requested file sent!");


        } catch (Exception ex) {
            //The file does not exist so print error message
            System.out.println("Requested file " + filename + " not found :(");
        }
    }
}
