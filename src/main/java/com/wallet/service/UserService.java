package com.wallet.service;

import com.wallet.dto.CreateUserRequest;
import com.wallet.dto.UserResponse;
import com.wallet.entity.User;
import com.wallet.entity.Wallet;
import com.wallet.repository.UserRepository;
import com.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository){
        this.repository = userRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest userRequest){
        log.info("User creation request is initiated");
        repository.findByEmailId(userRequest.getEmail())
                .ifPresent(user -> {
                    log.error("User already exists with the email");
                    throw new RuntimeException("User already exists with the email");
                });
        User user = new User(userRequest.getName(), userRequest.getEmail());
        User savedUser = repository.save(user);

        Wallet wallet = new Wallet(savedUser.getId());
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet updated {}", savedWallet.getBalance());
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmailId());
    }

    @Transactional
    public Optional<User> getUserInfo(Long userId) {
        log.info("Getting user info for user {}", userId);
        return repository.findById(userId);
    }
}
