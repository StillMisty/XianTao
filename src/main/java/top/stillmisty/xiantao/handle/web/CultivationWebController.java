package top.stillmisty.xiantao.handle.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.stillmisty.xiantao.domain.user.enums.PlatformType;
import top.stillmisty.xiantao.handle.command.CultivationCommandHandler;

import java.util.Map;

/**
 * 修仙系统 Web API 控制器
 * 提供加点、洗点、突破、护道等功能的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/cultivation")
@RequiredArgsConstructor
public class CultivationWebController {

    private final CultivationCommandHandler cultivationCommandHandler;

    /**
     * 属性加点
     * POST /api/cultivation/allocate-points
     * Body: { "openId": "xxx", "attributeName": "力量", "points": 5 }
     */
    @PostMapping("/allocate-points")
    public ResponseEntity<Map<String, Object>> allocatePoints(@RequestBody Map<String, Object> request) {
        String openId = (String) request.get("openId");
        String attributeName = (String) request.get("attributeName");
        Integer points = (Integer) request.get("points");

        if (openId == null || attributeName == null || points == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要参数：openId, attributeName, points"
            ));
        }

        String response = cultivationCommandHandler.handleAllocatePoints(
                PlatformType.WEB,
                openId,
                attributeName,
                points
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }

    /**
     * 洗点
     * POST /api/cultivation/reset-points
     * Body: { "openId": "xxx" }
     */
    @PostMapping("/reset-points")
    public ResponseEntity<Map<String, Object>> resetPoints(@RequestBody Map<String, Object> request) {
        String openId = (String) request.get("openId");

        if (openId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要参数：openId"
            ));
        }

        String response = cultivationCommandHandler.handleResetPoints(
                PlatformType.WEB,
                openId
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }

    /**
     * 突破
     * POST /api/cultivation/breakthrough
     * Body: { "openId": "xxx" }
     */
    @PostMapping("/breakthrough")
    public ResponseEntity<Map<String, Object>> breakthrough(@RequestBody Map<String, Object> request) {
        String openId = (String) request.get("openId");

        if (openId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要参数：openId"
            ));
        }

        String response = cultivationCommandHandler.handleBreakthrough(
                PlatformType.WEB,
                openId
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }

    /**
     * 建立护道关系
     * POST /api/cultivation/protection
     * Body: { "openId": "xxx", "protegeNickname": "张三" }
     */
    @PostMapping("/protection")
    public ResponseEntity<Map<String, Object>> establishProtection(@RequestBody Map<String, Object> request) {
        String openId = (String) request.get("openId");
        String protegeNickname = (String) request.get("protegeNickname");

        if (openId == null || protegeNickname == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要参数：openId, protegeNickname"
            ));
        }

        String response = cultivationCommandHandler.handleEstablishProtection(
                PlatformType.WEB,
                openId,
                protegeNickname
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }

    /**
     * 解除护道关系
     * DELETE /api/cultivation/protection
     * Body: { "openId": "xxx", "protegeNickname": "张三" }
     */
    @DeleteMapping("/protection")
    public ResponseEntity<Map<String, Object>> removeProtection(@RequestBody Map<String, Object> request) {
        String openId = (String) request.get("openId");
        String protegeNickname = (String) request.get("protegeNickname");

        if (openId == null || protegeNickname == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "缺少必要参数：openId, protegeNickname"
            ));
        }

        String response = cultivationCommandHandler.handleRemoveProtection(
                PlatformType.WEB,
                openId,
                protegeNickname
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }

    /**
     * 查询护道信息
     * GET /api/cultivation/protection?openId=xxx
     */
    @GetMapping("/protection")
    public ResponseEntity<Map<String, Object>> queryProtection(@RequestParam String openId) {
        String response = cultivationCommandHandler.handleQueryProtection(
                PlatformType.WEB,
                openId
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", response
        ));
    }
}
