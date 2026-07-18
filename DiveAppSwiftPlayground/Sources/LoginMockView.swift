import SwiftUI

struct LoginMockView: View {
    @EnvironmentObject private var appState: MockAppState

    var body: some View {
        VStack(spacing: AppSpacing.xxl) {
            Spacer()

            VStack(spacing: AppSpacing.md) {
                Image(systemName: "water.waves")
                    .font(.system(size: 58))
                    .foregroundStyle(AppColor.primary)

                Text("DiveApp")
                    .font(AppTypography.largeTitle)

                Text("다이빙 로그, 훈련, 커뮤니티를 한 곳에서 확인하는 Playground 버전")
                    .font(AppTypography.body)
                    .foregroundStyle(AppColor.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, AppSpacing.xl)
            }

            Spacer()

            VStack(spacing: AppSpacing.md) {
                Button("샘플 계정으로 시작") {
                    appState.login()
                }
                .buttonStyle(.diveAppPrimary)

                Button("새 사용자 흐름 보기") {
                    appState.loginAsNewUser()
                }
                .buttonStyle(.diveAppSecondary)

                Text("실제 소셜 로그인과 토큰 저장은 Mock으로 대체했습니다.")
                    .font(AppTypography.caption)
                    .foregroundStyle(AppColor.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(.horizontal, AppSpacing.xl)
            .padding(.bottom, AppSpacing.xxl)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppColor.background)
    }
}
