package de.rub.nds.virtualnetworklayer.packet.header.link.family;

/**
 * A {@link de.rub.nds.virtualnetworklayer.packet.header.Header} class implements the Family interface to indicate that it
 * contains a {@link AddressFamily} field.
 *
 * @author Marco Faltermeier <faltermeier@me.com>
 */
public interface Family {
    public static enum AddressFamily {
        INET(2),
        APPLETALK(16),
        INET6(24);

        private int id;

        private AddressFamily(int id) {
            this.id = id;
        }

        public static AddressFamily valueOf(int type) {
            for (AddressFamily t : values()) {
                if (t.id == type) {
                    return t;
                }
            }

            return null;
        }

    }

    public AddressFamily getAddressFamily();

}
