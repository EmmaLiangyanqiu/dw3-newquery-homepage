package com.bonc.dw3.schedule;

import com.bonc.dw3.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ysl on 2017/8/8.
 */
@Component
public class RequestProvEveryday implements CommandLineRunner {

    @Autowired
    private UserInfoMapper userInfoMapper;

    private static List<String> WHOLE_PROV=new ArrayList<>(32);//全国包含的省份id，不包含111
    private static List<String> NORTH_PROV=new ArrayList<>(32);//北10包含的省份id，不包含112
    private static List<String> SOUTH_PROV=new ArrayList<>(32);//南21省包含的省份id，不包含113
    @Override
    public void run(String... strings) throws Exception {
        WHOLE_PROV=userInfoMapper.queryProvById("111");
        NORTH_PROV=userInfoMapper.queryProvById("112");
        SOUTH_PROV=userInfoMapper.queryProvById("113");
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void updateProv(){
        WHOLE_PROV=userInfoMapper.queryProvById("111");
        NORTH_PROV=userInfoMapper.queryProvById("112");
        SOUTH_PROV=userInfoMapper.queryProvById("113");
    }

    public static boolean containTheProv(String reqProv,String queryProv){
        switch (queryProv){
            case "111":
                return WHOLE_PROV.contains(reqProv);
            case "112":
                return NORTH_PROV.contains(reqProv);
            case "113":
                return SOUTH_PROV.contains(reqProv);
            default:return false;
        }
    }
}
