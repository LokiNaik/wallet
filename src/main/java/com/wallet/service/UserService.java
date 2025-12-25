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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletRepository walletRepository,
                       PasswordEncoder passwordEncoder){
        this.repository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest userRequest){
        log.info("User creation request is initiated");
        repository.findByEmailId(userRequest.getEmail())
                .ifPresent(user -> {
                    log.error("User already exists with the email");
                    throw new RuntimeException("User already exists with the email");
                });
        User user = new User();
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setName(userRequest.getName());
        user.setEmailId(userRequest.getEmail());
        User savedUser = repository.save(user);

        if(savedUser.getId() == null){
            return null;
        }
        // Wallet creation
        Wallet wallet = new Wallet(savedUser.getId());
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet updated {}", savedWallet.getBalance());
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmailId(),
                savedUser.getRole());
    }

    @Transactional
    public Optional<User> getUserInfo(Long userId) {
        log.info("Getting user info for user {}", userId);
        return repository.findById(userId);
    }
}
