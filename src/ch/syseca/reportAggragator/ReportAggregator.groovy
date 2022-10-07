package ch.syseca.reportAggragator

@Grab('org.jsoup:jsoup:1.10.2')

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.Elements

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

class ReportAggregator {
    static Set<String> keyHeaderParam = new LinkedHashSet<>()
    static Set<String> valueHeaderParam = new LinkedHashSet<>()
    static String inputDirectoryParam = "../"
    static Set<String> excludedItemsParam = new HashSet<>(Arrays.asList("-", "null"))
    static def fileNameParam = 'dependencies.html'
    static String rootElementNameParam = "tbody"
    static String outputFileParam = "licenses.html"

    static void createReport() throws Exception {
        setUpHtmlReportReaderWriter()
        HtmlReportReader.read()

        HtmlReportWriter.write()

    }

    private static void setUpHtmlReportReaderWriter() {
        HtmlReportReader.fileName = fileNameParam
        HtmlReportReader.inputDirectory = inputDirectoryParam
        HtmlReportReader.rootElementName = rootElementNameParam
        HtmlReportReader.excludedItems = excludedItemsParam

        HtmlReportWriter.outputFile = outputFileParam

        ReportItem.keyHeader = new LinkedHashSet<>()
        ReportItem.keyHeader.addAll(keyHeaderParam)

        ReportItem.valueHeader = new LinkedHashSet<>()
        ReportItem.valueHeader.addAll(valueHeaderParam)
    }

    private static class ReportItem implements Comparable<ReportItem> {

        private static Set<String> keyHeader = new LinkedHashSet<>()
        private static Set<String> valueHeader = new LinkedHashSet<>()
        private String keyData
        private final Set<String> valueData = new HashSet<>()

        boolean hasExcludedItems(Set<String> excludedItems) {
            for (String exItem : excludedItems) {
                if (this.keyData.contains(exItem)) {
                    return true
                }
            }
            return false
        }

        static String createData(Map<String, String> elements, Set<String> header) {
            StringBuilder sb = new StringBuilder()
            for (String item : header) {
                sb.append(elements.get(item))
                sb.append(" : ")
            }
            sb.replace(sb.length() - 3, sb.length(), "")

            return sb.toString()
        }

        @Override
        String toString() {
            return "ReportItem{" +
                    "keyData='" + keyData + '\'' +
                    ", valueData='" + valueData + '\'' +
                    '}'
        }

        @Override
        boolean equals(Object o) {
            if (this == o) {
                return true
            }
            if (o == null || getClass() != o.getClass()) {
                return false
            }

            ReportItem that = (ReportItem) o

            return keyData == that.keyData
        }

        @Override
        int hashCode() {
            return keyData.hashCode()
        }

        @Override
        int compareTo(ReportItem o) {
            return keyData <=> o.keyData
        }
    }

    private static class HtmlReportReader {

        private static String inputDirectory
        private static String fileName
        private static String rootElementName
        private static Set<String> excludedItems
        private static final Set<ReportItem> reportItems = new HashSet<>()

        static void read() throws IOException {
            reportItems.removeAll(reportItems)

            File dir = new File(inputDirectory)
            String absolutePath = dir.getAbsolutePath()
            Path input = Paths.get(absolutePath)

            Files.find(input, 100, (path, attrs) -> path.endsWith(fileName)).forEach(file -> {

                try {
                    parseDependencies(readAsDocument(file))
                } catch (Exception e) {
                    e.printStackTrace()
                }
            })
        }

        private static Document readAsDocument(Path path) throws IOException {
            final byte[] allBytes = Files.readAllBytes(path)
            String xml = new String(allBytes, StandardCharsets.UTF_8)

            return Jsoup.parse(xml)
        }

        private static void parseDependencies(Document doc) {
            Elements sections = doc.select(rootElementName)

            sections.forEach(HtmlReportReader::addNodeItems)
        }

        private static void addNodeItems(Element s) {
            // defines indexes for each item in the header
            Map<Integer, String> headerIndexes = getActualOrderOfHeaderItems(s)

            for (int i = 1; i < s.children().size(); i++) {
                addReportItem(s.child(i), headerIndexes)
            }
        }

