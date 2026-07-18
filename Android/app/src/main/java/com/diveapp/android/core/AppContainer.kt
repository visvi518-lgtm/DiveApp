package com.diveapp.android.core

import android.content.Context
import com.diveapp.android.core.auth.AuthSession
import com.diveapp.android.core.auth.GoogleSocialAuthProvider
import com.diveapp.android.core.auth.NaverSocialAuthProvider
import com.diveapp.android.core.auth.TokenStorage
import com.diveapp.android.core.network.NetworkModule
import com.diveapp.android.repository.AuthRepository
import com.diveapp.android.repository.CertificateRepository
import com.diveapp.android.repository.CommunityRepository
import com.diveapp.android.repository.DiveLogRepository
import com.diveapp.android.repository.InformationRepository
import com.diveapp.android.repository.TrainingRepository
import com.diveapp.android.repository.UserRepository
import com.diveapp.android.service.AuthService
import com.diveapp.android.service.CertificateService
import com.diveapp.android.service.CommunityService
import com.diveapp.android.service.DiveLogService
import com.diveapp.android.service.InformationService
import com.diveapp.android.service.TrainingService
import com.diveapp.android.service.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Manual composition root: builds every dependency once and wires the auth
 * session's session-expiry callback into the networking layer. */
class AppContainer(context: Context) {
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val tokenStorage: TokenStorage = TokenStorage(context.applicationContext)

    private val networkModule: NetworkModule = NetworkModule(
        tokenStorage = tokenStorage,
        onSessionExpired = { authSession.handleSessionExpired() },
    )

    private val authService: AuthService = networkModule.retrofit.create(AuthService::class.java)
    private val userService: UserService = networkModule.retrofit.create(UserService::class.java)
    private val certificateService: CertificateService = networkModule.retrofit.create(CertificateService::class.java)
    private val informationService: InformationService = networkModule.retrofit.create(InformationService::class.java)
    private val trainingService: TrainingService = networkModule.retrofit.create(TrainingService::class.java)
    private val diveLogService: DiveLogService = networkModule.retrofit.create(DiveLogService::class.java)
    private val communityService: CommunityService = networkModule.retrofit.create(CommunityService::class.java)

    private val authRepository: AuthRepository = AuthRepository(authService, tokenStorage)
    val userRepository: UserRepository = UserRepository(userService)
    val certificateRepository: CertificateRepository = CertificateRepository(certificateService)
    val informationRepository: InformationRepository = InformationRepository(informationService)
    val trainingRepository: TrainingRepository = TrainingRepository(trainingService)
    val diveLogRepository: DiveLogRepository = DiveLogRepository(diveLogService)
    val communityRepository: CommunityRepository = CommunityRepository(communityService)

    val authSession: AuthSession = AuthSession(authRepository, userRepository, externalScope)

    val naverProvider: NaverSocialAuthProvider = NaverSocialAuthProvider()
    val googleProvider: GoogleSocialAuthProvider = GoogleSocialAuthProvider()
}
