import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlLanguage {

    public static void xmlParsing(String[] list, int number, String actualPath) {
        String textLinePart = list[number];
        number = number + 1;

        if (number == list.length) {
            return;
        }

        if (textLinePart.matches(" *<[\\w-]+[ \\w(= )\"-]*>")) {
            Pattern pattern = Pattern.compile(" *<[\\w]+");
            Matcher matcher = pattern.matcher(textLinePart);

            if (matcher.find()) {
                String pathName = matcher.group();
                if (actualPath.equals("")) {
                    actualPath = pathName.replaceAll("[<> ]", "");
                } else {
                    actualPath = actualPath + ", " + pathName.replaceAll("[<> ]", "");
                }
                System.out.println("\n" + "Element: \n" + "path = " + actualPath);
            }

            pattern = Pattern.compile("[\\w-]+[= ]+\"[\\w-]+\"");
            matcher = pattern.matcher(textLinePart);

            boolean attributesLog = false;

            while (matcher.find()) {
                if (!attributesLog) {
                    System.out.println("attributes:");
                    attributesLog = true;
                }
                String attributeName = matcher.group();
                attributeName = attributeName.replaceAll("[ ]", "").replaceAll("[=]", " = ");
                System.out.println(attributeName);
            }
            xmlParsing(list, number, actualPath);
        }

        if (textLinePart.matches(" *<[\\w-]+[ \\w(= )\"-]*>.*</[\\w-]+>")) {
            Pattern pattern = Pattern.compile("<[\\w-]+");
            Matcher matcher = pattern.matcher(textLinePart);
            if (matcher.find()) {
                String pathName = matcher.group();
                System.out.println("\n" + "Element: \n" + "path = " + actualPath + ", " + pathName.substring(1));
            }

            pattern = Pattern.compile(">.+<");
            matcher = pattern.matcher(textLinePart);
            if (matcher.find()) {
                String valueName = matcher.group();
                valueName = valueName.replaceAll("[><]", "\"");
                if (valueName.equals("\" \"")) {
                    valueName = "\"\"";
                }
                System.out.println("value = " + valueName);
            } else {
                System.out.println("value = \"\"");
            }
            pattern = Pattern.compile("[\\w-]+[= ]+\"[\\w-]+\"");
            matcher = pattern.matcher(textLinePart);

            boolean attributesLog = false;

            while (matcher.find()) {
                if (!attributesLog) {
                    System.out.println("attributes:");
                    attributesLog = true;
                }
                String attributeName = matcher.group();
                attributeName = attributeName.replaceAll("[ ]", "").replaceAll("[=]", " = ");
                System.out.println(attributeName);
            }

            System.out.println();
            xmlParsing(list, number, actualPath);
        }

        if (textLinePart.matches(" *<[\\w-]+[ \\w(= )\"-]* */>")) {
            Pattern pattern = Pattern.compile("[\\w-]+");
            Matcher matcher = pattern.matcher(textLinePart);
            if (matcher.find()) {
                String pathName = matcher.group();
                System.out.println("Element: \n" + "path = " + actualPath + ", " + pathName + "\n" + "value = null");
            }

            pattern = Pattern.compile("[\\w-]+[= ]+\"[\\w-]+\"");
            matcher = pattern.matcher(textLinePart);
            boolean attributesLog = false;

            while (matcher.find()) {
                if (!attributesLog) {
                    System.out.println("attributes:");
                    attributesLog = true;
                }
                String attributeName = matcher.group();
                attributeName = attributeName.replaceAll("[ ]", "").replaceAll("[=]", " = ");
                System.out.println(attributeName);
            }
            System.out.println();
            xmlParsing(list, number, actualPath);
        }

        if (textLinePart.matches(" *</\\w+>")) {
            int countCommas = actualPath.length() - actualPath.replace(",", "").length();

            if (countCommas > 0) {
                int textLinePartLength = textLinePart.replaceAll("[ </>]", "").length();
                actualPath = actualPath.substring(0, actualPath.length() - textLinePartLength - 2);
            } else {
                actualPath = "";
            }
            xmlParsing(list, number, actualPath);
        }
    }

    public static void fromIntermediateFormatToXML(String[] intermediateFormatArray, int number, ArrayList<String> pathElements, String xmlLineToPrint) {

        if (pathElements == null) {
            pathElements = new ArrayList<>();
        }

        boolean onlyQuotesInValue = false;

        if (intermediateFormatArray.length == number && !pathElements.isEmpty()) {
//            System.out.println("</" + pathElements.get(0) + ">");
            xmlLineToPrint += "</" + pathElements.get(0) + ">";
            if (xmlLineToPrint.startsWith("<root>")) {
                xmlLineToPrint = xmlLineToPrint.replaceAll("\n", "\n\t");
                xmlLineToPrint += "\n</root>";
            }
            xmlLineToPrint = xmlLineToPrint.replaceAll(">\t<", "><");
            System.out.println(xmlLineToPrint);
            return;
        }

        String intermediateFormat = intermediateFormatArray[number];

        number = number + 1;

        Deque<String> pathValues = null;
        String value = "";
        Deque<String> attributes = null;

        Pattern pattern = Pattern.compile("path=[\\w,]*");
        Matcher matcher = pattern.matcher(intermediateFormat);
        while (matcher.find()) {
            String[] path = matcher.group().substring(5).split(",");
            pathValues = new ArrayDeque<>(Arrays.asList(path));
        }

        pattern = Pattern.compile("value=[\\w.\"-]*");
        matcher = pattern.matcher(intermediateFormat);
        while (matcher.find()) {
            value = matcher.group();
            if (value.equals("value=\"\"")) {
                onlyQuotesInValue = true;
            }
            value = value.replaceAll("\"", "").substring(6);
        }

        pattern = Pattern.compile("attributes:[\\w=\"\\n.]*");
        matcher = pattern.matcher(intermediateFormat);
        while (matcher.find()) {
            String attributeLine = matcher.group();
            String[] attribute = attributeLine.substring(12).replaceAll("\n", " ").split("[= ]");
            attributes = new ArrayDeque<>(Arrays.asList(attribute));
        }

        int numberOfTabs = 0;

        if (!pathElements.isEmpty()) {
            for (int i = 0; i < pathElements.size(); i++) {
                if (pathElements.get(i).equals(pathValues.getFirst())) {
                    numberOfTabs++;
                    pathValues.removeFirst();
                } else {
                    for (int j = pathElements.size() - 1; j >= i; j--) {
                        for (int h = j; h >= 1; h--) {
                            xmlLineToPrint += "\t";
                        }
                        xmlLineToPrint += "</" + pathElements.get(j) + ">\n";
                        pathElements.remove(j);
                    }
                }
            }
            if (pathElements.isEmpty()) {
                xmlLineToPrint = "<root>\n" + xmlLineToPrint;
            }
        }

        if (numberOfTabs != 0) {
            for (int i = 0; i < numberOfTabs; i++) {
                xmlLineToPrint += "\t";
            }
        }

        xmlLineToPrint += "<" + pathValues.getLast();

        if (attributes == null && value.equals("null")) { }
        else if (attributes == null) {
            xmlLineToPrint += ">";
        } else {
            while (!attributes.isEmpty()) {
                xmlLineToPrint += " " + attributes.pollFirst() + "=" + attributes.pollFirst();
                if (!attributes.isEmpty()) {
                    xmlLineToPrint += " ";
                } else if (attributes.isEmpty() && !value.equals("null")) {
                    xmlLineToPrint += ">";
                }
            }
        }
        if (value.equals("")) {
            pathElements.add(pathValues.pollLast());
        } else if (value.equals("null")) {
            xmlLineToPrint += " />";
        } else {
            xmlLineToPrint += value + "</" + pathValues.pollLast() + ">";
        }
        if (!onlyQuotesInValue) {
            xmlLineToPrint += "\n";
        }
        fromIntermediateFormatToXML(intermediateFormatArray, number, pathElements, xmlLineToPrint);
    }

    public static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
