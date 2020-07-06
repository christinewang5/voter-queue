package com.christinewang;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Contains methods for generating QR codes
 * @author John Berberian
 * @author Christine Wang
 * */

public class QRLib {
    //Most Strings will be in UTF-8.
    private static final String STANDARD_CHARSET = "UTF-8";
    //These can be fine-tuned, I'm just setting them to 115 for now.
    private static final int STANDARD_WIDTH = 115;
    private static final int STANDARD_HEIGHT = 115;
    //We want a PNG for the HTML to render it.
    private static final String STANDARD_FORMAT = "png";
    //This specifies a standard black-on-white QR code.
    private static final MatrixToImageConfig STANDARD_CONFIG = new MatrixToImageConfig();

    /** Returns a base64-encoded String of the QR code made from input data.
     * @author John Berberian
     * @param data The URL that the QR code should go to.
     * @param charset The charset that you want to use (likely UTF-8).
     * @param width The width of the QR code.
     * @param height The height of the QR code.
     * @param format The image format (likely png).
     * @param config A MatrixToImageConfig object specifying how the QR code should be rendered.
     *               Normally, the standard black-on-white is fine.
     * @return A base64-encoded String of an image of a QR code representing the input data.
     * */
    public static String createQR_b64(String data, String charset, int width, int height,
                               String format, MatrixToImageConfig config)
            throws WriterException, IOException {
        //Re-encode data as charset.
        String dataInCharset = new String(data.getBytes(charset),charset);
        //Make a BitMatrix QR code out of that data.
        BitMatrix matrix = new MultiFormatWriter().encode(dataInCharset, BarcodeFormat.QR_CODE, width, height);
        //Make a stream so that we can get a byte[] out of matrix.
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //Write matrix to the stream.
        MatrixToImageWriter.writeToStream(matrix, format, outStream, config);
        //Convert the stream to a byte[]
        byte[] bytes = outStream.toByteArray();
        //And encode it with base64
        Base64.Encoder b64encoder = Base64.getEncoder();
        String b64encStr = b64encoder.encodeToString(bytes);
        //Return it! We're done!
        return b64encStr;
    }

    /** Returns a base64-encoded String of the QR code made from input data, with defaults enabled.
     * @author John Berberian
     * @param data The URL that the QR code should go to.
     * @return A base64-encoded String of a PNG of a QR code representing the input data.
     * */
    public static String createQR_b64(String data)
            throws WriterException, IOException {
        return createQR_b64(data, STANDARD_CHARSET, STANDARD_WIDTH,
                STANDARD_HEIGHT, STANDARD_FORMAT, STANDARD_CONFIG);
    }
}
