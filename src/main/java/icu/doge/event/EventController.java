package icu.doge.event;

import com.alibaba.fastjson2.JSON;
import icu.doge.service.AiChat;
import icu.doge.service.AiPaint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController("/onebot")
public class EventController {

    @Value("${bot.qqnumber}")
    private String selfQQ;
    @Value("${bot.nickNow}")
    private String botNick;

    private final AiChat aiChat;
    private final AiPaint aiPaint;

    public EventController(AiChat aiChat, AiPaint aiPaint) {
        this.aiChat = aiChat;
        this.aiPaint = aiPaint;
    }

    @PostMapping
    public void receiveEvent(@RequestBody Event body){

        System.out.println(JSON.toJSONString(body));

        if (!"message".equals(body.post_type())){
            return;
        }
        if (!"group".equals(body.message_type())){
            return;
        }

        List<Message> message = body.message();
        long count = message.stream()
                .filter(s -> "at".equals(s.type()) || "reply".equals(s.type()))
                .filter(s -> s.data().containsKey("qq") && selfQQ.equals(s.data().get("qq")))
                .count();
        if (count<=0){
            return;
        }
        List<Message> messages = message.stream().filter(s -> "text".equals(s.type()))
                .toList();
        if (CollectionUtils.isEmpty(messages)){
            return;
        }
        String text = (String) messages.getFirst().data().get("text");
        String userId = body.user_id();

        if (text.contains("画画")||text.contains("画图")){
            text=text.replace("@","")
                    .replace(botNick,"")
                    .replace("画画","")
                    .replace("画图","");
            aiPaint.paintNow(text,userId,Long.valueOf(body.group_id()));
            return;
        }

        aiChat.chatNow(text,userId,Long.valueOf(body.group_id()));

        return;
    }

    public record Event(Integer eventType,
                        Long time,
                        String message_id,
                        String self_id,
                        String post_type,
                        String user_id,
                        Integer font,
                        List<Message> message,
                        String raw_message,
                        String group_id,
                        String message_type,
                        String sub_type,
                        Sender sender
                        ){}

    public record Message(String type, Map<String, Object> data){}

    public record Sender(String user_id,
                         String nickname,
                         String card,
                         String sex,
                         Integer age,
                         String area,
                         String level,
                         String role,
                         String title

    ){}

}
