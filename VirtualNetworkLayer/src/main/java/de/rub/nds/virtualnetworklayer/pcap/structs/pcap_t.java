package de.rub.nds.virtualnetworklayer.pcap.structs;

import org.bridj.Pointer;
import org.bridj.TypedPointer;

/**
 * @author Marco Faltermeier <faltermeier@me.com>
 */
public class pcap_t extends TypedPointer {

    public pcap_t(long address) {
        super(address);
    }

    public pcap_t(Pointer pointer) {
        super(pointer);
    }
}