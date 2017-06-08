package com.bonc.dw3.service;

import com.bonc.dw3.mapper.TbuserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by YSL on 2017/5/27.
 */
@Service
public class MenuService {

    private static Logger log= LoggerFactory.getLogger(MenuService.class);
    @Autowired
    private TbuserMapper mapper;


    public Map<String,Object> data(String userId){
        List<Map<String,String>> origin=getAllMenu(userId);

        //构造 父-我-子 三层结构
        List<Map<String,Object>> father=new ArrayList<>(8);
        List<List<Map<String,Object>>> me=new ArrayList<>(4);
        Map<String,List<Map<String,String>>> son=new HashMap<>(8);
        List<Map<String,Object>> meHot=new ArrayList<>();
        Map<String,List<Map<String,String>>> sonHot=new HashMap<>(4);

        for (Map<String,String> m:origin){
            String level=m.get("levels").trim();
            String code=m.get("code");
            String pCode=m.get("pcode");
            String name=m.get("name");
            String icon=m.get("icon");
            String url=m.get("url");
            String alias=m.get("alias");
            if ("titleHot".equals(alias)){//暂时用于热门搜索
                if ("3".equals(level)){
                    Map<String,Object> tempHotMe=new HashMap<>();
                    tempHotMe.put("titleClassId",code);
                    tempHotMe.put("titleClassName",name);
                    meHot.add(tempHotMe);
                    List<Map<String,String>> temp=new ArrayList<>();
                    sonHot.put(code,temp);
                }else if ("4".equals(level)){
                    Map<String,String> temp=new HashMap<>();
                    temp.put("titleId",code);
                    temp.put("titleName",name);
                    temp.put("titleUrl",url);
                    temp.put("flag","1");
                    sonHot.get(pCode).add(temp);
                }else{
                    log.info("一条不正确的热门数据:%s;%s",code,name);
                }
            }

            if ("2".equals(level)){//在这里进入到father
                Map<String,Object> temp=new HashMap<>();
                List<Map<String,Object>> tempMe=new ArrayList<>();
                me.add(tempMe);
                temp.put("id",code);
                temp.put("name",name);
                temp.put("url",url);
                temp.put("imgName",icon);
                father.add(temp);
            }else if ("3".equals(level)){//在这里进入到me
                List<Map<String,String>> tempSon=new ArrayList<>();
                son.put(code,tempSon);
                if ("1".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(0).add(tempMe);
                }else if ("2".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(1).add(tempMe);
                }else if ("3".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(2).add(tempMe);
                }else if ("charts".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(3).add(tempMe);
                }else if ("helptool".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(4).add(tempMe);
                }else if ("query".equals(pCode)){
                    Map<String,Object> tempMe=new HashMap<>();
                    tempMe.put("classId",code);
                    tempMe.put("className",name);
                    me.get(5).add(tempMe);
                }else {
                    log.info("警告，查出不符合规定的数据:%s,%s",code,name);
                }
            }else if ("4".equals(level)){//在这里进入到son
                Map<String,String> tempSon=new HashMap<>();
                tempSon.put("nodeId",code);
                tempSon.put("nodeName",name);
                tempSon.put("nodeUrl",url);
                tempSon.put("flag","1");
                log.info("pcode为:"+pCode);
                son.get(pCode).add(tempSon);
            }else{
                log.info("走到最后出现了一条不符合规定的数据:%s,%s",code,name);
            }
        }//for循环结束

        //将sonHot中的数据加到meHot中
        for (Map<String,Object> ob:meHot){
            String myId= (String) ob.get("titleClassId");
            ob.put("list",sonHot.get(myId));
        }

        //将son中的数据加到me中
        for (List<Map<String,Object>> ob:me){
            for (Map<String,Object> meEach:ob){
                String myId= (String) meEach.get("classId");
                meEach.put("nodes",son.get(myId));
            }
        }

        //将me和meHot中的数据加到father中
        for (int i=0;i<father.size();i++){
            if (i+1<=meHot.size()){
                father.get(i).put("titleList",meHot.get(i));
            }
            father.get(i).put("treeList",me.get(i));
        }

        //包装返回结果
        Map<String,Object> result=new HashMap<>();
        result.put("svgList",father);
        return result;


    }

    public List<Map<String,String>> getAllMenu(String userId){
        String roleId=mapper.selectRoleByUserId(userId);
        String[] roleList=roleId.split(",");
        List<String> mostMenu=mapper.selectMostMenu(roleList);
        System.out.println("查询出的mostMenu："+mostMenu.size());
        Map<String,String> inAndOut=mapper.selectRoleInOut(userId);
        String in="0";
        String out="0";
        if (inAndOut!=null){
            in=inAndOut.get("rolein");
            out=inAndOut.get("roleout");
        }
        String[] inMenu=in.split(",");
        String[] outMenu=out.split(",");
        Set<String> menuSet=new HashSet<>(mostMenu);
        for (String s:inMenu){
            menuSet.add(s);
        }
        for (String s:outMenu){
            menuSet.remove(s);
        }
        StringBuilder sb=new StringBuilder();
        for (String me:menuSet){
            sb.append(me).append(",");
        }
        log.info(sb.toString());
        List<Map<String,String>> result=mapper.selectAllMenu(menuSet);
        System.out.println(result);
        return result;
    }





}
