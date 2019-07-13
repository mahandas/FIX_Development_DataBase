package util;


import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class CommonUtil {
	private static  JAXBContext context;
    public CommonUtil() {
    }

    /**
     * Converts Java Object (DTO) to XML String
     *
     * @param obj
     * @return XML String contents of the Java object
     * @throws JAXBException
     */
    public static String toXML(Object obj) throws JAXBException {
        // Create a JAXB context passing in the class of the object we want to marshal/unmarshal
        context = JAXBContext.newInstance(obj.getClass());
        // Create the marshaller, this will actually transform the object into XML
        final Marshaller marshaller = context.createMarshaller();
        // Create a stringWriter to hold the XML
        final StringWriter stringWriter = new StringWriter();
        // Marshal the javaObject and write the XML to the stringWriter
        marshaller.marshal(obj, stringWriter);
        // return the XML String contents of stringWriter after stripping the encoding header
        return stringWriter.toString().split(Pattern.quote("?>"))[1];

    }

  /**
     * Print's the message in tag name format on the console on providing the Data Dictionary
     *
     * @param out
     * @param dd
     * @param message
     * @throws FieldNotFound
     */


    /**
     * Rounds the number to the required decimal places
     *
     * @param number
     * @param decimalPlace
     * @return rounded number in string format
     */
    public static String round(double number, int decimalPlace) {
        return Double.toString(new BigDecimal(number).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).doubleValue());
    }


}
