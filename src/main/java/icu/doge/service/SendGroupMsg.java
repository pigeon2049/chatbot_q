package icu.doge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SendGroupMsg  {

    @Value("${bot.endpoint}")
    private String baseUrl;

    private final String url="/send_group_msg";

    private final RestTemplate restTemplate;


    public SendGroupMsg(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public void send(String msg,Long groupNumber){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("group_id",groupNumber);
        hashMap.put("message",msg);
        hashMap.put("auto_escape",false);
        restTemplate.postForEntity(baseUrl+url,hashMap,String.class);
    }

    public void sendPic(String path,Long groupNumber){

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message_type","group");
        hashMap.put("group_id",groupNumber);
        hashMap.put("auto_escape",false);


        List<Map<String,Object>> dataList = new ArrayList<>();

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("type","image");

        HashMap<String, Object> idMap = new HashMap<>();
        idMap.put("file",path);
        idMap.put("cache",0);
        idMap.put("id",40000);
        idMap.put("c",2);

        dataMap.put("data",idMap);


        dataList.add(dataMap);



        hashMap.put("message",dataList);


        restTemplate.postForEntity(baseUrl+url,hashMap,String.class);
    }



}
