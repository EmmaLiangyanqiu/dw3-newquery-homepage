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
            "${key}":"${dataMap[key]}"<#if key_has_next>,</#if>
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
