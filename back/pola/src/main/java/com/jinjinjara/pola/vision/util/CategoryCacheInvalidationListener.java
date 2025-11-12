package com.jinjinjara.pola.vision.util;

import com.jinjinjara.pola.vision.dto.common.CategoryChangedEvent;
import com.jinjinjara.pola.vision.service.EmbeddingCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryCacheInvalidationListener {

    private final EmbeddingCacheService embeddingCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChanged(CategoryChangedEvent event) {
        Long userId = event.getUserId();
        log.info("[EmbedCache] invalidate after commit. user={}", userId);
        embeddingCacheService.invalidate(userId); // 캐시 삭제만, 재계산은 다음 호출에서
    }
}
