[
 <#if contractList??>
   <#list contractList as contractMap>
     {
       <#list contractMap?keys as key>
        "${key}":"${contractMap[key]}"
       <#if key_has_next>,</#if>
       </#list>
     }
   <#if contractMap_has_next>,</#if>
   </#list>
    <#else>
    {
    "title":"",
    "data":""
    }   
 </#if>
]