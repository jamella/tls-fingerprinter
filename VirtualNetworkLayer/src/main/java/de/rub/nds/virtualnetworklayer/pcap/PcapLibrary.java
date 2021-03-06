package de.rub.nds.virtualnetworklayer.pcap;

import de.rub.nds.virtualnetworklayer.pcap.structs.*;
import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Platform;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;

/**
 * @author Marco Faltermeier <faltermeier@me.com>
 */
@Library("pcap")
@Runtime(CRuntime.class)
public class PcapLibrary {
    static {
        if (Platform.isWindows()) {
            BridJ.addNativeLibraryAlias("pcap", "wpcap");
        }

        BridJ.register();
    }

    public static native Pointer<Byte> pcap_lib_version();

    public static native pcap_t pcap_create(Pointer<Byte> source, Pointer<Byte> errbuf);

    public static native int pcap_activate(pcap_t p);

    public static native pcap_t pcap_open_live(Pointer<?> device, int snaplen, int promisc, int to_ms, Pointer<Byte> errbuf);

    public static native pcap_t pcap_open_offline(Pointer<Byte> fname, Pointer<Byte> errbuf);

    public static native int pcap_loop(pcap_t p, final int cnt, Pointer<?> callback, Pointer<?> user);

    public static native void pcap_breakloop(pcap_t p);

    public static native Pointer<Byte> pcap_lookupdev(Pointer<Byte> errbuf);

    public static native int pcap_findalldevs(Pointer<Pointer<pcap_if>> alldevsp, Pointer<Byte> errbuf);

    public static native int pcap_lookupnet(Pointer<?> device, Pointer<Integer> netp, Pointer<Integer> maskp, Pointer<Byte> errbuf);

    public static native void pcap_freealldevs(Pointer<pcap_if> alldevsp);

    public static native int pcap_datalink(pcap_t p);

    public static native int pcap_compile(pcap_t p, Pointer<bpf_program> fp, Pointer<Byte> str, int optimize, int netmask);

    public static native int pcap_setfilter(pcap_t p, Pointer<bpf_program> fp);

    public static native int pcap_set_snaplen(pcap_t p, int snaplen);

    public static native int pcap_set_promisc(pcap_t p, int mode);

    public static native int pcap_set_timeout(pcap_t p, int timeout);

    public static native int pcap_set_rfmon(pcap_t p, int rfmon);

    public static native int pcap_set_datalink(pcap_t p, int dlt);

    public static native void pcap_close(pcap_t p);


    public static native Pointer<Byte> pcap_geterr(pcap_t p);

    public static native pcap_dumper_t pcap_dump_open(pcap_t p, Pointer<Byte> fname);

    public static native void pcap_dump_close(pcap_dumper_t p);

    public static native int pcap_dump_flush(pcap_dumper_t p);

    public static native long pcap_dump_ftell(pcap_dumper_t p);
    
    public static native void pcap_dump(Pointer user, Pointer<pcap_pkthdr> h,
                                        Pointer<Byte> sp);

}
