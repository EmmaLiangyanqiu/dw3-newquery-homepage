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
                            <#if key != "garbage">
                            "${key}":"${keydataMap[key]}"<#if key_has_next>,</#if>
                            </#if>
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
                <#assign ordStr = "ord">
                <#assign ord = dataMap[ordStr]>
                <#if ord = "1">
                {
                    <#list dataMap?keys as key>
                        <#if key != "chartData" && key != "dataName" && key != "dataValue" && key != "selectTypeDisplay" && key != "dimension" && key!="deleteDisplay">
                        "${key}":"${dataMap[key]!''}"
                        </#if>
                        <#if key = "selectTypeDisplay" || key = "deleteDisplay">
                        "${key}":[
                            <#assign displayList = dataMap[key]>
                            <#list displayList as displayMap>
                            {
                                <#list displayMap?keys as key>
                                "${key}":"${displayMap[key]}"<#if key_has_next>,</#if>
                                </#list>
                            }<#if displayMap_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key = "dimension">
                        "${key}":[
                            <#assign dimensionList = dataMap[key]>
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
                            <#assign listaaa = dataMap[key]>
                            <#list listaaa as aaa>
                            "${aaa}"<#if aaa_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key = "chartData">
                        "${key}":[
                            <#assign chartDataList = dataMap[key]!''>
                            <#list chartDataList as chartDataMap>
                            {
                                <#list chartDataMap?keys as key>
                                    <#assign chartStr = "chartType">
                                    <#assign chartType = chartDataMap[chartStr]!''>
                                    <#if key = "chartType" || key = "unit">
                                    "${key}":"${chartDataMap[key]!''}"
                                    </#if>
                                    <#if key = "chart">
                                    "${key}":[
                                        <#if chartType = "line" || chartType = "monthBar" || chartType = "cityBar" || chartType = "cityRank">
                                            <#assign chartList = chartDataMap[key]>
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
                                            <#assign chartListPie = chartDataMap[key]>
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
                            }<#if chartDataMap_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key_has_next>,</#if>
                    </#list>
                }<#if dataMap_has_next>,</#if>
                </#if>
                <#if ord != "1">
                {
                    <#list dataMap?keys as key>
                        <#assign chartStr21 = "chartType">
                        <#assign chartType21 = dataMap[chartStr21]!''>
                        <#if key != "chart" && key != "dataValue" && key != "dataName" && key != "selectTypeDisplay" && key != "dimension" && key != "deleteDisplay">
                        "${key}":"${dataMap[key]!''}"
                        </#if>
                        <#if key = "selectTypeDisplay" || key = "deleteDisplay">
                        "${key}":[
                            <#assign displayList = dataMap[key]>
                            <#list displayList as displayMap>
                            {
                                <#list displayMap?keys as key>
                                "${key}":"${displayMap[key]}"<#if key_has_next>,</#if>
                                </#list>
                            }<#if displayMap_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key = "dimension">
                        "${key}":[
                            <#assign dimensionList = dataMap[key]>
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
                            <#assign list1 = dataMap[key]>
                            <#list list1 as data1>
                            "${data1}"<#if data1_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key = "chart">
                        "${key}":[
                            <#if chartType21 = "line" || chartType21 = "monthBar" || chartType21 = "cityBar" || chartType21 = "cityRank"|| chartType21 = "yearBar">
                                <#assign chartList = dataMap[key]>
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
                            <#if chartType21 = "pie" || chartType21 = "product" || chartType21 = "businessPie" || chartType21 = "channel" >
                                <#assign chartListPie = dataMap[key]>
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
                }<#if dataMap_has_next>,</#if>
                </#if>
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
