import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
            ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            //write server info to console
            System.out.println("Web server starting! Running on port " + server.getLocalPort());
            //Infinite loop
            while(true){
                Socket client = server.accept();
                System.out.println("New connection! " + client.getLocalAddress());
                MyTLSFileServerSession task = new MyTLSFileServerSession(client);
                exec.execute(task);
            }

        }catch (Exception ex){
            System.err.println(ex.getMessage());
        }
    }
}

class MyTLSFileServerSession implements Runnable {
    private BufferedOutputStream writer;
    //Datafields ##########################################
    private Socket socket;
    private BufferedReader reader;

    //Constructor #########################################
    //Creates a server session that handles a single client socket
    public MyTLSFileServerSession(Socket s) {
        try {
            socket = s;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    //Public Methods ######################################
    @Override
    public void run() {
        try {
            //Declare variables
            String line;
            String[] parts;
            String filename = "";
            //If there is input from the client
            if ((line = reader.readLine()) != null) {
                parts = line.split(" ");
                //If it is a valid request print out the requested file
                if (parts.length == 3 && parts[0].equals("GET")) {
                    filename = parts[1].substring(1);
                    System.out.println(filename);
                }
                //Go through the remaining parts of the HTTP request
                while (true) {
                    line = reader.readLine();
                    //If there is an error close the connection
                    if (line == null) {
                        socket.close();
                        //If we have reached the end break out of the loop
                    } else if (line.equals("")) {
                        break;
                    }
                }
                writeFile(filename);
            }
            //Close connection
            socket.close();

        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    //Sends a string to the client
    private void writeln(String s) throws IOException {
        //Adds new line characters necessary for HTTP protocol
        String line = (s + "\r\n");
        //Send as a byte stream
        byte[] array = line.getBytes();
        for (byte b : array) {
            writer.write(b);
        }
        writer.flush();
    }

    //Sends a file to the client
    private void writeFile(String filename) {

        try {
            //Declare objects
            byte[] buffer = new byte[1024];
            //Looks for a file on the desktop
            File file = new File("/Users/justin/Desktop/" + filename);
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
            System.out.println("Requested file found!");


        } catch (Exception ex) {
            //The file does not exist so print error message
            System.out.println("Requested file " + filename + " not found :(");
            //Send 404 error
            error404();
        }
    }
    //Sends a 404 error
    private void error404() {
        try {
            writeln("HTTP/1.1 404 Not Found");
            writeln("");
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
}
