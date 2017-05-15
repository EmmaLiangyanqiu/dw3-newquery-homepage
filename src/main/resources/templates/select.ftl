[
<#if resList??>
      <#list resList as selectMap>
         {
        <#list selectMap?keys as key>
          <#if key!="data">
          "${key}":"${selectMap[key]}"
          </#if>
           <#if key="data">
          "${key}":
            [
            <#assign dataList=selectMap["data"]>
             <#list dataList as dataMap>
               {
                <#list dataMap?keys as key>
                  "${key}":"${dataMap[key]}"
                  <#if key_has_next>,</#if>
                </#list>
               }
               <#if dataMap_has_next>,</#if>
             </#list>
            ]
          </#if>
          <#if key_has_next>,</#if>
        </#list>       
       }
         <#if selectMap_has_next>,</#if>
      </#list>
   <#else>
     {
      "tid":"",
      "tname":"",
      "data":[
         {
          "id":"",
          "text":""
         }
      ]
    }
</#if>
]