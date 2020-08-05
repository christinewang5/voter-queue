package com.christinewang;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import io.javalin.core.util.FileUtil;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static com.christinewang.QRLib.createQR_b64;
/** A class for checking that QRLib works.
 * @author John Berberian
 * @author Christine Wang
 * */
public class QRLibTest {
    public static void main(String[] args) throws NotFoundException, IOException, WriterException {
        boolean works=true;
        works &= check_createQR_b64(false);
        System.out.println(works);
        System.out.println(get_content("/home/kali/Pictures/qr_1.png"));
        System.out.println(get_content("/home/kali/Pictures/qr_2.png"));
    }
    /** Checks that createQR_b64 is working by decoding the QR code, and checking equality.
     * @author John Berberian
     * @param printStatus Toggles print statements in function.
     * @return A boolean representing failure or success. True - test passed. False - test failed.
     * */
    public static boolean check_createQR_b64(boolean printStatus) throws IOException, WriterException, NotFoundException {
        //This is any nonsense URL. Use as many options as possible.
        String sillyUrl = "www.abcd.com:12345/testing/testing/1/2/3?param1=why+even+try&param2=because+why+not";
        //Get our base64-encoded string of the QR code for this url.
        String b64_enc = createQR_b64(sillyUrl);
        //And turn it into a png byte array.
        byte[] png_data = Base64.getDecoder().decode(b64_enc);
        //Turn that into a BufferedImage.
        ByteArrayInputStream bis = new ByteArrayInputStream(png_data);
        BufferedImage bImage = ImageIO.read(bis);
        //And make that into a LuminanceSource.
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bImage);
        //Which is turned into a HybridBinarizer
        HybridBinarizer hybridBinarizer = new HybridBinarizer(source);
        //Which is turned into a bitmap.
        BinaryBitmap bitmap = new BinaryBitmap(hybridBinarizer);
        //We read the QR code from the bitmap.
        Result result = (new MultiFormatReader()).decode(bitmap);
        //We grab the string URL from the result.
        String resultUrl = result.getText();
        //And check for equality.
        if (printStatus) {
            if (resultUrl.equals(sillyUrl)) {
                System.out.println("It matches up!");
            } else {
                System.out.println("Houston, we have a problem.\nsillyUrl:\n" + sillyUrl + "\nresultUrl:\n" + resultUrl);
            }
        }
        return resultUrl.equals(sillyUrl);
    }

    /** Reads the QR content of a local png image.
     * @param filepath The path to the QR image.
     * @return The string content of the QR code.
     * */
    public static String get_content(String filepath) throws IOException, NotFoundException {
        //Read our file
        byte[] fileContent = FileUtils.readFileToByteArray(new File(filepath));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);
        //And turn it into a png byte array.
        byte[] png_data = Base64.getDecoder().decode(encodedString);
        //Turn that into a BufferedImage.
        ByteArrayInputStream bis = new ByteArrayInputStream(png_data);
        BufferedImage bImage = ImageIO.read(bis);
        //And make that into a LuminanceSource.
        BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bImage);
        //Which is turned into a HybridBinarizer
        HybridBinarizer hybridBinarizer = new HybridBinarizer(source);
        //Which is turned into a bitmap.
        BinaryBitmap bitmap = new BinaryBitmap(hybridBinarizer);
        //We read the QR code from the bitmap.
        Result result = (new MultiFormatReader()).decode(bitmap);
        //We grab the string URL from the result.
        String resultUrl = result.getText();
        return resultUrl;
    }
}
