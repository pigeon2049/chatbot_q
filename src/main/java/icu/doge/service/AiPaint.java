package icu.doge.service;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class AiPaint {

    @Value("${cloudflare.paint-url}")
    private  String url;

    @Value("${cloudflare.api-token}")
    private  String apiToken;

    @Value("${cloudflare.account-id}")
    private  String accountId;


    @Autowired
    private RestTemplate restTemplate;

    private final SendGroupMsg sendGroupMsg;

    public AiPaint(SendGroupMsg sendGroupMsg) {
        this.sendGroupMsg = sendGroupMsg;
    }

    public void paintNow(String text, String userId, Long groupNumber) {



        String finalUrl = url.replace("{account_id}", accountId);

        Map<String,String> postBody=new HashMap<>();
        postBody.put("prompt", text);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION,apiToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(JSON.toJSONString(postBody), headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(finalUrl, HttpMethod.POST, entity, byte[].class);
            if (response.getStatusCode() == HttpStatus.OK) {
                // 生成时间戳
                String timestamp = String.valueOf(System.currentTimeMillis());
                // 文件路径
                String filePath = "pic/" + timestamp + ".png";
                // 将响应的字节流写入文件
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(response.getBody());
                    // 获取文件的绝对路径
                    File file = new File(filePath);
                    String absolutePath = file.getAbsolutePath();
                    System.out.println("文件已生成：" + absolutePath);
                    sendGroupMsg.sendPic(absolutePath,groupNumber);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }


    }


}
