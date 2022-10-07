package ch.syseca.reportAggragator
@Grab('org.jsoup:jsoup:1.10.2')
@Grab('org.w3c:dom:2.3.0-jaxb-1.0.6')

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate

class HtmlReportReader {

    static String inputDirectory
    static String fileName
    static String rootElementName
    static Set<String> excludedItems
    static final Set<ReportItem> reportItems = new HashSet<>()

    static void read() throws IOException {
        reportItems.removeAll(reportItems)

        File dir = new File(inputDirectory)
        String absolutePath = dir.getAbsolutePath()
        Path input = Paths.get(absolutePath)

        Files.find(input, 100, fileNameMatcher()).forEach({ file ->
            try {
                parseDependencies(readAsDocument(file))
            } catch (Exception e) {
                e.printStackTrace()
            }
        })
    }

    private static BiPredicate<Path, BasicFileAttributes> fileNameMatcher() {
        { path, attrs -> path.endsWith(fileName) }
    }

    private static Document readAsDocument(Path path) throws IOException {
        final byte[] allBytes = Files.readAllBytes(path)
        String xml = new String(allBytes, StandardCharsets.UTF_8)

        return Jsoup.parse(xml)
    }

    private static void parseDependencies(Document doc) {
        Elements sections = doc.select(rootElementName)

        sections.forEach({ element -> addNodeItems(element) })
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
                .forEach({ childNode ->
                    elements.put(headerIndexes.get(childNode.siblingIndex()), getValue(childNode, extraVals))
                })

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
        Map<Integer, String> actualOrderOfHeaderItems = new HashMap<>()
        for (Element child : s.child(0).children()) {
            actualOrderOfHeaderItems.put(child.siblingIndex(), getValue(child, null))
        }

        return actualOrderOfHeaderItems
    }
}

