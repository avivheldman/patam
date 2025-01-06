package test;

import java.util.Date;

public class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    /**
     * Constructor for byte array input
     * @param data The byte array containing the message data
     */
    public Message(byte[] data) {
        this.data = data.clone();
        this.asText = new String(data);
        this.date = new Date();
        double temp;
        try {
            temp = Double.parseDouble(asText.trim());
        } catch (NumberFormatException e) {
            temp = Double.NaN;
        }
        this.asDouble = temp;
    }

    /**
     * Constructor for String input
     * @param text The string to be converted to a message
     */
    public Message(String text) {
        this(text.getBytes());
    }

    /**
     * Constructor for double input
     * @param value The double value to be converted to a message
     */
    public Message(double value) {
        this(String.valueOf(value));
    }
}