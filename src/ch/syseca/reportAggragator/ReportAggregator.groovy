package ch.syseca.reportAggragator

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

}

