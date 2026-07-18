import SwiftUI

struct DiveLogMockView: View {
    @EnvironmentObject private var appState: MockAppState
    @State private var isAddingLog = false

    var body: some View {
        NavigationStack {
            List {
                ForEach(appState.diveLogs) { log in
                    NavigationLink {
                        DiveLogDetailMockView(log: log)
                    } label: {
                        VStack(alignment: .leading, spacing: AppSpacing.xs) {
                            Text(log.title)
                                .font(AppTypography.headline)
                            Text("\(log.location) · \(log.date)")
                                .font(AppTypography.caption)
                                .foregroundStyle(AppColor.secondary)
                            HStack {
                                Text(log.type)
                                Text(log.depth)
                                Text(log.duration)
                            }
                            .font(AppTypography.caption)
                            .foregroundStyle(AppColor.primary)
                        }
                        .padding(.vertical, AppSpacing.xs)
                    }
                }
            }
            .navigationTitle("다이브 로그")
            .toolbar {
                Button {
                    isAddingLog = true
                } label: {
                    Image(systemName: "plus")
                }
            }
            .sheet(isPresented: $isAddingLog) {
                AddDiveLogMockView()
            }
        }
    }
}

struct DiveLogDetailMockView: View {
    var log: MockDiveLog

    var body: some View {
        List {
            Section("기록") {
                LabeledContent("장소", value: log.location)
                LabeledContent("종류", value: log.type)
                LabeledContent("수심", value: log.depth)
                LabeledContent("시간", value: log.duration)
                LabeledContent("날짜", value: log.date)
            }

            Section {
                EmptyStateView(
                    systemImage: "photo",
                    title: "사진 업로드 제외",
                    message: "Playground 버전에서는 파일 선택과 업로드를 제외했습니다."
                )
            }
        }
        .navigationTitle(log.title)
    }
}

struct AddDiveLogMockView: View {
    @EnvironmentObject private var appState: MockAppState
    @Environment(\.dismiss) private var dismiss
    @State private var title = ""
    @State private var location = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("새 로그") {
                    TextField("제목", text: $title)
                    TextField("장소", text: $location)
                }

                Section {
                    Button("Mock 로그 추가") {
                        appState.addDiveLog(title: title, location: location)
                        dismiss()
                    }
                }
            }
            .navigationTitle("로그 작성")
            .toolbar {
                Button("닫기") {
                    dismiss()
                }
            }
        }
    }
}
