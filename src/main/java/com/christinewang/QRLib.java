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

import static com.christinewang.AdminController.MIN_PRECINCT;

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
     * @param urlBase The base URL, such that the final url is "urlBase/urlCode(precinct)".
     * @param hasBreak Specifies whether or not the image should be followed by a br tag.
     * @param type The type of QR to generate. 0 for start, 1 for end, any other number for
     *             standard (raw), like wait.
     * @return HTML img string of base64-encoded QR code of "urlBase/urlCode(precinct)".
     * */
    public static String getQR(int precinct, String urlBase, boolean hasBreak, int type)
            throws IOException, WriterException {
        //If urlBase doesn't end in a slash...
        if (urlBase.charAt(urlBase.length()-1)!='/'){
            //...make sure it does.
            urlBase += "/";
        }
        String full_URL;
        if (type==0) {
            //Get the precinct code, shifted by min precinct, for 0-based indexing.
            full_URL = urlBase + Application.startURLs.get(precinct-MIN_PRECINCT);
        } else if (type==1) {
            //Get the precinct code, shifted by min precinct, for 0-based indexing.
            full_URL = urlBase + Application.endURLs.get(precinct-MIN_PRECINCT);
        } else {
            //Concat the urlBase and precinct, to get the full url.
            full_URL = urlBase + precinct;
        }
        //And get the base64 QR string for it.
        String b64_enc = createQR_b64(full_URL);
        //Wrap this in an html tag.
        String html_img = "<img src=\"data:image/png;base64,"+b64_enc+"\" alt=\"QR code for precinct "+precinct+"\">\n";
        //Allow the user to specify if we want a line break at the end.
        if (hasBreak){
            html_img += "<br>";
        }
        //Return our string.
        return html_img;
    }

    /** A function that gives us back a boxed precinct name.
     * @param index The precinct number relative to minprecinct.
     * @param minprecinct The min precinct to base our numbering off of.
     * @param voteService A VoteService connected to the db,
     *                    with access to the "precinct_names" table.
     * */
    public static String getLabel(int index, VoteService voteService, int minprecinct) {
        int precinct = minprecinct+index;
        return "<pborder>Precinct "+precinct+": "+voteService.getName(precinct)+"</pborder>";
    }

    /** Returns a single start-QR page.
     * @author John Berberian
     * @param precinct The precinct the QR should be for.
     * @param urlBase The base url for a start-vote.
     * @param voteService A VoteService connected to the db,
     *                    with access to the "precinct_names" table.
     * @return An HTML page, ready to be handed to Spark.
     * */
    public static String getStart_Printout(int precinct, String urlBase,
                                           VoteService voteService)
            throws IOException, WriterException {
        //Just concatenate a ton of templates, with our QR in the middle.
        return HTMLBase.Printout_Head+HTMLBase.Printout_Start_FirstHalf+
                getQR(precinct, urlBase, true, 0)+
                getLabel(precinct, voteService, MIN_PRECINCT) +
                HTMLBase.Printout_Start_SecondHalf+HTMLBase.Printout_Foot;
    }

    /** Returns a single end-QR page.
     * @author John Berberian
     * @param precinct The precinct the QR should be for.
     * @param urlBase The base url for an end-vote.
     * @param voteService A VoteService connected to the db,
     *                    with access to the "precinct_names" table.
     * @return An HTML page, ready to be handed to Spark.
     * */
    public static String getEnd_Printout(int precinct, String urlBase,
                                         VoteService voteService)
            throws IOException, WriterException {
        //Just concatenate a ton of templates, with our QR in the middle.
        return HTMLBase.Printout_Head+HTMLBase.Printout_End_FirstHalf+
                getQR(precinct, urlBase, true, 1)+
                getLabel(precinct, voteService, MIN_PRECINCT) +
                HTMLBase.Printout_End_SecondHalf+HTMLBase.Printout_Foot;
    }

    /** Gets a multi-page QR start poster for the precincts specified.
     * @author John Berberian
     * @param startPrecinct The starting precinct number.
     * @param endPrecinct The ending precinct number. All ints between this
     *                    and startPrecinct will be used as precinct nums.
     * @param urlBase The base url for a start-vote.
     * @param voteService A VoteService connected to the db,
     *                    with access to the "precinct_names" table.
     * @return An HTML page, ready to be handed to spark and printed multi-page.
     * */
    public static String getStart_Printouts(int startPrecinct, int endPrecinct,
                                            String urlBase, VoteService voteService)
            throws IOException, WriterException {
        //Start off with the header (css links, etc)
        String bigPage = HTMLBase.Printout_Head;
        //For each of our precincts in question...
        for (int i=startPrecinct;i<=endPrecinct;i++){
            //...concat the template pages, with the QR in the middle.
            bigPage += HTMLBase.Printout_Start_FirstHalf+
                    getQR(i, urlBase, true, 0)+
                    getLabel(i, voteService, startPrecinct) +
                    HTMLBase.Printout_Start_SecondHalf;
        }
        //And add our wonderful footer, which is nothing at the moment.
        bigPage += HTMLBase.Printout_Foot;
        //And we're done! Let's return.
        return bigPage;
    }

    /** Gets a multi-page QR end poster for the precincts specified.
     * @author John Berberian
     * @param startPrecinct The starting precinct number.
     * @param endPrecinct The ending precinct number. All ints between this
     *                    and startPrecinct will be used as precinct nums.
     * @param urlBase The base url for an end-vote.
     * @param voteService A VoteService connected to the db,
     *                    with access to the "precinct_names" table.
     * @return An HTML page, ready to be handed to spark and printed multi-page.
     * */
    public static String getEnd_Printouts(int startPrecinct, int endPrecinct,
                                          String urlBase, VoteService voteService)
            throws IOException, WriterException {
        //Start off with the header (css links, etc)
        String bigPage = HTMLBase.Printout_Head;
        //For each of our precincts in question...
        for (int i=startPrecinct;i<=endPrecinct;i++){
            //...concat the template pages, with the QR in the middle.
            bigPage += HTMLBase.Printout_End_FirstHalf+
                    getQR(i, urlBase, true, 1)+
                    getLabel(i, voteService, startPrecinct) +
                    HTMLBase.Printout_End_SecondHalf;
        }
        //And add our wonderful footer, which is nothing at the moment.
        bigPage += HTMLBase.Printout_Foot;
        //And we're done! Let's return.
        return bigPage;
    }
}
