    {
    "colFlags":[2,3],
    <#if kpiMap??>
     <#list kpiMap?keys as key>
      <#if key="title">
      "${key}":
         [
           <#assign titleList = kpiMap[key]>
            <#if (titleList?size>0)>
              <#list titleList as title>
                "${title}"
               <#if title_has_next>,</#if>
              </#list>
            <#else>
              "","","","",""
            </#if>
          ]
       </#if>
       <#if key="datalist">
        "${key}":
          [
            <#assign dataList = kpiMap[key]> 
            <#if (dataList?size>0)>
               <#list dataList as dataMap>
              {
                <#list dataMap?keys as key>
                   <#if key="values">
                     <#assign values=dataMap[key]>
                   "${key}":  
                    [
                       <#list values as value>
                         "${value}"
                       <#if value_has_next>,</#if>
                       </#list>
                     ]
                    </#if>
                     <#if key!="values" &&key!="kpiCode" &&key!="KPICODE">
                       <#if dataMap[key]??>
                       "${key?lower_case}":"${dataMap[key]}"
                       </#if>
                    </#if>
                    <#if key="kpiCode"||key="KPICODE">
                        "kpiCode":"${dataMap[key]}"
                    </#if>
                   <#if key_has_next>,</#if>
                 </#list>
               }
                <#if dataMap_has_next>,</#if>
              </#list>
            <#else>
              {
               "id":"",
               "pid":"",
               "title":"",
               "kpiCode":"",
               "space":"",
               "values":["","","",""]
              }
            </#if>
          ]
       </#if>
      <#if key_has_next>,</#if>
     </#list>
    <#else>
    "colFlags":[],
     "title":["","","",""],
     "datalist":	
         [	
         	{
               "id":"",
               "pid":"",
               "title":"",
               "kpiCode":"",
               "space":"",
               "values":["","","",""],
               "user_drill":"",
               "channel_drill":"",
               "contract_drill":"",
               "region_drill":""               
              }
          ]
    </#if>
     }
 
   
         