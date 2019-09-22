
import java.io.*;
import java.net.*;


public class MyTLSFileClient {


    //Main ##########################################
    public static void main(String[] args) {
        //Check args
        if (args.length != 3) {
            System.out.println("Usage: <server_hostname> <server_port> <filename>");
            System.exit(0);
        }
        try{
            //Create socket
            Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
            //Create client
            MyTLSFileClient client = new MyTLSFileClient(socket);
            //Get file from server
            client.GetFile(args[2]);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //Datafields ##########################################
    private BufferedOutputStream serverWriter;
    private BufferedReader serverReader;
    private FileOutputStream writer;
    private InputStream reader;
    private Socket socket;
    private final String pathName = "/Users/justin/Desktop/";

    //Constructor #########################################
    public MyTLSFileClient(Socket s) throws IOException{
        socket = s;
        serverWriter = new BufferedOutputStream(socket.getOutputStream());
        serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    //Methods #############################################
    private void GetFile(String filename) throws IOException {
        //Declare objects
        File file = new File(pathName + "_" + filename);
        writer = new FileOutputStream(file);
        reader = socket.getInputStream();
        String line;
        byte[] buffer = new byte[16384];//16KiB
        int length = 0;

        //Send HTTP Request to server
        writeln("GET /" +filename+  " HTTP/1.1");
        writeln("");

        //Read a chunk of the file
        length = reader.read(buffer);
        //While not at the end of the file...
        while (length != -1) {
            writer.write(buffer, 0, length);
            writer.flush();
            //Read in the next chunk
            length = reader.read(buffer);
        }
        //Tidy up
        reader.close();
        writer.close();
        System.out.println("Requested file found!");

    }

    //Sends a string to the server
    private void writeln(String s) throws IOException {
        //Adds new line characters necessary for HTTP protocol
        String line = (s + "\r\n");
        //Send as a byte stream
        byte[] array = line.getBytes();
        for (byte b : array) {
            serverWriter.write(b);
        }
        serverWriter.flush();
    }


}
