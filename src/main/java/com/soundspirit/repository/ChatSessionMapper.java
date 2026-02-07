package com.soundspirit.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.soundspirit.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
