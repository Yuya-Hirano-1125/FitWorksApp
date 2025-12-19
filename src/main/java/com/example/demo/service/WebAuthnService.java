package com.example.demo.service;

import java.util.Base64;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 追加
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Authenticator;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthenticatorRepository;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.AuthenticationData;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.AuthenticatorTransport;
import com.webauthn4j.data.RegistrationData;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.attestation.statement.AttestationStatement;
import com.webauthn4j.data.client.CollectedClientData;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs;
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput;
import com.webauthn4j.server.ServerProperty;

@Service
public class WebAuthnService {

    @Autowired
    private AuthenticatorRepository authenticatorRepository;

    private final WebAuthnManager webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
    private final ObjectConverter objectConverter = new ObjectConverter();

    // ★修正: 設定ファイルから読み込むように変更（デフォルト値はローカル用）
    @Value("${webauthn.rp.id:localhost}")
    private String rpId;

    @Value("${webauthn.origin.url:http://localhost:8086}")
    private String originUrl;

    public Challenge generateChallenge() {
        return new DefaultChallenge();
    }

    /**
     * Base64URLデコード（パディング不足を補完してデコード）
     */
    private byte[] decodeBase64Url(String source) {
        if (source == null) {
            return new byte[0];
        }
        // パディング(=)が不足している場合に補完する
        String base64 = source.replace("-", "+").replace("_", "/");
        int padding = 4 - (base64.length() % 4);
        if (padding < 4) {
            base64 += "=".repeat(padding);
        }
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 新しい認証器を登録する
     */
    @Transactional
    public void register(String clientDataJSONStr, String attestationObjectStr, Challenge challenge, User user, String deviceName) {
        byte[] clientDataJSON = decodeBase64Url(clientDataJSONStr);
        byte[] attestationObject = decodeBase64Url(attestationObjectStr);

        RegistrationRequest registrationRequest = new RegistrationRequest(
            clientDataJSON,
            attestationObject
        );

        // ★修正: 定数ではなくフィールド変数を参照
        RegistrationParameters registrationParameters = new RegistrationParameters(
            new ServerProperty(new Origin(originUrl), rpId, challenge, null),
            null, 
            false, 
            false  
        );

        RegistrationData registrationData = webAuthnManager.parse(registrationRequest);
        webAuthnManager.validate(registrationData, registrationParameters);

        // DBに保存
        Authenticator auth = new Authenticator();
        auth.setUser(user);
        
        byte[] credentialIdBytes = registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData().getCredentialId();
        auth.setCredentialId(Base64.getUrlEncoder().withoutPadding().encodeToString(credentialIdBytes));
        
        COSEKey coseKey = registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData().getCOSEKey();
        try {
            byte[] publicKeyBytes = objectConverter.getCborConverter().writeValueAsBytes(coseKey);
            auth.setPublicKey(Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode public key", e);
        }

        auth.setCount(registrationData.getAttestationObject().getAuthenticatorData().getSignCount());
        auth.setName(deviceName != null ? deviceName : "New Device");
        
        authenticatorRepository.save(auth);
    }

    /**
     * ログイン認証を行う
     */
    @Transactional
    public User authenticate(String credentialId, String userHandle, String clientDataJSONStr, String authenticatorDataStr, String signatureStr, Challenge challenge) {
        Authenticator auth = authenticatorRepository.findByCredentialId(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found: " + credentialId));

        byte[] clientDataJSON = decodeBase64Url(clientDataJSONStr);
        byte[] authenticatorData = decodeBase64Url(authenticatorDataStr);
        byte[] signature = decodeBase64Url(signatureStr);
        byte[] userHandleBytes = userHandle != null ? decodeBase64Url(userHandle) : null;
        byte[] credentialIdBytes = decodeBase64Url(credentialId);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
            credentialIdBytes,
            userHandleBytes,
            clientDataJSON,
            authenticatorData,
            signature
        );

        COSEKey coseKey;
        try {
            byte[] publicKeyBytes = decodeBase64Url(auth.getPublicKey());
            coseKey = objectConverter.getCborConverter().readValue(publicKeyBytes, COSEKey.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode public key", e);
        }

        AttestedCredentialData attestedCredentialData = new AttestedCredentialData(
            AAGUID.ZERO,
            credentialIdBytes,
            coseKey
        );

        // コンストラクタエラーを回避するため、インターフェースを直接実装して CredentialRecord を作成
        CredentialRecord credentialRecord = new CredentialRecord() {
            @Override
            public AttestedCredentialData getAttestedCredentialData() {
                return attestedCredentialData;
            }

            @Override
            public AttestationStatement getAttestationStatement() {
                return null;
            }

            @Override
            public long getCounter() {
                return auth.getCount();
            }

            @Override
            public Set<AuthenticatorTransport> getTransports() {
                return null;
            }

            @Override
            public AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> getAuthenticatorExtensions() {
                return null;
            }

            @Override
            public Boolean isUvInitialized() {
                return null;
            }

            @Override
            public Boolean isBackupEligible() {
                return null;
            }

            @Override
            public Boolean isBackedUp() {
                return null;
            }

            // --- 追加実装（エラー解消のために必須）---
            @Override
            public CollectedClientData getClientData() {
                return null;
            }

            @Override
            public void setCounter(long value) {
                // 検証用一時オブジェクトのため実装不要
            }

            @Override
            public void setUvInitialized(boolean value) {
                // 検証用一時オブジェクトのため実装不要
            }

            @Override
            public void setBackupEligible(boolean value) {
                // 検証用一時オブジェクトのため実装不要
            }

            @Override
            public void setBackedUp(boolean value) {
                // 検証用一時オブジェクトのため実装不要
            }
        };

        // ★修正: 定数ではなくフィールド変数を参照
        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
            new ServerProperty(new Origin(originUrl), rpId, challenge, null),
            credentialRecord,
            null, // allowCredentials
            false, // userVerificationRequired
            true   // userPresenceRequired
        );

        AuthenticationData authenticationData = webAuthnManager.parse(authenticationRequest);
        webAuthnManager.validate(authenticationData, authenticationParameters);

        // カウンター更新
        auth.setCount(authenticationData.getAuthenticatorData().getSignCount());
        authenticatorRepository.save(auth);

        return auth.getUser();
    }
}