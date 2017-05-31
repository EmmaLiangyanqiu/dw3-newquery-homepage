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
            <#if key != "img">
            "${key}":"${dataMap[key]}"
            </#if>
            <#if key = "img">
            "${key}":[<#assign imgList = dataMap[key]><#list imgList as img>"${img}"<#if img_has_next>,</#if></#list>]
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
