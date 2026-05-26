package com.example.flood_alert.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import com.example.flood_alert.dbo.response.IntrospectResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.flood_alert.dbo.request.AuthenticateRequest;
import com.example.flood_alert.dbo.request.IntrospectRequest;
import com.example.flood_alert.dbo.response.AuthenticateResponse;
import com.example.flood_alert.entity.User;
import com.example.flood_alert.exception.AppException;
import com.example.flood_alert.exception.ErrorCode;
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

import org.springframework.beans.factory.annotation.Value;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level=AccessLevel.PRIVATE,makeFinal=true)
public class AuthenticationService {
    UserRepository userRepository;

    @NonFinal
    @Value("${jwt.signedKey}")
    protected String SIGNED_KEY;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException{
        var token=request.getToken();

        JWSVerifier verifier=new MACVerifier(SIGNED_KEY.getBytes());

        SignedJWT signedJWT=SignedJWT.parse(token);

        Date expirationDate=signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified=signedJWT.verify(verifier);

        return IntrospectResponse.builder()
                .valid(verified && expirationDate.after(new Date()))
                .build();
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request){
        var user=userRepository.findByEmailOrSodt(request.getLoginInfo(), request.getLoginInfo())
            .orElseThrow(()->new AppException(ErrorCode.LOGIN_INFO_EXISTED));

        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder(10);

        boolean authenticated=passwordEncoder.matches(request.getPassword(),user.getPassword());

        if(!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token=generateToken(user);


        return AuthenticateResponse.builder()
                .token(token)
                .authenticated(true)
                .role(user.getRole().name())
                .build();
        
    }

    //    Hàm generate token
    private String generateToken(User user) {
        //Bước 1; build token
        JWSHeader header=new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet=new JWTClaimsSet.Builder()
                .subject(user.getId().toString()) //Đại diện user đăng nhập
                .issuer("api-lulut.io.vn") //Xác định token issuer từ ai, thường là domain của mình
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()
                )) //Thời hạn token
                .claim("scope",buildScope(user))
                .build();

        //Tạo payload cho token
        Payload payload=new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject=new JWSObject(header,payload);

        //Bước 2: Ký token
        try {
            jwsObject.sign(new MACSigner(SIGNED_KEY.getBytes()));//Thuật toán MacSigner: thuật toán ký token mà khóa ký và khóa giải mã trùng nhau, thuật toán này cần chuỗi 32bit -> lấy bằng cách lên web Encryption key generator để lấy chuỗi ngẫu nhiên.
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token",e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(user.getRole() != null){
            stringJoiner.add(user.getRole().name());
        }

        return stringJoiner.toString();
    }
}
