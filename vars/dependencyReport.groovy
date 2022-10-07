import ch.syseca.reportAggragator.ReportAggregator

static def createReport(){
    ReportAggregator.createReport()
    println("This is println log")
}

static def setProjectHomeDir(String path){
    ReportAggregator.inputDirectoryParam = path
}

static def setOutputFile(String path){
    ReportAggregator.outputFileParam = path
}

static def setKeyHeader(LinkedHashSet<String> set){
    ReportAggregator.keyHeaderParam.removeAll(ReportAggregator.keyHeaderParam)
    ReportAggregator.keyHeaderParam.addAll(set)
}

static def setValueHeader(LinkedHashSet<String> set){
    ReportAggregator.valueHeaderParam.removeAll(ReportAggregator.valueHeaderParam)
    ReportAggregator.valueHeaderParam.addAll(set)
}

static def setExcludedItemsParam(HashSet<String> set){
    ReportAggregator.excludedItemsParam.removeAll(ReportAggregator.excludedItemsParam)
    ReportAggregator.valueHeaderParam.addAll(set)
}
