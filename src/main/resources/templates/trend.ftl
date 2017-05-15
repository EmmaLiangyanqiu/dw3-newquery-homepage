<#if trendMap??>
   {
    <#list trendMap?keys as key>
    <#if key == "unit">
    "unit":"${trendMap["unit"]}"
    <#else>
        "${key}":
        [
          <#assign dataList=trendMap[key]>
          <#list dataList as data>
           "${data}"
            <#if data_has_next>,</#if>
          </#list>
        ]
    </#if>
    <#if key_has_next>,</#if>
    </#list>
    }
    <#else>
    {
    "period":[""],
    "data":[""],
    "unit":""
    }
 </#if>      


 


 

 


 