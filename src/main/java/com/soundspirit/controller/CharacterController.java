package com.soundspirit.controller;

import com.soundspirit.common.Result;
import com.soundspirit.entity.AiCharacter;
import com.soundspirit.service.CharacterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;

    /**
     * 获取所有可用角色
     */
    @GetMapping
    public Result<List<AiCharacter>> listCharacters() {
        return Result.success(characterService.listAvailable());
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<AiCharacter> getCharacter(@PathVariable Long id) {
        return Result.success(characterService.getById(id));
    }

    /**
     * 根据分类获取角色
     */
    @GetMapping("/category/{category}")
    public Result<List<AiCharacter>> listByCategory(@PathVariable String category) {
        return Result.success(characterService.listByCategory(category));
    }
}
