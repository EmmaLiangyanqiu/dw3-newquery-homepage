[
  <#if channelList??>
    <#list channelList as channelMap>
     {
      <#list channelMap?keys as key>
        "${key}":"${channelMap[key]}"
         <#if key_has_next>,</#if>
      </#list>
     }
     <#if channelMap_has_next>,</#if>
    </#list>
        <#else>
    {
    "title":"",
    "data":""
    }
  </#if>
]

