[
<#-- 注释 -->
<#if dataList??>
    <#list dataList as dataMap>
    {
        <#list dataMap?keys as key>
            <#if key!="city">
            "${key}":"${dataMap[key]}"
            </#if>
            <#if key=="city">
            "city":[
                <#assign cityList=dataMap[key] >
                <#list cityList as cityMap>
                {
                    <#list cityMap?keys as key>
                    "${key}":"${cityMap[key]}"
                        <#if key_has_next>,</#if>
                    </#list>
                }
                    <#if cityMap_has_next>,</#if>
                </#list>
            ]
            </#if>
            <#if key_has_next>,</#if>
        </#list>
    }
        <#if dataMap_has_next>,</#if>
    </#list>
</#if>
]
