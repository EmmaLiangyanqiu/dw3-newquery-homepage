{
<#if resMap??>
<#list resMap?keys as key>
<#if key = "default">
    "${key}":<#assign defaultMap = resMap[key]>{<#list defaultMap?keys as key>"${key}":"${defaultMap[key]}"<#if key_has_next>,</#if></#list>}
</#if>
<#if key="selectList">
    "${key}":[
    <#assign dataList = resMap[key]>
    <#list dataList as dataMap>
        {<#list dataMap?keys as key>"${key}":"${dataMap[key]}"<#if key_has_next>,</#if></#list>}<#if dataMap_has_next>,</#if>
    </#list>
    ]
</#if>
</#list>
<#else>

</#if>
}