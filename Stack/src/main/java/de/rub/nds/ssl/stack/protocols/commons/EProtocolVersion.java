package de.rub.nds.ssl.stack.protocols.commons;

import java.util.HashMap;
import java.util.Map;

/**
 * Supported protocol versions of SSL/TLS.
 *
 * @author Christopher Meyer - christopher.meyer@rub.de
 * @version 0.1 Nov 14, 2011
 */
public enum EProtocolVersion {

    /**
     * SSL 3.0 protocol version.
     */
    SSL_3_0(new byte[]{0x3, 0x0}),
    /**
     * TLS 1.0 protocol version.
     */
    TLS_1_0(new byte[]{0x3, 0x1}),
    /**
     * TLS 1.1 protocol version.
     */
    TLS_1_1(new byte[]{0x3, 0x2}),
    /**
     * TLS 1.2 protocol version.
     */
    TLS_1_2(new byte[]{0x3, 0x3});
    /**
     * Length of the protocol version id: 2 Bytes.
     */
    public static final int LENGTH_ENCODED = 2;
    /**
     * Map of an id to the protocol version.
     */
    private static final Map<Integer, EProtocolVersion> ID_MAP =
            new HashMap<Integer, EProtocolVersion>(4);
    /**
     * Id of the protocol version.
     */
    private final byte[] id;
    /**
     * Bits in byte.
     */
    private static final int BITS_IN_BYTE = 8;

    static {
        byte[] id;
        for (EProtocolVersion tmp : EProtocolVersion.values()) {
            id = tmp.getId();
            ID_MAP.put(id[0] << BITS_IN_BYTE | id[1] & 0xff, tmp);
        }
    }

    /**
     * Construct a version with the given id.
     *
     * @param idBytes Id of this version
     */
    EProtocolVersion(final byte[] idBytes) {
        id = idBytes;
    }

    /**
     * Get the Id of this protocol version.
     *
     * @return Id as byte array
     */
    public byte[] getId() {
        byte[] tmp = new byte[id.length];
        // deep copy
        System.arraycopy(id, 0, tmp, 0, tmp.length);

        return tmp;
    }

    /**
     * Get the protocol version for a given id.
     *
     * @param id ID of the desired protocol version
     * @return Associated protocol version
     */
    public static EProtocolVersion getProtocolVersion(final byte[] id) {
        final int protocolVersion;
        if (id == null || id.length != LENGTH_ENCODED) {
            throw new IllegalArgumentException(
                    "ID must not be null and have a length of exactly "
                    + LENGTH_ENCODED + " bytes.");
        }

        protocolVersion = id[0] << BITS_IN_BYTE | id[1] & 0xff;

        if (!ID_MAP.containsKey(protocolVersion)) {
            throw new IllegalArgumentException("No such protocol version.");
        }

        return ID_MAP.get(protocolVersion);
    }
}