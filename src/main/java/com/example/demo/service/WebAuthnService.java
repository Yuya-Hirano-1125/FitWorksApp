package com.example.demo.service;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${webauthn.rp.id:localhost}")
    private String rpId;

    // 一般的なデフォルトポート 8080 に変更
    @Value("${webauthn.origin.url:http://localhost:8080}")
    private String originUrl;

    public Challenge generateChallenge() {
        return new DefaultChallenge();
    }

    /**
     * ユーザーに紐づくCredential IDのリストを取得する
     */
    @Transactional(readOnly = true)
    public List<String> getCredentialIds(User user) {
        List<Authenticator> authenticators = authenticatorRepository.findByUser(user);
        return authenticators.stream()
                .map(Authenticator::getCredentialId)
                .collect(Collectors.toList());
    }

    /**
     * Base64URLデコード (Java標準ライブラリ使用・パディング補完付き)
     */
    private byte[] decodeBase64Url(String source) {
        if (source == null || source.isEmpty()) {
            return new byte[0];
        }
        String base64Url = source;
        int padding = 4 - (base64Url.length() % 4);
        if (padding < 4) {
            base64Url += "=".repeat(padding);
        }
        try {
            return Base64.getUrlDecoder().decode(base64Url);
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 Decode Error: " + e.getMessage());
            System.err.println("Input String: " + source);
            throw e;
        }
    }

    /**
     * 新しい認証器を登録する
     */
    @Transactional
    public void register(String clientDataJSONStr, String attestationObjectStr, Challenge challenge, User user, String deviceName) {
        System.out.println("Registering: clientDataJSON length=" + (clientDataJSONStr != null ? clientDataJSONStr.length() : "null"));
        System.out.println("Registering: attestationObject length=" + (attestationObjectStr != null ? attestationObjectStr.length() : "null"));

        byte[] clientDataJSON = decodeBase64Url(clientDataJSONStr);
        byte[] attestationObject = decodeBase64Url(attestationObjectStr);

        // 引数順序修正済み: (attestationObject, clientDataJSON)
        RegistrationRequest registrationRequest = new RegistrationRequest(
            attestationObject,
            clientDataJSON
        );

        RegistrationParameters registrationParameters = new RegistrationParameters(
            new ServerProperty(new Origin(originUrl), rpId, challenge, null),
            null, 
            false, 
            false  
        );

        RegistrationData registrationData = webAuthnManager.parse(registrationRequest);
        webAuthnManager.validate(registrationData, registrationParameters);

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

        // 引数順序修正済み: (..., authenticatorData, clientDataJSON, ...)
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
            credentialIdBytes,
            userHandleBytes,
            authenticatorData, // binary
            clientDataJSON,    // JSON
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

        CredentialRecord credentialRecord = new CredentialRecord() {
            @Override
            public AttestedCredentialData getAttestedCredentialData() { return attestedCredentialData; }
            @Override
            public AttestationStatement getAttestationStatement() { return null; }
            @Override
            public long getCounter() { return auth.getCount(); }
            @Override
            public Set<AuthenticatorTransport> getTransports() { return null; }
            @Override
            public AuthenticationExtensionsAuthenticatorOutputs<RegistrationExtensionAuthenticatorOutput> getAuthenticatorExtensions() { return null; }
            @Override
            public Boolean isUvInitialized() { return null; }
            @Override
            public Boolean isBackupEligible() { return null; }
            @Override
            public Boolean isBackedUp() { return null; }
            @Override
            public CollectedClientData getClientData() { return null; }
            @Override
            public void setCounter(long value) {}
            @Override
            public void setUvInitialized(boolean value) {}
            @Override
            public void setBackupEligible(boolean value) {}
            @Override
            public void setBackedUp(boolean value) {}
        };

        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
            new ServerProperty(new Origin(originUrl), rpId, challenge, null),
            credentialRecord,
            null, 
            false, 
            true
        );

        AuthenticationData authenticationData = webAuthnManager.parse(authenticationRequest);
        webAuthnManager.validate(authenticationData, authenticationParameters);

        auth.setCount(authenticationData.getAuthenticatorData().getSignCount());
        authenticatorRepository.save(auth);

        return auth.getUser();
    }
}