package eu.pbillerot.android.teou;

/**
 * Created by billerot on 08/08/16.
 */
public class Ja {
    private static final String TAG = "Ja";

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
