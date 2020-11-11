import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Please, type text file name: ");
        String txtName = scanner.nextLine();
        String fileName = ".///input/" + txtName;
        Path filePath = Paths.get(fileName);
        String textLine = null;
        boolean jsonToXml = true;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        try {
            textLine = Files.readString(filePath, StandardCharsets.US_ASCII);
        } catch (IOException e) {
            System.out.println("File not found: " + e.getMessage());
        }

        if (textLine.startsWith("{")) {
            textLine = textLine.replaceAll(" ", "");
            String[] textLineArray = textLine.split("(?=(\\{|}))");
            Deque<String> arrayWithTextLineToDeque = new ArrayDeque<>(Arrays.asList(textLineArray));

            Deque<String> stock = new ArrayDeque<>();
            JsonFormat.jsonParsing(arrayWithTextLineToDeque, 0, stock, null);
        } else {
            jsonToXml = false;
            String[] textLineArray = textLine.split("\\r?\\n");

            if (textLineArray.length == 1) {
                textLine = textLine.replaceAll("2></", "2> </").replaceAll("2\"></", "2\"> </");
                String multiLineXml = XmlLanguage.prettyFormat(textLine, 2);
                textLineArray = multiLineXml.split("\\r?\\n");
            }
            XmlLanguage.xmlParsing(textLineArray, 0, "");
        }

        String capturedConsoleOutput = baos.toString().replaceAll("(?m)^[ \t]*\r?\n", "");
        String[] intermediateFormatArray = capturedConsoleOutput.replaceAll(" = ", "=").replaceAll(", ", ",").split("Element:");
        System.out.flush();
        System.setOut(old);

        ArrayList<String> abs = new ArrayList<>();

        if (jsonToXml) {
            XmlLanguage.fromIntermediateFormatToXML(intermediateFormatArray, 1, abs, "");
        } else {
            JsonFormat.fromIntermediateFormatToJSON(intermediateFormatArray, 1, abs, "{\n");
        }
    }
}
