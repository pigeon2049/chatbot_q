package icu.doge.service;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import icu.doge.ai.domain.ChatRecord;
import icu.doge.ai.service.IChatRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class AiChat {

    private final SendGroupMsg sendGroupMsg;
    @Value("${cloudflare.ai-url}")
    private  String url;

    @Value("${cloudflare.api-token}")
    private  String apiToken;

    @Value("${cloudflare.account-id}")
    private  String accountId;

    @Value("${cloudflare.model}")
    private  String model;

    @Value("${cloudflare.system-prompt}")
    private  String systemPrompt;

    private final WebClient.Builder webClientBuilder;

    private final IChatRecordService chatRecordService;

    public String getUniqueId() {
        return new SnowflakeGenerator().next().toString();
    }


    public AiChat(WebClient.Builder webClientBuilder, IChatRecordService chatRecordService, SendGroupMsg sendGroupMsg) {
        this.webClientBuilder = webClientBuilder;
        this.chatRecordService = chatRecordService;
        this.sendGroupMsg = sendGroupMsg;
    }

    private void insertChatRecord(String role, String id, String content, Long userId) {
        ChatRecord tempUser = new ChatRecord();
        tempUser.setRole(role);
        tempUser.setCId(id);
        tempUser.setTime(new Date());
        tempUser.setContent(content);
        tempUser.setUserId(userId);
        chatRecordService.insertChatRecord(tempUser);
    }

    public void chatNow(String text, String userId, Long groupId) {

        Long userIdNow = Long.valueOf(userId);

        LinkedList<AiContent> contents=new LinkedList<>();
        List<ChatRecord> chatRecords = chatRecordService.selectChatRecordListByUserId(userId);
        String uniqueId;
        if (CollectionUtils.isEmpty(chatRecords)){
            uniqueId = getUniqueId();
            contents.addFirst(new AiContent("system", systemPrompt));
            contents.addLast(new AiContent("user", text));
            insertChatRecord("system", uniqueId, systemPrompt, userIdNow);
        }else {
            chatRecords.forEach(s-> contents.addLast(new AiContent(s.getRole(),s.getContent())));
            ChatRecord first = chatRecords.getFirst();
            if (!"system".equals(first.getRole())){
                contents.addFirst(new AiContent("system", systemPrompt));
            }
            uniqueId=chatRecords.getLast().getCId();
            contents.addLast(new AiContent("user", text));
        }
        insertChatRecord("user", uniqueId, text, userIdNow);

        final String finalUniqueId=uniqueId;
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", JSON.toJSON(contents));
        requestBody.put("stream", true);

        String finalUrl = url.replace("{account_id}", accountId).replace("{model_name}", model);
        WebClient.ResponseSpec responseSpec = webClientBuilder.baseUrl(finalUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, apiToken)
                .build()
                .post()
                .body(BodyInserters.fromValue(requestBody))
                .retrieve();
        StringBuilder allReplies = new StringBuilder();
        responseSpec.bodyToFlux(String.class)
                .toStream()
                .forEach(data -> {
                    if ("[DONE]".equals(data)) {
                        // Insert all replies into the database
                        String repliesString = allReplies.toString();
                        insertChatRecord("assistant", finalUniqueId,repliesString,userIdNow);
                        sendGroupMsg.send(repliesString,groupId);
                        System.out.println("[DONE]");
                    } else {
                        try {
                            allReplies.append(JSONObject.parse(data).getString("response"));
                        } catch (Exception ignored) {
                        }
                    }
                });


    }

    public record AiContent(String role,String content){}


}
