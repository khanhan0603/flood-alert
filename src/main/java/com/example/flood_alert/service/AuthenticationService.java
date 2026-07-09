package com.example.flood_alert.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flood_alert.dbo.request.AuthenticateRequest;
import com.example.flood_alert.dbo.request.IntrospectRequest;
import com.example.flood_alert.dbo.request.LogoutRequest;
import com.example.flood_alert.dbo.request.RefreshRequest;
import com.example.flood_alert.dbo.response.AuthenticateResponse;
import com.example.flood_alert.dbo.response.IntrospectResponse;
import com.example.flood_alert.entity.InvalidatedToken;
import com.example.flood_alert.entity.RefreshToken;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.enums.RescueGroupType;
import com.example.flood_alert.enums.Role;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
import com.example.flood_alert.repository.InvalidatedTokenRepository;
import com.example.flood_alert.repository.RefreshTokenRepository;
import com.example.flood_alert.repository.RescueGroupMemberRepository;
import com.example.flood_alert.repository.RescueGroupRepository;
import com.example.flood_alert.repository.RescueTeamRepository;
import com.example.flood_alert.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    UserRepository userRepository;
    RescueTeamRepository rescueTeamRepository;
    RescueGroupRepository rescueGroupRepository;
    RefreshTokenRepository refreshTokenRepository;
    RescueGroupMemberRepository rescueGroupMemberRepository;

    @NonFinal
    @Value("${jwt.signedKey}")
    protected String SIGNED_KEY;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {

        try {

            verifyToken(request.getToken());

            return IntrospectResponse.builder()
                    .valid(true)
                    .build();

        } catch (Exception e) {

            return IntrospectResponse.builder()
                    .valid(false)
                    .build();

        }
    }

    @Transactional
    public AuthenticateResponse refresh(RefreshRequest request)
            throws ParseException, JOSEException {

        SignedJWT signedJWT = verifyToken(request.getRefreshToken());

        String type = signedJWT.getJWTClaimsSet()
                .getStringClaim("type");

        if (!"refresh".equals(type)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Lấy refresh token từ DB
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        // Kiểm tra revoke
        if (refreshToken.isRevoked()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Kiểm tra token trong DB quá hạn chưa
        if (refreshToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Lấy user
        User user = refreshToken.getUser();

        // Thu hồi refresh token cũ
        refreshToken.setRevoked(true); // Đánh dấu token đã bị thu hồi

        refreshTokenRepository.save(refreshToken);

        // Sinh token mới
        String accessToken = generateAccessToken(user);

        String newRefreshToken = generateRefreshToken(user);

        // Lưu refresh token mới
        saveRefreshToken(user, newRefreshToken);

        // Trả response
        return AuthenticateResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .id(user.getId())
                .areaId(user.getArea().getId())
                .hoten(user.getHoten())
                .sodt(user.getSodt())
                .role(user.getRole().name())
                .teamId(user.getTeam() != null ? user.getTeam().getId() : null)
                .teamName(user.getTeam() != null ? user.getTeam().getName() : null)
                .build();
    }

    @Transactional
    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        var user = userRepository.findActiveByEmailOrPhone(request.getLoginInfo())
                .orElseThrow(() -> new AppException(ErrorCode.LOGIN_INFO_EXISTED));

        // Test team leader
        Boolean isTeamLeader = false;
        // Test group leader
        Boolean isGroupLeader = false;

        if (user.getRole().equals(Role.RESCUER)) {
            isTeamLeader = rescueTeamRepository.existsByLeaderId(user.getId());
            isGroupLeader = rescueGroupRepository.existsByLeaderId(user.getId());
        }

        RescueGroupType groupType = null;

        if (user.getRole() == Role.RESCUER) {
            groupType = rescueGroupMemberRepository
                    .findGroupTypeByUserId(user.getId())
                    .orElse(null);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String accessToken = generateAccessToken(user);

        String refreshToken = generateRefreshToken(user);

        saveRefreshToken(user, refreshToken);

        return AuthenticateResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .authenticated(true)
                .id(user.getId())
                .areaId(user.getArea().getId())
                .hoten(user.getHoten())
                .sodt(user.getSodt())
                .role(user.getRole().name())
                .teamId(user.getTeam() != null
                        ? user.getTeam().getId()
                        : null)
                .teamName(user.getTeam() != null
                        ? user.getTeam().getName()
                        : null)
                .isTeamLeader(isTeamLeader)
                .isGroupLeader(isGroupLeader)
                .groupType(groupType)
                .build();

    }

    // Hàm lấy người dùng nếu có đăng nhập trên hệ thống phục vụ tạo sos
    public User getCurrentUserOrNull() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            return null;
        }

        UUID userId = UUID.fromString(authentication.getName());

        return userRepository.findById(userId).orElse(null);
    }

    // Logout
    @Transactional
    public void logout(LogoutRequest request)
            throws ParseException, JOSEException {

        // Verify JWT
        SignedJWT signedJWT = verifyToken(request.getAccessToken());
        verifyToken(request.getRefreshToken());
        String jti = signedJWT.getJWTClaimsSet().getJWTID();

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (!refreshToken.isRevoked()) {

            refreshToken.setRevoked(true);

            refreshTokenRepository.save(refreshToken);
        }
        // Lưu blacklist
        InvalidatedToken token = InvalidatedToken.builder()
                .jwtId(jti)
                .expiryTime(
                        LocalDateTime.now().plusHours(1))
                .build();

        invalidatedTokenRepository.save(token);

    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanInvalidatedToken() {

        invalidatedTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());

    }

    // Hàm generate token
    private String generateToken(
            User user,
            String type,
            long amount,
            ChronoUnit unit) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer("api-lulut.io.vn")
                .issueTime(new Date())
                .expirationTime(
                        new Date(
                                Instant.now()
                                        .plus(amount, unit)
                                        .toEpochMilli()))
                .claim("scope", buildScope(user))
                .claim("type", type)
                .build();

        Payload payload = new Payload(claims.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {

            jwsObject.sign(new MACSigner(SIGNED_KEY.getBytes()));

            return jwsObject.serialize();

        } catch (JOSEException e) {

            throw new RuntimeException(e);

        }

    }

    private String generateAccessToken(User user) {

        return generateToken(
                user,
                "access",
                1,
                ChronoUnit.HOURS);

    }

    private String generateRefreshToken(User user) {

        return generateToken(
                user,
                "refresh",
                7,
                ChronoUnit.DAYS);

    }

    // Lưu refresh token
    private void saveRefreshToken(User user, String token) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryTime(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

    }

    private SignedJWT verifyToken(String token)
            throws JOSEException, ParseException {

        SignedJWT signedJWT = SignedJWT.parse(token);

        JWSVerifier verifier = new MACVerifier(SIGNED_KEY.getBytes());

        boolean verified = signedJWT.verify(verifier);

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!verified || expiration.before(new Date())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;

    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (user.getRole() != null) {
            stringJoiner.add(user.getRole().name());
        }

        return stringJoiner.toString();
    }

    public User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {

            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        UUID userId = UUID.fromString(authentication.getName());

        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

}
