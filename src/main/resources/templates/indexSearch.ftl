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
                <#assign ordStr = "ord">
                <#assign ord = dataMap[ordStr]>
                <#if ord = "1">
                {
                    <#list dataMap?keys as key>
                        <#if key != "chartData" && key != "dataName" && key != "dataValue">
                        "${key}":"${dataMap[key]}"
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
                            <#assign chartDataList = dataMap[key]>
                            <#list chartDataList as chartDataMap>
                            {
                                <#list chartDataMap?keys as key>
                                    <#assign chart = "chartType">
                                    <#assign chartType = chartDataMap[chart]>
                                    <#if key = "chartType" || key = "unit">
                                    "${key}":"${chartDataMap[key]}"
                                    </#if>
                                    <#if key = "chart">
                                    "${key}":[
                                    <#assign chartList = chartDataMap[key]>
                                    <#list chartList as chartMap>
                                    {
                                        <#list chartMap?keys as key>
                                        <#if chartType = "line">
                                        <#if key = "data">
                                        "${key}":[
                                        <#assign dataList = chartMap[key]>
                                        <#list dataList as data>
                                            ${data}<#if data_has_next>,</#if>
                                        </#list>
                                        ]
                                        </#if>
                                        <#if key = "chartX">
                                                "${key}":[
                                                    <#assign chartXList = chartMap[key]>
                                                    <#list chartXList as chartX>
                                                    "${chartX}"<#if chartX_has_next>,</#if>
                                                    </#list>
                                                ]
                                                </#if>
                                        </#if>
                                        <#if chartType = "monthBar" || chartType = "cityBar">
                                            <#if key = "sequentialData" || key = "totalData">
                                                "${key}":[
                                                    <#assign dataList1 = chartMap[key]>
                                                    <#list  dataList1 as data1>
                                                    ${data1}<#if data1_has_next>,</#if>
                                                    </#list>
                                                ]
                                                </#if>
                                            <#if key = "chartX">
                                                "${key}":[
                                                    <#assign dataList2 = chartMap[key]>
                                                    <#list  dataList2 as data2>
                                                    "${data2}"<#if data2_has_next>,</#if>
                                                    </#list>
                                                ]
                                                </#if>
                                        </#if>
                                            <#if chartType = "pie">
                                                <#if key = "data">
                                                "${key}":[
                                                    <#assign dataList2 = chartDataMap[key]>
                                                    <#list dataList2 as dataMap2>
                                                    {
                                                        <#list dataMap2?keys as key>
                                                        "${key}":"${dataMap2[key]}"<#if key_has_next>,</#if>
                                                        </#list>
                                                    }<#if dataMap2_has_next>,</#if>
                                                    </#list>
                                                ]
                                                </#if>
                                            </#if>

                                        </#list>
                                    }
                                    <#if chartMap_has_next>,</#if>
                                    </#list>
                                    ]
                                    </#if>






                                    <#if chartType = "cityRank">
                                        <#if key = "tableTitle">
                                        "${key}":[
                                            <#assign tableTitleList = chartDataMap[key]>
                                            <#list tableTitleList as tableTitle>
                                            "${tableTitle}"<#if tableTitle_has_next>,</#if>
                                            </#list>
                                        ]
                                        </#if>
                                        <#if key = "tableValue">
                                        "${key}":[
                                            <#assign tableValueList = chartDataMap[key]>
                                            <#list tableValueList as tableValueMap>
                                            {
                                                <#list tableValueMap?keys as key>
                                                    <#if key != "value">
                                                    "${key}":"${tableValueMap[key]}"
                                                    </#if>
                                                    <#if key = "value">
                                                    "${key}":[
                                                        <#assign valueList = tableValueMap[key]>
                                                        <#list valueList as value>
                                                        "${value}"<#if value_has_next>,</#if>
                                                        </#list>
                                                    ]
                                                    </#if>
                                                    <#if key_has_next>,</#if>
                                                </#list>
                                            }<#if tableValueMap_has_next>,</#if>
                                            </#list>
                                        ]
                                        </#if>
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
                        <#if key != "chartX" && key != "data" && key != "dataValue" && key != "dataName">
                        "${key}":"${dataMap[key]}"
                        </#if>
                        <#if key = "dataName" || key = "chartX" || key = "dataValue">
                        "${key}":[
                            <#assign list1 = dataMap[key]>
                            <#list list1 as data1>
                            "${data1}"<#if data1_has_next>,</#if>
                            </#list>
                        ]
                        </#if>
                        <#if key = "data">
                        "${key}":[
                            <#assign list2 = dataMap[key]>
                            <#list list2 as data2>
                            ${data2}<#if data2_has_next>,</#if>
                            </#list>
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
"data":[]
</#if>
}
