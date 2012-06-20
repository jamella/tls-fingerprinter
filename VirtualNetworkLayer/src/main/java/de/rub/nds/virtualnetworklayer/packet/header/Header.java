package de.rub.nds.virtualnetworklayer.packet.header;

import de.rub.nds.virtualnetworklayer.pcap.Pcap;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;

public abstract class Header implements Cloneable {
    private ByteBuffer payload;
    protected Header previousHeader;
    private int offset;

    protected int getFirstNibble(int offset) {
        return (payload.get(offset) & 0xF0) >> 4;
    }

    protected int getSecondNibble(int offset) {
        return (payload.get(offset) & 0x0F);
    }

    protected int getByte(int offset) {
        return payload.get(offset);
    }

    protected int getUByte(int offset) {
        return payload.get(offset) & 0xFF;
    }

    protected short getShort(int offset) {
        return payload.getShort(offset);
    }

    public int getUShort(int offset) {
        return payload.getShort(offset) & 0xffff;
    }

    public long getUInteger(int offset) {
        return ((long) payload.getInt(offset) & 0xffffffffL);
    }

    protected int getInteger(int offset) {
        return payload.getInt(offset);
    }

    protected long getLong(int offset) {
        return payload.getLong(offset);
    }

    protected byte[] getBytes(int offset, int length) {
        return Arrays.copyOfRange(this.payload.array(), this.offset + offset, this.offset + offset + length);
    }

    public int getOffset() {
        return offset;
    }

    public int getPayloadOffset() {
        return getOffset() + getLength();
    }

    public int getPayloadLength() {
        return this.payload.limit() - getLength();
    }

    public byte[] getPayload() {
        return getBytes(getLength(), getPayloadLength());
    }

    public Header getPreviousHeader() {
        return previousHeader;
    }

    public boolean isFragmented() {
        return getPayloadLength() > payload.limit();
    }

    Header clone(Header previousHeader, int offset) {

        try {
            Header newHeader = (Header) super.clone();
            newHeader.previousHeader = previousHeader;
            newHeader.offset = offset;
            return newHeader;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    void peer(ByteBuffer payload) {
        this.payload = payload;
    }

    public abstract int getLength();

    public abstract int getId();

    public abstract boolean isBound(LinkedList<Header> previousHeaders, Pcap.DataLinkType dataLinkType);

}