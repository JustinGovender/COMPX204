import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MyTLSFileClient {


    //Main ##########################################
    public static void main(String[] args) {
        //Check args
        if (args.length != 4) {
            System.out.println("Usage: <server_ip> <server_port> <filename> <output_filename>");
            System.exit(0);
        }
        //Create client
        MyTLSFileClient client = new MyTLSFileClient(args[0], Integer.parseInt(args[1]), args[3]);
        //Get file from server
        client.GetFile(args[2]);
    }

    //Datafields ##########################################
    private FileOutputStream writer;
    private DatagramSocket socket;
    private InetSocketAddress serverAddress;
    private final String pathName = "/Users/justin/Desktop/";
    private byte currentBlockSeq = 1;
    private DatagramPacket data;
    private final int TIMEOUT = 1000;

    //Constructor #########################################
    public MyTLSFileClient(String serverIp, int serverPort, String outFile) {
        try {
            socket = new DatagramSocket();
            serverAddress = new InetSocketAddress(InetAddress.getByName(serverIp), serverPort);
            writer = new FileOutputStream(pathName + outFile);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //Methods #############################################
    public void GetFile(String filename) {
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
