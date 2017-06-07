{
<#if resMap??>
<#list resMap?keys as key>
    <#if key = "nextFlag">
    "${key}":"${resMap[key]}"
    </#if>
    <#if key = "data">
    "${key}":[
        <#assign dataList = resMap[key]>
        <#list dataList as dataMap>
        {
            <#list dataMap?keys as key>
            <#assign type = "markType">
            <#assign markType = dataMap[type]>
            <#if key != "data">
            "${key}":"${dataMap[key]}"
            </#if>
            <#if key = "data" && markType = "1">
            "${key}":{
                <#assign allDataMap = dataMap[key]>
                <#list allDataMap?keys as key>
                <#assign chartTypeStr = "chartType">
                <#assign chartType = allDataMap[chartTypeStr]>
                <#if key != "dataName" && key != "dataValue" && key != "chart">
                "${key}":"${allDataMap[key]}"
                </#if>
                <#if key = "dataName" || key = "dataValue">
                "${key}":[
                    <#assign allList = allDataMap[key]>
                    <#list allList as all>
                    "${all}"
                    <#if all_has_next>,</#if>
                    </#list>
                ]
                </#if>
                <#if key = "chart">
                "${key}":[


                    <#if chartType = "line" || chartType = "monthBar" || chartType = "cityBar" || chartType = "cityRank">
                        <#assign chartList = allDataMap[key]>
                        <#list chartList as chartMap>
                        {
                            <#list chartMap?keys as key>
                                <#if key = "data" || key = "sequentialData" || key = "totalData">
                                "${key}":[
                                    <#assign dataList1 = chartMap[key]>
                                    <#list dataList1 as data1>
                                    ${data1}<#if data1_has_next>,</#if>
                                    </#list>
                                ]
                                </#if>
                                <#if key = "chartX" || key = "tableTitle">
                                "${key}":[
                                    <#assign dataList2 = chartMap[key]>
                                    <#list dataList2 as data2>
                                    "${data2}"<#if data2_has_next>,</#if>
                                    </#list>
                                ]
                                </#if>
                                <#if key = "tableValue">
                                "${key}":[
                                    <#assign dataList3 = chartMap[key]>
                                    <#list dataList3 as dataMap3>
                                    {
                                        <#list dataMap3?keys as key>
                                            <#if key = "rank" || key = "cityName">
                                            "${key}":"${dataMap3[key]}"
                                            </#if>
                                            <#if key = "value">
                                            "${key}":[
                                                <#assign valueLsit = dataMap3[key]>
                                                <#list valueLsit as values>
                                                "${values}"<#if values_has_next>,</#if>
                                                </#list>
                                            ]
                                            </#if>
                                            <#if key_has_next>,</#if>
                                        </#list>
                                    }<#if dataMap3_has_next>,</#if>
                                    </#list>
                                ]
                                </#if>
                                <#if key_has_next>,</#if>
                            </#list>
                        }
                        </#list>
                    </#if>
                    <#if chartType = "pie">
                        <#assign chartListPie = allDataMap[key]>
                        <#list chartListPie as chartMapPie>
                        {
                            <#list chartMapPie?keys as key>
                            "${key}":"${chartMapPie[key]}"
                                <#if key_has_next>,</#if>
                            </#list>
                        }<#if chartMapPie_has_next>,</#if>
                        </#list>
                    </#if>

                ]
                </#if>
                <#if key_has_next>,</#if>
                </#list>
            }
            </#if>
            <#if key = "data" && markType = "2">
            "${key}":{
                <#assign allDataMap2 = dataMap[key]>
                <#list allDataMap2?keys as key>
                "${key}":"${allDataMap2[key]}"
                <#if key_has_next>,</#if>
                </#list>
            }
            </#if>
            <#if key = "data" && markType = "3">
            "${key}":{
                <#assign allDataMap3 = dataMap[key]>
                <#list allDataMap3?keys as key>
                <#if key != "img">
                "${key}":"${allDataMap3[key]}"
                </#if>
                <#if key = "img">
                "${key}":[
                    <#assign imgList = allDataMap3[key]>
                    <#list imgList as img>
                    "${img}"
                    <#if img_has_next>,</#if>
                    </#list>
                ]
                </#if>
                    <#if key_has_next>,</#if>
                </#list>
            }
            </#if>
                <#if key_has_next>,</#if>
            </#list>
        }<#if dataMap_has_next>,</#if>
        </#list>
    ]
    </#if>
        <#if key_has_next>,</#if>
</#list>
<#else>
    "nextFlag":"",
    "data":[]
</#if>
}
