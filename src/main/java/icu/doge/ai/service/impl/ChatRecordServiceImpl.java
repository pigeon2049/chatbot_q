package icu.doge.ai.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import icu.doge.ai.domain.ChatRecord;
import icu.doge.ai.mapper.ChatRecordMapper;
import icu.doge.ai.service.IChatRecordService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class ChatRecordServiceImpl implements IChatRecordService {

    private final ChatRecordMapper chatRecordMapper;

    public ChatRecordServiceImpl(ChatRecordMapper chatRecordMapper) {
        this.chatRecordMapper = chatRecordMapper;
    }

    @Override
    public List<ChatRecord> selectChatRecordListByCid(String cId) {
        QueryWrapper<ChatRecord> chatRecordQueryWrapper = new QueryWrapper<>();
        chatRecordQueryWrapper.eq("c_id", cId);
        return chatRecordMapper.selectList(chatRecordQueryWrapper);
    }

    @Override
    public void insertChatRecord(ChatRecord chatRecord) {
        chatRecordMapper.insert(chatRecord);
    }

    @Override
    public List<ChatRecord> selectChatRecordListByUserId(String userId) {
        QueryWrapper<ChatRecord> chatRecordQueryWrapper = new QueryWrapper<>();
        chatRecordQueryWrapper.eq("user_id", userId);
        chatRecordQueryWrapper.gt("time",new Date(System.currentTimeMillis()-(30*60*1000)));
        chatRecordQueryWrapper.orderByAsc("id");
        return chatRecordMapper.selectList(chatRecordQueryWrapper);
    }
}
