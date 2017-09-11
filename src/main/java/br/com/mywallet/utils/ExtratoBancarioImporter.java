package br.com.mywallet.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtratoBancarioImporter {

    private Pattern pattern;
    private Matcher matcher;

    private static final String DATE_PATTERN =
            "(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])";

    public ExtratoBancarioImporter(){
        pattern = Pattern.compile(DATE_PATTERN);
    }

    public List<String> parsePdfDocument(String path) throws IOException {
        return parsePdfDocument(new File(path));
    }

    public List<String> parsePdfDocumentContaCorrente(String path) throws IOException {
        return parsePdfDocumentContaCorrente(new File(path));
    }

    public List<String> parsePdfDocumentContaCorrente(File path) throws IOException {
        PDDocument pdf = PDDocument.load(path);
        PDFTextStripper stripper = new PDFTextStripper();

        //Retrieving text from PDF document
        String text = stripper.getText(pdf);
        BufferedReader reader = new BufferedReader(new StringReader(text));

        System.out.println(text);
        System.out.println("Final");
        String sCurrentLine;
        int i=1;
        //String tag = "DESPESAS EM REAL";
        //String tag2 = "OUTROS LANÇAMENTOS";
        String tag4 = "SDO CTA/APL";
        String tag = "SALDO";
        String tag2 = "APLIC";
        String tag3 = "Data de emissão";
        boolean tagsPassed = false;
        boolean tags2Passed = false;
        boolean tags3Passed = false;
        boolean tags4Passed = false;

        StringBuffer strBuffer = new StringBuffer();
        System.out.println("Linhas");
        List<String> transacoes = new ArrayList();
        Pattern p = Pattern.compile("(0?[1-9]|[12][0-9]|3[01])/(0?[1-9]|1[012])");
        while ((sCurrentLine = reader.readLine()) != null) {
            System.out.println(sCurrentLine);
            if (sCurrentLine.contains("SMARTFIT")) {
                int a = 2;
            }
            if(sCurrentLine.contains(tag)){
                tagsPassed = true;
            }
            if(sCurrentLine.contains(tag2)){
                tags2Passed = true;
            }
            if(sCurrentLine.contains(tag3)){
                tags3Passed = true;
            }
            if(sCurrentLine.contains(tag4)){
                tags4Passed = true;
            }


            Matcher m = p.matcher(sCurrentLine);
            boolean isMateched = m.matches();
            int groupCount = m.groupCount();
            if(isMateched && groupCount ==1){
                String group1 = m.group(0);

            }else if (isMateched && groupCount ==2){
                String group1 = m.group(0);
                String group2 = m.group(1);
            }
            boolean encontrou = sCurrentLine.matches("\\d\\d/\\d\\d");
            boolean found = m.find();
            boolean validate = false;
            try{
                if(sCurrentLine.length() > 5)
                    validate = validate(sCurrentLine.substring(0,5));
            }catch(Throwable t){
                t.printStackTrace();
            }
            if( validate  && !tagsPassed && !tags2Passed && !tags3Passed && !tags4Passed) {
                String aTrn = addCommaContaCorrente(sCurrentLine);
                strBuffer.append(aTrn);
                strBuffer.append("\n");
                transacoes.add(aTrn);
            }
            tagsPassed = tags2Passed = tags3Passed = tags4Passed = false;
        }

        pdf.close();
        return transacoes;
    }

    public boolean validate(final String date){

        matcher = pattern.matcher(date);

        if(matcher.matches()){

            matcher.reset();

            if(matcher.find()){

                String day = matcher.group(1);
                String month = matcher.group(2);


                if (day.equals("31") &&
                        (month.equals("4") || month .equals("6") || month.equals("9") ||
                                month.equals("11") || month.equals("04") || month .equals("06") ||
                                month.equals("09"))) {
                    return false; // only 1,3,5,7,8,10,12 has 31 days
                } else{
                    return true;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public List<String> parsePdfDocument(File path) throws IOException {
        PDDocument pdf = PDDocument.load(path);
        PDFTextStripper stripper = new PDFTextStripper();

        //Retrieving text from PDF document
        String text = stripper.getText(pdf);
        BufferedReader reader = new BufferedReader(new StringReader(text));

        System.out.println(text);
        System.out.println("Final");
        String sCurrentLine;
        int i=1;
        //String tag = "DESPESAS EM REAL";
        //String tag2 = "OUTROS LANÇAMENTOS";
        String tag = "Lançamentos nacionais";
        String tag2 = "Lançamentos internacionais";
        boolean tagsPassed = false;
        boolean tags2Passed = false;
        StringBuffer strBuffer = new StringBuffer();
        System.out.println("Linhas");
        List<String> transacoes = new ArrayList();
        while ((sCurrentLine = reader.readLine()) != null) {
            System.out.println(sCurrentLine);
            if(sCurrentLine.contains(tag)){
                tagsPassed = true;
            }
            if(sCurrentLine.contains(tag2)){
                tags2Passed = true;
            }

            Pattern p = Pattern.compile("\\d\\d/\\d\\d");
            Matcher m = p.matcher(sCurrentLine);

            if(m.find() &&(tagsPassed || tags2Passed)) {
                String aTrn = addComma(sCurrentLine);
                strBuffer.append(aTrn);
                strBuffer.append("\n");
                transacoes.add(aTrn);
            }
        }

        pdf.close();
        return transacoes;
    }

    public  String normalizeDecimalPoints(String valor){
        valor = valor.replace('.',',');
        return valor;

    }



    private  String addComma(String line) {
        int index = line.indexOf('/');


        if (line.contains("COREL AUTO POSTO")) {
            int a = 2;
        }
        StringBuffer newLine = new StringBuffer(line.substring(0, index));
        newLine.append('-');
        String aMonth = line.substring(index + 1, index + 3);
        String newMonth = convertDate(aMonth);
        newLine.append(newMonth);
        newLine.append(';');
        index += 3;
        newLine.append(line.substring(index));

        int index2 = newLine.lastIndexOf(",");
        char aChar = ' ';
        do{
            index2--;
            try {
                aChar = newLine.charAt(index2);
            }catch(StringIndexOutOfBoundsException e){
                e.printStackTrace();
                throw e;
            }

        }while(!Character.isWhitespace(aChar) && aChar != ' ');

        newLine.insert(index2,';');
        return newLine.toString();
    }

    public String removeIfHaveValues(String value){
        //1- Localiza índice do ultimo ';'
        //2- Itera regressivamente enquanto digito é numerico e sinal '-'
        //3- caso encontre conteudo, este será o valor final.
        //5- deixa apenas ultimo valor encontrado e descarta-se o anterior
        int index = value.lastIndexOf(';');
        int offset = index;
        //45: -
        //46: .
        //44: ,
        while(value.charAt(--offset) == 45 || value.charAt(offset) == 46 || value.charAt(offset) == 44 || Character.isDigit(value.charAt(offset))){
            System.out.println(value.charAt(offset));
        }

        String aValue = value.substring(offset,index);
        String novoValor = value;
        if(aValue.contains(",") || aValue.contains(".")){
            novoValor = value.substring(0,offset) + ";" + aValue;
        }
        return novoValor;
    }

    public static void main(String[] args) throws IOException {
        String value = "09-Feb; SAQUE S/CARTAO CXE001404 180,00-; 3.289,86-";
        String value1 = "13-Feb; RSHOP-BADARO SUCO-13/02; 4,00-";
        String value2 = "16-Feb; TBI 0788.43628-5     C/C 130,00-; 4.225,25-";
        String value3 = "21-Feb; INT LICENC SP 06226349 1.198,75-; 5.485,40-";

        String valor = new ExtratoBancarioImporter().removeIfHaveValues(value3);
        System.out.println("Resultado: [" + value3 + "]  [" + valor + "]");
    }

    private  String addCommaContaCorrente(String line) {
        int index = line.indexOf('/');


        if (line.contains("COREL AUTO POSTO")) {
            int a = 2;
        }
        StringBuffer newLine = new StringBuffer(line.substring(0, index));
        newLine.append('-');
        String aMonth = line.substring(index + 1, index + 3);
        String newMonth = convertDate(aMonth);
        newLine.append(newMonth);
        newLine.append(';');
        index += 3;
        newLine.append(line.substring(index));

        int index2 = newLine.lastIndexOf(",");
        char aChar = ' ';
        do{
            index2--;
            aChar = newLine.charAt(index2);

        }while(!Character.isWhitespace(aChar) && aChar != ' ');


        newLine.insert(index2,';');
        return removeIfHaveValues(newLine.toString());
    }

    private static String convertDate(String month){
        String newMonth = "none";
        if("01".equals(month)){
            return "Jan";
        }
        if("02".equals(month)){
            return "Feb";
        }
        if("03".equals(month)){
            return "Mar";
        }
        if("04".equals(month)){
            return "Apr";
        }
        if("05".equals(month)){
            return "May";
        }
        if("06".equals(month)){
            return "Jun";
        }
        if("07".equals(month)){
            return "Jul";
        }
        if("08".equals(month)){
            return "Aug";
        }
        if("09".equals(month)){
            return "Sep";
        }
        if("10".equals(month)){
            return "Oct";
        }
        if("11".equals(month)){
            return "Nov";
        }
        if("12".equals(month)){
            return "Dec";
        }
        return newMonth;
    }



}
