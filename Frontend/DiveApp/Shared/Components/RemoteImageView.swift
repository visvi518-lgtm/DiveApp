import SwiftUI

/// AsyncImage wrapper providing placeholder/loading/error states for every
/// remote image in the app, per Docs/08_DesignSystem.md "Images" section.
struct RemoteImageView: View {
    let url: URL?
    var contentMode: ContentMode = .fill

    var body: some View {
        AsyncImage(url: url) { phase in
            switch phase {
            case .empty:
                ZStack {
                    AppColor.surface
                    ProgressView()
                }
            case .success(let image):
                image
                    .resizable()
                    .aspectRatio(contentMode: contentMode)
            case .failure:
                ZStack {
                    AppColor.surface
                    Image(systemName: "photo")
                        .foregroundStyle(AppColor.secondary)
                }
            @unknown default:
                AppColor.surface
            }
        }
        .clipped()
    }
}
