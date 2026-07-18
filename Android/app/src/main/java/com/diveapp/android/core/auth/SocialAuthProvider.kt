package com.diveapp.android.core.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.diveapp.android.BuildConfig
import com.diveapp.android.model.AuthProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class SocialAuthException(val provider: AuthProvider, message: String) : Exception(message)

/** Wraps a provider's native login SDK and returns the raw token the backend
 * verifies (Naver access token / Google ID token). An Activity is required
 * because both NidOAuth.requestLogin and CredentialManager.getCredential
 * need one to launch their sign-in UI. */
interface SocialAuthProviding {
    val provider: AuthProvider
    suspend fun signIn(activity: Activity): String
}

/** Naver Login via the NidOAuth SDK (see build.gradle.kts for the
 * com.navercorp.nid:oauth dependency, and DiveApplication for
 * NidOAuth.initialize(...) using client credentials from local.properties). */
class NaverSocialAuthProvider : SocialAuthProviding {
    override val provider = AuthProvider.NAVER

    override suspend fun signIn(activity: Activity): String {
        if (BuildConfig.NAVER_CLIENT_ID.isEmpty()) {
            throw SocialAuthException(provider, "네이버 로그인이 설정되지 않았습니다 (NAVER_CLIENT_ID 누락).")
        }
        return requestNaverLogin(activity)
    }

    private suspend fun requestNaverLogin(activity: Activity): String = suspendCancellableCoroutine { continuation ->
        NidOAuth.requestLogin(
            activity,
            object : NidOAuthCallback {
                override fun onSuccess() {
                    val accessToken = NidOAuth.getAccessToken()
                    if (accessToken != null) {
                        continuation.resume(accessToken)
                    } else {
                        continuation.resumeWithException(
                            SocialAuthException(provider, "네이버 로그인 토큰을 가져오지 못했습니다."),
                        )
                    }
                }

                override fun onFailure(errorCode: String, errorDesc: String) {
                    continuation.resumeWithException(
                        SocialAuthException(provider, "네이버 로그인에 실패했습니다: $errorDesc"),
                    )
                }
            },
        )
    }
}

/** Google Sign-In via Credential Manager. Uses the Web-application-type
 * OAuth client ID as serverClientId (Google's own architecture — see
 * local.properties.example) so the resulting ID token's audience matches the
 * backend's single GOOGLE_CLIENT_ID setting, shared with the Web client. */
class GoogleSocialAuthProvider : SocialAuthProviding {
    override val provider = AuthProvider.GOOGLE

    override suspend fun signIn(activity: Activity): String {
        if (BuildConfig.GOOGLE_WEB_CLIENT_ID.isEmpty()) {
            throw SocialAuthException(provider, "구글 로그인이 설정되지 않았습니다 (GOOGLE_WEB_CLIENT_ID 누락).")
        }
        val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setNonce(generateNonce())
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

        val response = try {
            CredentialManager.create(activity).getCredential(activity, request)
        } catch (e: GetCredentialException) {
            throw SocialAuthException(provider, "구글 로그인에 실패했습니다: ${e.message}")
        }

        val credential = response.credential
        if (credential is CustomCredential) {
            try {
                return GoogleIdTokenCredential.createFrom(credential.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                throw SocialAuthException(provider, "구글 로그인 응답을 처리하지 못했습니다.")
            }
        }
        throw SocialAuthException(provider, "구글 로그인 응답을 처리하지 못했습니다.")
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
