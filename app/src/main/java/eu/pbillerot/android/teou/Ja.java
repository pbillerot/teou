package eu.pbillerot.android.teou;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

/**
 * Created by billerot on 08/08/16.
 */
public class Ja {
    private static final String TAG = "Ja";


    public static boolean isConnected(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();

        return isConnected;
    }

    public static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * Obtenir le chemin d'un fichier multilingue placé sous ASSETS
     * @param filename
     * @return le chemin du fichier complet
     */
    public static String getAssetsPath(String filename) {
        String path = "file:///android_asset/" + "en" + "/" + filename;
        switch(Locale.getDefault().getLanguage().toLowerCase()) {
            // 30 pays francophones
            case "fr": // France
            case "be": // Belgique
            case "bj": // Benin
            case "bf": // Burjina Faso
            case "bi": // Burundi
            case "ca": // Canada
            case "cm": // Cameroun
            case "km": // Comores
            case "cg": // Congo
            case "ci": // Cote d'Ivoire
            case "dj": // Djibouti
            case "ga": // Gabon
            case "gn": // Guinée
            case "gq": // Guinée Equatoriale
            case "gf": // Guyane Française
            case "ht": // Haiti
            case "lu": // Luxembourg
            case "mg": // Madagascar
            case "ml": // Mali
            case "mc": // Monaco
            case "ne": // Nigeria
            case "cf": // République Centrafricaine
            case "cd": // République démocratique du Congo
            case "rw": // Rwanda
            case "sn": // Sénégal
            case "sc": // Seychelles
            case "ch": // Suisse
            case "td": // Tchad
            case "tg": // Togo
            case "vu": // Vanuatu
                // Couramment utilisé
            case "dz": // Algérie
            case "ad": // Andorre
            case "mu": // Ile Maurice
            case "lb": // Liban
            case "mr": // Mauritanie
            case "tn": // Tunisie
                path = "file:///android_asset/" + Locale.getDefault().getLanguage() + "/" + filename;;
                break;
            default:
                path = "file:///android_asset/" + "en" + "/" + filename;
                break;
        }
        return path;
    }

    /**
     *  retourne les n 1er caractéres de gauche de la chaéne
     */
    public static String left(String pstr) {
        String str;
        if ( pstr == null ) {
            str = "";
        } else {
            str = pstr.trim();
        } // endif
        return str;
    } // end left

    /**
     *  retourne une chaéne é partir d'une position de caractére
     *  (la position commence é 1)
     */
    public static String mid(String pstr, int pos) {
        String str;
        str = pstr;
        if ( pos == 0 ) pos = 1;
        if ( pstr == null ) str = "";
        if ( str.length() + 1 > pos ) {
            str = left(pstr.substring(pos-1));
        } // end-if
        return str;
    } // end mid

    /**
     *  retourne les n caractéres de droite de la chaéne
     */
    public static String right(String pstr, int plen) {
        String str = "";
        str = pstr;
        if ( pstr == null ) str = "";
        if ( pstr.equals("null") ) str = "";
        if ( str.length() > plen ) {
            str = str.substring(str.length() - plen);
        } // end-if
        return str;
    } // end right

    /**
     *  remplacer un string par un autre
     */
    public static String remplace(String source, String ancien, String nouveau) {
        StringBuffer buf = new StringBuffer();
        String str = source;
        int pos = str.indexOf(ancien);
        while ( pos != -1 ) {
            buf.append(str.substring(0, pos));
            buf.append(nouveau);
            pos = pos + ancien.length();
            if ( pos < str.length() ) {
                str = str.substring(pos);
            } else {
                str = "";
                break;
            } // endif
            pos = str.indexOf(ancien);
        } // end while
        buf.append(str);
        return buf.toString();
    } // end remplace
}
