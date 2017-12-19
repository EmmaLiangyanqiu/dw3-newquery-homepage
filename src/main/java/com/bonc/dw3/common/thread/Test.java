/*
package com.bonc.dw3.common.thread;

import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

*/
/**
 * @author guopeng
 * @date 2017/12/19
 * @description
 *//*

public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        List<Map<String, Object>> result = new ArrayList<>();
        ExecutorService pool = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i ++){
            MyCallable myCallable = new MyCallable(new RestTemplate(), "url", "param");
            Future future = pool.submit(myCallable);
            List<Map<String, Object>> aaa = (List<Map<String, Object>>) future.get();
            result.addAll(aaa);
            System.out.println(future.get());
        }
        pool.shutdown();
        System.out.println(result);
    }
}
*/
