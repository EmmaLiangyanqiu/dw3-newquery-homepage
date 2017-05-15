[
  <#if clientList??>
   <#list clientList as clientMap>
     {
      <#list clientMap?keys as key>
        "${key}":"${clientMap[key]}"
       <#if key_has_next>,</#if>
      </#list>
     }
    <#if clientMap_has_next>,</#if>
   </#list>
    <#else>
    {
    "period":"",
    "data":""
    } 
  </#if>
]
