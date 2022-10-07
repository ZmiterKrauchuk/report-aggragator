
def createReport(){
    ReportAggregator.createReport()
}

def setProjectHomeDir(String path){
    ReportAggregator.inputDirectoryParam = path
}

def setOutputFile(String path){
    ReportAggregator.outputFileParam = path
}

def setKeyHeader(LinkedHashSet<String> set){
    ReportAggregator.keyHeaderParam.removeAll(ReportAggregator.keyHeaderParam)
    ReportAggregator.keyHeaderParam.addAll(set)
}

def setValueHeader(LinkedHashSet<String> set){
    ReportAggregator.valueHeaderParam.removeAll(ReportAggregator.valueHeaderParam)
    ReportAggregator.valueHeaderParam.addAll(set)
}

def setExcludedItemsParam(HashSet<String> set){
    ReportAggregator.excludedItemsParam.removeAll(ReportAggregator.excludedItemsParam)
    ReportAggregator.valueHeaderParam.addAll(set)
}
