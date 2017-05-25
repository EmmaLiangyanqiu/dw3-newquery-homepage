{
<#if resMap??>
<#list resMap?keys as key>
<#if key="recentVisitList">
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