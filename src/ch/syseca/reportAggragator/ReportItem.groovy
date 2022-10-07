package ch.syseca.reportAggragator

import com.cloudbees.groovy.cps.NonCPS

class ReportItem implements Comparable<ReportItem> {

    static Set<String> keyHeader = new LinkedHashSet<>()
    static Set<String> valueHeader = new LinkedHashSet<>()
    String keyData
    final Set<String> valueData = new HashSet<>()

    boolean hasExcludedItems(Set<String> excludedItems) {
        for (String exItem in excludedItems) {
            if (this.keyData.contains(exItem)) {
                return true
            }
        }
        return false
    }

    static String createData(Map<String, String> elements, Set<String> header) {
        StringBuilder sb = new StringBuilder()
        for (String item in header) {
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
    @NonCPS
    int hashCode() {
        return keyData.hashCode()
    }

    @Override
    int compareTo(ReportItem o) {
        return keyData <=> o.keyData
    }
}
