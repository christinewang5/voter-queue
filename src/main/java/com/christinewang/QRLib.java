package com.christinewang;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Contains methods for generating QR codes
 * @author John Berberian
 * @author Christine Wang
 * */

public class QRLib {
    //Most Strings will be in UTF-8.
    private static final String STANDARD_CHARSET = "UTF-8";
    //500x500 should get us around 4"x4".
    private static final int STANDARD_WIDTH = 500;
    private static final int STANDARD_HEIGHT = 500;
    //We want a PNG for the HTML to render it.
    private static final String STANDARD_FORMAT = "png";
    //This specifies a standard black-on-white QR code.
    private static final MatrixToImageConfig STANDARD_CONFIG = new MatrixToImageConfig();
    //This specifies the level of error-correction. H for highest.
    //Should be able to tolerate 30% obscured, and be readable.
    private static final ErrorCorrectionLevel STANDARD_ERROR = ErrorCorrectionLevel.H;

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
                               String format, MatrixToImageConfig config,
                                      ErrorCorrectionLevel error_corr)
            throws WriterException, IOException {
        //Re-encode data as charset.
        String dataInCharset = new String(data.getBytes(charset),charset);
        //Make a hint map for error correction.
        Map hints = new HashMap();
        hints.put(EncodeHintType.ERROR_CORRECTION, error_corr);
        //Make a BitMatrix QR code out of that data.
        BitMatrix matrix = new MultiFormatWriter().encode(dataInCharset,
                BarcodeFormat.QR_CODE, width, height, hints);
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
                STANDARD_HEIGHT, STANDARD_FORMAT, STANDARD_CONFIG,
                STANDARD_ERROR);
    }

    /** Returns an HTML img QR code for urlBase/precinct.
     * @author John Berberian
     * @param precinct The precinct that the QR code should be for.
     * @param urlBase The base URL, such that the final url is "urlBase/precinct".
     * @param hasBreak Specifies whether or not the image should be followed by a br.
     * @return HTML img string of base64-encoded QR code of "urlBase/precinct".
     * */
    public static String getQR(int precinct, String urlBase, boolean hasBreak)
            throws IOException, WriterException {
        if (urlBase.charAt(urlBase.length()-1)!='/'){
            urlBase += "/";
        }
        String full_URL = urlBase+precinct;
        String b64_enc = createQR_b64(full_URL);
        String html_img = "<img src=\"data:image/png;base64,"+b64_enc+"\" alt=\"QR code for precinct "+precinct+"\">\n";
        if (hasBreak){
            html_img += "<br>";
        }
        return html_img;
    }

    public static String getStart_Printout(int precinct, String urlBase)
            throws IOException, WriterException {
        return HTMLBase.Printout_Head+HTMLBase.Printout_Start_FirstHalf+
                getQR(precinct, urlBase, false)+
                HTMLBase.Printout_Start_SecondHalf+HTMLBase.Printout_Foot;
    }

    public static String getEnd_Printout(int precinct, String urlBase)
            throws IOException, WriterException {
        return HTMLBase.Printout_Head+HTMLBase.Printout_End_FirstHalf+
                getQR(precinct, urlBase, false)+
                HTMLBase.Printout_End_SecondHalf+HTMLBase.Printout_Foot;
    }

    public static String getStart_Printouts(int startPrecinct, int endPrecinct, String urlBase)
            throws IOException, WriterException {
        String bigPage = HTMLBase.Printout_Head;
        for (int i=startPrecinct;i<=endPrecinct;i++){
            bigPage += HTMLBase.Printout_Start_FirstHalf+
                    getQR(i, urlBase, false)+
                    HTMLBase.Printout_Start_SecondHalf;
        }
        bigPage += HTMLBase.Printout_Foot;
        return bigPage;
    }

    public static String getEnd_Printouts(int startPrecinct, int endPrecinct, String urlBase)
            throws IOException, WriterException {
        String bigPage = HTMLBase.Printout_Head;
        for (int i=startPrecinct;i<=endPrecinct;i++){
            bigPage += HTMLBase.Printout_End_FirstHalf+
                    getQR(i, urlBase, false)+
                    HTMLBase.Printout_End_SecondHalf;
        }
        bigPage += HTMLBase.Printout_Foot;
        return bigPage;
    }
}