        private static void addReportItem(Element node, Map<Integer, String> headerIndexes) {
            Map<String, String> elements = new HashMap<>()
            Map<Integer, Elements> extraVals = new HashMap<>() // for those nodes, which have children (Licenses)

            node.children()
                    .forEach(childNode ->
                            elements.put(headerIndexes.get(childNode.siblingIndex()), getValue(childNode, extraVals)))

            applyData(elements)

            if (extraVals.keySet().size() > 0) {
                for (Elements e : extraVals.values()) {
                    elements.put(headerIndexes.get(extraVals.keySet().iterator().next()), getValue(e.first(), extraVals))
                    applyData(elements)
                }
            }

        }

        private static void applyData(Map<String, String> elements) {
            String keyData = ReportItem.createData(elements, ReportItem.keyHeader)
            String valueData = ReportItem.createData(elements, ReportItem.valueHeader)

            Map<String, ReportItem> searchNodes = new HashMap<>()
            for (ReportItem item : reportItems) {
                searchNodes.put(item.keyData, item)
            }

            ReportItem reportItem = searchNodes.get(keyData)

            if (reportItem == null) {
                reportItem = new ReportItem()
                reportItem.keyData = keyData
            }
            reportItem.valueData.add(valueData)

            if (!hasExcludedItems(reportItem)) {
                reportItems.add(reportItem)
            }
        }

        private static boolean hasExcludedItems(ReportItem actualItem) {
            if (excludedItems != null) {
                return actualItem.hasExcludedItems(excludedItems)
            }
            return false
        }

        private static String getValue(Element td, Map<Integer, Elements> extraVals) {
            Elements tds = td.children()
            if (tds.size() > 1 && tds.get(0).select("a") != null
                    && tds.get(0).select("a").first() != null) {

                String value = tds.get(0).select("a").first().text()
                tds.remove(0)
                extraVals.put(td.siblingIndex(), tds)
                return value

            } else if (td.text() != null) {
                return td.text()
            } else {
                return "-"
            }
        }

        private static Map<Integer, String> getActualOrderOfHeaderItems(Element s) {
            return s.child(0).children().stream()
                    .collect(Collectors.toMap(Node::siblingIndex,
                            td -> getValue(td as Element, null)))
        }
    }

    private static class HtmlReportWriter {

        private static String outputFile

        private static final String intro = "\n<!DOCTYPE html>\n " +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\" />\n" +
                "<title>Report aggregation result</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table> \n" +
                "<tbody>"

        private static final String conclusion = "</tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>"

        private static Set<ReportItem> reportItems = HtmlReportReader.reportItems

        private static List<ReportItem> getSortedReportItems() {
            List<ReportItem> list = new ArrayList<>(reportItems)
            Collections.sort(list)
            return list
        }

        static void write() throws FileNotFoundException {

            String html = prepareDocument()

            File file = new File((outputFile))
            try (PrintWriter out = new PrintWriter(file.getPath())) {
                out.println(html)
            }
        }

        private static String prepareDocument() {
            return intro + createHeader() + createBody() + conclusion
        }

        private static StringBuilder createBody() {
            if (reportItems.size() > 0) {
                StringBuilder sb = new StringBuilder()
                addAllItems(sb)
                return sb
            } else {
                return null
            }
        }

        private static void addAllItems(StringBuilder sb) {
            getSortedReportItems().forEach(i -> addOneItem(i, sb))
        }

        private static void addOneItem(ReportItem item, StringBuilder sb) {
            if (item.keyData != null) {
                sb.append(String.format("<tr><td>%s</td>", item.keyData))
                sb.append(String.format("<td>%s</td></tr>", getSortedValueData(item)))
            }
        }

        private static String getSortedValueData(ReportItem item) {
            Set<String> raw = item.valueData
            List<String> list = new ArrayList<>(raw)
            Collections.sort(list)
            if (list.get(0) != "null") {
                return join(list, "<br/>")
            } else {
                return "-"
            }
        }

        private static StringBuilder createHeader() {
            StringBuilder sb = new StringBuilder()
            sb.append(String.format("<tr><th>%s</th>", createKeyHeader()))
            sb.append(String.format("<th>%s</th></tr>", createValueHeader()))
            return sb
        }

        private static String createKeyHeader() {
            return join(ReportItem.keyHeader, " : ")
        }

        private static String createValueHeader() {
            return join(ReportItem.valueHeader, " : ")
        }

        private static String join(Collection var0, String var1) {
            StringBuffer var2 = new StringBuffer()

            for (Iterator var3 = var0.iterator(); var3.hasNext(); var2.append((String) var3.next())) {
                if (var2.length() != 0) {
                    var2.append(var1)
                }
            }

            return var2.toString()
        }
    }
}

