package br.com.mywallet.utils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by brienze on 4/30/17.
 */
public class UtilDate {
    public static String converterMoedaBr(String valorBrazil) throws ParseException {
        NumberFormat nf =
                NumberFormat.getNumberInstance(new
                        Locale("pt","BR"));
        double d = Double.parseDouble(String.valueOf(nf.parse(valorBrazil)));
        String f = nf.format(d);
        return f;
    }

    public static String converterMoedaUs(String valorBrazil) throws ParseException {
        NumberFormat nf =
                NumberFormat.getNumberInstance(new
                        Locale("pt","BR"));
        double d = Double.parseDouble(String.valueOf(nf.parse(valorBrazil)));
        String f = nf.format(d);
        return f;
    }
}
