{
<#if resMap??>
<#list resMap?keys as key>
    "${key}":[
    <#assign svgList = resMap[key]>
    <#if (svgList?size>0)>
        <#list svgList as svgMap>
        {
        <#list svgMap?keys as key>
        <#if key != "titleList" && key != "treeList">
            "${key}":"${svgMap[key]}"
        </#if>
        <#if key = "titleList">
            "${key}":{
            <#assign titleMap = svgMap[key]>
            <#list titleMap?keys as key>
            <#if key != "list">
                "${key}":"${titleMap[key]}"
            </#if>
            <#if key = "list">
                "${key}":[
                <#assign list = titleMap[key]>
                <#list list as map>
                    {<#list map?keys as key>"${key}":"${map[key]}"<#if key_has_next>,</#if></#list>}<#if map_has_next>,</#if>
                </#list>
                ]
            </#if>
                <#if key_has_next>,</#if>
            </#list>
            }
        </#if>
        <#if key = "treeList">
            "${key}":[
            <#assign treeList = svgMap[key]>
            <#list treeList as treeMap>
                {
                <#list treeMap?keys as key>
                <#if key != "nodes">
                    "${key}":"${treeMap[key]}"
                </#if>
                <#if key = "nodes">
                    "${key}":[
                    <#assign nodesList = treeMap[key]>
                    <#list nodesList as nodesMap>
                        {<#list nodesMap?keys as key>"${key}":"${nodesMap[key]}"<#if key_has_next>,</#if></#list>}<#if nodesMap_has_next>,</#if>
                    </#list>
                    ]
                </#if>
                    <#if key_has_next>,</#if>
                </#list>
                }<#if treeMap_has_next>,</#if>
            </#list>
            ]
        </#if>
            <#if key_has_next>,</#if>
        </#list>
        }<#if svgMap_has_next>,</#if>
        </#list>
    </#if>
    ]
</#list>
<#else>

</#if>
}
 
   
         