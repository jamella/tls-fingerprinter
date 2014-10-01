package de.rub.nds.ssl.analyzer.vnl.fingerprint.serialization;

import de.rub.nds.ssl.analyzer.vnl.SessionIdentifier;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.ClientHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.Fingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.ServerHelloFingerprint;
import de.rub.nds.ssl.analyzer.vnl.fingerprint.TLSFingerprint;
import de.rub.nds.ssl.stack.Utility;
import de.rub.nds.ssl.stack.protocols.commons.ECipherSuite;
import de.rub.nds.ssl.stack.protocols.commons.ECompressionMethod;
import de.rub.nds.ssl.stack.protocols.commons.EProtocolVersion;
import de.rub.nds.ssl.stack.protocols.commons.Id;
import de.rub.nds.ssl.stack.protocols.handshake.extensions.datatypes.EExtensionType;
import de.rub.nds.virtualnetworklayer.p0f.signature.MTUSignature;
import de.rub.nds.virtualnetworklayer.p0f.signature.TCPSignature;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility to serialize {@link Fingerprint}s.
 *
 * Add all types that may be signs and their serialization type to <code>serializeSign()</code>
 * <br>
 * TODO: rework this with an architecture
 *
 * @author jBiegert azrdev@qrdn.de
 */
public class Serializer {
    private static Logger logger = Logger.getLogger(Serializer.class);

    /**
     * Build the Serialized form of a sign
     */
    public static String serializeSign(Object sign) {
        if(sign == null)
            return "";

        //FIXME: better mapping & dispatch of sign-type -> serialization method. Maybe use Visitor for TLSFingerprint.serialize()

        if(sign instanceof Object[])
            return serializeList((Object[]) sign);
        else if(sign instanceof byte[]) {
            logger.debug("Serializing byte[]");
            return Utility.bytesToHex((byte[]) sign, false);
        } else if(sign instanceof Collection)
            return serializeList((Collection) sign);

        else if(sign instanceof Id)
            return Utility.bytesToHex(((Id) sign).getBytes(), false);
        else if(sign instanceof EProtocolVersion)
            return Utility.bytesToHex(((EProtocolVersion) sign).getId(), false);
        else if(sign instanceof ECompressionMethod)
            return Utility.bytesToHex(((ECompressionMethod) sign).getId(), false);
        else if(sign instanceof ECipherSuite)
            return Utility.bytesToHex(((ECipherSuite) sign).getId(), false);
        else if(sign instanceof EExtensionType)
            return Utility.bytesToHex(((EExtensionType) sign).getId(), false);
        else
            return sign.toString();
    }

    public static final String LIST_DELIMITER = ",";

    private static String serializeList(Collection arr) {
        StringBuilder sb = new StringBuilder();

        for(Object o : arr) {
            // recursive call to serialize that element
            // never put Object[] as sign value, or this will break!
            sb.append(serializeSign(o)).append(LIST_DELIMITER);
        }
        if(sb.length() > 0)
            // delete trailing delimiter
            sb.setLength(sb.length() - LIST_DELIMITER.length());

        return sb.toString();
    }

    private static String serializeList(Object[] arr) {
        return serializeList(Arrays.asList(arr));
    }

    public static String serialize(SessionIdentifier session,
                                   TLSFingerprint tlsFingerprint) {
        return session.serialize() + tlsFingerprint.serialize();
    }

    public static String serialize(TLSFingerprint fp) {
        return serializeServerHello(fp.getServerHelloSignature())
                + Serializer.serializeServerTcp(fp.getServerTcpSignature())
                + Serializer.serializeServerMtu(fp.getServerMtuSignature());
    }

    public static String serializeClientHello(Fingerprint clientHelloSignature) {
        if(clientHelloSignature == null) {
            return "";
        }

        return String.format("\t%s: %s\n", FingerprintId.ClientHello.id,
                clientHelloSignature.serialize());
    }

    public static String serializeServerHello(Fingerprint serverHelloSignature) {
        if(serverHelloSignature == null) {
            return "";
        }

        return String.format("\t%s: %s\n", FingerprintId.ServerHello.id,
                serverHelloSignature.serialize());
    }

    public static String serializeServerTcp(
            de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature sig) {
        if(sig == null)
            return "";

        return String.format("\t%s: %s\n", FingerprintId.ServerTcp.id,
                TCPSignature.writeToString(sig));
    }

