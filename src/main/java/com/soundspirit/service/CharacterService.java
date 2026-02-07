package com.soundspirit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soundspirit.entity.AiCharacter;
import com.soundspirit.repository.AiCharacterMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI角色服务
 */
@Service
@RequiredArgsConstructor
public class CharacterService {

    private final AiCharacterMapper characterMapper;

    /**
     * 获取所有可用角色
     */
    public List<AiCharacter> listAvailable() {
        return characterMapper.selectList(
                new LambdaQueryWrapper<AiCharacter>()
                        .eq(AiCharacter::getStatus, 1)
                        .orderByAsc(AiCharacter::getSortOrder)
        );
    }

    /**
     * 根据ID获取角色
     */
    public AiCharacter getById(Long id) {
        return characterMapper.selectById(id);
    }

    /**
     * 根据分类获取角色
     */
    public List<AiCharacter> listByCategory(String category) {
        return characterMapper.selectList(
                new LambdaQueryWrapper<AiCharacter>()
                        .eq(AiCharacter::getStatus, 1)
                        .eq(AiCharacter::getCategory, category)
                        .orderByAsc(AiCharacter::getSortOrder)
        );
    }
}
