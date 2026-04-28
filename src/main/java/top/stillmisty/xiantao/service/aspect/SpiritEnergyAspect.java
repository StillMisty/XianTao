package top.stillmisty.xiantao.service.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import top.stillmisty.xiantao.domain.land.entity.Fudi;
import top.stillmisty.xiantao.domain.land.repository.FudiRepository;
import top.stillmisty.xiantao.service.annotation.ConsumeSpiritEnergy;

import java.time.LocalDateTime;

/**
 * 精力消耗切面
 * 拦截 @ConsumeSpiritEnergy 注解的方法，统一处理精力懒恢复、好感减免、情绪更新
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class SpiritEnergyAspect {

    private final FudiRepository fudiRepository;

    @Around("@annotation(consumeSpiritEnergy)")
    public Object handleEnergyConsumption(ProceedingJoinPoint joinPoint, ConsumeSpiritEnergy consumeSpiritEnergy) throws Throwable {
        Long userId = extractUserId(joinPoint.getArgs());
        if (userId == null) {
            return joinPoint.proceed();
        }

        Fudi fudi = fudiRepository.findByUserId(userId).orElse(null);
        if (fudi == null) {
            return joinPoint.proceed();
        }

        // 懒恢复精力
        fudi.restoreEnergy();

        // 计算实际消耗（含好感减免）
        int baseCost = consumeSpiritEnergy.value();
        int actualCost = fudi.calculateEnergyConsumption(baseCost);

        // 扣除精力
        fudi.deductEnergy(actualCost);
        fudi.setLastEnergyUpdate(LocalDateTime.now());

        fudiRepository.save(fudi);

        log.debug(
                "地灵精力扣除 - userId: {}, baseCost: {}, actualCost: {}, remaining: {}/{}",
                userId, baseCost, actualCost, fudi.getSpiritEnergy(), fudi.getSpiritEnergyMax()
        );

        return joinPoint.proceed();
    }

    /**
     * 从方法参数中提取第一个 Long 类型参数作为 userId
     */
    private Long extractUserId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long longVal) {
                return longVal;
            }
        }
        return null;
    }
}
