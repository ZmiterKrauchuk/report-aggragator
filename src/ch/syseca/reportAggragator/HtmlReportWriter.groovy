package ch.syseca.reportAggragator

class HtmlReportWriter {

    static String outputFile

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
        println "HtmlReportWriter start"
        String html = prepareDocument()

        File file = new File((outputFile))
        PrintWriter out = new PrintWriter(file.getPath())
        try {
            out.println(html)
        } finally {
            out.close()
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
        getSortedReportItems().each{ i -> addOneItem(i, sb) }
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
            return list.join("<br/>")
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
        return ReportItem.keyHeader.join(" : ")
    }

    private static String createValueHeader() {
        return ReportItem.valueHeader.join(" : ")
    }

}
