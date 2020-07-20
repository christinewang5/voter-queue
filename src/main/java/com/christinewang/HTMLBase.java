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

    /** Template of AES-PBKDF2-protector
     * */
    public static final String protected_template=
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                    "    <meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                    "    <title>Password Protected Page</title>\n" +
                    "    <style>\n" +
                    "        html, body {\n" +
                    "            margin: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            font-family: Arial, Helvetica, sans-serif;\n" +
                    "        }\n" +
                    "        #dialogText {\n" +
                    "            padding: 10px 30px;\n" +
                    "            color: white;\n" +
                    "            background-color: #333333;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #dialogWrap {\n" +
                    "            position: absolute;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "            display: table;\n" +
                    "            background-color: #EEEEEE;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #dialogWrapCell {\n" +
                    "            display: table-cell;\n" +
                    "            text-align: center;\n" +
                    "            vertical-align: middle;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #mainDialog {\n" +
                    "            max-width: 400px;\n" +
                    "            margin: 5px;\n" +
                    "            border: solid #AAAAAA 1px;\n" +
                    "            border-radius: 10px;\n" +
                    "            box-shadow: 3px 3px 5px 3px #AAAAAA;\n" +
                    "            margin-left: auto;\n" +
                    "            margin-right: auto;\n" +
                    "            background-color: #FFFFFF;\n" +
                    "            overflow: hidden;\n" +
                    "            text-align: left;\n" +
                    "        }\n" +
                    "        #passArea {\n" +
                    "            padding: 20px 30px;\n" +
                    "            background-color: white;\n" +
                    "        }\n" +
                    "        #passArea > * {\n" +
                    "            margin: 5px auto;\n" +
                    "        }\n" +
                    "        #pass {\n" +
                    "            width: 100%;\n" +
                    "            height: 40px;\n" +
                    "            font-size: 30px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #messageWrapper {\n" +
                    "            float: left;\n" +
                    "            vertical-align: middle;\n" +
                    "            line-height: 30px;\n" +
                    "        }\n" +
                    "        \n" +
                    "        .notifyText {\n" +
                    "            display: none;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #invalidPass {\n" +
                    "            color: red;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #success {\n" +
                    "            color: green;\n" +
                    "        }\n" +
                    "        \n" +
                    "        #submitPass {\n" +
                    "            font-size: 20px;\n" +
                    "            border-radius: 5px;\n" +
                    "            background-color: #E7E7E7;\n" +
                    "            border: solid gray 1px;\n" +
                    "            float: right;\n" +
                    "        }\n" +
                    "        #contentFrame {\n" +
                    "            position: absolute;\n" +
                    "            top: 0;\n" +
                    "            left: 0;\n" +
                    "            width: 100%;\n" +
                    "            height: 100%;\n" +
                    "        }\n" +
                    "        #attribution {\n" +
                    "            position: absolute;\n" +
                    "            bottom: 0;\n" +
                    "            left: 0;\n" +
                    "            right: 0;\n" +
                    "            text-align: center;\n" +
                    "            padding: 10px;\n" +
                    "            font-weight: bold;\n" +
                    "            font-size: 0.8em;\n" +
                    "        }\n" +
                    "        #attribution, #attribution a {\n" +
                    "            color: #999;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "  </head>\n" +
                    "  <body>\n" +
                    "    <iframe id=\"contentFrame\" frameBorder=\"0\" allowfullscreen></iframe>\n" +
                    "    <div id=\"dialogWrap\">\n" +
                    "        <div id=\"dialogWrapCell\">\n" +
                    "            <div id=\"mainDialog\">\n" +
                    "                <div id=\"dialogText\">This page is password protected.</div>\n" +
                    "                <div id=\"passArea\">\n" +
                    "                    <p id=\"passwordPrompt\">Password</p>\n" +
                    "                    <input id=\"pass\" type=\"password\" name=\"pass\">\n" +
                    "                    <div>\n" +
                    "                        <span id=\"messageWrapper\">\n" +
                    "                            <span id=\"invalidPass\" class=\"notifyText\">Sorry, please try again.</span>\n" +
                    "                            <span id=\"success\" class=\"notifyText\">Success!</span>\n" +
                    "                            &nbsp;\n" +
                    "                        </span>\n" +
                    "                        <button id=\"submitPass\" type=\"button\">Submit</button>\n" +
                    "                        <div style=\"clear: both;\"></div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "    <div id=\"attribution\">\n" +
                    "        Protected by <a href=\"https://www.maxlaumeister.com/pagecrypt/\">PageCrypt</a>\n" +
                    "    </div>\n" +
                    "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/crypto-js/3.1.2/rollups/aes.js\"></script>\n" +
                    "    <script src=\"https://cdnjs.cloudflare.com/ajax/libs/crypto-js/3.1.2/rollups/pbkdf2.js\"></script>" +
                    "    <script>\n" +
                    "        /*! srcdoc-polyfill - v0.1.1 - 2013-03-01\n" +
                    "        * http://github.com/jugglinmike/srcdoc-polyfill/\n" +
                    "        * Copyright (c) 2013 Mike Pennisi; Licensed MIT */\n" +
                    "        (function( window, document, undefined ) {\n" +
                    "\t\n" +
                    "\t        var idx, iframes;\n" +
                    "\t        var _srcDoc = window.srcDoc;\n" +
                    "\t        var isCompliant = !!(\"srcdoc\" in document.createElement(\"iframe\"));\n" +
                    "\t        var implementations = {\n" +
                    "\t\t        compliant: function( iframe, content ) {\n" +
                    "\n" +
                    "\t\t\t        if (content) {\n" +
                    "\t\t\t\t        iframe.setAttribute(\"srcdoc\", content);\n" +
                    "\t\t\t        }\n" +
                    "\t\t        },\n" +
                    "\t\t        legacy: function( iframe, content ) {\n" +
                    "\n" +
                    "\t\t\t        var jsUrl;\n" +
                    "\n" +
                    "\t\t\t        if (!iframe || !iframe.getAttribute) {\n" +
                    "\t\t\t\t        return;\n" +
                    "\t\t\t        }\n" +
                    "\n" +
                    "\t\t\t        if (!content) {\n" +
                    "\t\t\t\t        content = iframe.getAttribute(\"srcdoc\");\n" +
                    "\t\t\t        } else {\n" +
                    "\t\t\t\t        iframe.setAttribute(\"srcdoc\", content);\n" +
                    "\t\t\t        }\n" +
                    "\n" +
                    "\t\t\t        if (content) {\n" +
                    "\t\t\t\t        // The value returned by a script-targeted URL will be used as\n" +
                    "\t\t\t\t        // the iFrame's content. Create such a URL which returns the\n" +
                    "\t\t\t\t        // iFrame element's `srcdoc` attribute.\n" +
                    "\t\t\t\t        jsUrl = \"javascript: window.frameElement.getAttribute('srcdoc');\";\n" +
                    "\n" +
                    "\t\t\t\t        iframe.setAttribute(\"src\", jsUrl);\n" +
                    "\n" +
                    "\t\t\t\t        // Explicitly set the iFrame's window.location for\n" +
                    "\t\t\t\t        // compatability with IE9, which does not react to changes in\n" +
                    "\t\t\t\t        // the `src` attribute when it is a `javascript:` URL, for\n" +
                    "\t\t\t\t        // some reason\n" +
                    "\t\t\t\t        if (iframe.contentWindow) {\n" +
                    "\t\t\t\t\t        iframe.contentWindow.location = jsUrl;\n" +
                    "\t\t\t\t        }\n" +
                    "\t\t\t        }\n" +
                    "\t\t        }\n" +
                    "\t        };\n" +
                    "\t        var srcDoc = window.srcDoc = {\n" +
                    "\t\t        // Assume the best\n" +
                    "\t\t        set: implementations.compliant,\n" +
                    "\t\t        noConflict: function() {\n" +
                    "\t\t\t        window.srcDoc = _srcDoc;\n" +
                    "\t\t\t        return srcDoc;\n" +
                    "\t\t        }\n" +
                    "\t        };\n" +
                    "\n" +
                    "\t        // If the browser supports srcdoc, no shimming is necessary\n" +
                    "\t        if (isCompliant) {\n" +
                    "\t\t        return;\n" +
                    "\t        }\n" +
                    "\n" +
                    "\t        srcDoc.set = implementations.legacy;\n" +
                    "\n" +
                    "\t        // Automatically shim any iframes already present in the document\n" +
                    "\t        iframes = document.getElementsByTagName(\"iframe\");\n" +
                    "\t        idx = iframes.length;\n" +
                    "\n" +
                    "\t        while (idx--) {\n" +
                    "\t\t        srcDoc.set( iframes[idx] );\n" +
                    "\t        }\n" +
                    "\n" +
                    "        }( this, this.document ));\n" +
                    "    </script>\n" +
                    "    <script>\n" +
                    "        var pl = /*ENCRYPTED_PAYLOAD}}*/\"\";\n" +
                    "        \n" +
                    "        var submitPass = document.getElementById('submitPass');\n" +
                    "        var passEl = document.getElementById('pass');\n" +
                    "        var invalidPassEl = document.getElementById('invalidPass');\n" +
                    "        var successEl = document.getElementById('success');\n" +
                    "        var contentFrame = document.getElementById('contentFrame');\n" +
                    "        \n" +
                    "        if (pl === \"\") {\n" +
                    "            submitPass.disabled = true;\n" +
                    "            passEl.disabled = true;\n" +
                    "            alert(\"This page is meant to be used with the encryption tool. It doesn't work standalone.\");\n" +
                    "        }\n" +
                    "        \n" +
                    "        function doSubmit(evt) {\n" +
                    "            try {\n" +
                    "                var decrypted = decryptFile(CryptoJS.enc.Base64.parse(pl.data), passEl.value, CryptoJS.enc.Base64.parse(pl.salt), CryptoJS.enc.Base64.parse(pl.iv));\n" +
                    "                if (decrypted === \"\") throw \"No data returned\";\n" +
                    "                \n" +
                    "                // Set default iframe link targets to _top so all links break out of the iframe\n" +
                    "                decrypted = decrypted.replace(\"<head>\", \"<head><base href=\\\".\\\" target=\\\"_top\\\">\");\n" +
                    "                \n" +
                    "                srcDoc.set(contentFrame, decrypted);\n" +
                    "                \n" +
                    "                successEl.style.display = \"inline\";\n" +
                    "                passEl.disabled = true;\n" +
                    "                submitPass.disabled = true;\n" +
                    "                setTimeout(function() {\n" +
                    "                    dialogWrap.style.display = \"none\";\n" +
                    "                }, 1000);\n" +
                    "            } catch (e) {\n" +
                    "                invalidPassEl.style.display = \"inline\";\n" +
                    "                passEl.value = \"\";\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        submitPass.onclick = doSubmit;\n" +
                    "        passEl.onkeypress = function(e){\n" +
                    "            if (!e) e = window.event;\n" +
                    "            var keyCode = e.keyCode || e.which;\n" +
                    "            invalidPassEl.style.display = \"none\";\n" +
                    "            if (keyCode == '13'){\n" +
                    "              // Enter pressed\n" +
                    "              doSubmit();\n" +
                    "              return false;\n" +
                    "            }\n" +
                    "        }\n" +
                    "        \n" +
                    "        function decryptFile(contents, password, salt, iv) {\n" +
                    "            var _cp = CryptoJS.lib.CipherParams.create({\n" +
                    "                ciphertext: contents\n" +
                    "            });\n" +
                    "            var key = CryptoJS.PBKDF2(password, salt, { keySize: 256/32, iterations: 100 });\n" +
                    "            var decrypted = CryptoJS.AES.decrypt(_cp, key, {iv: iv});\n" +
                    "            \n" +
                    "            return decrypted.toString(CryptoJS.enc.Utf8);\n" +
                    "        }\n" +
                    "    </script>\n" +
                    "  </body>\n" +
                    "</html>";

    /** HTML for admin panel
     * */
    public static final String admin =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Title</title>\n" +
                    "</head>\n" +
                    "\n" +
                    "<body>\n" +
                    "<h1>Upload example</h1>\n" +
                    "<form method=\"post\" action=\"/upload-example\" enctype=\"multipart/form-data\">\n" +
                    "    <input type=\"file\" name=\"REPLACEME\">\n" +
                    "    <button>Submit</button>\n" +
                    "</form>\n" +
                    "</body>\n" +
                    "</html>";
}
