package com.community.common.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 在当前数据库事务成功提交后执行副作用操作。
 *
 * <p>Redis 不参与 Spring 的数据库事务，因此缓存和热榜更新不能假设可以回滚。
 * 没有事务上下文时直接执行，适用于定时任务等非事务调用。</p>
 */
@Component
public class AfterCommitExecutor {

    public void execute(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                });
    }
}
