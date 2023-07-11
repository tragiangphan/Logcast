package com.programme.logcast;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class LogcatServer extends NanoHTTPD {
    private static final Integer PORT = 5656;
    private Map<String, String> htmlDataMap;
    private Map<String, String> csvDataMap;
    private Map<String, Boolean> changedMap;

    public LogcatServer() {
        super("0.0.0.0", PORT);
        htmlDataMap = new HashMap<>();
        csvDataMap = new HashMap<>();
        changedMap = new HashMap<>();
    }

    public LogcatServer(Map<String, String> htmlDataMap, Map<String, String> csvDataMap, Map<String, Boolean> changedMap) {
        super("0.0.0.0", PORT);
        this.htmlDataMap = htmlDataMap;
        this.csvDataMap = csvDataMap;
        this.changedMap = changedMap;
    }

    public synchronized void setHtmlData(String packageName, String htmlData) {
        // Set the HTML data for the specified package
        htmlDataMap.put(packageName, htmlData);
    }

    public synchronized String getHtmlData(String packageName) {
        // Get the HTML data for the specified package
        return htmlDataMap.get(packageName);
    }

    public synchronized void setCSVData(String packageName, String csvData) {
        // Set the HTML data for the specified package
        csvDataMap.put(packageName, csvData);
    }

    public synchronized String getCSVData(String packageName) {
        // Get the HTML data for the specified package
        return csvDataMap.get(packageName);
    }

    public synchronized void setChanged(String packageName, boolean changed) {
        // Set the change flag for the specified package
        changedMap.put(packageName, changed);
    }

    public synchronized boolean hasChanged(String packageName) {
        // Check if the log for the specified package has changed
        return Boolean.TRUE.equals(changedMap.getOrDefault(packageName, false));
    }

    @Override
    public Response serve(IHTTPSession session) {
        StringBuilder responseBuilder = new StringBuilder();

        responseBuilder.append("<html>");
        responseBuilder.append("<head>");
        responseBuilder.append("<title>Logcast Service</title>");
        responseBuilder.append("<link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">\n" +
                "<link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>\n" +
                "<link href=\"https://fonts.googleapis.com/css2?family=Work+Sans:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap\" rel=\"stylesheet\">");
        responseBuilder.append("<style>");
        responseBuilder.append("table {" +
                "    border-collapse: collapse;" +
                "    font-family: 'Work Sans', sans-serif;" +
                "    width: 70%;" +
                "}" +
                "th {" +
                "    padding: 8px;" +
                "    border: 1px solid #A76F6F;" +
                "    text-align: center;" +
                "    font-size: 1.2em;" +
                "}" +
                "td {" +
                "    padding: 8px;" +
                "    border: 1px solid #A76F6F;" +
                "    text-align: center;" +
                "}" +
                "tr:nth-child(even) {" +
                "    background-color: #FFECEC;" +
                "}" +
                "tr:hover {" +
                "    background-color: #F4D3D3;" +
                "}" +
                "th {" +
                "    background-color: #A76F6F;" +
                "    color: #FFFFFF;" +
                "    text-align: center;" +
                "}");
        responseBuilder.append("table { border-collapse: collapse; width: 100%}");
        responseBuilder.append(".short { width: 7% }");
        responseBuilder.append(".medium { width: 20% }");
        responseBuilder.append(".long { width: 52% }");
        responseBuilder.append(".btn { display: flex; justify-content: center; }");
        responseBuilder.append(".button-9 {" +
                "  appearance: button;" +
                "  backface-visibility: hidden;" +
                "  background-color: #2D4356;" +
                "  border-radius: 6px;" +
                "  border-width: 0;" +
                "  box-shadow: rgba(50, 50, 93, .1) 0 0 0 1px inset,rgba(50, 50, 93, .1) 0 2px 5px 0,rgba(0, 0, 0, .07) 0 1px 1px 0;" +
                "  box-sizing: border-box;" +
                "  color: #fff;" +
                "  cursor: pointer;" +
                "  font-family: 'Work Sans', sans-serif;" +
                "  font-size: 100%;" +
                "  height: 44px;" +
                "  line-height: 1.15;" +
                "  margin: 12px 0 0;" +
                "  outline: none;" +
                "  overflow: hidden;" +
                "  padding: 0 25px;" +
                "  position: relative;" +
                "  text-align: center;" +
                "  text-transform: none;" +
                "  transform: translateZ(0);" +
                "  transition: all .2s,box-shadow .08s ease-in;" +
                "  user-select: none;" +
                "  -webkit-user-select: none;" +
                "  touch-action: manipulation;" +
                "  width: 30%;" +
                "}" +
                "" +
                ".button-9:disabled {" +
                "  cursor: default;" +
                "}" +
                "" +
                ".button-9:focus {" +
                "  box-shadow: rgba(50, 50, 93, .1) 0 0 0 1px inset, rgba(50, 50, 93, .2) 0 6px 15px 0, rgba(0, 0, 0, .1) 0 2px 2px 0, rgba(50, 151, 211, .3) 0 0 0 4px;" +
                "}");
        responseBuilder.append("</style>");
        responseBuilder.append("</head>");
        responseBuilder.append("<body>");
        responseBuilder.append("<label for=\"tentacles\">Enter milliseconds for reload page:</label>");
        responseBuilder.append("<input type=\"number\" id=\"milliseconds\" name=\"milliseconds\" min=\"5\" max=\"86400\">");

        for (String packageName : htmlDataMap.keySet()) {
            String htmlData = htmlDataMap.get(packageName);
            boolean hasChanged = Boolean.TRUE.equals(changedMap.getOrDefault(packageName, false));
            // Handle logic when button is pressed
            if (session.getUri().equals("/download/logcat.csv")) {
                String csvHeader = "Date, Time, PID, Level, Name, Message \n";
                String csvData = csvDataMap.get(packageName);
                String csvContent = csvHeader + csvData;

                assert csvData != null;
                InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
                // Push data in byte type and response compile to csv
                Response response = newFixedLengthResponse(Response.Status.OK, "text/csv", inputStream, csvContent.length());
                response.addHeader("Content-Disposition", "attachment; filename=" + packageName + ".csv");
                return response;
            }
            if (htmlData != null) {

                responseBuilder.append("<h2 style=\"{color: #435B66; font-family: 'Work Sans', sans-serif;}\">").append(packageName.toUpperCase()).append("</h2>");
                responseBuilder.append("<table>");
                responseBuilder.append("<tr><th class=\"short\">Date</th><th class=\"short\">Time</th><th class=\"short\">PID</th><th class=\"short\">Level</th><th class=\"medium\">Name</th><th class=\"long\">Message</th></tr>");
                responseBuilder.append(htmlData);
                responseBuilder.append("</table>");
                responseBuilder.append("<div class=\"btn\">");
                responseBuilder.append("<button class=\"button-9\" onclick=\"downloadCSV()\">See All</button>");
                responseBuilder.append("</div>");
                responseBuilder.append("<script>");
                responseBuilder.append("function downloadCSV() {");
                responseBuilder.append("    window.location.href = \"/download/logcat.csv\";");
                responseBuilder.append("}");
                responseBuilder.append("</script>");
            }

            if (hasChanged) {
                // Clear the change flag for the package after displaying the logs
                changedMap.put(packageName, false);
            }
        }

        responseBuilder.append("<script>");
        responseBuilder.append("setTimeout(function() {");
        responseBuilder.append("var sec = document.getElementById(\"milliseconds\").value");
        responseBuilder.append("window.location.reload(); }, sec);");
        responseBuilder.append("</script>");
        responseBuilder.append("</body>");
        responseBuilder.append("</html>");

        return newFixedLengthResponse(responseBuilder.toString());
    }
}