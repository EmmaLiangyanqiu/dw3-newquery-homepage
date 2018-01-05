{
<#if resMap??>
    <#list resMap?keys as key>
        <#if key = "nextFlag">
        "${key}":"${resMap[key]}"
        </#if>
        <#if key = "keyword">
        "${key}":[
            <#assign keywordList = resMap[key]>
            <#list keywordList as keywordMap>
            {
                <#list keywordMap?keys as key>
                    <#if key = "flag">
                    "${key}":${keywordMap[key]?c}
                    </#if>
                    <#if key = "keyData" || key = "recommendData">
                    "${key}":{
                        <#assign keydataMap = keywordMap[key]>
                        <#list keydataMap?keys as key>
                            "${key}":"${keydataMap[key]}"<#if key_has_next>,</#if>
                        </#list>
                    }
                    </#if><#if key_has_next>,</#if>
                </#list>
            }<#if keywordMap_has_next>,</#if>
            </#list>
        ]
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
                            <#assign chartType = allDataMap[chartTypeStr]!''>
                            <#if key != "dataName" && key != "dataValue" && key != "chart" && key != "dimension" && key != "selectTypeDisplay" && key != "deleteDisplay">
                            "${key}":"${allDataMap[key]!''}"
                            </#if>

                            <#if key = "selectTypeDisplay" || key = "deleteDisplay">
                            "${key}":[
                                <#assign displayList = allDataMap[key]>
                                <#list displayList as displayMap>
                                {
                                    <#list displayMap?keys as key>
                                    "${key}":"${displayMap[key]}"
                                    </#list>
                                }<#if displayMap_has_next>,</#if>
                                </#list>
                            ]
                            </#if>
                            <#if key = "dimension">
                            "${key}":[
                                <#assign dimensionList = allDataMap[key]>
                                <#list dimensionList as dimensionMap>
                                {
                                    <#list dimensionMap?keys as key>
                                        <#if key != "selectType">
                                        "${key}":"${dimensionMap[key]}"
                                        </#if>
                                        <#if key = "selectType">
                                        "${key}":[
                                            <#assign selectTypeList = dimensionMap[key]>
                                            <#list selectTypeList as selectTypeMap>
                                            {
                                                <#list selectTypeMap?keys as key>
                                                "${key}":[
                                                    <#assign selectList = selectTypeMap[key]>
                                                    <#list selectList as selects>
                                                    "${selects}"<#if selects_has_next>,</#if>
                                                    </#list>
                                                ]
                                                </#list>
                                            }<#if selectTypeMap_has_next>,</#if>
                                            </#list>
                                        ]
                                        </#if>
                                        <#if key_has_next>,</#if>
                                    </#list>
                                }
                                </#list>
                            ]
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
                                <#if chartType = "line" || chartType = "monthBar" || chartType = "cityBar" || chartType = "cityRank"|| chartType21 = "yearBar">
                                    <#assign chartList = allDataMap[key]>
                                    <#list chartList as chartMap>
                                    {
                                        <#list chartMap?keys as key>
                                            <#if key = "title">
                                            "${key}":"${chartMap[key]!''}"
                                            </#if>
                                            <#if key = "data" || key = "sequentialData" || key = "totalData">
                                            "${key}":[
                                                <#assign dataList1 = chartMap[key]>
                                                <#list dataList1 as data1>
                                                "${data1}"<#if data1_has_next>,</#if>
                                                </#list>
                                            ]
                                            </#if>
                                            <#if key = "yoyData">
                                            "YoYData":[
                                                <#assign dataList111 = chartMap[key]>
                                                <#list dataList111 as data111>
                                                "${data111}"<#if data111_has_next>,</#if>
                                                </#list>
                                            ]
                                            </#if>
                                            <#if key = "chartX" || key = "tableTitle" || key = "example">
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
                                <#if chartType = "pie" || chartType = "product" || chartType = "businessPie" || chartType = "channel" >
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
                    <#if key = "data" && markType = "4">
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
"keyword":[],
"data":[]
</#if>
}