    public static String serializeServerMtu(
            de.rub.nds.virtualnetworklayer.fingerprint.Fingerprint.Signature sig) {
        if(sig == null)
            return "";

        return String.format("\t%s: %s\n", FingerprintId.ServerMtu.id,
                MTUSignature.writeToString(sig));
    }

    public static List<Id> deserializeList(String serialized) {
        List<Id> bytes = new ArrayList<>(serialized.length());
        for(String item : serialized.split(LIST_DELIMITER, -1)) {
            bytes.add(new Id(Utility.hexToBytes(item.trim())));
        }

        return bytes;
    }

    public static Map<SessionIdentifier, List<TLSFingerprint>>
    deserialize(BufferedReader reader) throws IOException {
        Map<SessionIdentifier, List<TLSFingerprint>> fingerprints = new HashMap<>();

        String line;
        SessionIdentifier sid = null;
        ClientHelloFingerprint clientHelloSignature = null;
        ServerHelloFingerprint serverHelloSignature = null;
        TCPSignature serverTcpSignature = null;
        MTUSignature serverMtuSignature = null;

        while((line = reader.readLine()) != null) {
            final String line_trimmed = line.trim();

            if(line_trimmed.startsWith("#"))
                continue;
            if(line_trimmed.isEmpty())
                continue;

            if(line.startsWith("\t")) {
                try {
                    String[] split = line.trim().split("\\s", 2);

                    if (FingerprintId.ClientHello.isAtStart(split[0])) {
                        clientHelloSignature = new ClientHelloFingerprint(split[1]);
                        if(sid != null) sid.setClientHelloSignature(clientHelloSignature);
                    } else if (FingerprintId.ServerHello.isAtStart(split[0])) {
                        serverHelloSignature = new ServerHelloFingerprint(split[1]);
                    } else if (FingerprintId.ServerTcp.isAtStart(split[0])) {
                        serverTcpSignature = new TCPSignature(split[1]);
                    } else if (FingerprintId.ServerMtu.isAtStart(split[0])) {
                        serverMtuSignature = new MTUSignature(split[1]);
                    } else {
                        logger.debug("Unrecognized signature: " + line);
                    }
                } catch(IllegalArgumentException ex) {
                    logger.debug("Error reading signature: " + line);
                }
            } else {
                commitFingerprint(sid, new TLSFingerprint(serverHelloSignature,
                                serverTcpSignature, serverMtuSignature),
                        fingerprints);

                try {
                    sid = new SessionIdentifier(line);
                } catch(IllegalArgumentException e) {
                    logger.debug("Error reading SessionIdentifier: " + e, e);
                    sid = null;
                }
            }
        }

        commitFingerprint(sid, new TLSFingerprint(serverHelloSignature,
                serverTcpSignature, serverMtuSignature), fingerprints);

        return fingerprints;
    }

    /**
     * helper for {@link #deserialize(java.io.BufferedReader)}
     */
    private static void commitFingerprint(
            SessionIdentifier sessionId,
            TLSFingerprint tlsFingerprint,
            Map<SessionIdentifier, List<TLSFingerprint>> fingerprints) {

        if(sessionId != null && tlsFingerprint != null) {
            List<TLSFingerprint> fps;
            if(fingerprints.containsKey(sessionId)) {
                // append to list of fingerprints belonging to SessionIdentifier
                fps = fingerprints.get(sessionId);
                if(! fps.contains(tlsFingerprint)) {
                    fps.add(tlsFingerprint);
                } else {
                    logger.warn("Duplicate fingerprint in file for " + sessionId);
                }
            } else {
                fps = new ArrayList<>(1);
                fps.add(tlsFingerprint);
                fingerprints.put(sessionId, fps);
            }
        }
    }

    private enum FingerprintId {
        ClientHello("ClientHello"),
        ServerHello("ServerHello"),
        ServerTcp("ServerTCP"),
        ServerMtu("ServerMTU");

        public final String id;
        private final Pattern pattern;
        FingerprintId(String id) {
            this.id = id;
            pattern = Pattern.compile(id, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
        }
        public boolean isAtStart(CharSequence input) {
            return pattern.matcher(input).lookingAt();
        }
    }
}
