package de.rub.nds.virtualnetworklayer;

import de.rub.nds.virtualnetworklayer.connection.Connection;
import de.rub.nds.virtualnetworklayer.connection.pcap.PcapConnection;
import de.rub.nds.virtualnetworklayer.connection.socket.SocketConnection;
import de.rub.nds.virtualnetworklayer.packet.Packet;

import java.io.IOException;

/**
 * This class demonstrates {@link SocketConnection} and {@link PcapConnection}.
 * </p>
 * exemplary output:
 * <pre>
 * [192.168.6.30, 51473 | 173.194.35.151, 80]
 * 1340708736153 Response
 * HTTP/1.0 302 Found...
 * ---
 * [192.168.6.30, 51474 | 173.194.35.151, 80]
 * 1340709460690 Response [192.168.6.30, 51474 | 173.194.35.151, 80]
 * �,"`g�Cf� E  4SY  8�K��#��� P���Hp=���Ā ާ�
 * aI$�u
 * 1340705543569 Extended Response [173.194.35.151, 80 | 192.168.6.30, 51156]
 * �,"`g�Cf� E  4S\  8�H��#��� P���HxA���Ā ޟ�
 * aI$��uHTTP/1.0 302 Foun...
 * </pre>
 *
 * @author Marco Faltermeier <faltermeier@me.com>
 */
public class ConnectionDemo {
    private static Connection connection;

    public static void main(String[] args) throws IOException, InterruptedException {
        //create connection solely based on sockets api
        connection = SocketConnection.create("www.google.de", 80);
        printConnection(1);

        System.out.println("---");
        System.out.println("");

        //create connection based on sockets api and pcap
        connection = PcapConnection.create("www.google.de", 80);
        //read two packets, since there is an empty one
        printConnection(2);
    }

    private static void printConnection(int readPacketsCount) {
        try {
            String request = "GET / HTTP/1.0 \r\n\r\n";
            connection.write(request.getBytes());

            System.out.println(connection);
            readPackets(readPacketsCount);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    private static void readPackets(int count) throws IOException {
        for (int i = 0; i < count; i++) {
            Packet packet = connection.read(1000);
            System.out.println(packet);
            System.out.println(new String(packet.getContent()));
        }
    }

}
