package com.wallet.repository;

import com.wallet.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wallet.dto.TransactionStatus;
import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    // Find transactions by ownerUserId
    List<WalletTransaction> findByOwnerUserIdOrderByTransactionTimeDesc(Long ownerUserId);

    // Find transactions by ownerUserId and status
    List<WalletTransaction> findByOwnerUserIdAndStatusOrderByTransactionTimeDesc(
            Long ownerUserId,
            TransactionStatus status);
}