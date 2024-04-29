package icu.doge.ai.service;





import icu.doge.ai.domain.ChatRecord;

import java.util.List;



public interface IChatRecordService
{


    public List<ChatRecord> selectChatRecordListByCid(String cId);

    public void insertChatRecord(ChatRecord chatRecord);

    public List<ChatRecord> selectChatRecordListByUserId(String userId);

}
