[
<#if resList??>
    <#list resList as resMap>
    {<#list resMap?keys as key>"${key}":"${resMap[key]}"<#if key_has_next>,</#if></#list>}<#if resMap_has_next>,</#if>
    </#list>
<#else>
</#if>
]

