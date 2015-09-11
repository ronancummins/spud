package utils;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ronanc
 */
public class Utils {

    private final static Logger logger = Logger.getLogger(Utils.class.getName());

    
    public synchronized static String tidyWord(String str){
       
        if(str.matches("[-]+")){
            return "";
        }else if(str.length() > 20){
            return "";
        }else if( str.startsWith("<") && str.endsWith(">")){
            return "";
        }else {
            
            //to lower case
            str = str.toLowerCase();

            //replace any non word characters
            str = str.replaceAll("[^a-zA-Z0-9- ]", "");

            

            return str;
        }
        
    }    
    
    
    
    public static String strip_whitespace(String word){
        word = word.replaceAll("[^a-zA-Z0-9-]", "");
        return word;
    }
    

   
}
