package alv.splash.browser;

import android.net.Uri;

public class UrlValidator {

    public String processInput(String input) {
        // Hapus spasi di awal dan akhir input
        String query = input.trim();

        // Cek apakah input kosong
        if (query.isEmpty()) {
            return "https://www.google.com";
        }

        // Cek apakah dimulai dengan http:// atau https://
        boolean startsWithHttp = query.toLowerCase().startsWith("http://");
        boolean startsWithHttps = query.toLowerCase().startsWith("https://");
        boolean hasProtocol = startsWithHttp || startsWithHttps;

        // Cek apakah mengandung titik (domain)
        boolean hasDomain = query.contains(".");

        if (hasProtocol) {
            // Jika dimulai dengan protocol, cek apakah memiliki domain
            if (hasDomain) {
                return query; // Gunakan URL sebagaimana adanya
            } else {
                // Jika tidak ada domain, perlakukan sebagai search query
                return "https://www.google.com/search?q=" + Uri.encode(query);
            }
        } else if (hasDomain) {
            // Jika memiliki domain tapi tidak ada protocol, tambahkan https://
            return "https://" + query;
        } else {
            // Jika tidak ada protocol dan domain, perlakukan sebagai search query
            if (query.startsWith("about:")) {
                return query;
            } else if (query.startsWith("/")) {
                if (query.contains("Sarang Monyet") || query.contains("sarang monyet")) {
                    return "https://www.tiktok.com/";
                } else if (query.equals("/Youtube") || query.equals("/youtube") || query.equals("/yt")) {
                    return "https://www.youtube.com/";
                } else if (query.equals("/Instagram") || query.equals("/instagram") || query.equals("/ig")) {
                    return "https://www.instagram.com/";
                } else if (query.equals("/Facebook") || query.equals("/facebook") || query.equals("/fb")) {
                    return "https://www.facebook.com/";
                } else if (query.equals("/Twitter") || query.equals("/twitter") || query.equals("/tw")) {
                    return "https://www.twitter.com/";
                } else if (query.startsWith("/go")) {
                    query = query.substring(3).trim(); // Hapus "/go" hanya jika di awal
                    return "https://www.google.com/search?q=" + Uri.encode(query);
                } else if (query.startsWith("/bi")) {
                    query = query.substring(3).trim();
                    return "https://www.bing.com/search?&q=" + Uri.encode(query);
                } else if (query.startsWith("/dg")) {
                    query = query.substring(3).trim();
                    return "https://www.duckduckgo.com/html/?q=" + Uri.encode(query);
                } else if (query.startsWith("/sp")) {
                    query = query.substring(3).trim();
                    return "https://www.startpage.com/sp/search?query=" + Uri.encode(query);
                } else if (query.startsWith("/wp")) {
                    query = query.substring(3).trim();
                    return "https://www.wikipedia.org/wiki/Special:Search?search=" + Uri.encode(query);
                } else if (query.startsWith("/rd")) {
                    query = query.substring(3).trim();
                    return "https://www.reddit.com/search/?q=" + Uri.encode(query);
                }  else if (query.startsWith("/br")) {
                    query = query.substring(3).trim();
                    return "https://search.brave.com/search?q=" + Uri.encode(query);
                } else if (query.startsWith("/kb")) {
                    query = query.substring(3).trim();
                    return "https://kolotibablo.com/";
                } else{
                    return "https://www.google.com/search?q=" + Uri.encode(query);
                }




            } else {
                //default
                return "https://www.google.com/search?q=" + Uri.encode(query);
            }

            //return "https://www.google.com/search?q=" + Uri.encode(query);
        }
    }
}