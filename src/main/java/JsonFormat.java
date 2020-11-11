import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonFormat {

    public static void jsonParsing(Deque<String> list, int number, Deque<String> stock, String lastValueFromStock) {
        String textLinePart = list.pollFirst();

        number = number + 1;

        if (list.isEmpty()) {
            return;
        }

        if (textLinePart.equals("},\n\"\":") || textLinePart.equals("},\"\":")) {
            lastValueFromStock = "NOK";
            list.removeFirst();
            jsonParsing(list, number, stock, lastValueFromStock);
        }

        if (textLinePart.startsWith("}") && !stock.isEmpty()) {
            stock.removeLast();
        }

        if (textLinePart.equals("{")) {
            System.out.println("\nvalue = \"\"" );
        }

        Deque<String> values = new ArrayDeque<>();
        Pattern valuePattern = Pattern.compile("(:\"?[\\w-.']+\"?|:\"\")");
        Matcher valueMatcher = valuePattern.matcher(textLinePart);
        while (valueMatcher.find()) {
            String value = valueMatcher.group().replaceAll(":", "");
            if (!value.startsWith("\"") && !value.equals("null")) {
                value = "\"" + value + "\"";
            }
            values.add(value);
        }

        boolean correctParams = true;

        Deque<String> params = new ArrayDeque<>();

        if (textLinePart.startsWith("{\n\"@") || textLinePart.startsWith("{\n\"#") || textLinePart.startsWith("{\"@") || textLinePart.startsWith("{\"#")) {
            Pattern pattern = Pattern.compile("\"[@#]?\\w*\":");
            Matcher matcher = pattern.matcher(textLinePart);
            while (matcher.find()) {
                String paramName = matcher.group().replaceAll("[\":]", "");
                if (paramName.equals("@") || paramName.equals("#") || paramName.equals("")) {
                    correctParams = false;
                }
                params.add(paramName);
            }
        }
        if (!params.isEmpty() && !params.getLast().substring(1).equals(lastValueFromStock)) {
            correctParams = false;
        } else if (!list.isEmpty()) {
            Pattern pattern = Pattern.compile("\"\\w*\":");
            Matcher matcher = pattern.matcher(textLinePart);
            while (matcher.find()) {
                String path = "";
                System.out.print("\nElement:\npath = ");
                for (String pathNameFromStock : stock) {
                    path += pathNameFromStock + ", ";
                }
                String pathName = matcher.group().replaceAll("[\":]", "");
                lastValueFromStock = pathName;
                stock.add(pathName);
                path += pathName;
                System.out.print(path);

                if (!values.isEmpty()) {
                    System.out.println("\nvalue = " + values.pollFirst());
                    stock.removeLast();
                }
            }
        }

        printAttributes:
        while (!params.isEmpty()) {
            if (params.getFirst().equals("@")) {
                params.removeFirst();
                values.removeFirst();
                continue printAttributes;
            }
            if (params.getLast().equals(lastValueFromStock) && (params.getFirst().startsWith("@") || params.getFirst().startsWith("#"))) {
                params.removeFirst();
                values.removeFirst();
                continue printAttributes;
            }
            if (correctParams) {
/*                    if (params.pollLast().substring(1).equals(stock.peekLast())) {
                        System.out.println("\nvalue = " + values.pollLast());
                    }
                    if (!params.isEmpty()) {
                        if (params.getFirst().startsWith("@")) {
                            System.out.println("\nattributes:");
                            while (!params.isEmpty()) {
                                String param = params.pollFirst();
                                param = param.substring(1);
                                String value = values.pollFirst();
                                if (value.equals("null")) {
                                    value = "\"\"";
                                }
                                System.out.println(param + " = " + value);
                            }
                        }
                    }*/
                String valueLine = "";
                String attributesLine = "\nattributes:";

                while (!params.isEmpty() && params.getFirst().startsWith("@")) {
                    String param = params.pollFirst();
                    param = param.substring(1);
                    String value = values.pollFirst();
                    if (value.equals("null")) {
                        value = "\"\"";
                    }
                    attributesLine += "\n" + param + " = " + value;
                }
                if (params.pollLast().substring(1).equals(stock.peekLast()) && !values.isEmpty()) {
                    valueLine = "\nvalue = " + values.pollLast();
                }
                if (!valueLine.equals("")) {
                    System.out.println(valueLine);
                }
                if (!attributesLine.equals("\nattributes:")) {
                    System.out.println(attributesLine);
                }
            } else {
                if (params.getFirst().equals("#")) {
                    System.out.println("\nvalue = \"\"");
                    params.removeFirst();
                } else {
                    System.out.print("\nElement:\npath = ");
                    for (String pathName : stock) {
                        System.out.print(pathName + ", ");
                    }

                    String param = params.pollFirst();
                    if (param.startsWith("@") || param.startsWith("#")) {
                        System.out.print(param.substring(1));
                    } else {
                        System.out.print(param);
                    }
                    System.out.print("\nvalue = " + values.pollFirst() + "\n");
                }
            }
        }
        jsonParsing(list, number, stock, lastValueFromStock);
    }

    public static void fromIntermediateFormatToJSON(String[] intermediateFormatArray, int number, ArrayList<String> pathElements, String jsonLineToPrint) {
        if (pathElements == null) {
            pathElements = new ArrayList<>();
        }

        boolean isValuePresent = false;

        if (intermediateFormatArray.length == number) {
            while (!pathElements.isEmpty()) {
                for (int i = 0; i < pathElements.size(); i++) {
                    jsonLineToPrint += "\t";
                }
                jsonLineToPrint += "}\n";
                pathElements.remove(pathElements.size() - 1);
            }
            jsonLineToPrint += "}";
            System.out.println(jsonLineToPrint);
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

        pattern = Pattern.compile("value=[\\w.\"-@ ]*");
        matcher = pattern.matcher(intermediateFormat);
        while (matcher.find()) {
            isValuePresent = true;
            value = matcher.group();
            if (!value.equals("value=\"\"")) {
                value = value.replaceAll("\"", "").substring(6);
            } else {
                value = "";
            }
        }

        pattern = Pattern.compile("attributes:[\\w=\"\\n.]*");
        matcher = pattern.matcher(intermediateFormat);
        while (matcher.find()) {
            String attributeLine = matcher.group();
            String[] attribute = attributeLine.substring(12).replaceAll("\n", " ").split("[= ]");
            attributes = new ArrayDeque<>(Arrays.asList(attribute));
        }

        int numberOfTabs = 1;
        String tabsForPath = "";

        if (!pathElements.isEmpty()) {
            for (int i = 0; i < pathElements.size(); i++) {
                if (pathElements.get(i).equals(pathValues.getFirst())) {
                    numberOfTabs++;
                    pathValues.removeFirst();
                }
            }
        }

        for (int i = 0; i < numberOfTabs; i++) {
            tabsForPath += "\t";
        }

        jsonLineToPrint += tabsForPath + "\"" + pathValues.getLast() + "\": ";

        if (isValuePresent && attributes == null) {
            if (value.equals("null")) {
                jsonLineToPrint += value + ",\n";
            } else {
                jsonLineToPrint += "\"" + value + "\",\n";
            }
        } else if (!isValuePresent && attributes == null) {
            pathElements.add(pathValues.pollLast());
            jsonLineToPrint += "{\n";
        } else {
            boolean elemInsideElem = false;
            jsonLineToPrint += "{\n";
            while (!attributes.isEmpty()) {
                jsonLineToPrint += tabsForPath + "\t\"@" + attributes.pollFirst() + "\": " + attributes.pollFirst() + ",\n";
            }
            jsonLineToPrint += tabsForPath + "\t\"#" + pathValues.getLast() + "\": ";
            if (value.equals("null")) {
                jsonLineToPrint += value;
            } else if (!isValuePresent) {
                pathElements.add(pathValues.getLast());
                pathElements.add(pathValues.getLast());
                jsonLineToPrint += "{";
                elemInsideElem = true;
            } else {
                jsonLineToPrint += "\"" + value + "\"";
            }
            jsonLineToPrint += "\n";

            if (!elemInsideElem) {
                if (intermediateFormatArray.length != number) {
                    jsonLineToPrint += "},\n";
                } else {
                    jsonLineToPrint += "}\n";
                }
            }
        }
        fromIntermediateFormatToJSON(intermediateFormatArray, number, pathElements, jsonLineToPrint);
    }
}
