package com.christinewang;

//import static com.christinewang.Controller.WEB_HOST;
//import static com.christinewang.Controller.WEB_PORT;

/** Contains a bunch of templates for HTML
 * @author John Berberian
 * @author Christine Wang
 * */
public class HTMLBase {

    /** A header for the HTML. Currently contains just CSS links.
     * */
    public static final String Printout_Head =
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"/Fonts/Lato/latofonts.css\">\n" +
                    "<link rel=\"stylesheet\" type=\"text/css\" href=\"/Fonts/Lato/latostyle.css\">\n";

    /** A footer for the HTML. Currently contains nothing.
     * */
    public static final String Printout_Foot =
            "";

    /** The first half of a starting printout.
     *  Consists of: a big centered div, with some stuff (read the comments) in it.
     *  Should go before the QR code.
     * */
    public static final String Printout_Start_FirstHalf =
            "<div style=\"text-align: center;\">\n" +
                    "    <!--Behold, a nice header.-->\n" +
                    "    <img src=\"/Images/header3.png\" width=\"910\" height=\"193\" alt=\"banner\" >\n" +
                    "    <!--Way to many line breaks, coming your way!-->\n" +
                    "    <br><br><br><br><br><br><br>\n" +
                    "    <!--A nice sub-banner.-->\n" +
                    "    <h1>Estimate the Wait!</h1>\n" +
                    "    <!--And another line break, to separate it from the image.-->\n" +
                    "    <br>\n" +
                    "    <!--This is our big QR code.-->\n";

    /** The second half of a starting printout.
     *  Consists of: a big centered div, with some stuff (read the comments) in it.
     *  Then there's a page break, so that everything prints nicely.
     *  Should go after the QR code
     * */
    public static final String Printout_Start_SecondHalf =
            "    <!--Some more line breaks... catching a theme here?-->\n" +
                    "    <br><br><br>\n" +
                    "    <!--And an under-image header.-->\n" +
                    "    <h2>Scan this when you enter.</h2>\n" +
                    "    <!--You guessed it!-->\n" +
                    "    <br>\n" +
                    "    <!--Some nice info text.-->\n" +
                    "    <p>Scan this code to get the current wait time at</p><p>this polling station and help improve estimates.</p>\n" +
                    "    <!--Too many line breaks? Never heard of it.-->\n" +
                    "    <br><br>\n" +
                    "    <!--And a website link.-->\n" +
                    "    <p>For more information, please visit</p><p>website.com/about</p>\n" +
                    "</div>\n" +
                    "<!--Now, if you thought my line breaks were bad, wait till I get started on page breaks.-->\n" +
                    "<p style=\"page-break-before: always\">\n";

    /** The first half of an ending printout.
     *  Consists of: a big centered div, with some stuff (read the comments) in it.
     *  Should go before the QR code.
     * */
    public static final String Printout_End_FirstHalf =
            "<div style=\"text-align: center;\">\n" +
                    "    <!--Behold, a nice header.-->\n" +
                    "    <img src=\"/Images/header3.png\" width=\"910\" height=\"193\" alt=\"banner\" >\n" +
                    "    <!--Way to many line breaks, coming your way!-->\n" +
                    "    <br><br><br><br><br><br><br>\n" +
                    "    <!--A nice sub-banner.-->\n" +
                    "    <h1>Help Estimate the Wait!</h1>\n" +
                    "    <!--And another line break, to separate it from the image.-->\n" +
                    "    <br>\n" +
                    "    <!--This is our big QR code.-->\n";

    /** The second half of an ending printout.
     *  Consists of: a big centered div, with some stuff (read the comments) in it.
     *  Then there's a page break, so that everything prints nicely.
     *  Should go after the QR code
     * */
    public static final String Printout_End_SecondHalf =
            "    <!--Some more line breaks... catching a theme here?-->\n" +
                    "    <br><br><br>\n" +
                    "    <!--And an under-image header.-->\n" +
                    "    <h2>Scan this when you exit.</h2>\n" +
                    "    <!--You guessed it!-->\n" +
                    "    <br>\n" +
                    "    <!--Some nice info text.-->\n" +
                    "    <p>Scan this code to find out how long you waited </p><p>and improve estimates for others.</p>\n" +
                    "    <!--Too many line breaks? Never heard of it.-->\n" +
                    "    <br><br>\n" +
                    "    <!--And a website link.-->\n" +
                    "    <p>For more information, please visit</p><p>website.com/about</p>\n" +
                    "</div>\n" +
                    "<!--Now, if you thought my line breaks were bad, wait till I get started on page breaks.-->\n" +
                    "<p style=\"page-break-before: always\">\n";
}
