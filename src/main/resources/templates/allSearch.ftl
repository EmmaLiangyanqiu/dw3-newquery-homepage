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
                <#if key != "dataName" && key != "dataValue" && key != "chartData" && key != "chartX">
                "${key}":"${allDataMap[key]}"
                </#if>
                <#if key = "dataName" || key = "dataValue" || key = "chartX">
                "${key}":[
                    <#assign allList = allDataMap[key]>
                    <#list allList as all>
                    "${all}"
                    <#if all_has_next>,</#if>
                    </#list>
                ]
                </#if>
                <#if key = "chartData">
                "${key}":[
                    <#assign allList1 = allDataMap[key]>
                    <#list allList1 as all1>
                    ${all1}
                        <#if all1_has_next>,</#if>
                    </#list>
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
