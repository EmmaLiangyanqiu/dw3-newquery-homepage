[
  <#if kpiDataList??>
   <#list kpiDataList as kpiDataMap>
      {
      <#list kpiDataMap?keys as key>
        <#if key!="values" &&key!="kpiCode" &&key!="KPICODE">
          <#if kpiDataMap[key]??>
          "${key?lower_case}":"${kpiDataMap[key]}"
          </#if>
        </#if>
        <#if key="kpiCode"||key="KPICODE">
           "kpiCode":"${kpiDataMap[key]}"
         </#if>
        <#if key=="values">
          "${key}":[
              <#assign dataList=kpiDataMap[key]>
              <#list dataList as data>
                 "${data}"
                 <#if data_has_next>,</#if>
              </#list>
          ]
        </#if>
        <#if key_has_next>,</#if>	
      </#list>
      }
      <#if kpiDataMap_has_next>,</#if>
   </#list>
   <#else>
   {
   	"kpiCode":"",
   	"pid":"",
   	"id":"",
   	"space":"",
   	"title":"",
   	"province":"",
   	"values":["","","","",""]
   }
  </#if>
]
